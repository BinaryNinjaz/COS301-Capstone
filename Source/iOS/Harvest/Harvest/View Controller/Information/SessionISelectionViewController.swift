//
//  SessionInformationViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/05/04.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit

class SessionSelectionViewController: UITableViewController {
  var pageIndex: String?
  let pageSize: UInt = 21
  var isLoading = false
  var selectedSession: Session?
  var filteredSessions: SortedDictionary<String, SortedArray<SearchPair<Session>>>?
  var sessions = SortedDictionary<Date, SortedSet<Session>>(>)
  typealias SessionsIndex = SortedDictionary<Date, SortedSet<Session>>.Index
  @IBOutlet weak var searchBar: UISearchBar?
  var searchText: String = ""
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    refreshControl = UIRefreshControl()
    refreshControl?.addTarget(self, action: #selector(refreshList(_:)), for: .valueChanged)
    tableView.addSubview(refreshControl!)
    
    searchBar?.delegate = self
    
    loadNewPage()
  }
  
  override func viewWillAppear(_ animated: Bool) {
    selectedSession = nil
    if let selPath = tableView.indexPathForSelectedRow {
      tableView.deselectRow(at: selPath, animated: true)
    }
  }
  
  func loadNewPage() {
    guard sessions.count > 0 else {
      let label = UILabel(frame: tableView.frame)
      label.textAlignment = .center
      label.textColor = UIColor.black.withAlphaComponent(0.3)
      label.numberOfLines = -1
      label.text = "Session are created when work is done from the 'Yield Tracker' Tab."
      tableView.backgroundView = label
      return
    }
    tableView.backgroundView = nil
    
    guard !isLoading else {
      return
    }
    
    isLoading = true
    tableView.reloadData()
    HarvestDB.getSessions(limitedToLast: pageSize) { psessions in
      self.sessions.accumulateByDay(with: psessions)
      if self.searchText.isEmpty {
        self.filteredSessions = nil
      } else {
        self.filteredSessions = self.sessions.search(for: self.searchText)
      }
      DispatchQueue.main.async {
        self.isLoading = false
        self.tableView.reloadData()
      }
    }
  }
  
  @objc func refreshList(_ refreshControl: UIRefreshControl) {
    guard !isLoading else {
      return
    }
    
    isLoading = true
    HarvestDB.getRefreshedSessions(limitedToLast: pageSize) { psessions in
      self.sessions.removeAll()
      self.sessions.accumulateByDay(with: psessions)
      if self.searchText.isEmpty {
        self.filteredSessions = nil
      } else {
        self.filteredSessions = self.sessions.search(for: self.searchText)
      }
      DispatchQueue.main.async {
        self.isLoading = false
        self.refreshControl?.endRefreshing()
        self.tableView.reloadData()
      }
    }
  }
  
  var itemGroupCount: Int {
    return filteredSessions?.count ?? sessions.count
  }
  
  override func numberOfSections(in tableView: UITableView) -> Int {
    return itemGroupCount + (isLoading ? 1 : 0)
  }
  
  override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    if isLoading && section == itemGroupCount {
      return 1
    }
    
    if let filtered = filteredSessions {
      let s = filtered.startIndex
      let i = filtered.index(s, offsetBy: section)
      return filtered[i].value.count
    } else {
      return sessions[SessionsIndex(section)].value.count
    }
  }
  
  override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    if isLoading && indexPath.section == itemGroupCount {
      let cell = tableView.dequeueReusableCell(withIdentifier: "sessionSelectionLoadingCell", for: indexPath)
      for subview in cell.contentView.subviews where subview is UIActivityIndicatorView {
        (subview as? UIActivityIndicatorView)?.startAnimating()
      }
      return cell
    }
    
    let cell = tableView.dequeueReusableCell(withIdentifier: "sessionSelectionCell", for: indexPath)
    
    if let filtered = filteredSessions {
      let s = filtered.startIndex
      let i = filtered.index(s, offsetBy: indexPath.section)
      let pair = filtered[i].value[indexPath.row]
      let (item, reason) = (pair.item, pair.reason)
      
      let formatter = DateFormatter()
      formatter.dateStyle = .short
      formatter.timeStyle = .short
      
      cell.textLabel?.text = item.foreman.name + " - " + formatter.string(from: item.startDate)
      cell.detailTextLabel?.text = reason
    } else {
      let sidx = SessionsIndex(indexPath.section)
      
      let formatter = DateFormatter()
      formatter.dateStyle = .none
      formatter.timeStyle = .short
      
      let session = sessions[sidx].value[indexPath.row]
      let date = session.startDate
      let name = session.foreman.description
      cell.textLabel?.text = name + " - " + formatter.string(from: date)
      cell.detailTextLabel?.text = ""
    }
    
    return cell
  }
  
  override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    guard !isLoading || indexPath.section != itemGroupCount else {
      return
    }
    
    if let filtered = filteredSessions {
      let s = filtered.startIndex
      let i = filtered.index(s, offsetBy: indexPath.section)
      selectedSession = filtered[i].value[indexPath.row].item
      performSegue(withIdentifier: "SessionToItem", sender: self)
    } else {
      let sidx = SessionsIndex(indexPath.section)
      
      selectedSession = sessions[sidx].value[indexPath.row]
      performSegue(withIdentifier: "SessionToItem", sender: self)
    }
  }
  
  override func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
    guard !isLoading || section != itemGroupCount else {
      return nil
    }
    
    if let filtered = filteredSessions {
      let s = filtered.startIndex
      let i = filtered.index(s, offsetBy: section)
      return filtered[i].key
    } else {
      let formatter = DateFormatter()
      formatter.dateStyle = .medium
      
      let sidx = SessionsIndex(section)
      let date = sessions[sidx].key
      return formatter.string(from: date)
    }
  }
  
  override func scrollViewDidEndDragging(_ scrollView: UIScrollView, willDecelerate decelerate: Bool) {
    if scrollView.contentOffset.y + scrollView.frame.size.height >= scrollView.contentSize.height {
      if !isLoading {
        loadNewPage()
      }
    }
  }
  
  override func shouldPerformSegue(withIdentifier identifier: String, sender: Any?) -> Bool {
    return selectedSession != nil
  }
  
  override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
    let vc = segue.destination
    
    guard let entityVC = vc as? EntityViewController else {
      fatalError("Check to make sure only one segue exists to an EntityViewController")
    }
    
    guard let session = selectedSession else {
      fatalError("Somthing went wrong. Never get to segue without a selected session")
    }
    
    entityVC.title = session.description
    entityVC.entity = .session(session)
    
  }
}

extension SessionSelectionViewController: UISearchBarDelegate {
  func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
    self.searchText = searchText
    if searchText.isEmpty {
      filteredSessions = nil
    } else {
      filteredSessions = sessions.search(for: searchText)
    }
    tableView.reloadData()
  }
}
