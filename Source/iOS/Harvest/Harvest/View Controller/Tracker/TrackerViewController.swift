//
//  TrackerViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/03/29.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit
import CoreLocation

class TrackerViewController: UIViewController {
  static var tracker: Tracker? = nil
  var tracker: Tracker? {
    get {
      return TrackerViewController.tracker
    }
    set {
      TrackerViewController.tracker = newValue
    }
  }
  var locationManager: CLLocationManager!
  var currentLocation: CLLocation?
  var workers: [Worker] = [] {
    didSet {
      filteredWorkers = workers
    }
  }
  var lastLocationPoll: Date? = nil
  var filteredWorkers: [Worker] = []
  
  @IBOutlet weak var startSessionButton: UIButton!
  @IBOutlet weak var workerCollectionView: UICollectionView!
  @IBOutlet weak var searchBar: UISearchBar!
  @IBOutlet weak var yieldLabel: UILabel!
  
  
  @IBAction func startSession(_ sender: Any) {
    if tracker == nil {
      if locationManager == nil {
        locationManager = CLLocationManager()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
      }
      
      locationManager.requestAlwaysAuthorization()
      if CLLocationManager.locationServicesEnabled() {
        locationManager.startUpdatingLocation()
        
        startSessionButton.setTitle("Stop", for: .normal)
        let sessionLayer = CAGradientLayer.gradient(colors: UIColor.Bootstrap.orange, locations: [0, 1], cornerRadius: 40, borderColor: UIColor.Bootstrap.orange[1])
        startSessionButton.apply(gradient: sessionLayer)
        
        tracker = Tracker(wid: HarvestUser.current.workingForID?.wid ?? HarvestUser.current.uid)
        searchBar.isUserInteractionEnabled = true
        
        workerCollectionView.reloadData()
      }
    } else {
      locationManager.stopUpdatingLocation()
      
      startSessionButton.setTitle("Start", for: .normal)
      let sessionLayer = CAGradientLayer.gradient(colors: UIColor.Bootstrap.green, locations: [0, 1], cornerRadius: 40, borderColor: UIColor.Bootstrap.green[1])
      startSessionButton.apply(gradient: sessionLayer)
      tracker?.storeSession()
      
      
      let amount = tracker?.totalCollected() ?? 0
      let alert = UIAlertController.alertController(title: "\(amount) Bags Collected", message: "The session duration was \(tracker?.durationFormatted() ?? "")")
      
      present(alert, animated: true, completion: nil)
      
      tracker = nil
      searchBar.isUserInteractionEnabled = false
      
      workerCollectionView.reloadData()
    }
  }
  
  func updateWorkerCells(with newWorkers: [Worker]) {
    workers.removeAll(keepingCapacity: true)
    for worker in newWorkers {
      workers.append(worker)
    }
    DispatchQueue.main.async {
      self.workerCollectionView.reloadData()
    }
  }
  
  override func viewDidLoad() {
    super.viewDidLoad()
    startSessionButton.layer.cornerRadius = 40
    hideKeyboardWhenTappedAround()
    
    
    let sessionLayer = CAGradientLayer.gradient(colors: UIColor.Bootstrap.green, locations: [0, 1], cornerRadius: 40, borderColor: UIColor.Bootstrap.green[1])
    startSessionButton.apply(gradient: sessionLayer)
    
    HarvestDB.watchWorkers { (workers) in
      self.updateWorkerCells(with: workers)
    }
    
    workerCollectionView.accessibilityIdentifier = "workerClickerCollectionView"
    
    workerCollectionView.contentInset = UIEdgeInsets(top: 0,
                                                     left: 0,
                                                     bottom: 106,
                                                     right: 0)
    
    yieldLabel.attributedText = attributedStringForYieldCollection(0, 0)
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
  }

}

extension TrackerViewController : CLLocationManagerDelegate {
  func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
    guard let loc = locations.first else {
      return
    }
    currentLocation = loc
    tracker?.track(location: loc)
    if (lastLocationPoll == nil || Date().timeIntervalSince(lastLocationPoll!) > 60 || true) {
      HarvestDB.update(location: loc.coordinate)
      lastLocationPoll = Date()
    }
  }
}

func attributedStringForYieldCollection(_ a: Int, _ p: Int) -> NSAttributedString {
  let boldFont = [NSAttributedStringKey.font: UIFont.systemFont(ofSize: 15, weight: .bold)]
  let regularFont = [NSAttributedStringKey.font: UIFont.systemFont(ofSize: 15, weight: .regular)]
  
  let current = NSAttributedString(string: "Current Yield: ", attributes: boldFont)
  let currentAmount = NSAttributedString(string: a.description, attributes: regularFont)
  let expected = NSAttributedString(string: "\nExpected Yield: ", attributes: boldFont)
  let expectedAmount = NSAttributedString(string: p.description, attributes: regularFont)
  
  let result = NSMutableAttributedString()
  result.append(current)
  result.append(currentAmount)
  result.append(expected)
  result.append(expectedAmount)
  
  return result
}

extension TrackerViewController : UICollectionViewDataSource {
  
  var shouldDisplayMessage: Bool {
    return tracker == nil
      || workers.isEmpty
      || filteredWorkers.isEmpty
  }
  
  func numberOfSections(in collectionView: UICollectionView) -> Int {
    return 1
  }
  
