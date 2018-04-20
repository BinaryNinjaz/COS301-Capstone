//
//  EntityViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/19.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Eureka

class EntityViewController: FormViewController {
  var entity: EntityItem? = nil
  
  @IBOutlet weak var saveButton: UIBarButtonItem!
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    entity?.information(for: form) {
      self.navigationItem.rightBarButtonItem?.isEnabled = true
    }
  }
  
  override func viewDidAppear(_ animated: Bool) {
    
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
  }
  
  @IBAction func saveButtonTouchUp(_ sender: Any) {
    guard let entity = entity else {
      return
    }
    
    switch entity {
    case let .farm(f):
      if let t = f.tempory {
        HarvestDB.save(farm: t)
        self.navigationItem.rightBarButtonItem?.isEnabled = false
      }
    case let .orchard(o):
      if let t = o.tempory {
        HarvestDB.save(orchard: t)
        self.navigationItem.rightBarButtonItem?.isEnabled = false
      }
    case let .worker(w):
      if let t = w.tempory {
        HarvestDB.save(worker: t)
        self.navigationItem.rightBarButtonItem?.isEnabled = false
      }
    case let .session(s):
      if let t = s.tempory {
        HarvestDB.save(session: t)
        self.navigationItem.rightBarButtonItem?.isEnabled = false
      }
    }
  }
  
  /*
  // MARK: - Navigation

  // In a storyboard-based application, you will often want to do a little preparation before navigation
  override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
      // Get the new view controller using segue.destinationViewController.
      // Pass the selected object to the new view controller.
  }
  */

}
