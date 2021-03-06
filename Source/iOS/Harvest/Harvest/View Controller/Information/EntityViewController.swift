//
//  EntityViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/19.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import Eureka
import SCLAlertView

public struct Box: Equatable {
}

public final class EntityViewController: ReloadableFormViewController, TypedRowControllerType {
  public var row: RowOf<Box>!
  public typealias RowValue = Box
  public var onDismissCallback: ((UIViewController) -> Void)?
  
  var entity: EntityItem?
  
  @IBOutlet weak var saveButton: UIBarButtonItem!
  
  override public func viewDidLoad() {
    super.viewDidLoad()
  }
  
  override public func viewDidAppear(_ animated: Bool) {
    
  }
  
  override public func tearDown() {
    form.removeAll()
  }
  
  override public func setUp() {
    entity?.information(for: self) {
      self.navigationItem.rightBarButtonItem?.isEnabled = true
    }
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
      SCLAlertView().showError("Invalid Input", subTitle: errorList)
      return
    }
    
    switch entity {
    case let .farm(f) where f.tempory != nil:
      HarvestDB.save(farm: f.tempory!)
      f.makeChangesPermanent()
      SCLAlertView.showSuccessToast(message: "Farm Saved")
      self.navigationItem.rightBarButtonItem?.isEnabled = false
      
    case let .orchard(o) where o.tempory != nil:
      HarvestDB.save(orchard: o.tempory!)
      o.makeChangesPermanent()
      SCLAlertView.showSuccessToast(message: "Orchard Saved")
      self.navigationItem.rightBarButtonItem?.isEnabled = false
      
    case let .worker(w) where w.tempory != nil:
      HarvestDB.save(worker: w.tempory!, oldWorker: w)
      w.makeChangesPermanent()
      SCLAlertView.showSuccessToast(message: "Worker Saved")
      self.navigationItem.rightBarButtonItem?.isEnabled = false
      
    case let .session(s) where s.tempory != nil:
      HarvestDB.save(session: s.tempory!)
      SCLAlertView.showSuccessToast(message: "Session Saved")
      self.navigationItem.rightBarButtonItem?.isEnabled = false
      
    case let .user(u) where u.temporary != nil:
      HarvestDB.save(harvestUser: u.temporary!, oldEmail: u.accountIdentifier)
      HarvestUser.current = HarvestUser(json: u.temporary!.json())
      SCLAlertView.showSuccessToast(message: "User Details Saved")
      self.navigationItem.rightBarButtonItem?.isEnabled = false
      
    default:
      SCLAlertView().showError("Cannot Save", subTitle: "The item you are trying to save cannot be saved")
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
