//
//  SignInOptionViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/06/22.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit
import GoogleSignIn
import Firebase
import Disk
import SCLAlertView

class SignInOptionViewController: UIViewController {
  @IBOutlet weak var signUpButton: UIButton!
  @IBOutlet weak var farmerSignInButton: UIView!
  @IBOutlet weak var foremanSignInButton: UIView!
  @IBOutlet weak var titleVisualEffectView: UIVisualEffectView!
  
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
    signUpButton.apply(gradient: .signUpButton)
    
    if let user = Auth.auth().currentUser {
      HarvestUser.current.setUser(user, nil, HarvestDB.requestWorkingFor(self, { succ in
        if succ, let vc = self.mainViewToPresent() {
          self.present(vc, animated: true, completion: nil)
        }
      }))
      
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
          GIDSignIn.sharedInstance().signIn()
        }
      }
    }
  }
  
  override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
    return UIInterfaceOrientationMask.portrait
  }
  
  override var prefersStatusBarHidden: Bool {
    return true
  }
  
  func attemptSignIn(with credential: AuthCredential) {
    HarvestDB.signIn(with: credential, on: self) { success in
      if success {
        if let vc = self.mainViewToPresent() {
          self.present(vc, animated: true, completion: nil)
        }
      }
    }
  }
  
  func attempSignIn(username: String, password: String) {
    HarvestDB.signIn(withEmail: username, andPassword: password, on: self) {w in
      if w, let vc = self.mainViewToPresent() {
        self.present(vc, animated: true, completion: nil)
      }
    }
  }
  
  @IBAction func showFarmerSignIn(_ sender: UITapGestureRecognizer) {
    guard sender.state == .ended else {
      return
    }
    
    guard let farmerSignInVC = storyboard?
      .instantiateViewController(withIdentifier: "signInViewController") else {
        return
    }
    
    present(farmerSignInVC, animated: true, completion: nil)
  }
  
  @IBAction func showForemanSignIn(_ sender: UIGestureRecognizer) {
    guard sender.state == .ended else {
      return
    }
    
    guard let farmerSignInVC = storyboard?
      .instantiateViewController(withIdentifier: "foremanSignInViewController") else {
        return
    }
    
    present(farmerSignInVC, animated: true, completion: nil)
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
  }
  
  override func viewWillLayoutSubviews() {
    farmerSignInButton.setWidth(min(342, view.frame.width - 32))
    farmerSignInButton.setHeight(min(180, view.frame.height / 4))
    farmerSignInButton.setOriginX(view.frame.width / 2 - farmerSignInButton.frame.width / 2)
    farmerSignInButton.setOriginY(view.frame.height / 2 - farmerSignInButton.frame.height - 64)
    
    foremanSignInButton.setWidth(farmerSignInButton.frame.width)
    foremanSignInButton.setHeight(farmerSignInButton.frame.height)
    foremanSignInButton.setOriginX(farmerSignInButton.frame.origin.x)
    foremanSignInButton.setOriginY(view.frame.height / 2 - 32)
    
    titleVisualEffectView.setWidth(farmerSignInButton.frame.width)
    titleVisualEffectView.setOriginX(view.frame.width / 2 - titleVisualEffectView.frame.width / 2)
    titleVisualEffectView.setOriginY(farmerSignInButton.frame.origin.y - titleVisualEffectView.frame.height - 16)
    
    signUpButton.setWidth(farmerSignInButton.frame.width)
    signUpButton.setOriginX(farmerSignInButton.frame.origin.x)
    signUpButton.setOriginY(view.frame.height - signUpButton.frame.height - 32)
    
    farmerSignInButton.layer.masksToBounds = true
    foremanSignInButton.layer.masksToBounds = true
    farmerSignInButton.layer.cornerRadius = 16
    foremanSignInButton.layer.cornerRadius = 16
    farmerSignInButton.layer.borderColor = UIColor.white.withAlphaComponent(0.5).cgColor
    foremanSignInButton.layer.borderColor = UIColor.white.withAlphaComponent(0.5).cgColor
    farmerSignInButton.layer.borderWidth = 2.5
    foremanSignInButton.layer.borderWidth = 2.5
    
    signUpButton.apply(gradient: .signUpButton)
  }
}

extension SignInOptionViewController: GIDSignInUIDelegate {
  
}

extension SignInOptionViewController: GIDSignInDelegate {
  func sign(_ signIn: GIDSignIn!, didSignInFor user: GIDGoogleUser!, withError error: Error!) {
    if let error = error {
      SCLAlertView().showError("An Error Occured", subTitle: error.localizedDescription)
      return
    }
    
    guard let authentication = user.authentication else {
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
