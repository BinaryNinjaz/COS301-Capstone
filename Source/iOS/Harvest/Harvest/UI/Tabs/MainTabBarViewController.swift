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
    guard let idx = viewControllers?
      .index(where: { $0.restorationIdentifier == "informationViewController" }) else {
      return
    }
    viewControllers?.remove(at: idx)
  }
  
  func setUpForFarmer() {
    guard nil == viewControllers?
      .index(where: { $0.restorationIdentifier == "informationViewController" }) else {
        return
    }
    
    guard let infoVC = storyboard?.instantiateViewController(withIdentifier: "informationViewController") else {
      return
    }
    
    viewControllers?.insert(infoVC, at: 1)
  }
}
