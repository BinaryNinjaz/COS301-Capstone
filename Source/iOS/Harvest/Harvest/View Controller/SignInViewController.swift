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
  
  func attempSignIn(username: String, password: String) {
    signInButton.isHidden = true
    signUpButton.isHidden = true
    activityIndicator.startAnimating()
    HarvestDB.signIn(withEmail: username, andPassword: password, on: self) {w in
      if w,
        let vc = self
          .storyboard?
          .instantiateViewController(withIdentifier: "winView") {
        self.present(vc, animated: true, completion: nil)
      }
      self.signInButton.isHidden = false
      self.signUpButton.isHidden = false
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
  
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
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

