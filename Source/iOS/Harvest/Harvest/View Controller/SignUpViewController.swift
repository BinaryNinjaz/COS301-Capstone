//
//  SignUpViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/03/26.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit

class SignUpViewController: UIViewController {

  @IBOutlet weak var firstnameTextField: UITextField!
  @IBOutlet weak var lastnameTextField: UITextField!
  @IBOutlet weak var usernameTextField: UITextField!
  @IBOutlet weak var passwordTextField: UITextField!
  @IBOutlet weak var confirmPasswordTextField: UITextField!
  @IBOutlet weak var signUpButton: UIButton!
  @IBOutlet weak var cancelButton: UIButton!
  @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
  @IBOutlet weak var signUpVisualEffect: UIVisualEffectView!
  @IBOutlet weak var backgroundImageView: UIImageView!
  
  @IBAction func signUpTouchUp(_ sender: UIButton) {
    guard let username = usernameTextField.text else {
      let alert = UIAlertController.alertController(
        title: "No email address provided",
        message: "Please provide an email address to create an account")
      
      present(alert, animated: true, completion: nil)
      
      return
    }
    
    let password = passwordTextField.text ?? ""
    
    let emailRegex = try! NSRegularExpression(
      pattern: "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}")

    let urange = NSMakeRange(0, username.count)
    let match = emailRegex.rangeOfFirstMatch(in: username, range: urange)

    guard match == urange else {
      let alert = UIAlertController.alertController(
        title: "Invalid Email Address",
        message: "Please provide a valid email address")

      present(alert, animated: true, completion: nil)

      return
    }
    
    guard let fname = firstnameTextField.text else {
      let alert = UIAlertController.alertController(
        title: "No first name provided",
        message: "Please provide a first name to create an account")
      
      present(alert, animated: true, completion: nil)
      
      return
    }
    
    guard let lname = lastnameTextField.text else {
      let alert = UIAlertController.alertController(
        title: "No last name provided",
        message: "Please provide a last name to create an account")
      
      present(alert, animated: true, completion: nil)
      
      return
    }
    
    guard let confirmedPassword = confirmPasswordTextField.text else {
      let alert = UIAlertController.alertController(
        title: "No confirm password provided",
        message: "Please provide a confirm password to create an account")
      
      present(alert, animated: true, completion: nil)
      
      return
    }
    
    guard confirmedPassword == password else {
      let alert = UIAlertController.alertController(
        title: "Mismatching passwords",
        message: "Your passwords are not matching. Please provide the same password in both password prompts")
      
      present(alert, animated: true, completion: nil)
      
      return
    }
    
    signUpButton.isHidden = true
    cancelButton.isHidden = true
    activityIndicator.startAnimating()
    HarvestDB.signUp(withEmail: username, andPassword: password, name: (fname, lname), on: self) {w in
      if w {
        HarvestDB.signIn(withEmail: username, andPassword: password, on: self) {w in
          if w,
            let vc = self
              .storyboard?
              .instantiateViewController(withIdentifier: "mainTabBarViewController") {
            self.present(vc, animated: true, completion: nil)
          }
          self.signUpButton.isHidden = false
          self.cancelButton.isHidden = false
          self.activityIndicator.stopAnimating()
        }
      } else {
        self.signUpButton.isHidden = false
        self.cancelButton.isHidden = false
        self.activityIndicator.stopAnimating()
      }
    }
    
    
  }
  
  @IBAction func cancelTouchUp(_ sender: Any) {
    dismiss(animated: true, completion: nil)
  }
  
  override func viewDidLoad() {
    super.viewDidLoad()
    hideKeyboardWhenTappedAround()
    
    signUpButton.apply(gradient: .green)
    cancelButton.apply(gradient: .blue)
    
    signUpVisualEffect.layer.cornerRadius = 24
    signUpVisualEffect.clipsToBounds = true
    
    backgroundImageView.parallaxEffect(x: (-30, 30), y: (-20, 20))
    
    firstnameTextField.addLeftImage(#imageLiteral(resourceName: "Name"))
    lastnameTextField.addLeftImage(#imageLiteral(resourceName: "Name"))
    usernameTextField.addLeftImage(#imageLiteral(resourceName: "Mail"))
    passwordTextField.addLeftImage(#imageLiteral(resourceName: "Lock"))
    confirmPasswordTextField.addLeftImage(#imageLiteral(resourceName: "Lock"))
  }

  override var prefersStatusBarHidden: Bool {
    return true
  }
  
  
  
  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
  }

  override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
    
  }
}
