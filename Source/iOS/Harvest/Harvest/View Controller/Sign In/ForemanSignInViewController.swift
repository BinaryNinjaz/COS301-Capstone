//
//  ForemanSignInViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/06/22.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit
import Firebase
import SCLAlertView

class ForemanSignInViewController: UIViewController {
  @IBOutlet weak var titleVisualEffectView: UIVisualEffectView!
  @IBOutlet weak var numberInputTextField: UITextField!
  @IBOutlet weak var nextButton: UIButton!
  @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
  @IBOutlet weak var instructionLabel: UILabel!
  @IBOutlet weak var cancelButton: UIButton!
  @IBOutlet weak var resendButton: UIButton!
  @IBOutlet weak var instructionVisualEffectView: UIVisualEffectView!
  
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
        instructionLabel.text = """
          Please enter your phone number to receive a SMS with a verification code. Ensure you add \
          your area code. Example \(Phoney.formatted(number: "0123456789") ?? "")
          """
        numberInputTextField.addLeftImage(#imageLiteral(resourceName: "Phone"))
        numberInputTextField.placeholder = "Phone Number"
        numberInputTextField.keyboardType = .phonePad
        numberInputTextField.text = ""
        
        nextButton.setTitle("Get Sign In Code", for: .normal)
        
        cancelButton.isHidden = true
        resendButton.isHidden = true
        
      case .wantsVerificationCode:
        instructionLabel.text = """
          We've sent you a 6-digit verification code via SMS, please enter it above once you've \
          received it.
          """
        numberInputTextField.addLeftImage(#imageLiteral(resourceName: "Link"))
        numberInputTextField.placeholder = "SMS Verification Code"
        numberInputTextField.keyboardType = .numberPad
        numberInputTextField.text = ""
        
        nextButton.setTitle("Sign In", for: .normal)
        
        cancelButton.isHidden = false
        resendButton.isHidden = false
      }
    }
  }
  
  var isLoading: Bool = false {
    didSet {
      let ani = {
        self.nextButton.alpha = self.isLoading ? 0 : 1
        self.numberInputTextField.alpha = self.isLoading ? 0 : 1
        self.instructionLabel.alpha = self.isLoading ? 0 : 1
        self.cancelButton.alpha = self.isLoading ? 0 : 1
        self.resendButton.alpha = self.isLoading ? 0 : 1
        
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
    HarvestDB.signIn(with: credential) { success in
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
      phoneNumber = Phoney.formatted(number: numberInputTextField.text ?? "")?.removedFirebaseInvalids()
      guard let pn = phoneNumber else {
        SCLAlertView().showError(
          "Invalid Phone Number",
          subTitle: "Please enter a valid phone number to sign in.")
        return
      }
      
      HarvestDB.verify(phoneNumber: pn, on: self) { succ in
        if !succ && self.state == .wantsVerificationCode {
          self.state.nextState()
        }
        self.isLoading = false
      }
      
    case .wantsVerificationCode:
      verificationCode = numberInputTextField.text
      
      guard let vc = verificationCode else {
        SCLAlertView().showError(
          "No Verification Code",
          subTitle: "Please enter the verification code that was sent to you by SMS.")
        return
      }
      
      if let verificationID = UserDefaults.standard.getVerificationID() {
        attempSignIn(withVerificationID: verificationID, verificationCode: vc)
      } else {
        SCLAlertView().showError(
          "Verification Failed",
          subTitle: "Please try requesting another verification code and trying again.")
      }
    }
    
    state.nextState()
  }
  
  @IBAction func cancelButtonTouchUp(_ sender: UIButton) {
    if state == .wantsVerificationCode {
      state.nextState()
    }
  }
  
  @IBAction func resendButtonTouchUp(_ sender: UIButton) {
    guard let pn = phoneNumber, state == .wantsVerificationCode else {
      SCLAlertView().showError("Invalid Phone Number", subTitle: "Please enter a valid phone number to sign in.")
      return
    }
    
    HarvestDB.verify(phoneNumber: pn, on: self) { _ in
      self.isLoading = false
    }
    
    state.nextState()
  }
  
  override func viewWillLayoutSubviews() {
    activityIndicator.setOriginY(view.frame.height / 2 - 64 - 32)
    
    nextButton.setWidth(min(view.frame.width - 32, 342))
    nextButton.setOriginX(view.frame.width / 2 - nextButton.frame.width / 2)
    nextButton.setOriginY(activityIndicator.frame.origin.y)
    
    cancelButton.setWidth(nextButton.frame.width / 2 - 4)
    cancelButton.setOriginX(nextButton.frame.origin.x)
    cancelButton.setOriginY(nextButton.frame.origin.y + nextButton.frame.height + 16)
    
    resendButton.setWidth(cancelButton.frame.width)
    resendButton.setOriginX(cancelButton.frame.origin.x + cancelButton.frame.width + 8)
    resendButton.setOriginY(cancelButton.frame.origin.y)
    
    instructionVisualEffectView.setWidth(nextButton.frame.width)
    instructionVisualEffectView.setOriginX(nextButton.frame.origin.x)
    instructionVisualEffectView.setOriginY(nextButton.frame.origin.y - instructionVisualEffectView.frame.height - 8)
    
    numberInputTextField.setOriginX(instructionVisualEffectView.frame.origin.x)
    numberInputTextField.setOriginY(instructionVisualEffectView.frame.origin.y - numberInputTextField.frame.height - 8)
    numberInputTextField.setWidth(instructionVisualEffectView.frame.width)
    
    titleVisualEffectView.setWidth(numberInputTextField.frame.width)
    titleVisualEffectView.setOriginX(view.frame.width / 2 - titleVisualEffectView.frame.width / 2)
    titleVisualEffectView.setOriginY(numberInputTextField.frame.origin.y - titleVisualEffectView.frame.height - 16)
    
    nextButton.apply(gradient: .signInButton)
    cancelButton.apply(gradient: .orangeButton)
    resendButton.apply(gradient: .signInButton)
  }
}
