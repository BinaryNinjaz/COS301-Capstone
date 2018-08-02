//
//  SessionFilterManager.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/31.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit

class SessionFilterManager: NSObject, UICollectionViewDataSource, UICollectionViewDelegate {
  var collectionView: UICollectionView?
  var selectedOrchards: [String] = []
  var orchardSelectionChanged: ([String]) -> Void = { _ in }
  
  var orchards: [Orchard] = [] {
    didSet {
      DispatchQueue.main.async {
        self.collectionView?.reloadData()
      }
    }
  }
  
  override init() {
    super.init()
    _ = Entities.shared.listen {
      self.orchards = Entities.shared.orchards.map { $0.value }
    }
  }
  
  func numberOfSections(in collectionView: UICollectionView) -> Int {
    return 1
  }
  
  func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
    return orchards.count
  }
  
  func select(_ sel: Bool, cell: UICollectionViewCell?) {
    guard let cell = cell as? SessionFilterItemCollectionViewCell else {
      return
    }
    
    if sel {
      cell.backgroundColor = UIColor.harvestGreen.withAlphaComponent(0.8)
      cell.titleLabel.textColor = .white
    } else {
      cell.backgroundColor = UIColor.harvestGreen.withAlphaComponent(0.1)
      cell.titleLabel.textColor = UIColor.harvestGreen.withAlphaComponent(0.8)
    }
  }
  
  func collectionView(
    _ collectionView: UICollectionView,
    cellForItemAt indexPath: IndexPath
    ) -> UICollectionViewCell {
    guard let cell = collectionView
      .dequeueReusableCell(withReuseIdentifier: "sessionOrchardFilterItem", for: indexPath)
      as? SessionFilterItemCollectionViewCell else {
        return UICollectionViewCell()
    }
    
    let orchard = orchards[indexPath.row]
    
    if let farm = Entities.shared.farms.first(where: { $0.value.id == orchard.assignedFarm }) {
      cell.titleLabel.text = farm.value.name + " - " + orchard.name
    } else {
      cell.titleLabel.text = orchard.name
    }
    
    select(selectedOrchards.contains(orchard.id), cell: cell)
    
    return cell
  }
  
  func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
    let orchard = orchards[indexPath.row]
    let cell = collectionView.cellForItem(at: indexPath)
    
    if let idx = selectedOrchards.index(of: orchard.id) {
      select(false, cell: cell)
      selectedOrchards.remove(at: idx)
    } else {
      select(true, cell: cell)
      selectedOrchards.append(orchard.id)
    }
    
    orchardSelectionChanged(selectedOrchards)
  }
}
