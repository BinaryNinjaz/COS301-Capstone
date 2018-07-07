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
  static var tracker: Tracker?
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
  var currentOrchardID: String = "" {
    didSet {
      if currentOrchardID != "" {
        filteredWorkers = filteredWorkers.filter { $0.assignedOrchards.contains(currentOrchardID) }
        DispatchQueue.main.async {
          self.workerCollectionView.reloadData()
        }
      }
    }
  }
  var workers: [Worker] = [] {
    didSet {
      filteredWorkers = workers
      if currentOrchardID != "" {
        filteredWorkers = filteredWorkers.filter { $0.assignedOrchards.contains(currentOrchardID) }
        DispatchQueue.main.async {
          self.workerCollectionView.reloadData()
        }
      }
    }
  }
  var filteredWorkers: [Worker] = []
  var expectedYield: Double = .nan
  
  @IBOutlet weak var startSessionButton: UIButton!
  @IBOutlet weak var workerCollectionView: UICollectionView!
  @IBOutlet weak var searchBar: UISearchBar!
  @IBOutlet weak var yieldLabel: UILabel!
  
  fileprivate func finishCollecting() {
    locationManager.stopUpdatingLocation()
    
    startSessionButton.setTitle("Start", for: .normal)
    let sessionLayer = CAGradientLayer.gradient(colors: .startSession,
                                                locations: [0, 1],
                                                cornerRadius: 40,
                                                borderColor: [UIColor].startSession[1])
    startSessionButton.apply(gradient: sessionLayer)
    tracker?.storeSession()
    
    tracker = nil
    expectedYield = .nan
    yieldLabel.attributedText = attributedStringForYieldCollection(0, expectedYield)
    searchBar.isUserInteractionEnabled = false
    
    workerCollectionView.reloadData()
  }
  
  fileprivate func discardCollections() {
    self.locationManager.stopUpdatingLocation()
    
    self.startSessionButton.setTitle("Start", for: .normal)
    let sessionLayer = CAGradientLayer.gradient(colors: .startSession,
                                                locations: [0, 1],
                                                cornerRadius: 40,
                                                borderColor: [UIColor].startSession[1])
    self.startSessionButton.apply(gradient: sessionLayer)
    
    self.tracker = nil
    expectedYield = .nan
    yieldLabel.attributedText = attributedStringForYieldCollection(0, expectedYield)
    self.searchBar.isUserInteractionEnabled = false
    
    self.workerCollectionView.reloadData()
  }
  
  fileprivate func presentYieldCollection() {
    let amount = tracker?.totalCollected() ?? 0
    let alert = UIAlertController(title: "\(amount) Bags Collected",
      message: "The session duration was \(tracker?.durationFormatted() ?? "")",
      preferredStyle: .alert)
    
    let collect = UIAlertAction(title: "Finish Collecting", style: .default) { _ in
      self.finishCollecting()
    }
    
    let cancel = UIAlertAction(title: "Continue Collecting", style: .cancel) { _ in }
    
    let discard = UIAlertAction(title: "Discard All Collections", style: .default) { _ in
      self.discardCollections()
    }
    
    alert.addAction(collect)
#if DEBUG
    alert.addAction(discard)
#endif
    alert.addAction(cancel)
    
    present(alert, animated: true, completion: nil)
  }
  
  fileprivate func presentNoYieldCollection() {
    let alert = UIAlertController(title: "0 Bags Collected",
      message: "There was no bags collected. Would you like to save this session?",
      preferredStyle: .alert)
    
    let collect = UIAlertAction(title: "Yes, Finish Collecting", style: .default) { _ in
      self.finishCollecting()
    }
    
    let discard = UIAlertAction(title: "No, Discard All Collections", style: .default) { _ in
      self.discardCollections()
    }
    
    alert.addAction(collect)
    alert.addAction(discard)
    
    present(alert, animated: true, completion: nil)
  }
  
  override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
    return UIDevice.current.userInterfaceIdiom == .phone ? .portrait : .all
  }
  
  @IBAction func startSession(_ sender: Any) {
    if tracker == nil {
      if locationManager == nil {
        locationManager = CLLocationManager()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyHundredMeters
      }
      
      locationManager.requestAlwaysAuthorization()
      if CLLocationManager.locationServicesEnabled() {
        locationManager.startUpdatingLocation()
        startSessionButton.setTitle("Stop", for: .normal)
        let sessionLayer = CAGradientLayer.gradient(colors: .stopSession,
                                                    locations: [0, 1],
                                                    cornerRadius: 40,
                                                    borderColor: [UIColor].stopSession[1])
        startSessionButton.apply(gradient: sessionLayer)
        
        tracker = Tracker(wid: HarvestUser.current.workingForID?.wid ?? HarvestUser.current.uid)
        searchBar.isUserInteractionEnabled = true
        
        workerCollectionView.reloadData()
      } else {
        UIAlertController.present(title: "Cannot Access Location",
                                  message: "Please turn on location services for Harvest from within the Settings App",
                                  on: self)
      }
    } else {
      if tracker?.collections.count ?? 0 > 0 {
        presentYieldCollection()
      } else {
        presentNoYieldCollection()
      }
      
    }
  }
  
  func updateWorkerCells(with newWorkers: [Worker]) {
    workers.removeAll(keepingCapacity: true)
    for worker in newWorkers where worker.kind == .worker {
      workers.append(worker)
    }
    DispatchQueue.main.async {
      self.workerCollectionView.reloadData()
    }
  }
  
  override func viewDidLoad() {
    super.viewDidLoad()
    startSessionButton.layer.cornerRadius = startSessionButton.frame.width / 2
    hideKeyboardWhenTappedAround()
    
    let sessionLayer = CAGradientLayer.gradient(colors: .startSession,
                                                locations: [0, 1],
                                                cornerRadius: 40,
                                                borderColor: [UIColor].startSession[1])
    startSessionButton.apply(gradient: sessionLayer)
    
    Entities.shared.getOnce(.worker) { _ in }
    
    HarvestDB.watchWorkers { (workers) in
      self.updateWorkerCells(with: workers)
    }
    
    workerCollectionView.accessibilityIdentifier = "workerClickerCollectionView"
    
    workerCollectionView.contentInset = UIEdgeInsets(top: 0,
                                                     left: 0,
                                                     bottom: 106,
                                                     right: 0)
    
    yieldLabel.attributedText = attributedStringForYieldCollection(0, expectedYield)
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
  }

}

