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
    
    if HarvestUser.current.workingForID != nil {
      result?.setUpForForeman()
    } else {
      result?.setUpForFarmer()
    }
    
    return result
  }
  
  func attemptSignIn(with credential: AuthCredential) {
    isLoading = true
    HarvestDB.signIn(with: credential, on: self) { success in
      self.isLoading = false
      if success {
        if let vc = self.mainViewToPresent() {
          self.present(vc, animated: true, completion: nil)
        }
      }
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
  
  func attempSignIn(withVerificationID vid: String, verificationCode code: String) {
    let credential = PhoneAuthProvider.provider().credential(withVerificationID: vid,
                                                             verificationCode: code)
    attemptSignIn(with: credential)
  }
  
  @IBAction func signInTouchUp(_ sender: UIButton) {
    guard let username = usernameTextField.text, username != "" else {
      UIAlertController.present(title: "No Email or Phone Number Provided",
                                message: """
                                Please input an email address to log in as a farm owner.
                                Or enter a phone number to log in as a foreman.
                                """,
                                on: self)
      return
    }
    
    switch username {
    case let u where u.isEmail():
      guard let password = passwordTextField.text, password != "" else {
        UIAlertController.present(title: "Password Not Long Enough",
                                  message: "Password length must be at least 6 characters long",
                                  on: self)
        return
      }
      attempSignIn(username: username, password: password)
      
    case let u where u.isPhoneNumber():
      if let password = passwordTextField.text, password != "" {
        if let verificationID = UserDefaults.standard.getVerificationID() {
          attempSignIn(withVerificationID: verificationID, verificationCode: password)
        } else {
          UIAlertController.present(title: "Enter Only Phone Number",
                                    message: """
                                    Please enter only phone number in the username first and press \
                                    'Send Verification Code'. Then enter the code into the password\
                                    field once you have it then press 'Log in with Harvest'
                                    """, on: self)
        }
        
      } else {
        isLoading = true
        HarvestDB.verify(phoneNumber: u, on: self) { _ in
          self.isLoading = false
        }
      }
      
    default:
      UIAlertController.present(title: "Unknown Username",
                                message: """
                                Please input an email address to log in as a farm owner.
                                Or enter a phone number to log in as a foreman.
                                """,
                                on: self)
      
    }
  }
  
  @IBAction func usernameDidEdit(_ sender: UITextField) {
    guard let un = usernameTextField.text, un != "" else {
      return
    }
    
    switch un {
    case let u where u.isEmail():
      if !passwordTextField.isSecureTextEntry {
        passwordTextField.isSecureTextEntry = true
      }
      signInButton.setTitle("Log in with Harvest", for: .normal)
      
    case let u where u.isPhoneNumber():
      if passwordTextField.isSecureTextEntry {
        passwordTextField.isSecureTextEntry = false
      }
      if let p = passwordTextField.text, p == "" {
        signInButton.setTitle("Send Verification Code", for: .normal)
      } else {
        signInButton.setTitle("Log in with Harvest", for: .normal)
      }
      
    default:
      if !passwordTextField.isSecureTextEntry {
        passwordTextField.isSecureTextEntry = true
      }
      signInButton.setTitle("Log in with Harvest", for: .normal)
    }
  }
  
  @IBAction func forgotAccountTouchUp(_ sender: UIButton) {
    
    let emailRequest = UIAlertController(
      title: "Reset Password",
      message: "Please enter your email. You will then receive an email to reset your password.",
      preferredStyle: .alert)
    
    emailRequest.addTextField { (email) in email.keyboardType = .emailAddress }
    
    emailRequest.addAction(UIAlertAction(title: "Request Reset", style: .default, handler: { [weak emailRequest] _ in
      guard let email = emailRequest?.textFields?[0].text, email != "" else {
        UIAlertController.present(title: "No Email",
                                  message: "Please enter an email address",
                                  on: self)
        
        return
      }
      guard email.isEmail() else {
        UIAlertController.present(title: "Not a Valid Email",
                                  message: "Please enter a valid email address",
                                  on: self)
        
        return
      }
      
      HarvestDB.resetPassword(forEmail: email, on: self)
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
    
    NotificationCenter
      .default
      .addObserver(self,
                   selector: #selector(keyboardWillShow),
                   name: NSNotification.Name.UIKeyboardWillShow,
                   object: nil)
    NotificationCenter
      .default
      .addObserver(self,
                   selector: #selector(keyboardWillHide),
                   name: NSNotification.Name.UIKeyboardWillHide,
                   object: nil)
    
    hideKeyboardWhenTappedAround()
    
    usernameTextField.delegate = self
    passwordTextField.delegate = self
    
    usernameTextField.addLeftImage(#imageLiteral(resourceName: "Mail"))
    passwordTextField.addLeftImage(#imageLiteral(resourceName: "Lock"))
    
    textGroupView.layer.cornerRadius = 5
    
    if let user = Auth.auth().currentUser {
      isLoading = true
      HarvestUser.current.setUser(user, nil) { (_) in
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

extension SignInViewController: GIDSignInUIDelegate {
  
}

extension SignInViewController: GIDSignInDelegate {
  func sign(_ signIn: GIDSignIn!, didSignInFor user: GIDGoogleUser!, withError error: Error!) {
    if let error = error {
      UIAlertController.present(title: "An Error Occured",
                                message: error.localizedDescription,
                                on: self)
      
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
    orLabelVisualEffectView.setOriginX(view.frame.width / 2 - orLabel.frame.width / 2)
    activityIndicator.setOriginX(view.frame.width / 2 - activityIndicator.frame.width / 2)
    
    signInButton.apply(gradient: .green)
    signUpButton.apply(gradient: .blue)
    googleSignInButton.apply(gradient: .google)
  }
}
