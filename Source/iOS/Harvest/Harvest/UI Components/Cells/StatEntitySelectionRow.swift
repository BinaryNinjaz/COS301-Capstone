//
//  StatEntitySelectionRow.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/11.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import Eureka

final class StatEntitySelectionRow: OptionsRow<PushSelectorCell<[String]>>, PresenterRowType, RowType {
  
  typealias PresenterRow = StatEntitySelectionViewController
  
  /// Defines how the view controller will be presented, pushed, etc.
  open var presentationMode: PresentationMode<PresenterRow>?
  
  /// Will be called before the presentation occurs.
  open var onPresentCallback: ((FormViewController, PresenterRow) -> Void)?
  
  var actuallyChanged: ((RowOf<Orchard>) -> Void)?
  
  var timePeriod: TimePeriod?
  var timeStep: TimeStep?
  var grouping: StatKind = .worker
  var mode: TimedGraphMode?
  
  required init(tag: String?) {
    super.init(tag: tag)
    cell.detailTextLabel?.textColor = .white
    presentationMode = .show(
      controllerProvider: ControllerProvider.callback {
        let storyboard = UIStoryboard(name: "Main", bundle: Bundle.main)
        let vc = storyboard.instantiateViewController(withIdentifier: "statEntitySelectionViewController")
        guard let rowVC = vc as? StatEntitySelectionViewController else {
          fatalError("We should never get here. We instantiated from statEntitySelectionViewController")
        }
//        rowVC.actuallyChanged = self.actuallyChanged
        rowVC.timePeriod = self.timePeriod
        rowVC.timeStep = self.timeStep
        rowVC.grouping = self.grouping
        rowVC.mode = self.mode
        return rowVC
      },
      onDismiss: { vc in _ = vc.navigationController?.popViewController(animated: true) })
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