extension TrackerViewController: CLLocationManagerDelegate {
  func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
    guard let loc = locations.first else {
      return
    }
    currentLocation = loc
    if let oid = tracker?.track(location: loc) {
      currentOrchardID = oid
      tracker?.updateExpectedYield(orchardId: oid) { expected in
        self.expectedYield = expected
        DispatchQueue.main.async {
          self.yieldLabel.attributedText = attributedStringForYieldCollection(
            self.tracker?.totalCollected() ?? 0,
            self.expectedYield)
        }
      }
    }
  }
  
  func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
    print(error)
  }
}

func attributedStringForYieldCollection(_ a: Int, _ b: Double) -> NSAttributedString {
  let boldFont = [NSAttributedStringKey.font: UIFont.systemFont(ofSize: 15, weight: .bold)]
  let regularFont = [NSAttributedStringKey.font: UIFont.systemFont(ofSize: 15, weight: .regular)]
  
  let current = NSAttributedString(string: "Current Yield: ", attributes: boldFont)
  let currentAmount = NSAttributedString(string: a.description, attributes: regularFont)
  let expected = NSAttributedString(string: "\nExpected Yield: ", attributes: boldFont)
  let expectedText = b.isNaN ? " -- " : Int(b).description
  let expectedAmount = NSAttributedString(string: expectedText, attributes: regularFont)
  
  let result = NSMutableAttributedString()
  result.append(current)
  result.append(currentAmount)
  result.append(expected)
  result.append(expectedAmount)
  
  return result
}

extension TrackerViewController: UICollectionViewDataSource {
  
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
  
  func incrementBagCollection(at indexPath: IndexPath) -> (WorkerCollectionViewCell) -> Void {
    return { cell in
      self.locationManager.requestLocation()
      guard let loc = self.currentLocation else {
        cell.inc?(cell)
        return
      }
      self.tracker?.collect(for: self.filteredWorkers[indexPath.row], at: loc)
      
      cell.yieldLabel.text = self
        .tracker?
        .collections[self.filteredWorkers[indexPath.row]]?.count.description ?? "0"
      
      self.yieldLabel.attributedText = attributedStringForYieldCollection(
        self.tracker?.totalCollected() ?? 0,
        self.expectedYield)
    }
  }
  
  func decrementBagCollection(at indexPath: IndexPath) -> (WorkerCollectionViewCell) -> Void {
    return { cell in
      self.tracker?.pop(for: self.filteredWorkers[indexPath.row])
      
      cell.yieldLabel.text = self
        .tracker?
        .collections[self.filteredWorkers[indexPath.row]]?.count.description ?? "0"
      
      self.yieldLabel.attributedText = attributedStringForYieldCollection(
        self.tracker?.totalCollected() ?? 0,
        self.expectedYield)
    }
  }
  
  // swiftlint:disable function_body_length
  func collectionView(
    _ collectionView: UICollectionView,
    cellForItemAt indexPath: IndexPath
  ) -> UICollectionViewCell {
    let labelCellID = "labelWorkerCollectionViewCell"
    let loadingCellID = "loadingWorkerCollectionViewCell"
    let workerCellID = "workerCollectionViewCell"
    
    guard tracker != nil else {
      guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: labelCellID, for: indexPath)
        as? LabelWorkingCollectionViewCell else {
        return UICollectionViewCell()
      }
      cell.textLabel.text = "Press 'Start' to begin tracking worker collections"
      return cell
    }
    
