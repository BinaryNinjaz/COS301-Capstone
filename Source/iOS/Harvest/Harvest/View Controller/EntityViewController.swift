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
  var other: [EntityItem] = []
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    entity?.information(using: other, for: form)
  }
  
  override func viewDidAppear(_ animated: Bool) {
    
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
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
