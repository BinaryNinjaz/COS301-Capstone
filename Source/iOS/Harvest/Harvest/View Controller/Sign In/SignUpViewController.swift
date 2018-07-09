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
  @IBOutlet weak var inputTextFieldGroup: TextFieldGroupView!
  @IBOutlet weak var titleLabelVisualEffectView: UIVisualEffectView!
  
  var isLoading: Bool = false {
    didSet {
      UIView.animate(withDuration: 0.5) {
        self.signUpButton.alpha = self.isLoading ? 0 : 1
        self.cancelButton.alpha = self.isLoading ? 0 : 1
        
        if self.isLoading {
          self.activityIndicator.startAnimating()
        } else {
          self.activityIndicator.stopAnimating()
        }
        
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
  
  // swiftlint:disable function_body_length
  @IBAction func signUpTouchUp(_ sender: UIButton) {
    guard let username = usernameTextField.text, username != "" else {
      UIAlertController.present(title: "No email address provided",
                                message: "Please provide an email address to create an account",
                                on: self)
      return
    }
    
    guard let password = passwordTextField.text, password.count >= 6 else {
      UIAlertController.present(title: "Password too short",
                                message: "Password must be at least 6 characters long",
                                on: self)
      return
    }
    
    guard username.isEmail() else {
      UIAlertController.present(title: "Invalid Email Address",
                                message: "Please provide a valid email address",
                                on: self)
      return
    }
    
    guard let fname = firstnameTextField.text, fname != "" else {
      UIAlertController.present(title: "No first name provided",
                                message: "Please provide a first name to create an account",
                                on: self)
      return
    }
    
    guard let lname = lastnameTextField.text, lname != "" else {
      UIAlertController.present(title: "No last name provided",
                                        message: "Please provide a last name to create an account",
                                        on: self)
      return
    }
    
    guard let confirmedPassword = confirmPasswordTextField.text, confirmedPassword != "" else {
      UIAlertController.present(title: "No confirm password provided",
                                message: "Please provide a confirm password to create an account",
                                on: self)
      return
    }
    
    guard confirmedPassword == password else {
      UIAlertController.present(title: "Mismatching passwords",
                                message: """
                                Your passwords are not matching. Please provide the same password in both\
                                password prompts
                                """,
                                on: self)
      return
    }
    
    isLoading = true
    HarvestDB.signUp(withEmail: username, andPassword: password, name: (fname, lname), on: self) {w in
      if w {
        HarvestDB.signIn(withEmail: username, andPassword: password, on: self) {w in
          if w,
            let vc = self.mainViewToPresent() {
            self.present(vc, animated: true, completion: nil)
          }
          self.isLoading = false
        }
      } else {
        self.isLoading = false
      }
    }
    
  }
  // swiftlint:enable function_body_length
  
  @IBAction func cancelTouchUp(_ sender: Any) {
    dismiss(animated: true, completion: nil)
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
    
    firstnameTextField.addLeftImage(#imageLiteral(resourceName: "Name"))
    lastnameTextField.addLeftImage(#imageLiteral(resourceName: "Name"))
    usernameTextField.addLeftImage(#imageLiteral(resourceName: "Mail"))
    passwordTextField.addLeftImage(#imageLiteral(resourceName: "Lock"))
    confirmPasswordTextField.addLeftImage(#imageLiteral(resourceName: "Lock"))
    
    firstnameTextField.delegate = self
    lastnameTextField.delegate = self
    usernameTextField.delegate = self
    passwordTextField.delegate = self
    confirmPasswordTextField.delegate = self
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

  override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
    
  }
}

extension SignUpViewController: UITextFieldDelegate {
  func textFieldShouldReturn(_ textField: UITextField) -> Bool {
    
    if textField === firstnameTextField {
      lastnameTextField.becomeFirstResponder()
    } else if textField === lastnameTextField {
      usernameTextField.becomeFirstResponder()
    } else if textField === usernameTextField {
      passwordTextField.becomeFirstResponder()
    } else if textField === passwordTextField {
      confirmPasswordTextField.becomeFirstResponder()
    } else if textField === confirmPasswordTextField {
      signUpTouchUp(signUpButton)
    }
    
    return true
  }
  
  @objc func keyboardWillShow(notification: NSNotification) {
    if let keyboardFrame = (notification.userInfo?[UIKeyboardFrameBeginUserInfoKey] as? NSValue)?.cgRectValue {
      if signUpButton.frame.origin.y + signUpButton.frame.height > view.frame.height - keyboardFrame.height
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

extension SignUpViewController {
  override func viewWillLayoutSubviews() {
    super.viewWillLayoutSubviews()
    
    inputTextFieldGroup.setWidth(min(view.frame.width - 32, 342))
    inputTextFieldGroup.setOriginX(view.frame.width / 2 - inputTextFieldGroup.frame.width / 2)
    
    signUpButton.setWidth(inputTextFieldGroup.frame.width)
    signUpButton.setOriginX(inputTextFieldGroup.frame.origin.x)
    
    activityIndicator.setOriginX(view.frame.width / 2 - activityIndicator.frame.width / 2)
    
    titleLabelVisualEffectView.setWidth(inputTextFieldGroup.frame.width)
    titleLabelVisualEffectView.setOriginX(inputTextFieldGroup.frame.origin.x)
    
    signUpButton.apply(gradient: .signUpButton)
  }
}