  func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
    return shouldDisplayMessage ? 1 : filteredWorkers.count
  }
  
  func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
    
    guard tracker != nil else {
      guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "labelWorkerCollectionViewCell", for: indexPath) as? LabelWorkingCollectionViewCell else {
        return UICollectionViewCell()
      }
      cell.textLabel.text = "Press 'Start' to begin tracking worker collections"
      return cell
    }
    
    guard !workers.isEmpty else {
      guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "loadingWorkerCollectionViewCell", for: indexPath) as? LoadingWorkerCollectionViewCell else {
        return UICollectionViewCell()
      }
//      cell.textLabel.text = "No workers were added to your farm"
      return cell
    }
    
    guard !filteredWorkers.isEmpty else {
      guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "labelWorkerCollectionViewCell", for: indexPath) as? LabelWorkingCollectionViewCell else {
        return UICollectionViewCell()
      }
      guard let searchText = searchBar.text else {
        cell.textLabel.text = "Unknown Error Occured"
        return cell
      }
      cell.textLabel.text = "No workers that contains '\(searchText)' in their name"
      return cell
    }
    
    
    guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "workerCollectionViewCell", for: indexPath) as? WorkerCollectionViewCell else {
      return UICollectionViewCell()
    }
    
    let worker = filteredWorkers[indexPath.row]
    
    cell.myBackgroundView.frame.size = cell.frame.size
    
    let r = collectionView.convert(cell.frame.origin, to: collectionView)
    
    let h = collectionView.frame.height
    
    let cy = r.y
    let ch = cell.frame.size.height
    let g = CAGradientLayer.gradient(
      colors: [
        UIColor.green(atFraction: cy / h),
        UIColor.green(atFraction: (cy + ch) / h)
      ],
      locations: [0, 1],
      cornerRadius: 5,
      borderColor: .clear)
    
    cell.myBackgroundView.apply(gradient: g)
    
    cell.nameLabel.text = worker.firstname + " " + worker.lastname
    cell.yieldLabel.text = String(tracker?.collections[worker]?.count ?? 0)
    
    
    cell.inc = { this in
      guard self.tracker != nil else {
        let alert = UIAlertController.alertController(
          title: "Session Not Started",
          message: "Please start the session before collecting yields")
        
        self.present(alert, animated: true, completion: nil)
        return
      }
      
      guard let loc = self.currentLocation else {
        let alert = UIAlertController.alertController(
          title: "Location Unavailable",
          message: "Please allow location service from the 'Settings' app")
        
        self.present(alert, animated: true, completion: nil)
        return
      }
      
      self.tracker?.collect(for: self.filteredWorkers[indexPath.row], at: loc)
      
      this.yieldLabel.text = self
        .tracker?
        .collections[self.filteredWorkers[indexPath.row]]?.count.description ?? "0"
      
      self.yieldLabel.attributedText = attributedStringForYieldCollection(
        self.tracker?.totalCollected() ?? 0,
        Int(Double(self.tracker?.totalCollected() ?? 0) * 1.1))
    }
    
    cell.dec = { this in
      guard self.tracker != nil else {
        let alert = UIAlertController.alertController(
          title: "Session Not Started",
          message: "Please start the session before collecting yields")
        
        self.present(alert, animated: true, completion: nil)
        return
      }
      
      self.tracker?.pop(for: self.filteredWorkers[indexPath.row])
      
      this.yieldLabel.text = self
        .tracker?
        .collections[self.filteredWorkers[indexPath.row]]?.count.description ?? "0"
      
      
      
      self.yieldLabel.attributedText = attributedStringForYieldCollection(
        self.tracker?.totalCollected() ?? 0,
        Int(Double(self.tracker?.totalCollected() ?? 0) * 1.1))
    }
    
    return cell
  }
  
  func scrollViewDidScroll(_ scrollView: UIScrollView) {
    let cells = workerCollectionView.visibleCells
    
    for cell in cells {
      guard let cell = cell as? WorkerCollectionViewCell else {
        continue
      }
      
      let r = workerCollectionView.convert(cell.frame.origin, to: workerCollectionView.superview)
      
      let h = workerCollectionView.frame.height
      
      let cy = r.y
      let ch = cell.frame.size.height
      
      let g = CAGradientLayer.gradient(
        colors: [
          UIColor.green(atFraction: cy / h),
          UIColor.green(atFraction: (cy + ch) / h)
        ],
        locations: [0, 1],
        cornerRadius: 5,
        borderColor: .clear)
      
      cell.myBackgroundView.apply(gradient: g)
    }
  }
}

extension TrackerViewController : UICollectionViewDelegate {
  func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
    
  }
}

extension TrackerViewController : UICollectionViewDelegateFlowLayout {
  func collectionView(_ collectionView: UICollectionView,
                      layout collectionViewLayout: UICollectionViewLayout,
                      sizeForItemAt indexPath: IndexPath) -> CGSize {
    
    let w = collectionView.frame.width
    let h = collectionView.frame.height - 300
    
    let n = CGFloat(Int(w / 186))
    
    let cw = w / n - ((n - 1) / n)
    
    return CGSize(width: shouldDisplayMessage ? w - 2 : cw,
                  height: shouldDisplayMessage ? h : 109);
  }
}

extension TrackerViewController : UISearchBarDelegate {
  func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
    filteredWorkers = workers.filter({ (worker) -> Bool in
      guard searchText != "" else {
        return true
      }
      return (worker.firstname + " " + worker.lastname)
        .uppercased()
        .contains(searchText.uppercased())
    })
    
    workerCollectionView.reloadData()
    searchBar.becomeFirstResponder()
  }
}
