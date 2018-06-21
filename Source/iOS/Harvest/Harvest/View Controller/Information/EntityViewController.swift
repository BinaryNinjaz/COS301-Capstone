//
//  EntityViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/19.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Eureka

public struct Box: Equatable {
}

public class EntityViewController: FormViewController, TypedRowControllerType {
  public var row: RowOf<Box>!
  public typealias RowValue = Box
  public var onDismissCallback: ((UIViewController) -> Void)?
  
  var entity: EntityItem?
  
  @IBOutlet weak var saveButton: UIBarButtonItem!
  
  override public func viewDidLoad() {
    super.viewDidLoad()
    entity?.information(for: self) {
      self.navigationItem.rightBarButtonItem?.isEnabled = true
    }
  }
  
  override public func viewDidAppear(_ animated: Bool) {
    
  }

  override public func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
  }
  
  @IBAction func saveButtonTouchUp(_ sender: Any) {
    guard let entity = entity else {
      return
    }
    
    let errors = form.validate(includeHidden: false)
    guard errors.count == 0 else {
      let errorList = errors.lazy.map { $0.msg }.joined(separator: "\n")
      UIAlertController.present(title: "Invalid Input", message: errorList, on: self)
      return
    }
    
    switch entity {
    case let .farm(f) where f.tempory != nil:
      HarvestDB.save(farm: f.tempory!)
      self.navigationItem.rightBarButtonItem?.isEnabled = false
      
    case let .orchard(o) where o.tempory != nil:
      HarvestDB.save(orchard: o.tempory!)
      self.navigationItem.rightBarButtonItem?.isEnabled = false
      
    case let .worker(w) where w.tempory != nil:
      HarvestDB.save(worker: w.tempory!, oldNumber: w.phoneNumber)
      w.phoneNumber = w.tempory!.phoneNumber
      self.navigationItem.rightBarButtonItem?.isEnabled = false
      
    case let .session(s) where s.tempory != nil:
      HarvestDB.save(session: s.tempory!)
      self.navigationItem.rightBarButtonItem?.isEnabled = false
      
    case let .user(u) where u.temporary != nil:
      HarvestDB.save(harvestUser: u.temporary!)
      HarvestUser.current = HarvestUser(json: u.temporary!.json())
      self.navigationItem.rightBarButtonItem?.isEnabled = false
      
    default:
      // FIXME: Show some error message
      break
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
