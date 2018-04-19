//
//  InformationEntityItemTableViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/19.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit

class InformationEntityItemTableViewController: UITableViewController {
  var items = SortedDictionary<String, EntityItem>(<)
  var selectedEntity: EntityItem? = nil
  
  override func viewDidLoad() {
    super.viewDidLoad()
  }
  
  override func viewWillAppear(_ animated: Bool) {
    if let sel = tableView.indexPathForSelectedRow {
      tableView.deselectRow(at: sel, animated: false)
    }
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
  }

  override func numberOfSections(in tableView: UITableView) -> Int {
    return 1
  }

  override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    return items.count
  }

  override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    let cell = tableView.dequeueReusableCell(withIdentifier: "informationEntityItemCell", for: indexPath)
    
    guard let item = items[indexPath.row] else {
      return cell
    }
    
    switch item {
    case let .worker(w):
      cell.textLabel?.text = w.firstname + " " + w.lastname
    case let .orchard(o):
      cell.textLabel?.text = o.name
    case let .farm(f):
      cell.textLabel?.text = f.name
    case .userInfo:
      break
    }
    
    return cell
  }
  
  override func tableView(_ tableView: UITableView, willSelectRowAt indexPath: IndexPath) -> IndexPath? {
    selectedEntity = items[indexPath.row]
    return indexPath
  }

  // MARK: - Navigation

  // In a storyboard-based application, you will often want to do a little preparation before navigation
  override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
    let vc = segue.destination
    
    if let entityViewController = vc as? EntityViewController {
      entityViewController.entity = selectedEntity
      if navigationItem.title == "Workers" {
        guard let item = items["___orchards___"],
          case let .userInfo(userInfo) = item else {
          return
        }
        entityViewController.other = userInfo as? [EntityItem] ?? []
      } else if navigationItem.title == "Orchards" {
        guard let item = items["___farms___"],
          case let .userInfo(userInfo) = item else {
            return
        }
        entityViewController.other = userInfo as? [EntityItem] ?? []
      }
      
    }
  }

}
