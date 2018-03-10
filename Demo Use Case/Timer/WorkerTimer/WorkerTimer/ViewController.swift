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
  var working: Bool = false
  var lastLocationPollTime: Date? = nil
  
  // Location Trackers
  var locationManager = CLLocationManager()
  var currentLocation: CLLocation?
  var currentWorkArea: WorkArea?
  var tappedWorkArea: WorkArea?
  var zoomLevel: Float = 15.0
  var autoMoveCameraToCurrentLocation = false
  var coder: GMSGeocoder!
  
  let updateTitle: (ViewController) -> Void = { controller in
    if let tapped = controller.tappedWorkArea {
      controller.timerLabel.text = "⏹ " + tapped.title + "\n" + formatTimeInterval(tapped.workingTime)
      return
    }
    
    if let duration = controller.currentWorkArea?.workingTime {
      controller.timerLabel.text = "⏺ " + controller.currentWorkArea!.title + "\n" + formatTimeInterval(duration)
    } else {
      controller.timerLabel.text = "Not in a Work Area"
    }
  }
  // Store user locations
  var locationBuffer = [WorkArea]()
  
  @IBAction func startButtonPressed(_ sender: UIButton) {
    timerButton.isSelected = !timerButton.isSelected
    guard working else {
      locationManager.requestAlwaysAuthorization()
      working = true
      lastLocationPollTime = Date()
      timerButton.backgroundColor = .red
      timerLabel.text = "Starting..."
      return
    }
    updateTitle(self)
    working = false
    lastLocationPollTime = nil
    timerButton.backgroundColor = .green
    let storyboard = UIStoryboard(name: "Main", bundle: nil)
    let vc = storyboard.instantiateViewController(withIdentifier: "WorkTimesTable") as! WorkTimesTableViewController
    vc.locationBuffer = locationBuffer
    for i in locationBuffer.indices {
      locationBuffer[i].workingTime = 0.0
    }
    self.navigationController?.pushViewController(vc, animated: true)
  }
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    mapView.isMyLocationEnabled = true
    mapView.mapType = GMSMapViewType.normal
    mapView.settings.compassButton = true
    mapView.delegate = self
    mapView.settings.myLocationButton = true
    mapView.padding = UIEdgeInsets(top: 42, left: 0, bottom: 101, right: 0)
    
    locationManager.delegate = self
    locationManager.startUpdatingLocation()
    
    coder = GMSGeocoder()
    
    locationBuffer.append(WorkArea(id: 0,
                                   title: "Outside Home",
                                   start: CLLocationCoordinate2D(latitude: -25.832900, longitude: 28.187155),
                                   end: CLLocationCoordinate2D(latitude: -25.836900, longitude: 28.193528)))
    
    locationBuffer.append(WorkArea(id: 1,
                                   title: "Inside Home",
                                   start: CLLocationCoordinate2D(latitude: -25.836900, longitude: 28.187155),
                                   end: CLLocationCoordinate2D(latitude: -25.842100, longitude: 28.193528)))
    
    
    
    
    
    
    for loc in locationBuffer {
      loc.area.map = mapView
    }
    
    timerButton.layer.cornerRadius = timerButton.frame.size.width / 2
    
    timerButton.setTitle("STOP", for: .selected)
    timerButton.setTitle("START", for: .normal)
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
  }
  
  func mapView(_ mapView: GMSMapView, didTapAt coordinate: CLLocationCoordinate2D) {
    for wa in locationBuffer {
      if wa.contains(coordinate) {
        return
      }
    }
    tappedWorkArea?.tap()
    tappedWorkArea = nil
  }
  
  func mapView(_ mapView: GMSMapView, didTap overlay: GMSOverlay) {
    let dict = overlay.userData as! [String: Any]
    
    let id = dict["id"] as! UInt32
    
    guard let find = locationBuffer.first(where: { $0.id == id }) else {
      tappedWorkArea?.tap()
      return
    }
    
    if let tapped = tappedWorkArea, find.id == tapped.id {
      tappedWorkArea?.tap()
      tappedWorkArea = nil
    } else {
      tappedWorkArea?.tap()
      tappedWorkArea = find
      tappedWorkArea?.tap()
    }
  }


}

extension ViewController: CLLocationManagerDelegate {
  
  // Handle incoming location events.
  func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
    currentLocation = locations.last ?? currentLocation
    let camera = GMSCameraPosition.camera(withLatitude: currentLocation!.coordinate.latitude,
                                          longitude: currentLocation!.coordinate.longitude,
                                          zoom: zoomLevel)
    
    if mapView.isHidden {
      mapView.isHidden = false
      mapView.camera = camera
    } else if !autoMoveCameraToCurrentLocation {
      mapView.animate(to: camera)
      autoMoveCameraToCurrentLocation = true
    }
    
    guard working else {
      return
    }
    
    coder.reverseGeocodeCoordinate(currentLocation!.coordinate) { res, err in
      if let err = err {
        print("Geocoder Error: \(err.localizedDescription)")
        return
      }
      guard let coord = res?.firstResult()?.coordinate else {
        print("Geocoder Error")
        return
      }
      let d = self.lastLocationPollTime == nil ? 0.0 : Date().timeIntervalSince(self.lastLocationPollTime!)
      self.lastLocationPollTime = Date()
      self.currentWorkArea = self.locationBuffer.visit(location: coord, forDuration: d) ?? self.currentWorkArea
      if self.currentWorkArea?.id == self.tappedWorkArea?.id {
        self.tappedWorkArea = self.currentWorkArea
      }
      self.updateTitle(self)
    }
    
    
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

