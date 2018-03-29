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
  
  @IBOutlet weak var startSessionButton: UIButton!
  @IBOutlet weak var expectedYieldLabel: UILabel!
  @IBOutlet weak var collectButton: UIButton!
  @IBOutlet weak var yieldAmountTextField: UITextField!
  
  
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
        startSessionButton.backgroundColor = .orange
        
        tracker = Tracker()
      }
    } else {
      locationManager.stopUpdatingLocation()
      
      collectButton.isEnabled = false
      startSessionButton.backgroundColor = .moss
      
      tracker = nil
    }
  }
  
  @IBAction func collectYield(_ sender: Any) {
    guard let yieldText = yieldAmountTextField.text else {
      let alert = UIAlertController.alertController(
        title: "Missing Yield Amount",
        message: "Please enter amount of yield collect before pressing collect")
      
      present(alert, animated: true, completion: nil)
      return
    }
    
    guard let yield = Double(yieldText) else {
      let alert = UIAlertController.alertController(
        title: "Incorrect Yield",
        message: "Please enter a number for the yield amount")
      
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
    
    yieldAmountTextField.text = ""
    tracker.collect(yield: yield, at: loc)
    view.endEditing(true)
  }
  
  override func viewDidLoad() {
    super.viewDidLoad()
    startSessionButton.layer.cornerRadius = 60
    hideKeyboardWhenTappedAround()
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
