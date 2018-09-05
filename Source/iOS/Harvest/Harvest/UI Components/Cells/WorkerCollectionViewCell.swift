//
//  WorkerCollectionViewCell.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/05.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import UIKit

class WorkerCollectionViewCell: UICollectionViewCell {
  @IBOutlet weak var nameLabel: UILabel!
  @IBOutlet weak var yieldLabel: UILabel!
  @IBOutlet weak var myBackgroundView: UIView!
  @IBOutlet weak var incButton: UIButton!
  @IBOutlet weak var decButton: UIButton!
  var countLabelHoldGestureRecognizer: UILongPressGestureRecognizer?
  
  var inc: ((WorkerCollectionViewCell, Int) -> Void)?
  var dec: ((WorkerCollectionViewCell) -> Void)?
  var rec: ((Int) -> Void)?
  
  func initGesturesIfNeeded() {
    guard countLabelHoldGestureRecognizer == nil else {
      return
    }
    
    countLabelHoldGestureRecognizer = .init(target: self, action: #selector(holdCountLabel(recognizer:)))
    yieldLabel.addGestureRecognizer(countLabelHoldGestureRecognizer!)
  }
  
  @objc func holdCountLabel(recognizer: UIGestureRecognizer) {
    rec?(0)
  }
  
  @IBAction func incrementTouchUp(_ sender: Any) {
    inc?(self, 0)
  }
  
  @IBAction func decrementTouchUp(_ sender: Any) {
    dec?(self)
  }
  
}
