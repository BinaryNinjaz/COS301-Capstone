//
//  TrackerViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/03/29.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import UIKit
import CoreLocation
import SCLAlertView

// swiftlint:disable type_body_length
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
  
  @IBOutlet weak var infoEffectViewHeightConstraint: NSLayoutConstraint?
  var locationManager: CLLocationManager?
  var currentLocation: CLLocation?
  var changedOrchard: Bool?
  var sessionOrchards: [String] = []
  var workers: [Worker] = [] {
    didSet {
      DispatchQueue.main.async {
        self.workerCollectionView?.reloadData()
      }
    }
  }
  var filteredWorkers: [Worker] {
    return workers.filter { (worker) -> Bool in
      if sessionOrchards.isEmpty {
        return true
      }
      
      if !sessionOrchards.isEmpty
      && !worker.assignedOrchards.contains(where: { sessionOrchards.contains($0) }) {
        return false
      }
      guard let searchText = searchBar?.text else {
        return true
      }
      guard !searchText.isEmpty else {
        return true
      }
      return (worker.firstname + " " + worker.lastname)
        .uppercased()
        .contains(searchText.uppercased())
    }
  }
  var gotWorkers = false
  
  @IBOutlet weak var startSessionButton: UIButton?
  @IBOutlet weak var workerCollectionView: UICollectionView?
  @IBOutlet weak var searchBar: UISearchBar?
  @IBOutlet weak var yieldLabel: UILabel?
  @IBOutlet weak var infoEffectView: UIVisualEffectView!
  
  var isPhoneLandscape: Bool {
    return (UIDevice.current.orientation.isPortrait || !UIDevice.current.orientation.isValidInterfaceOrientation)
      && UIDevice.current.userInterfaceIdiom == .phone
  }
  
  var startButtonCornerRadius: CGFloat {
    if isPhoneLandscape {
      return startSessionButton?.frame.width == startSessionButton?.frame.height
        ? (startSessionButton?.frame.width ?? 10) / 2
        : 8
    } else {
      return 8
    }
  }
  
  func updateStartButtonLayer(isStart: Bool) {
    if isStart {
      startSessionButton?.setTitle("Start", for: .normal)
      let sessionLayer = CAGradientLayer.gradient(colors: .startSession,
                                                  locations: [0, 1],
                                                  cornerRadius: self.startButtonCornerRadius,
                                                  borderColor: [UIColor].startSession[1])
      startSessionButton?.apply(gradient: sessionLayer)
    } else {
      self.startSessionButton?.setTitle("Stop", for: .normal)
      let sessionLayer = CAGradientLayer.gradient(colors: .stopSession,
                                                  locations: [0, 1],
                                                  cornerRadius: self.startButtonCornerRadius,
                                                  borderColor: [UIColor].stopSession[1])
      self.startSessionButton?.apply(gradient: sessionLayer)
    }
  }
  
  fileprivate func endCollecting() {
    locationManager?.stopUpdatingLocation()
    
    updateStartButtonLayer(isStart: true)
    
    searchBar?.isUserInteractionEnabled = false
    
    sessionOrchards = []
    yieldLabel?.attributedText = attributedStringForYieldCollection(0)
    
    workerCollectionView?.reloadData()
  }
  
  fileprivate func finishCollecting() {
    if changedOrchard == false {
      tracker?.modifyOrchardAreas()
    }
    endCollecting()
    tracker?.storeSession()
    tracker = nil
    changedOrchard = nil
  }
  
  fileprivate func discardCollections() {
    endCollecting()
    tracker = nil
    changedOrchard = nil
  }
  
  fileprivate func presentYieldCollection() {
    let amount = tracker?.totalCollected() ?? 0
    
    let collection = SCLAlertView(appearance: .optionsAppearance)
    collection.addButton("Finish Collecting") {
      self.finishCollecting()
    }
    collection.addButton("Continue Collecting") {}
    collection.addButton("Discard All Collections") {
      self.discardCollections()
    }
    
    collection.showNotice(
      "\(amount) Bags Collected",
      subTitle: "The session duration was \(tracker?.durationFormatted() ?? "")")
  }
  
  fileprivate func presentNoYieldCollection() {
    let collection = SCLAlertView(appearance: .optionsAppearance)
    
    collection.addButton("Yes, Finish Collecting") {
      self.finishCollecting()
    }
    
    collection.addButton("No, Discard All Collections") {
      self.discardCollections()
    }
    
    collection.showWarning(
      "Save 0 Bags Collected?",
      subTitle: "There was no bags collected. Would you like to save this session?")
  }
  
  override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
    return UIDevice.current.userInterfaceIdiom == .phone ? .portrait : .all
  }
  
  func attributedStringForYieldCollection(_ a: Int) -> NSAttributedString {
    let boldFont = [NSAttributedStringKey.font: UIFont.systemFont(ofSize: 15, weight: .bold)]
    let regularFont = [NSAttributedStringKey.font: UIFont.systemFont(ofSize: 15, weight: .regular)]
    
    let current = NSAttributedString(string: "Current Yield: ", attributes: boldFont)
    let currentAmount = NSAttributedString(string: a.description + "\n", attributes: regularFont)
    
//    let o = Entities.shared.orchards.first { $0.value.id == tracker?.currentOrchard }?.value
//    let oname = o?.description ?? ""
//    let currentLoc = NSAttributedString(string: "Current Location: ", attributes: boldFont)
//    let name = NSAttributedString(string: oname + "\n", attributes: regularFont)
    
    let os = Entities.shared.orchards.first { $0.value.id == sessionOrchards.first }?.value
    let onames = os?.description ?? ""
    let currentSel = NSAttributedString(string: "Selected Orchard: ", attributes: boldFont)
    let names = NSAttributedString(string: onames + "\n", attributes: regularFont)
    
    let result = NSMutableAttributedString()
    
    if os != nil {
      result.append(currentSel)
      result.append(names)
    }
    result.append(current)
    result.append(currentAmount)
//    if o != nil {
//      result.append(currentLoc)
//      result.append(name)
//    }
    
    return result
  }
  
  // swiftlint:disable function_body_length
  func requestSelectedOrchard(_ completion: @escaping () -> Void) {
    let alert = UIAlertController(
      title: "Select An Orchard",
      message: "Choose an orchard to filter in the workers you'll be working with.",
      preferredStyle: .actionSheet)
    
    let lastSel = sessionOrchards.first
    
    for (_, orchard) in Entities.shared.orchards {
      let sel = lastSel == orchard.id ? "✅ " : ""
      let action = UIAlertAction(
        title: sel + orchard.description,
        style: .default) { (_) in
          self.sessionOrchards.removeAll()
          self.sessionOrchards.append(orchard.id)
          self.workerCollectionView?.reloadData()
          self.yieldLabel?.attributedText = self
            .attributedStringForYieldCollection(self.tracker?.totalCollected() ?? 0)
          completion()
      }
      alert.addAction(action)
    }
    let action = UIAlertAction(
      title: "Select All",
      style: .default) { (_) in
        self.sessionOrchards.removeAll()
        self.workerCollectionView?.reloadData()
        self.yieldLabel?.attributedText = self
          .attributedStringForYieldCollection(self.tracker?.totalCollected() ?? 0)
        completion()
    }
    alert.addAction(action)
    let cancel = UIAlertAction(title: "Cancel", style: .cancel, handler: nil)
    alert.addAction(cancel)
    alert.popoverPresentationController?.sourceView = yieldLabel
    
    if Entities.shared.farms.isEmpty {
      let notice = SCLAlertView()
      
      notice.showEdit(
        "No Farms",
        subTitle: """
        You have not added any farms. Please add a farm in 'Information' before using the yield collector.
        """)
    } else if Entities.shared.orchards.isEmpty {
      let notice = SCLAlertView()
      
      notice.showEdit(
        "No Orchards",
        subTitle: """
        You have not added any orchards. Please add orchards in 'Information' before using the yield collector.
        """)
    } else if Entities.shared.workers.isEmpty {
      let notice = SCLAlertView()
      
      notice.showEdit(
        "No Workers",
        subTitle: """
        You have not added any workers. Please add workers in 'Information' before using the yield collector.
        """)
    } else {
      present(alert, animated: true, completion: nil)
    }
  }
  
  @IBAction func startSession(_ sender: Any) {
    if tracker == nil {
      if locationManager == nil {
        locationManager = CLLocationManager()
        locationManager?.delegate = self
        locationManager?.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
      }
      
      HarvestDB.checkLocationRequested { locRequested in
        LocationTracker.shared.requestLocation(wantsLocation: locRequested)
      }
      
      HarvestDB.listenLocationRequested { locRequested in
        LocationTracker.shared.requestLocation(wantsLocation: locRequested)
      }
      
      locationManager?.requestAlwaysAuthorization()
      if CLLocationManager.locationServicesEnabled() {
        locationManager?.startUpdatingLocation()
        requestSelectedOrchard {
          if self.changedOrchard == nil {
            self.changedOrchard = false
          } else if self.changedOrchard == false {
            self.changedOrchard = true
          }
          
          self.updateStartButtonLayer(isStart: false)
          
          self.tracker = Tracker(wid: HarvestUser.current.selectedWorkingForID?.wid ?? HarvestUser.current.uid)
          self.searchBar?.isUserInteractionEnabled = true
          
          self.workerCollectionView?.reloadData()
        }
      } else {
        SCLAlertView().showError(
          "Cannot Access Location",
          subTitle: "Please turn on location services for Harvest from within the Settings App")
      }
    } else {
      if tracker?.collections.count ?? 0 > 0 {
        presentYieldCollection()
      } else {
        presentNoYieldCollection()
      }
      locationManager?.stopUpdatingLocation()
    }
  }
  
  func updateWorkerCells(with newWorkers: [Worker]) {
    gotWorkers = !newWorkers.isEmpty
    workers.removeAll(keepingCapacity: true)
    for worker in newWorkers where worker.kind == .worker {
      workers.append(worker)
    }
    DispatchQueue.main.async {
      self.workerCollectionView?.reloadData()
    }
  }
  
  override func viewDidLoad() {
    super.viewDidLoad()
    hideKeyboardWhenTappedAround()
    
    self.updateStartButtonLayer(isStart: true)
    
    _ = Entities.shared.listen {
      self.gotWorkers = !Entities.shared.workers.isEmpty
      self.updateWorkerCells(with: Entities.shared.workers.map { $0.value })
    }
    
    let infoTouch = UITapGestureRecognizer(target: self, action: #selector(tapInfo(_:)))
    infoTouch.numberOfTapsRequired = 1
    infoTouch.numberOfTouchesRequired = 1
    infoEffectView.addGestureRecognizer(infoTouch)
    
    workerCollectionView?.accessibilityIdentifier = "workerClickerCollectionView"
    
    workerCollectionView?.contentInset = UIEdgeInsets(
      top: 56,
      left: 0,
      bottom: (tabBarController?.tabBar.frame.height ?? 0) + (startSessionButton?.frame.height ?? 0),
      right: 0)
    
    yieldLabel?.attributedText = attributedStringForYieldCollection(0)
  }
  
  @objc func tapInfo(_ recognizer: UIGestureRecognizer) {
    if tracker != nil {
      requestSelectedOrchard {  }
    }
  }
  
  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
  }
  
}

