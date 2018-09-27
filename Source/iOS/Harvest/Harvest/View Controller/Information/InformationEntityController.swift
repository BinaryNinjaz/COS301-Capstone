//
//  InformationEntityCollectionViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/19.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import UIKit

private let reuseIdentifier = "informationEntityCell"

class InformationEntityController: UICollectionViewController {
  
  var entities: [String] {
    if Entities.shared.farms.isEmpty {
      return ["Farms"]
    } else if Entities.shared.orchards.isEmpty {
      return ["Farms", "Orchards"]
    } else {
      return ["Farms", "Orchards", "Workers"]
    }
  }
  var entity = ""
  var selectedKind: EntityItem.Kind = .none
  var goingToIndexPath: IndexPath? = nil {
    willSet {
      if let currentIndexPath = goingToIndexPath {
        let cell = collectionView?.cellForItem(at: currentIndexPath)
          as? InformationEntityCollectionViewCell
        cell?.activityIndicator.stopAnimating()
        cell?.image.alpha = 1.0
      }
      if let newIndexPath = newValue {
        let cell = collectionView?.cellForItem(at: newIndexPath)
          as? InformationEntityCollectionViewCell
        cell?.activityIndicator.startAnimating()
        cell?.image.alpha = 0.5
      }
    }
  }
  
  override func viewDidLoad() {
    super.viewDidLoad()
  }
  
  override func viewDidAppear(_ animated: Bool) {
    entity = ""
    selectedKind = .none
    collectionView.reloadData()
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
  }

  // MARK: - Navigation
  override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
    let vc = segue.destination
    switch vc {
    case is InformationEntityItemTableViewController:
      guard let tableViewController = vc as? InformationEntityItemTableViewController else {
        fatalError("We should never get here")
      }
      tableViewController.navigationItem.title = entity
      tableViewController.kind = selectedKind
      
    default:
      break
    }
  }
  
  override func shouldPerformSegue(withIdentifier identifier: String, sender: Any?) -> Bool {
    if selectedKind == .none {
      return false
    }
    return true
  }

  // MARK: UICollectionViewDataSource

  override func numberOfSections(in collectionView: UICollectionView) -> Int {
    return 1
  }

  override func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
    return entities.count
  }

  override func collectionView(
    _ collectionView: UICollectionView,
    cellForItemAt indexPath: IndexPath
  ) -> UICollectionViewCell {
    guard let cell = collectionView
      .dequeueReusableCell(withReuseIdentifier: "informationEntityCell", for: indexPath)
      as? InformationEntityCollectionViewCell else {
        return collectionView.dequeueReusableCell(withReuseIdentifier: reuseIdentifier, for: indexPath)
    }
    
    let entity = entities[indexPath.row]
    
    cell.image.image = UIImage(named: entity)
    cell.textLabel.text = entity
    if goingToIndexPath == indexPath {
      cell.activityIndicator.startAnimating()
      cell.image.alpha = 0.75
    } else {
      cell.activityIndicator.stopAnimating()
      cell.image.alpha = 1.0
    }
    
    cell.layer.cornerRadius = 16
    
    return cell
  }
  
  override func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
    entity = entities[indexPath.row]
    guard goingToIndexPath == nil else {
      return
    }
    goingToIndexPath = indexPath
    
    switch entity {
    case "Workers":
      Entities.shared.getMultiplesOnce([.farm, .orchard, .worker]) { _ in
        self.selectedKind = .worker
        self.performSegue(withIdentifier: "EntityToItems", sender: self)
        self.goingToIndexPath = nil
      }
      
    case "Orchards":
      Entities.shared.getMultiplesOnce([.worker, .farm, .orchard]) { _ in
        self.selectedKind = .orchard
        self.performSegue(withIdentifier: "EntityToItems", sender: self)
        self.goingToIndexPath = nil
      }
      
    case "Farms":
      Entities.shared.getMultiplesOnce([.orchard, .farm]) { _ in
        self.selectedKind = .farm
        self.performSegue(withIdentifier: "EntityToItems", sender: self)
        self.goingToIndexPath = nil
      }
      
    case "Sessions":
      Entities.shared.getMultiplesOnce([.orchard, .worker, .session]) { _ in
        self.selectedKind = .session
        self.performSegue(withIdentifier: "EntityToItems", sender: self)
        self.goingToIndexPath = nil
      }
      
    default:
      break
    }
  }
}

extension InformationEntityController: UICollectionViewDelegateFlowLayout {
  func collectionView(_ collectionView: UICollectionView,
                      layout collectionViewLayout: UICollectionViewLayout,
                      sizeForItemAt indexPath: IndexPath
  ) -> CGSize {
    
    let w, h: CGFloat
    if #available(iOS 11.0, *) {
      let sf = view.safeAreaInsets
      w = collectionView.frame.width
        - sf.left
        - sf.right
      h = (collectionView.frame.height
        - sf.top
        - sf.bottom)
        / 3.2
    } else {
      w = collectionView.frame.width
      h = (collectionView.frame.height
        - collectionView.contentInset.top
        - collectionView.contentInset.bottom
        - view.layoutMargins.top
        - view.layoutMargins.bottom)
        / 3.2
    }
    
    let n = min(CGFloat(Int(w / 256)), 2)
    
    let cw = (w - n - 1) / n
    let ch = max(h, cw * 0.5)
    
    return CGSize(width: cw, height: ch)
  }
  
  override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
    super.viewWillTransition(to: size, with: coordinator)
    guard let flowLayout = collectionView?.collectionViewLayout as? UICollectionViewFlowLayout else {
      return
    }
    flowLayout.invalidateLayout()
    collectionView?.reloadData()
  }
}
