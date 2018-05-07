//
//  MainTabBarViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/17.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit

class MainTabBarViewController: UITabBarController {
  override func viewDidLoad() {
    super.viewDidLoad()

    // Do any additional setup after loading the view.
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
  }
  
  func setUpForForeman() {
    guard let infoIdx = viewControllers?
      .index(where: { $0.restorationIdentifier == "informationViewController" }) else {
      return
    }
    
    guard let sessionIdx = viewControllers?
      .index(where: { $0.restorationIdentifier == "sessionViewController" }) else {
        return
    }
    
    guard let statIdx = viewControllers?
      .index(where: { $0.restorationIdentifier == "statViewController" }) else {
        return
    }
    
    viewControllers?.remove(at: infoIdx)
    viewControllers?.remove(at: sessionIdx)
    viewControllers?.remove(at: statIdx)
  }
  
  func setUpForFarmer() {
    guard nil == viewControllers?
      .index(where: { $0.restorationIdentifier == "informationViewController" }) else {
        return
    }
    
    guard nil == viewControllers?
      .index(where: { $0.restorationIdentifier == "sessionViewController" }) else {
        return
    }
    
    guard nil == viewControllers?
      .index(where: { $0.restorationIdentifier == "statViewController" }) else {
        return
    }
    
    guard let infoVC = storyboard?.instantiateViewController(withIdentifier: "informationViewController") else {
      return
    }
    
    guard let sessionVC = storyboard?.instantiateViewController(withIdentifier: "informationViewController") else {
      return
    }
    
    guard let statVC = storyboard?.instantiateViewController(withIdentifier: "informationViewController") else {
      return
    }
    
    viewControllers?.insert(statVC, at: 1)
    viewControllers?.insert(sessionVC, at: 1)
    viewControllers?.insert(infoVC, at: 1)
    
  }
}
