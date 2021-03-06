//
//  MainTabBarViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/17.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import UIKit
import FirebaseAuth

class MainTabBarViewController: UITabBarController {
  override func viewDidLoad() {
    super.viewDidLoad()

    // Do any additional setup after loading the view.
  }
  
  var tutorialProgressListnerId: Int?
  
  override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    
    Entities.shared.start()
    
    if HarvestUser.current.isFarmer {
      if !UserDefaults.standard.bool(forKey: HarvestUser.current.uid + "SignedIn") {
        UserDefaults.standard.set(true, forKey: HarvestUser.current.uid + "SignedIn")
        StatStore.shared.setUpPredefinedGraphs()
        let vc = storyboard?.instantiateViewController(withIdentifier: "carouselViewController")
        if let avc = vc as? CarouselViewController {
          avc.showTutorial()
          present(avc, animated: true, completion: nil)
        }
      } else {
        tutorialProgressListnerId = Entities.shared.listen {
          self.tutorialProgression()
        }
      }
    }
    
//    UserDefaults.standard.set(false, forKey: HarvestUser.current.uid + "SignedIn")
  }
  
  func tutorialProgression() {
    let isFarmsEmpty = Entities.shared.farms.isEmpty
    let isOrchardsEmpty = Entities.shared.orchards.isEmpty
    let isWorkersEmpty = Entities.shared.workers.isEmpty
    
    if isFarmsEmpty || isOrchardsEmpty || isWorkersEmpty {
      if let infoIdx = viewControllers?
        .index(where: {$0.restorationIdentifier == informationViewControllerKey}) {
        selectedIndex = infoIdx
      }
      self.setUpForFarmerMissingData()
    } else {
      self.setUpForFarmer()
    }
  }
  
  override func viewWillDisappear(_ animated: Bool) {
    super.viewWillDisappear(animated)
    
    HarvestDB.removeListnerForLocationRequests()
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
  }
  
  let trackerViewControllerKey = "trackerViewController"
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
    
    if viewControllers?.index(where: { $0.restorationIdentifier == trackerViewControllerKey }) == nil {
      if let trackerVC = storyboard?.instantiateViewController(withIdentifier: trackerViewControllerKey) {
        viewControllers?.insert(trackerVC, at: 0)
      }
    }
  }
  
  func setUpForFarmer() {
    let anchor = 1
    
    if viewControllers?.index(where: { $0.restorationIdentifier == statViewControllerKey }) == nil {
      if let infoVC = storyboard?.instantiateViewController(withIdentifier: statViewControllerKey) {
        viewControllers?.insert(infoVC, at: anchor)
      }
    }
    
    if viewControllers?.index(where: { $0.restorationIdentifier == sessionsViewControllerKey }) == nil {
      if let sessionVC = storyboard?.instantiateViewController(withIdentifier: sessionsViewControllerKey) {
        viewControllers?.insert(sessionVC, at: anchor)
      }
    }
    
    if viewControllers?.index(where: { $0.restorationIdentifier == informationViewControllerKey }) == nil {
      if let statVC = storyboard?.instantiateViewController(withIdentifier: informationViewControllerKey) {
        viewControllers?.insert(statVC, at: anchor)
      }
    }
    
    if viewControllers?.index(where: { $0.restorationIdentifier == trackerViewControllerKey }) == nil {
      if let trackerVC = storyboard?.instantiateViewController(withIdentifier: trackerViewControllerKey) {
        viewControllers?.insert(trackerVC, at: 0)
      }
    }
  }
  
  func setUpForFarmerMissingData() {
    if let infoIdx = viewControllers?
      .index(where: { $0.restorationIdentifier == trackerViewControllerKey}) {
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
}
