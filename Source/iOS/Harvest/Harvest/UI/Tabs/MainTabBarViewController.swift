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
    for item in tabBar.items ?? [] {
      item.image = item.image?.resizeImage(toWidth: 22)
    }
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
  }
  
  override func tabBar(_ tabBar: UITabBar, didSelect item: UITabBarItem) {
    item.image = item.image?.resizeImage(toWidth: 22)
  }
  
  override func tabBar(_ tabBar: UITabBar, didEndCustomizing items: [UITabBarItem], changed: Bool) {
    for item in items {
      item.image = item.image?.resizeImage(toWidth: 22)
    }
  }
}
