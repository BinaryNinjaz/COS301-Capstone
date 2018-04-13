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
  var tracker: Tracker?
  var locationManager: CLLocationManager!
  var currentLocation: CLLocation?
  var workers: [Worker] = []
  
  @IBOutlet weak var startSessionButton: UIButton!
  @IBOutlet weak var workerCollectionView: UICollectionView!
  
  
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
        
        tracker = Tracker()
        
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
      
      workerCollectionView.reloadData()
    }
  }
  
  @IBAction func collectYield(_ sender: Any) {
    guard let idx = workerCollectionView.indexPathsForSelectedItems?.first else {
      return
    }
    
    guard let loc = currentLocation else {
      let alert = UIAlertController.alertController(
        title: "Location Unavailable",
        message: "Please allow location service from the 'Settings' app")
      
      present(alert, animated: true, completion: nil)
      return
    }
    
    tracker?.collect(for: workers[idx.row], at: loc)
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
    
    HarvestDB.getWorkers { (workers) in
      self.updateWorkerCells(with: workers)
    }
    
    workerCollectionView.accessibilityIdentifier = "workerClickerCollectionView"
    
    workerCollectionView.contentInset = UIEdgeInsets(top: 0,
                                                     left: 0,
                                                     bottom: 106,
                                                     right: 0)
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
  }
}

extension TrackerViewController : UICollectionViewDataSource {
  func numberOfSections(in collectionView: UICollectionView) -> Int {
    return 1
  }
  
  func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
    return tracker == nil || workers.isEmpty ? 1 : workers.count
  }
  
  func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
    
    guard tracker != nil else {
      guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "startingWorkerCollectionViewCell", for: indexPath) as? StartingWorkingCollectionViewCell else {
        return UICollectionViewCell()
      }
      
      return cell
    }
    
    guard !workers.isEmpty else {
      guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "loadingWorkerCollectionViewCell", for: indexPath) as? LoadingWorkerCollectionViewCell else {
        return UICollectionViewCell()
      }
      
      return cell
    }
    
    
    guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "workerCollectionViewCell", for: indexPath) as? WorkerCollectionViewCell else {
      return UICollectionViewCell()
    }
    
    let worker = workers[indexPath.row]
    
    cell.myBackgroundView.frame.size = cell.frame.size
    
    cell.myBackgroundView.apply(gradient: CAGradientLayer.blue)
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
      
      self.tracker?.collect(for: self.workers[indexPath.row], at: loc)
      
      this.yieldLabel.text = self
        .tracker?
        .collections[self.workers[indexPath.row]]?.count.description ?? "0"
    }
    
    cell.dec = { this in
      guard self.tracker != nil else {
        let alert = UIAlertController.alertController(
          title: "Session Not Started",
          message: "Please start the session before collecting yields")
        
        self.present(alert, animated: true, completion: nil)
        return
      }
      
      self.tracker?.pop(for: self.workers[indexPath.row])
      
      this.yieldLabel.text = self
        .tracker?
        .collections[self.workers[indexPath.row]]?.count.description ?? "0"
    }
    
    return cell
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
    let h = collectionView.frame.height - 186
    
    let n = CGFloat(Int(w / 186))
    
    let cw = w / n - ((n - 1) / n)
    
    return CGSize(width: tracker == nil || workers.isEmpty ? w - 2 : cw,
                  height: tracker == nil || workers.isEmpty ? h : 109);
  }
}
