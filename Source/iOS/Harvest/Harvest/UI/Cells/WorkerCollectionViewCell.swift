//
//  WorkerCollectionViewCell.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/05.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit

class WorkerCollectionViewCell: UICollectionViewCell {
  @IBOutlet weak var nameLabel: UILabel!
  @IBOutlet weak var yieldLabel: UILabel!
  @IBOutlet weak var myBackgroundView: UIView!
  
  var inc: ((WorkerCollectionViewCell) -> ())?
  var dec: ((WorkerCollectionViewCell) -> ())?
  
  @IBAction func incrementTouchUp(_ sender: Any) {
    inc?(self)
  }
  
  @IBAction func decrementTouchUp(_ sender: Any) {
    dec?(self)
  }
  
}
