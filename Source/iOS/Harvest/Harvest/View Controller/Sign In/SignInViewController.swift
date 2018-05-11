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
    hideKeyboardWhenTappedAround()
    
    signInButton.apply(gradient: .green)
    signUpButton.apply(gradient: .blue)
    googleSignInButton.apply(gradient: .google)
    signInVisualEffect.layer.cornerRadius = 24
    signInVisualEffect.clipsToBounds = true
    
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
}