    guard !workers.isEmpty else {
      guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: loadingCellID, for: indexPath)
        as? LoadingWorkerCollectionViewCell else {
        return UICollectionViewCell()
      }
//      cell.textLabel.text = "No workers were added to your farm"
      return cell
    }
    
    guard !filteredWorkers.isEmpty else {
      guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: labelCellID, for: indexPath)
        as? LabelWorkingCollectionViewCell else {
        return UICollectionViewCell()
      }
      guard let searchText = searchBar.text else {
        cell.textLabel.text = "Unknown Error Occured"
        return cell
      }
      cell.textLabel.text = "No workers that contains '\(searchText)' in their name"
      return cell
    }
    
    guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: workerCellID, for: indexPath)
      as? WorkerCollectionViewCell else {
      return UICollectionViewCell()
    }
    
    let worker = filteredWorkers[indexPath.row]
    
    cell.myBackgroundView.frame.size = cell.frame.size
    
    let r = collectionView.convert(cell.frame.origin, to: collectionView)
    
    let h = collectionView.frame.height
    
    let cy = r.y
    let ch = cell.frame.size.height
    let g = CAGradientLayer.gradient(colors: [
                                       UIColor.gradientColor(from: .sessionTiles, atFraction: cy / h),
                                       UIColor.gradientColor(from: .sessionTiles, atFraction: (cy + ch) / h)
                                     ],
                                     locations: [0, 1],
                                     cornerRadius: 0,
                                     borderColor: .clear)
    
    cell.myBackgroundView.apply(gradient: g)
    
    cell.nameLabel.text = worker.firstname + " " + worker.lastname
    cell.yieldLabel.text = String(tracker?.collections[worker]?.count ?? 0)
    
    let topColor = UIColor.gradientColor(from: .sessionTilesText, atFraction: cy / h)
    let bottomColor = UIColor.gradientColor(from: .sessionTilesText, atFraction: (cy + ch) / h)
    
    cell.nameLabel.textColor = .black
    cell.yieldLabel.textColor = .black
    cell.incButton.setTitleColor(topColor, for: .normal)
    cell.decButton.setTitleColor(bottomColor, for: .normal)
    
    cell.inc = incrementBagCollection(at: indexPath)
    cell.dec = decrementBagCollection(at: indexPath)
    
    return cell
  }
  
  func scrollViewDidScroll(_ scrollView: UIScrollView) {
    let cells = workerCollectionView.visibleCells
    
    for cell in cells {
      guard let cell = cell as? WorkerCollectionViewCell else {
        continue
      }
      
      cell.myBackgroundView.frame.size = cell.frame.size
      
      let r = workerCollectionView.convert(cell.frame.origin, to: workerCollectionView.superview)
      
      let h = workerCollectionView.frame.height
      
      let cy = r.y
      let ch = cell.frame.size.height
      
      let g = CAGradientLayer.gradient(colors: [
                                         UIColor.gradientColor(from: .sessionTiles, atFraction: cy / h),
                                         UIColor.gradientColor(from: .sessionTiles, atFraction: (cy + ch) / h)
                                       ],
                                       locations: [0, 1],
                                       cornerRadius: 0,
                                       borderColor: .clear)
      
      cell.myBackgroundView.apply(gradient: g)
      
      let topColor = UIColor.gradientColor(from: .sessionTilesText, atFraction: cy / h)
      let bottomColor = UIColor.gradientColor(from: .sessionTilesText, atFraction: (cy + ch) / h)
      
      cell.nameLabel.textColor = .black
      cell.yieldLabel.textColor = .black
      cell.incButton.setTitleColor(topColor, for: .normal)
      cell.decButton.setTitleColor(bottomColor, for: .normal)
    }
  }
}

extension TrackerViewController: UICollectionViewDelegate {
  func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
    
  }
}

extension TrackerViewController: UICollectionViewDelegateFlowLayout {
  func collectionView(_ collectionView: UICollectionView,
                      layout collectionViewLayout: UICollectionViewLayout,
                      sizeForItemAt indexPath: IndexPath) -> CGSize {
    
    let sbh = UIApplication.shared.statusBarFrame.height
    let tbh = tabBarController?.tabBar.frame.height ?? 0
    let nch = navigationController?.navigationBar.frame.height ?? 0
    let coh = startSessionButton.frame.height + 8
    let ofh = sbh + tbh + nch + coh
    
    let w = collectionView.frame.width
    let h = collectionView.frame.height - ofh
    
    let n = CGFloat(Int(w / 156))
    
    let cw = w / n - ((n - 1) / n)
    
    return CGSize(width: shouldDisplayMessage ? w - 2 : cw,
                  height: shouldDisplayMessage ? h : 109)
  }
  
  override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
    super.viewWillTransition(to: size, with: coordinator)
    guard let flowLayout = workerCollectionView.collectionViewLayout as? UICollectionViewFlowLayout else {
      return
    }
    flowLayout.invalidateLayout()
    workerCollectionView.reloadData()
  }
}

extension TrackerViewController: UISearchBarDelegate {
  func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
    filteredWorkers = workers.filter({ (worker) -> Bool in
      guard worker.assignedOrchards.contains(currentOrchardID) else {
        return false
      }
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
