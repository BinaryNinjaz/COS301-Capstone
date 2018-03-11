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
    if !controller.working {
      controller.timerLabel.text = "Tap Start to Begin"
      return
    }
    
    let headerAttribute = [
      NSAttributedStringKey.font: UIFont.systemFont(ofSize: 20, weight: UIFont.Weight.semibold)
    ]
    let bodyAttibute = [
      NSAttributedStringKey.font: UIFont.systemFont(ofSize: 20, weight: UIFont.Weight.light)
    ]
    
    if let tapped = controller.tappedWorkArea {
      let ms = NSMutableAttributedString()
      let msTitle = NSAttributedString(string: "⚫️ \(tapped.title)", attributes: headerAttribute)
      let msBody = NSAttributedString(string: "\n\(formatTimeInterval(tapped.workingTime))", attributes: bodyAttibute)
      
      ms.append(msTitle)
      ms.append(msBody)
      
      controller.timerLabel.attributedText = ms
      return
    }
    
    if let duration = controller.currentWorkArea?.workingTime {
      let ms = NSMutableAttributedString()
      let msTitle = NSAttributedString(string: "🔵 \(controller.currentWorkArea!.title)", attributes: headerAttribute)
      let msBody = NSAttributedString(string: "\n\(formatTimeInterval(duration))", attributes: bodyAttibute)
      
      ms.append(msTitle)
      ms.append(msBody)
      
      controller.timerLabel.attributedText = ms
    } else {
      let msTitle = NSAttributedString(string: "❌ Not in a Work Area", attributes: headerAttribute)
      
      controller.timerLabel.attributedText = msTitle
    }
  }
  // Store user locations
  var workZones = [WorkArea]()
  
  @IBAction func startButtonPressed(_ sender: UIButton) {
    timerButton.isSelected = !timerButton.isSelected
    guard working else {
      locationManager.requestAlwaysAuthorization()
      working = true
      tappedWorkArea?.tap()
      tappedWorkArea = nil
      lastLocationPollTime = Date()
      timerButton.backgroundColor = .red
      let headerAttribute = [
        NSAttributedStringKey.font: UIFont.systemFont(ofSize: 20, weight: UIFont.Weight.semibold)
      ]
      let msTitle = NSAttributedString(string: "🔴 Starting...", attributes: headerAttribute)
      timerLabel.attributedText = msTitle
      return
    }
    updateTitle(self)
    working = false
    lastLocationPollTime = nil
    timerButton.backgroundColor = .green
    let storyboard = UIStoryboard(name: "Main", bundle: nil)
    let vc = storyboard
      .instantiateViewController(
        withIdentifier: "WorkTimesTable") as! WorkTimesTableViewController
    vc.locationBuffer = workZones
    for i in workZones.indices {
      workZones[i].workingTime = 0.0
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
    
    workZones.append(WorkArea(id: 0,
                             title: "Outside Home",
                             start: CLLocationCoordinate2D(
                              latitude: -25.832900,
                              longitude: 28.187155),
                             end: CLLocationCoordinate2D(
                              latitude: -25.836900,
                              longitude: 28.193528)))
    
    workZones.append(WorkArea(id: 1,
                                   title: "Inside Home",
                                   start: CLLocationCoordinate2D(
                                    latitude: -25.836900,
                                    longitude: 28.187155),
                                   end: CLLocationCoordinate2D(
                                    latitude: -25.842100,
                                    longitude: 28.193528)))
    
    workZones.append(WorkArea(id: 2,
                              title: "North Campus",
                              start: CLLocationCoordinate2D(
                                latitude: -25.750657,
                                longitude: 28.228649),
                              end: CLLocationCoordinate2D(
                                latitude: -25.753012,
                                longitude: 28.231911)))
    
    workZones.append(WorkArea(id: 3,
                              title: "West Campus",
                              start: CLLocationCoordinate2D(
                                latitude: -25.753128,
                                longitude: 28.225404),
                              end: CLLocationCoordinate2D(
                                latitude: -25.756538,
                                longitude: 28.228913)))
    
    workZones.append(WorkArea(id: 4,
                              title: "South Campus",
                              start: CLLocationCoordinate2D(
                                latitude: -25.753272,
                                longitude: 28.229095),
                              end: CLLocationCoordinate2D(
                                latitude: -25.756836,
                                longitude: 28.232210)))
    
    workZones.append(WorkArea(id: 5,
                              title: "East Campus",
                              start: CLLocationCoordinate2D(
                                latitude: -25.752205,
                                longitude: 28.232455),
                              end: CLLocationCoordinate2D(
                                latitude: -25.757786,
                                longitude: 28.236677)))
    
    for loc in workZones {
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
  
  func mapView(
    _ mapView: GMSMapView,
    didTapAt coordinate: CLLocationCoordinate2D
  ) {
    for wa in workZones {
      if wa.contains(coordinate) {
        return
      }
    }
    // if we tap something thats not a zone deselect the last tapped
    tappedWorkArea?.tap()
    tappedWorkArea = nil
  }
  
  func mapView(_ mapView: GMSMapView, didTap overlay: GMSOverlay) {
    let dict = overlay.userData as! [String: Any]
    
    let id = dict["id"] as! UInt32
    
    guard let find = workZones.first(where: { $0.id == id }) else {
      tappedWorkArea?.tap() // set the last tapped location off
      return
    }
    
    if let tapped = tappedWorkArea, find.id == tapped.id {
      tappedWorkArea?.tap()
      tappedWorkArea = nil
    } else {
      tappedWorkArea?.tap() // set the last tapped location off
      tappedWorkArea = find
      tappedWorkArea?.tap()
    }
    updateTitle(self)
  }


}

extension ViewController: CLLocationManagerDelegate {
  
  // Handle incoming location events.
  func locationManager(
    _ manager: CLLocationManager,
    didUpdateLocations locations: [CLLocation]
  ) {
    currentLocation = locations.last ?? currentLocation // use old if not update
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
      let d = self.lastLocationPollTime == nil
        ? 0.0
        : Date().timeIntervalSince(self.lastLocationPollTime!)
      self.lastLocationPollTime = Date()
      self.currentWorkArea = self.workZones.visit(
        location: coord,
        forDuration: d) ?? self.currentWorkArea
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

