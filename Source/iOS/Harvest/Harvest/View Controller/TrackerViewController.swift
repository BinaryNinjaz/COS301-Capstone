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
      
      locationManager.requestWhenInUseAuthorization()
      if CLLocationManager.locationServicesEnabled() {
        locationManager.startUpdatingLocation()
        
        startSessionButton.setTitle("Stop", for: .normal)
        let sessionLayer = CAGradientLayer.gradient(colors: UIColor.Bootstrap.orange, locations: [0, 1], cornerRadius: 40, borderColor: UIColor.Bootstrap.orange[1])
        startSessionButton.apply(gradient: sessionLayer)
        
        tracker = Tracker()
      }
    } else {
      locationManager.stopUpdatingLocation()
      
      startSessionButton.setTitle("Start", for: .normal)
      let sessionLayer = CAGradientLayer.gradient(colors: UIColor.Bootstrap.green, locations: [0, 1], cornerRadius: 40, borderColor: UIColor.Bootstrap.green[1])
      startSessionButton.apply(gradient: sessionLayer)
      tracker?.storeSession()
      
      tracker = nil
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
  
  override func viewDidLoad() {
    super.viewDidLoad()
    startSessionButton.layer.cornerRadius = 40
    hideKeyboardWhenTappedAround()
    
    let sessionLayer = CAGradientLayer.gradient(colors: UIColor.Bootstrap.green, locations: [0, 1], cornerRadius: 40, borderColor: UIColor.Bootstrap.green[1])
    startSessionButton.apply(gradient: sessionLayer)
    
    HarvestDB.getWorkers { (workers) in
      self.workers.removeAll(keepingCapacity: true)
      for worker in workers {
        self.workers.append(worker)
      }
      DispatchQueue.main.async {
        self.workerCollectionView.reloadData()
      }
    }
    
    workerCollectionView.accessibilityIdentifier = "workerClickerCollectionView"
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
    return workers.count
  }
  
  func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
    guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "workerCollectionViewCell", for: indexPath) as? WorkerCollectionViewCell else {
      return UICollectionViewCell()
    }
    
    let worker = workers[indexPath.row]
    
    cell.myBackgroundView.frame.size = cell.frame.size
    
    cell.myBackgroundView.apply(gradient: CAGradientLayer.blue)
    cell.nameLabel.text = worker.firstname + " " + worker.lastname
    cell.yieldLabel.text = String(tracker?.collections[worker]?.count ?? 0)
    
    return cell
  }
}

extension TrackerViewController : UICollectionViewDelegate {
  func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
    guard tracker != nil else {
      let alert = UIAlertController.alertController(
        title: "Session Not Started",
        message: "Please start the session before collecting yields")
      
      present(alert, animated: true, completion: nil)
      return
    }
    
    guard let loc = currentLocation else {
      let alert = UIAlertController.alertController(
        title: "Location Unavailable",
        message: "Please allow location service from the 'Settings' app")
      
      present(alert, animated: true, completion: nil)
      return
    }
    
    tracker?.collect(for: workers[indexPath.row], at: loc)
    
    workerCollectionView.deselectItem(at: indexPath, animated: true)
    
    workerCollectionView.reloadData()
  }
}

extension TrackerViewController : UICollectionViewDelegateFlowLayout {
  func collectionView(_ collectionView: UICollectionView,
                      layout collectionViewLayout: UICollectionViewLayout,
                      sizeForItemAt indexPath: IndexPath) -> CGSize {
    return CGSize(width: collectionView.frame.width / 2 - 0.5,
                  height: collectionView.frame.width / 2 - 0.5);
  }
}
