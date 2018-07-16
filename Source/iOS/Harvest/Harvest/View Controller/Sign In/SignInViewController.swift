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

class SignInViewController: UIViewController {
  @IBOutlet weak var usernameTextField: UITextField!
  @IBOutlet weak var passwordTextField: UITextField!
  @IBOutlet weak var signInButton: UIButton!
  @IBOutlet weak var signUpButton: UIButton!
  @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
  @IBOutlet weak var googleSignInButton: UIButton!
  @IBOutlet weak var signInVisualEffect: UIVisualEffectView!
  @IBOutlet weak var forgotAccountButton: UIButton!
  @IBOutlet weak var textGroupView: TextFieldGroupView!
  @IBOutlet weak var orLabel: UILabel!
  @IBOutlet weak var inputTextFieldGroup: TextFieldGroupView!
  @IBOutlet weak var forgotAccountVisualEffectView: UIVisualEffectView!
  @IBOutlet weak var orLabelVisualEffectView: UIVisualEffectView!
  @IBOutlet weak var titleLabelVisualEffectView: UIVisualEffectView!
  
  @IBOutlet weak var backgroundImageView: UIImageView!
  var isLoading: Bool = false {
    didSet {
      UIView.animate(withDuration: 0.5) {
        self.signInButton.alpha = self.isLoading ? 0 : 1
        self.googleSignInButton.alpha = self.isLoading ? 0 : 1
        self.signUpButton.alpha = self.isLoading ? 0 : 1
        self.forgotAccountButton.alpha = self.isLoading ? 0 : 1
        self.orLabel.alpha = self.isLoading ? 0 : 1
      }
      
      if isLoading  {
        activityIndicator.startAnimating()
      } else {
        activityIndicator.stopAnimating()
      }
    }
  }
  
  func mainViewToPresent() -> UIViewController? {
    let result: UIViewController?
    
    if HarvestUser.current.workingForID != nil {
      result = storyboard?.instantiateViewController(withIdentifier: "mainTabBarViewController")
      (result as? MainTabBarViewController)?.setUpForForeman()
    } else {
      result = storyboard?.instantiateViewController(withIdentifier: "mainTabBarViewController")
      (result as? MainTabBarViewController)?.setUpForFarmer()
    }
    
    return result
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
      let alert = UIAlertController.alertController(
        title: "No Email",
        message: "Please input an email address to log in")
      
      present(alert, animated: true, completion: nil)
      
      return
    }
    
    let password = passwordTextField.text ?? ""
    
    attempSignIn(username: username, password: password)
  }
  
  @IBAction func forgotAccountTouchUp(_ sender: UIButton) {
    
    let emailRequest = UIAlertController(title: "Reset Password",
                                         message: "Please enter your email, you will receive an email to reset your password.",
                                         preferredStyle: .alert)
    
    emailRequest.addTextField { (email) in email.keyboardType = .emailAddress }
    
    emailRequest.addAction(UIAlertAction(title: "Request Reset", style: .default, handler: { [weak emailRequest] _ in
      guard let email = emailRequest?.textFields?[0].text else {
        let alert = UIAlertController.alertController(
          title: "No Email",
          message: "Please eneter a valid email address")
        
        self.present(alert, animated: true, completion: nil)
        
        return
      }
      
      Auth.auth().sendPasswordReset(withEmail: email) { (error) in
        if let err = error {
          let alert = UIAlertController.alertController(
            title: "An Error Occured",
            message: err.localizedDescription)
          
          self.present(alert, animated: true, completion: nil)
          
          return
        }
        
        let alert = UIAlertController.alertController(
          title: "Password Reset Sent",
          message: "An email was sent to \(email) to reset your password")
        
        self.present(alert, animated: true, completion: nil)
      }
    }))
    
    emailRequest.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
    
    present(emailRequest, animated: true, completion: nil)
    
  }
  
  @IBAction func googleSignInTouchUp(_ sender: UIButton) {
    isLoading = true
    GIDSignIn.sharedInstance().signIn()
  }
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow), name: NSNotification.Name.UIKeyboardWillShow, object: nil)
    NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide), name: NSNotification.Name.UIKeyboardWillHide, object: nil)
    
    hideKeyboardWhenTappedAround()
    
    usernameTextField.delegate = self
    passwordTextField.delegate = self
    
    usernameTextField.addLeftImage(#imageLiteral(resourceName: "Mail"))
    passwordTextField.addLeftImage(#imageLiteral(resourceName: "Lock"))
    
    textGroupView.layer.cornerRadius = 5
    
    if let user = Auth.auth().currentUser {
      isLoading = true
      HarvestUser.current.setUser(user, nil) { (valid) in
        if let vc = self.mainViewToPresent() {
          self.present(vc, animated: true, completion: nil)
        }
        self.isLoading = false
      }
      
      if let oldSession = try? Disk.retrieve("session", from: .applicationSupport, as: Tracker.self) {
        oldSession.storeSession()
      }
    } else {
      GIDSignIn.sharedInstance().delegate = self
      GIDSignIn.sharedInstance().uiDelegate = self
      
      if let username = UserDefaults.standard.getUsername() {
        if let password = UserDefaults.standard.getPassword() {
          attempSignIn(username: username, password: password)
        } else {
          isLoading = true
          GIDSignIn.sharedInstance().signIn()
        }
      }
    }
  }
  
  override var prefersStatusBarHidden: Bool {
    return true
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
  }
}

