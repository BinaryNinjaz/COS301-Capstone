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
  var tracker: Tracker!
  var locationManager: CLLocationManager!
  var currentLocation: CLLocation?
  var workers: [Worker] = []
  
  @IBOutlet weak var startSessionButton: UIButton!
  @IBOutlet weak var expectedYieldLabel: UILabel!
  @IBOutlet weak var collectButton: UIButton!
  @IBOutlet weak var workerPicker: UIPickerView!
  
  
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
        
        collectButton.isEnabled = true
        startSessionButton.setTitle("Stop", for: .normal)
        let sessionLayer = CAGradientLayer.gradient(colors: UIColor.Bootstrap.orange, locations: [0, 1], cornerRadius: 60, borderColor: UIColor.Bootstrap.orange[1])
        startSessionButton.apply(gradient: sessionLayer)
        
        tracker = Tracker()
      }
    } else {
      locationManager.stopUpdatingLocation()
      
      collectButton.isEnabled = false
      startSessionButton.setTitle("Start", for: .normal)
      let sessionLayer = CAGradientLayer.gradient(colors: UIColor.Bootstrap.green, locations: [0, 1], cornerRadius: 60, borderColor: UIColor.Bootstrap.green[1])
      startSessionButton.apply(gradient: sessionLayer)
      HarvestDB.collect(from: tracker.collections, from: HarvestUser.current.name, on: Date())
      
      tracker = nil
    }
  }
  
  @IBAction func collectYield(_ sender: Any) {
    let idx = workerPicker.selectedRow(inComponent: 0)
    
    guard let loc = currentLocation else {
      let alert = UIAlertController.alertController(
        title: "Location Unavailable",
        message: "Please allow location service from the 'Settings' app")
      
      present(alert, animated: true, completion: nil)
      return
    }
    
    tracker.collect(for: workers[idx], at: loc)
  }
  
  override func viewDidLoad() {
    super.viewDidLoad()
    startSessionButton.layer.cornerRadius = 35
    hideKeyboardWhenTappedAround()
    
    let sessionLayer = CAGradientLayer.gradient(colors: UIColor.Bootstrap.green, locations: [0, 1], cornerRadius: 35, borderColor: UIColor.Bootstrap.green[1])
    startSessionButton.apply(gradient: sessionLayer)
    
    collectButton.apply(gradient: .green)
    
    workerPicker.delegate = self
    workerPicker.dataSource = self
    
    HarvestDB.getWorkers { (workers) in
      for worker in workers {
        self.workers.append(worker)
      }
      DispatchQueue.main.async {
        self.workerPicker.reloadAllComponents()
      }
    }
    
    // Do any additional setup after loading the view.
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
  }

}

extension TrackerViewController : CLLocationManagerDelegate {
  func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
    currentLocation = locations.first
  }
}

extension TrackerViewController : UIPickerViewDelegate, UIPickerViewDataSource {
  func numberOfComponents(in pickerView: UIPickerView) -> Int {
    return 1
  }
  
  func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
    return workers.count
  }
  
  func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
    return workers[row].firstname + " " + workers[row].lastname
  }
  
}
