//
//  MainTabBarViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/17.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import UIKit

class MainTabBarViewController: UITabBarController {
  override func viewDidLoad() {
    super.viewDidLoad()

    // Do any additional setup after loading the view.
  }
  
  override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    
    HarvestDB.checkLocationRequested { locRequested in
      LocationTracker.shared.requestLocation(wantsLocation: locRequested)
    }
    
    HarvestDB.listenLocationRequested { locRequested in
      LocationTracker.shared.requestLocation(wantsLocation: locRequested)
    }
    
    Entities.shared.start()
  }
  
  override func viewWillDisappear(_ animated: Bool) {
    super.viewWillDisappear(animated)
    
    HarvestDB.removeListnerForLocationRequests()
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
  }
  
  let informationViewControllerKey = "informationViewController"
  let statViewControllerKey = "statViewController"
  let sessionsViewControllerKey = "sessionViewController"
  
  func setUpForForeman() {
    if let infoIdx = viewControllers?
      .index(where: { $0.restorationIdentifier == informationViewControllerKey}) {
      viewControllers?.remove(at: infoIdx)
    }
    
    if let sessionIdx = viewControllers?
      .index(where: { $0.restorationIdentifier == sessionsViewControllerKey }) {
        viewControllers?.remove(at: sessionIdx)
    }
    
    if let statIdx = viewControllers?
      .index(where: { $0.restorationIdentifier == statViewControllerKey }) {
        viewControllers?.remove(at: statIdx)
    }
    
  }
  
  func setUpForFarmer() {
    if viewControllers?.index(where: { $0.restorationIdentifier == statViewControllerKey }) == nil {
      if let infoVC = storyboard?.instantiateViewController(withIdentifier: statViewControllerKey) {
        viewControllers?.insert(infoVC, at: 1)
      }
    }
    
    if viewControllers?.index(where: { $0.restorationIdentifier == sessionsViewControllerKey }) == nil {
      if let sessionVC = storyboard?.instantiateViewController(withIdentifier: sessionsViewControllerKey) {
        viewControllers?.insert(sessionVC, at: 1)
      }
    }
    
    if viewControllers?.index(where: { $0.restorationIdentifier == informationViewControllerKey }) == nil {
      if let statVC = storyboard?.instantiateViewController(withIdentifier: informationViewControllerKey) {
        viewControllers?.insert(statVC, at: 1)
      }
    }
  }
}
