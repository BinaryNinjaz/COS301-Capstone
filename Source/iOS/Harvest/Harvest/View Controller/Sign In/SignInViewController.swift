//
//  ViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/03/26.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit
import Firebase
import GoogleSignIn
import Disk
import SCLAlertView

class SignInViewController: UIViewController {
  @IBOutlet weak var usernameTextField: UITextField!
  @IBOutlet weak var passwordTextField: UITextField!
  @IBOutlet weak var signInButton: UIButton!
  @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
  @IBOutlet weak var googleSignInButton: UIButton!
  @IBOutlet weak var signInVisualEffect: UIVisualEffectView!
  @IBOutlet weak var forgotAccountButton: UIButton!
  @IBOutlet weak var cancelButton: UIButton!
  @IBOutlet weak var textGroupView: TextFieldGroupView!
  @IBOutlet weak var orLabel: UILabel!
  @IBOutlet weak var forgotAccountVisualEffectView: UIVisualEffectView!
  @IBOutlet weak var orLabelVisualEffectView: UIVisualEffectView!
  @IBOutlet weak var titleLabelVisualEffectView: UIVisualEffectView!
  
  @IBOutlet weak var backgroundImageView: UIImageView!
  var isLoading: Bool = false {
    didSet {
      UIView.animate(withDuration: 0.5) {
        self.signInButton.alpha = self.isLoading ? 0 : 1
        self.googleSignInButton.alpha = self.isLoading ? 0 : 1
        self.forgotAccountButton.alpha = self.isLoading ? 0 : 1
        self.orLabel.alpha = self.isLoading ? 0 : 1
        self.cancelButton.alpha = self.isLoading ? 0 : 1
      }
      
      if isLoading {
        activityIndicator.startAnimating()
      } else {
        activityIndicator.stopAnimating()
      }
    }
  }
  
  func mainViewToPresent() -> UIViewController? {
    let result = storyboard?
      .instantiateViewController(withIdentifier: "mainTabBarViewController")
      as? MainTabBarViewController
    
    if !HarvestUser.current.workingForID.isEmpty {
      result?.setUpForForeman()
    } else {
      result?.setUpForFarmer()
    }
    
    return result
  }
  
  func attemptSignIn(with credential: AuthCredential) {
    isLoading = true
    HarvestDB.signIn(with: credential) { success in
      if success, let vc = self.mainViewToPresent() {
        self.present(vc, animated: true, completion: nil)
      }
      self.isLoading = false
    }
  }
  
  func attempSignIn(username: String, password: String) {
    isLoading = true
    HarvestDB.signIn(withEmail: username, andPassword: password, on: self) {w in
      if w, let vc = self.mainViewToPresent() {
        self.present(vc, animated: true, completion: nil)
      }
      self.isLoading = false
    }
  }
  
  @IBAction func signInTouchUp(_ sender: UIButton) {
    guard let username = usernameTextField.text, username != "" else {
      SCLAlertView().showError(
        "No Email Provided",
        subTitle: "Please input an email address to log in as a farm owner.")
      return
    }
    
    guard let password = passwordTextField.text, password != "" else {
      SCLAlertView().showError(
        "Password Not Long Enough",
        subTitle: "Password length must be at least 6 characters long")
      return
    }
    
    attempSignIn(username: username, password: password)
  }
  
  @IBAction func forgotAccountTouchUp(_ sender: UIButton) {
    let emailRequestAlert = SCLAlertView(appearance: .warningAppearance)
    
    let textField = emailRequestAlert.addTextField()
    
    emailRequestAlert.addButton("Cancel", action: {})
    emailRequestAlert.addButton("Request Reset") {
      guard let email = textField.text, email != "" else {
        SCLAlertView().showError(
          "No Email",
          subTitle: "Please enter an email address")
        
        return
      }
      guard email.isEmail() else {
        SCLAlertView().showError("Not a Valid Email", subTitle: "Please enter a valid email address")
        
        return
      }
      
      HarvestDB.resetPassword(forEmail: email)
    }
    
    emailRequestAlert.showEdit(
      "Reset Password",
      subTitle: "Please enter your email, you will then receive an email to reset your password.")
    
  }
  