extension TrackerViewController: CLLocationManagerDelegate {
  func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
    guard let loc = locations.first, loc.horizontalAccuracy < 250 else {
      currentLocation = locations.first
      return
    }
    
    currentLocation = loc
    tracker?.track(location: loc)
    self.yieldLabel?.attributedText = self.attributedStringForYieldCollection(
      self.tracker?.totalCollected() ?? 0)
  }
  
  func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
    print(error)
  }
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
  
  func incrementBagCollection(at indexPath: IndexPath) -> (WorkerCollectionViewCell, Int) -> Void {
    return { cell, tries in
      self.locationManager?.requestLocation()
      guard let loc = self.currentLocation else {
        if tries < 100 {
          cell.inc?(cell, tries + 1)
        }
        return
      }
      self.tracker?.collect(
        for: self.filteredWorkers[indexPath.row],
        at: loc,
        selectedOrchard: self.sessionOrchards.first)
      
      cell.yieldLabel.text = self
        .tracker?
        .collections[self.filteredWorkers[indexPath.row]]?.count.description ?? "0"
      
      self.yieldLabel?.attributedText = self.attributedStringForYieldCollection(
        self.tracker?.totalCollected() ?? 0)
    }
  }
  
  func decrementBagCollection(at indexPath: IndexPath) -> (WorkerCollectionViewCell) -> Void {
    return { cell in
      self.tracker?.pop(for: self.filteredWorkers[indexPath.row])
      
      cell.yieldLabel.text = self
        .tracker?
        .collections[self.filteredWorkers[indexPath.row]]?.count.description ?? "0"
      
      self.yieldLabel?.attributedText = self.attributedStringForYieldCollection(
        self.tracker?.totalCollected() ?? 0)
    }
  }
  
  func labelWorkingCell(
    title: String,
    on collectionView: UICollectionView,
    for indexPath: IndexPath
  ) -> UICollectionViewCell {
    let labelCellID = "labelWorkerCollectionViewCell"
    guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: labelCellID, for: indexPath)
      as? LabelWorkingCollectionViewCell else {
        return UICollectionViewCell()
    }
    cell.textLabel.text = title
    return cell
  }
  
  func loadingCell(on collectionView: UICollectionView, for indexPath: IndexPath) -> UICollectionViewCell {
    let loadingCellID = "loadingWorkerCollectionViewCell"
    guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: loadingCellID, for: indexPath)
      as? LoadingWorkerCollectionViewCell else {
        return UICollectionViewCell()
    }
    return cell
  }
  
  func labelFilteredErrorCell(
    on collectionView: UICollectionView,
    for indexPath: IndexPath
  ) -> UICollectionViewCell {
    let labelCellID = "labelWorkerCollectionViewCell"
    guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: labelCellID, for: indexPath)
      as? LabelWorkingCollectionViewCell else {
        return UICollectionViewCell()
    }
    guard let searchText = searchBar?.text else {
      cell.textLabel.text = "Unknown Error Occured"
      return cell
    }
    if searchText.isEmpty {
      let firstOrchard = sessionOrchards.first
      let orchard = Entities.shared.orchards.first { $0.value.id == firstOrchard }
      let oname = orchard == nil
        ? "an orchard"
        : "\"" + orchard!.value.description + "\""
      cell.textLabel.text = "No workers are assigned to \(oname)"
    } else {
      cell.textLabel.text = "No workers that contains '\(searchText)' in their name"
    }
    return cell
  }
  
  func setupWorkerCollectionViewCell(
    _ cell: WorkerCollectionViewCell,
    _ worker: Worker,
    on collectionView: UICollectionView,
    for indexPath: IndexPath
  ) {
    cell.myBackgroundView.frame.size = cell.frame.size
    
    let r = collectionView.convert(cell.frame.origin, to: collectionView)
    
    let h = collectionView.frame.height
    
    let cy = r.y
    let ch = cell.frame.size.height
    let g = CAGradientLayer.gradient(
      colors: [
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
  }
  
  func collectionView(
    _ collectionView: UICollectionView,
    cellForItemAt indexPath: IndexPath
  ) -> UICollectionViewCell {
    guard tracker != nil else {
      return labelWorkingCell(
        title: "Press 'Start' to begin tracking worker collections",
        on: collectionView,
        for: indexPath)
    }
    
    guard gotWorkers else {
      return labelWorkingCell(
        title: "No workers were added to your farm.\nAdd workers in Information",
        on: collectionView,
        for: indexPath)
    }
    
    guard !workers.isEmpty else {
      return loadingCell(on: collectionView, for: indexPath)
    }
    
    guard !filteredWorkers.isEmpty else {
      return labelFilteredErrorCell(on: collectionView, for: indexPath)
    }
    
    let workerCellID = "workerCollectionViewCell"
    guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: workerCellID, for: indexPath)
      as? WorkerCollectionViewCell else {
        return UICollectionViewCell()
    }
    
    let worker = filteredWorkers[indexPath.row]
    
    setupWorkerCollectionViewCell(cell, worker, on: collectionView, for: indexPath)
    
    return cell
  }
  
  func scrollViewDidScroll(_ scrollView: UIScrollView) {
    let cells = workerCollectionView?.visibleCells ?? []
    
    for cell in cells {
      guard let cell = cell as? WorkerCollectionViewCell, let workerCollectionView = workerCollectionView else {
        continue
      }
      
      cell.myBackgroundView.frame.size = cell.frame.size
      
      let r = workerCollectionView.convert(cell.frame.origin, to: workerCollectionView.superview)
      
      let h = workerCollectionView.frame.height
      
      let cy = r.y
      let ch = cell.frame.size.height
      
      let g = CAGradientLayer.gradient(
        colors: [
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

extension TrackerViewController {
  override func viewWillLayoutSubviews() {
    super.viewWillLayoutSubviews()
    
    self.startSessionButton?.setOriginX(view.layoutMargins.left + 8)
    
    let fullWidth = self.infoEffectView.frame.width
    let startWidth = self.startSessionButton?.frame.width ?? 0.0
    let startLeft = self.startSessionButton?.frame.origin.x ?? 0.0
    
    self.yieldLabel?.setWidth(fullWidth - startWidth - startLeft - 16)
    self.yieldLabel?.setOriginX(startLeft + 8 + startWidth)
    
    if isPhoneLandscape {
      infoEffectView.setHeight(80.0)
      infoEffectViewHeightConstraint?.constant = -80.0
      startSessionButton?.setHeight(64)
    } else {
      infoEffectView.setHeight(56.0)
      infoEffectViewHeightConstraint?.constant = -56.0
      startSessionButton?.setHeight(40)
    }
    infoEffectView.updateConstraints()
    updateStartButtonLayer(isStart: tracker == nil)
  }
}

extension TrackerViewController: UICollectionViewDelegateFlowLayout {
  func collectionView(_ collectionView: UICollectionView,
                      layout collectionViewLayout: UICollectionViewLayout,
                      sizeForItemAt indexPath: IndexPath) -> CGSize {
    
    let sbh = UIApplication.shared.statusBarFrame.height
    let tbh = tabBarController?.tabBar.frame.height ?? 0
    let nch = navigationController?.navigationBar.frame.height ?? 0
    let coh = (startSessionButton?.frame.height ?? 0) + 8
    let ofh = sbh + tbh + nch + coh
    
    let w = collectionView.frame.width
    let h = collectionView.frame.height
      - ofh
      - view.layoutMargins.top
      - view.layoutMargins.bottom
    
    let n = CGFloat(Int(w / 156))
    
    let cw = w / n - ((n - 1) / n)
    
    return CGSize(width: shouldDisplayMessage ? w - 2 : cw,
                  height: shouldDisplayMessage ? h : 109)
  }
  
  override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
    super.viewWillTransition(to: size, with: coordinator)
    guard let flowLayout = workerCollectionView?.collectionViewLayout as? UICollectionViewFlowLayout else {
      return
    }
    flowLayout.invalidateLayout()
    workerCollectionView?.reloadData()
  }
}

extension TrackerViewController: UISearchBarDelegate {
  func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
    workerCollectionView?.reloadData()
    searchBar.becomeFirstResponder()
  }
}
