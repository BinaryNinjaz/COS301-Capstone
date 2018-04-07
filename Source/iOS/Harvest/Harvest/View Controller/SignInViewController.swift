//
//  ViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/03/26.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit
import Firebase

class SignInViewController: UIViewController {
  
  @IBOutlet weak var usernameTextField: UITextField!
  @IBOutlet weak var passwordTextField: UITextField!
  @IBOutlet weak var signInButton: UIButton!
  @IBOutlet weak var signUpButton: UIButton!
  @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
  @IBOutlet weak var orLabel: UILabel!
  
  func attempSignIn(username: String, password: String) {
    signInButton.isHidden = true
    signUpButton.isHidden = true
    orLabel.isHidden = true
    activityIndicator.startAnimating()
    HarvestDB.signIn(withEmail: username, andPassword: password, on: self) {w in
      if w,
        let vc = self
          .storyboard?
          .instantiateViewController(withIdentifier: "mainTabBarViewController") {
        self.present(vc, animated: true, completion: nil)
      }
      self.signInButton.isHidden = false
      self.signUpButton.isHidden = false
      self.orLabel.isHidden = false
      self.activityIndicator.stopAnimating()
    }
  }
  
  @IBAction func signInTouchUp(_ sender: UIButton) {
    guard let username = usernameTextField.text else {
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
  
  override func viewDidLoad() {
    super.viewDidLoad()
    hideKeyboardWhenTappedAround()
    
    signInButton.apply(gradient: .green)
    signUpButton.apply(gradient: .blue)
    
    if let username = UserDefaults.standard.getUsername(),
      let password = UserDefaults.standard.getPassword() {
      attempSignIn(username: username, password: password)
      return
    }
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
  }
}