extension SignInViewController : GIDSignInUIDelegate {
  
}

extension SignInViewController : GIDSignInDelegate {
  func sign(_ signIn: GIDSignIn!, didSignInFor user: GIDGoogleUser!, withError error: Error!) {
    if let error = error  {
      let alert = UIAlertController.alertController(
        title: "An Error Occured",
        message: error.localizedDescription)
      
      self.present(alert, animated: true, completion: nil)
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
    
    Auth.auth().signIn(with: credential) { (user, error) in
      if let error = error {
        let alert = UIAlertController.alertController(
          title: "An Error Occured",
          message: error.localizedDescription)
        
        self.present(alert, animated: true, completion: nil)
        self.isLoading = false
        return
      }
      
      guard let user = user else {
        let alert = UIAlertController.alertController(
          title: "Sign In Failure",
          message: "Unknown Error Occured")
        self.present(alert, animated: true, completion: nil)
        return
      }
      
      HarvestUser.current.setUser(user, nil) { valid in
        if let vc = self.mainViewToPresent() {
          self.present(vc, animated: true, completion: nil)
        }
        self.isLoading = false
      }
      if let oldSession = try? Disk.retrieve("session", from: .applicationSupport, as: Tracker.self) {
        oldSession.storeSession()
      }
    }
  }
}

extension SignInViewController : UITextFieldDelegate {
  func textFieldShouldReturn(_ textField: UITextField) -> Bool {
    signInTouchUp(signInButton)
    return true
  }
  
  @objc func keyboardWillShow(notification: NSNotification) {
    if let keyboardFrame = (notification.userInfo?[UIKeyboardFrameBeginUserInfoKey] as? NSValue)?.cgRectValue {
      if googleSignInButton.frame.origin.y + googleSignInButton.frame.height > view.frame.height - keyboardFrame.height
      && self.view.frame.origin.y == 0 {
        let group = self.inputTextFieldGroup.frame
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
    
    signUpButton.setWidth(textGroupView.frame.width)
    signUpButton.setOriginX(textGroupView.frame.origin.x)
    forgotAccountButton.setWidth(textGroupView.frame.width)
    forgotAccountVisualEffectView.setOriginX(textGroupView.frame.origin.x)
    forgotAccountVisualEffectView.setOriginY(view.frame.height - forgotAccountButton.frame.height - 16)
    signUpButton.setOriginY(forgotAccountVisualEffectView.frame.origin.y - signUpButton.frame.height - 8)
    
    titleLabelVisualEffectView.setOriginX(view.frame.width / 2 - titleLabelVisualEffectView.frame.width / 2)
    orLabel.setOriginX(view.frame.width / 2 - orLabel.frame.width / 2)
    activityIndicator.setOriginX(view.frame.width / 2 - activityIndicator.frame.width / 2)
    
    
    signInButton.apply(gradient: .green)
    signUpButton.apply(gradient: .blue)
    googleSignInButton.apply(gradient: .google)
  }
}
