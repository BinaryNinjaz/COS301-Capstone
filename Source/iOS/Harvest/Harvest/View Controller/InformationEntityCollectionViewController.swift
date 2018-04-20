//
//  InformationEntityCollectionViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/19.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit

private let reuseIdentifier = "informationEntityCell"

enum EntityItem {
  case farm(Farm)
  case orchard(Orchard)
  case worker(Worker)
  case userInfo(Any)
  
  var name: String {
    switch self {
    case let .farm(f): return f.name
    case let .orchard(o): return o.name
    case let .worker(w): return w.firstname + " " + w.lastname
    case .userInfo: return ""
    }
  }
}

class InformationEntityCollectionViewController: UICollectionViewController {
  
  let entities = ["Farms", "Orchards", "Workers"]
  var entity = ""
  var items = SortedDictionary<String, EntityItem>(<)
  var goingToIndexPath: IndexPath? = nil {
    willSet {
      if let currentIndexPath = goingToIndexPath {
        let cell = collectionView?.cellForItem(at: currentIndexPath)
          as? InformationEntityCollectionViewCell
        cell?.activityIndicator.stopAnimating()
      }
      if let newIndexPath = newValue {
        let cell = collectionView?.cellForItem(at: newIndexPath)
          as? InformationEntityCollectionViewCell
        cell?.activityIndicator.startAnimating()
      }
    }
  }
  
  override func viewDidLoad() {
    super.viewDidLoad()
  }
  
  override func viewDidAppear(_ animated: Bool) {
    items = SortedDictionary<String, EntityItem>(<)
    entity = ""
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
      tableViewController.items = items
      tableViewController.navigationItem.title = entity
      
    default:
      break
    }
  }
  
  override func shouldPerformSegue(withIdentifier identifier: String, sender: Any?) -> Bool {
    if items.isEmpty {
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
    } else {
      cell.activityIndicator.stopAnimating()
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
      HarvestDB.getWorkers { (workers) in
        HarvestDB.getOrchards({ (orchards) in
          self.items = SortedDictionary(
            uniqueKeysWithValues: workers.map { worker in
              return (worker.firstname + " " + worker.lastname, .worker(worker))
          }, <)
          self.items["___orchards___"] = .userInfo(orchards.map { EntityItem.orchard($0) })
          Entities.shared.workers = items
          self.performSegue(withIdentifier: "EntityToItems", sender: self)
          self.goingToIndexPath = nil
        })
        
      }
      
    case "Orchards":
      HarvestDB.getOrchards { (orchards) in
        HarvestDB.getFarms({ (farms) in
          self.items = SortedDictionary(
            uniqueKeysWithValues: orchards.map { orchard in
              return (orchard.name, .orchard(orchard))
          }, <)
          self.items["___farms___"] = .userInfo(farms.map { EntityItem.farm($0) })
          Entities.shared.orchards = items
          self.performSegue(withIdentifier: "EntityToItems", sender: self)
          self.goingToIndexPath = nil
        })
      }
      
    case "Farms":
      HarvestDB.getFarms { (farms) in
        self.items = SortedDictionary(
          uniqueKeysWithValues: farms.map { farm in
            return (farm.name, .farm(farm))
        }, <)
        Entities.shared.farms = items
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
    let w = collectionView.frame.width * 0.95
    let spacing = 64 as CGFloat
    let h = collectionView.frame.height
      - (navigationController?.navigationBar.frame.height ?? 0.0)
      - (tabBarController?.tabBar.frame.height ?? 0.0)
      - spacing
    
    return CGSize(width: w - 2, height: h / 3)
  }
}
