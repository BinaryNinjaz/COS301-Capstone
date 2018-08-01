//
//  StatEntitySelectionRow.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/11.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Eureka

final class StatEntitySelectionRow: OptionsRow<PushSelectorCell<[EntityItem]>>, PresenterRowType, RowType {
  
  typealias PresenterRow = StatEntitySelectionViewController
  
  /// Defines how the view controller will be presented, pushed, etc.
  open var presentationMode: PresentationMode<PresenterRow>?
  
  /// Will be called before the presentation occurs.
  open var onPresentCallback: ((FormViewController, PresenterRow) -> Void)?
  
  var actuallyChanged: ((RowOf<Orchard>) -> Void)?
  
  var startDate: Date?
  var endDate: Date?
  var period: HarvestCloud.TimePeriod?
  var grouping: HarvestCloud.GroupBy = .worker
  
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
        rowVC.startDate = self.startDate
        rowVC.endDate = self.endDate
        rowVC.period = self.period
        rowVC.grouping = self.grouping
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
