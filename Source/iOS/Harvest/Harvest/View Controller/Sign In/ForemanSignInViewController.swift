//
//  ForemanSignInViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/06/22.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit
import Firebase

class ForemanSignInViewController: UIViewController {
  @IBOutlet weak var titleVisualEffectView: UIVisualEffectView!
  @IBOutlet weak var numberInputTextField: UITextField!
  @IBOutlet weak var nextButton: UIButton!
  @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
  
  var phoneNumber: String?
  var verificationCode: String?
  
  enum State {
    case wantsPhoneNumber
    case wantsVerificationCode
    
    mutating func nextState() {
      switch self {
      case .wantsVerificationCode: self = .wantsPhoneNumber
      case .wantsPhoneNumber: self = .wantsVerificationCode
      }
    }
  }
  var state: State = .wantsPhoneNumber {
    didSet {
      switch state {
      case .wantsPhoneNumber:
        numberInputTextField.addLeftImage(#imageLiteral(resourceName: "Phone"))
        numberInputTextField.placeholder = "Phone Number"
        numberInputTextField.textAlignment = .left
        numberInputTextField.keyboardType = .phonePad
        numberInputTextField.text = ""
        
        nextButton.setTitle("Get Sign In Code", for: .normal)
        
      case .wantsVerificationCode:
        numberInputTextField.leftView = nil
        numberInputTextField.placeholder = "SMS Verification Code"
        numberInputTextField.textAlignment = .center
        numberInputTextField.keyboardType = .numberPad
        numberInputTextField.text = ""
        
        nextButton.setTitle("Sign In", for: .normal)
      }
    }
  }
  
  var isLoading: Bool = false {
    didSet {
      let ani = {
        self.nextButton.alpha = self.isLoading ? 0 : 1
        self.numberInputTextField.alpha = self.isLoading ? 0 : 1
        
        if self.isLoading {
          self.activityIndicator.startAnimating()
        } else {
          self.activityIndicator.stopAnimating()
        }
      }
      
      UIView.animate(withDuration: 0.5, animations: ani)
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
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    numberInputTextField.layer.cornerRadius = 5
    
    state = .wantsPhoneNumber
    
    hideKeyboardWhenTappedAround()
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
  
  func attempSignIn(withVerificationID vid: String, verificationCode code: String) {
    let credential = PhoneAuthProvider.provider().credential(withVerificationID: vid,
                                                             verificationCode: code)
    attemptSignIn(with: credential)
  }
  
  @IBAction func dismissViewControllerTouchUp(_ sender: Any) {
    dismiss(animated: true, completion: nil)
  }
  
  @IBAction func nextButtonTouchUp(_ sender: Any) {
    switch state {
    case .wantsPhoneNumber:
      isLoading = true
      phoneNumber = numberInputTextField.text
      guard let pn = phoneNumber else {
        UIAlertController.present(title: "Invalid Phone Number",
                                  message: """
                                  Please enter a valid phone number to sign in.
                                  """,
                                  on: self)
        return
      }
      
      HarvestDB.verify(phoneNumber: pn, on: self) { _ in
        self.isLoading = false
      }
      
    case .wantsVerificationCode:
      verificationCode = numberInputTextField.text
      
      guard let vc = verificationCode else {
        UIAlertController.present(title: "No Verification Code",
                                  message: """
                                  Please enter the verification code that was sent to you by SMS.
                                  """,
                                  on: self)
        return
      }
      
      if let verificationID = UserDefaults.standard.getVerificationID() {
        attempSignIn(withVerificationID: verificationID, verificationCode: vc)
      } else {
        UIAlertController.present(title: "Verification Error",
                                  message: """
                                  Please try requesting another verification code and trying again.
                                  """,
                                  on: self)
      }
    }
    
    state.nextState()
  }
  
  override func viewWillLayoutSubviews() {
    activityIndicator.setOriginY(view.frame.height / 2 - 64 - 32)
    
    nextButton.setWidth(min(view.frame.width - 32, 342))
    nextButton.setOriginX(view.frame.width / 2 - nextButton.frame.width / 2)
    nextButton.setOriginY(activityIndicator.frame.origin.y)
    
    numberInputTextField.setOriginY(nextButton.frame.origin.y - numberInputTextField.frame.height - 16)
    numberInputTextField.setWidth(nextButton.frame.width)
    
    titleVisualEffectView.setWidth(numberInputTextField.frame.width)
    titleVisualEffectView.setOriginX(view.frame.width / 2 - titleVisualEffectView.frame.width / 2)
    titleVisualEffectView.setOriginY(numberInputTextField.frame.origin.y - titleVisualEffectView.frame.height - 16)
    
    nextButton.apply(gradient: .signInButton)
  }
}