  @IBAction func googleSignInTouchUp(_ sender: UIButton) {
    isLoading = true
    GIDSignIn.sharedInstance().delegate = self
    GIDSignIn.sharedInstance().uiDelegate = self
    GIDSignIn.sharedInstance().signIn()
  }
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    NotificationCenter.default.addObserver(self,
                                           selector: #selector(keyboardWillShow),
                                           name: NSNotification.Name.UIKeyboardWillShow,
                                           object: nil)
    NotificationCenter.default.addObserver(self,
                                           selector: #selector(keyboardWillHide),
                                           name: NSNotification.Name.UIKeyboardWillHide,
                                           object: nil)
    
    hideKeyboardWhenTappedAround()
    
    usernameTextField.delegate = self
    passwordTextField.delegate = self
    
    usernameTextField.addLeftImage(#imageLiteral(resourceName: "Mail"))
    passwordTextField.addLeftImage(#imageLiteral(resourceName: "Lock"))
    
    textGroupView.layer.cornerRadius = 5
  }
  
  @IBAction func dismissViewControllerTouchUp(_ sender: Any) {
    dismiss(animated: true, completion: nil)
  }
  
  override var prefersStatusBarHidden: Bool {
    return true
  }
  
  override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
    return UIInterfaceOrientationMask.portrait
  }
  
  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
  }
}

extension SignInViewController: GIDSignInUIDelegate {
  
}

extension SignInViewController: GIDSignInDelegate {
  func sign(_ signIn: GIDSignIn!, didSignInFor user: GIDGoogleUser!, withError error: Error!) {
    if let error = error {
      SCLAlertView().showError("An Error Occured", subTitle: error.localizedDescription)
      isLoading = false
      return
    }
    
    guard let authentication = user.authentication else {
      isLoading = false
      return
    }
    
    let credential = GoogleAuthProvider.credential(
      withIDToken: authentication.idToken,
      accessToken: authentication.accessToken)
    
    HarvestUser.current.firstname = user.profile.givenName
    HarvestUser.current.lastname = user.profile.familyName
    
    attemptSignIn(with: credential)
  }
}

extension SignInViewController: UITextFieldDelegate {
  func textFieldShouldReturn(_ textField: UITextField) -> Bool {
    signInTouchUp(signInButton)
    return true
  }
  
  @objc func keyboardWillShow(notification: NSNotification) {
    if let keyboardFrame = (notification.userInfo?[UIKeyboardFrameBeginUserInfoKey] as? NSValue)?.cgRectValue {
      if googleSignInButton.frame.origin.y + googleSignInButton.frame.height > view.frame.height - keyboardFrame.height
      && self.view.frame.origin.y == 0 {
        let group = self.textGroupView.frame
        if #available(iOS 11.0, *) {
          self.view.frame.origin.y -= group.origin.y - 48 - view.safeAreaInsets.top
        } else {
          self.view.frame.origin.y -= group.origin.y - 48
        }
      }
    }
  }
  
  @objc func keyboardWillHide(notification: NSNotification) {
    self.view.frame.origin.y = 0
  }
}

extension SignInViewController {
  override func viewWillLayoutSubviews() {
    super.viewWillLayoutSubviews()
    
    textGroupView.setWidth(min(view.frame.width - 32, 342))
    textGroupView.setOriginX(view.frame.width / 2 - textGroupView.frame.width / 2)
    
    signInButton.setWidth(textGroupView.frame.width)
    signInButton.setOriginX(textGroupView.frame.origin.x)
    googleSignInButton.setWidth(textGroupView.frame.width)
    googleSignInButton.setOriginX(textGroupView.frame.origin.x)
    
    forgotAccountButton.setWidth(textGroupView.frame.width)
    forgotAccountVisualEffectView.setOriginX(textGroupView.frame.origin.x)
    forgotAccountVisualEffectView.setOriginY(view.frame.height - forgotAccountButton.frame.height - 32)
    
    titleLabelVisualEffectView.setOriginX(view.frame.width / 2 - titleLabelVisualEffectView.frame.width / 2)
    orLabelVisualEffectView.setOriginX(view.frame.width / 2 - orLabel.frame.width / 2)
    activityIndicator.setOriginX(view.frame.width / 2 - activityIndicator.frame.width / 2)
    
    signInButton.apply(gradient: .signInButton)
    googleSignInButton.apply(gradient: .googleSignInButton)
  }
}
