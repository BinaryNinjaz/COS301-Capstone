//
//  AdminEurekaCell.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/24.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Eureka

public final class AdminRow: OptionsRow<PushSelectorCell<Box>>, PresenterRowType, RowType {
  
  public typealias PresenterRow = EntityViewController
  
  /// Defines how the view controller will be presented, pushed, etc.
  open var presentationMode: PresentationMode<PresenterRow>?
  
  /// Will be called before the presentation occurs.
  open var onPresentCallback: ((FormViewController, PresenterRow) -> Void)?
  
  public required init(tag: String?) {
    super.init(tag: tag)
    presentationMode = .show(
      controllerProvider: ControllerProvider.callback {
        let storyboard = UIStoryboard(name: "Main", bundle: Bundle.main)
        let vc = storyboard.instantiateViewController(withIdentifier: "entityViewController")
        let evc = vc as! EntityViewController
        return evc
      },
      onDismiss: { vc in _ = vc.navigationController?.popViewController(animated: true) })
  }
  
  public required init(tag: String?, admin: HarvestUser, callback: @escaping (AdminRow) -> ()) {
    super.init(tag: tag)
    presentationMode = .show(
      controllerProvider: ControllerProvider.callback {
        let storyboard = UIStoryboard(name: "Main", bundle: Bundle.main)
        let vc = storyboard.instantiateViewController(withIdentifier: "entityViewController")
        let evc = vc as! EntityViewController
        evc.entity = EntityItem.user(admin)
        return evc
      },
      onDismiss: { vc in _ = vc.navigationController?.popViewController(animated: true) })
    callback(self)
  }
  
  /**
   Extends `didSelect` method
   */
  open override func customDidSelect() {
    super.customDidSelect()
    guard let presentationMode = presentationMode, !isDisabled else { return }
    if let controller = presentationMode.makeController() {
      controller.row = self
      controller.title = selectorTitle ?? controller.title
      onPresentCallback?(cell.formViewController()!, controller)
      presentationMode.present(controller, row: self, presentingController: self.cell.formViewController()!)
    } else {
      presentationMode.present(nil, row: self, presentingController: self.cell.formViewController()!)
    }
  }
  
  /**
   Prepares the pushed row setting its title and completion callback.
   */
  open override func prepare(for segue: UIStoryboardSegue) {
    super.prepare(for: segue)
    guard let rowVC = segue.destination as? PresenterRow else { return }
    rowVC.title = selectorTitle ?? rowVC.title
    rowVC.onDismissCallback = presentationMode?.onDismissCallback ?? rowVC.onDismissCallback
    onPresentCallback?(cell.formViewController()!, rowVC)
    rowVC.row = self
  }
}
