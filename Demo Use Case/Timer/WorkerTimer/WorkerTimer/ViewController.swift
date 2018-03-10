//
//  ViewController.swift
//  WorkerTimer
//
//  Created by Letanyan Arumugam on 2018/03/09.
//

import UIKit
import GoogleMaps

class ViewController: UIViewController, GMSMapViewDelegate {
  // UI Components
  @IBOutlet weak var mapView: GMSMapView!
  @IBOutlet weak var timerButton: UIButton!
  @IBOutlet weak var timerLabel: UILabel!
  
  // Time Trackers
  var startTime: Date? = nil
  var endTime: Date? = nil
  var lastLocationPoll: Date? = nil
  
  // Location Trackers
  var locationManager = CLLocationManager()
  var currentLocation: CLLocation?
  var zoomLevel: Float = 15.0
  var autoMoveCameraToCurrentLocation = false
  var coder: GMSGeocoder!
  
  // Timer
  var timer: Timer! = nil
  let timerBlock: (ViewController) -> (Timer) -> Void = { controller in
    return { _ in
      guard let st = controller.startTime else {
        return
      }
      let duration = Date().timeIntervalSince(st)
      controller.timerLabel.text = formatTimeInterval(duration)
    }
  }
  // Store user locations
  var locationBuffer = [GMSAddress: TimeInterval]()
  
  @IBAction func startButtonPressed(_ sender: UIButton) {
    timerButton.isSelected = !timerButton.isSelected
    guard let st = startTime else {
      locationManager.requestAlwaysAuthorization()
      startTime = Date()
      lastLocationPoll = Date()
      endTime = nil
      timer = Timer(timeInterval: 1,
                    repeats: true,
                    block: timerBlock(self))
      timer.fire()
      RunLoop.current.add(timer, forMode: RunLoopMode.defaultRunLoopMode)
      timerButton.backgroundColor = .red
      return
    }
    if (timer.isValid) {
      timer.invalidate()
      timer = nil
    }
    endTime = Date()
    startTime = nil
    let duration = endTime!.timeIntervalSince(st)
    timerButton.backgroundColor = .green
    print(locationBuffer)
    timerLabel.text = formatTimeInterval(duration)
  }
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    mapView.isMyLocationEnabled = true
    mapView.mapType = GMSMapViewType.normal
    mapView.settings.compassButton = true
    mapView.delegate = self
    mapView.settings.myLocationButton = true
    
    locationManager.delegate = self
    locationManager.startUpdatingLocation()
    
    coder = GMSGeocoder()
    
    timerButton.setTitle("STOP", for: .selected)
    timerButton.setTitle("START", for: .normal)
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
  }


}

extension ViewController: CLLocationManagerDelegate {
  
  // Handle incoming location events.
  func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
    currentLocation = locations.last
    let camera = GMSCameraPosition.camera(withLatitude: currentLocation!.coordinate.latitude,
                                          longitude: currentLocation!.coordinate.longitude,
                                          zoom: zoomLevel)
    
    
    coder.reverseGeocodeCoordinate(currentLocation!.coordinate) { res, err in
      if let err = err {
        print("Geocoder Error: \(err.localizedDescription)")
        return
      }
      guard let addr = res?.firstResult() else {
        print("Geocoder Error: No Address")
        return
      }
      let d = Date().timeIntervalSince(self.lastLocationPoll!)
      self.locationBuffer[addr, default: 0.0] += d
    }
    
    if mapView.isHidden {
      mapView.isHidden = false
      mapView.camera = camera
    } else if !autoMoveCameraToCurrentLocation {
      mapView.animate(to: camera)
      autoMoveCameraToCurrentLocation = true
    }
    
    lastLocationPoll = Date()
  }
  
  // Handle authorization for the location manager.
  func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
    switch status {
    case .restricted:
      print("Location access was restricted.")
    case .denied:
      print("User denied access to location.")
    case .notDetermined:
      print("Location status not determined.")
    case .authorizedAlways: fallthrough
    case .authorizedWhenInUse:
      print("Location status is OK.")
    }
  }
  
  // Handle location manager errors.
  func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
    locationManager.stopUpdatingLocation()
    print("Error: \(error)")
  }
}

