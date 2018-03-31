//
//  SettingsTableViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/03/31.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit

class SettingsTableViewController: UITableViewController {

  override func viewDidLoad() {
    super.viewDidLoad()

    self.clearsSelectionOnViewWillAppear = false
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
  }
  
  override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    let section = indexPath.section
    let row = indexPath.row
    
    
    switch section {
    case 1:
      switch row {
      case 0:
        HarvestDB.signOut(on: self) { w in
          if w,
            let vc = self
              .storyboard?
              .instantiateViewController(withIdentifier: "signInViewController") {
            UserDefaults.standard.removeObject(forKey: "username")
            UserDefaults.standard.removeObject(forKey: "password")
            self.present(vc, animated: true, completion: nil)
          }
        }
        
      default:
        break
      }
      
      default:
        break
    }
    
  }
}
