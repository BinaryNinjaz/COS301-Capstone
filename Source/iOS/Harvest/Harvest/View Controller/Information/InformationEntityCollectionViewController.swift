//
//  InformationEntityCollectionViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/19.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit

private let reuseIdentifier = "informationEntityCell"

class InformationEntityCollectionViewController: UICollectionViewController {
  
  let entities = ["Farms", "Orchards", "Workers", "Sessions"]
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
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
  }

  // MARK: - Navigation

  // In a storyboard-based application, you will often want to do a little preparation before navigation
  override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    let vc = segue.destination
    switch vc {
    case is InformationEntityItemTableViewController:
      let tableViewController = vc as! InformationEntityItemTableViewController
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

  override func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
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
      Entities.shared.getMultiplesOnce([.worker, .orchard]) { es in
        self.selectedKind = .worker
        self.performSegue(withIdentifier: "EntityToItems", sender: self)
        self.goingToIndexPath = nil
      }
      
    case "Orchards":
      Entities.shared.getMultiplesOnce([.orchard, .farm]) { es in
        self.selectedKind = .orchard
        self.performSegue(withIdentifier: "EntityToItems", sender: self)
        self.goingToIndexPath = nil
      }
      
    case "Farms":
      Entities.shared.getMultiplesOnce([.farm, .orchard]) { es in
        self.selectedKind = .farm
        self.performSegue(withIdentifier: "EntityToItems", sender: self)
        self.goingToIndexPath = nil
      }
      
    case "Sessions":
      Entities.shared.getMultiplesOnce([.session, .orchard, .worker]) { es in
        self.selectedKind = .session
        self.performSegue(withIdentifier: "EntityToItems", sender: self)
        self.goingToIndexPath = nil
      }
      
    default:
      break
    }
  }
}


extension InformationEntityCollectionViewController : UICollectionViewDelegateFlowLayout {
  func collectionView(_ collectionView: UICollectionView,
                      layout collectionViewLayout: UICollectionViewLayout,
                      sizeForItemAt indexPath: IndexPath
  ) -> CGSize {
    
    let w: CGFloat
    if collectionView.frame.width * 0.95 < 412 {
      w = collectionView.frame.width * 0.95
    } else {
      let n = CGFloat(Int(collectionView.frame.width / 412))
      w = collectionView.frame.width / n - ((n - 1) / n)
    }
    
    let spacing = 64 as CGFloat
    let h = collectionView.frame.height
      - (navigationController?.navigationBar.frame.height ?? 0.0)
      - (tabBarController?.tabBar.frame.height ?? 0.0)
      - spacing
    
    return CGSize(width: w - 2, height: h / 3.5)
  }
}
