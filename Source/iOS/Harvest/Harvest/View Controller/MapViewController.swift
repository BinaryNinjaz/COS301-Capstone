//
//  MapViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/11.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit
import Firebase
import GoogleMaps


class MapViewController: UIViewController, GMSMapViewDelegate {
  var locationManager = CLLocationManager()
  var currentLocation: CLLocation?
  var zoomLevel: Float = 15.0
  var autoMoveCameraToCurrentLocation = false
  var collections = [(CLLocation, String)]()
  
  @IBOutlet weak var mapView: GMSMapView!
  
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
    
    HarvestDB.onLastSession { session in
      self.mapView.clear()
      guard let sessionInfo = session.first?.value as? [String: Any] else {
        return
      }
      guard let collections = sessionInfo["collections"] as? [String: Any] else {
        return
      }
      for (name, value) in collections {
        guard let points = value as? [Any] else {
          return
        }
        for cp in points {
          guard let cp = cp as? [String: Any] else {
            return
          }
          guard let coord = cp["coord"] as? [String: Any] else {
            return
          }
//          guard let dateSince = cp["date"] as? Double else {
//            return
//          }
          guard let lat = coord["lat"] as? Double else {
            return
          }
          guard let lng = coord["lng"] as? Double else {
            return
          }
//          let date = Date(timeIntervalSince1970: dateSince)
          
          let marker = GMSMarker(position:
            CLLocationCoordinate2D(latitude: lat, longitude: lng))
          
          marker.title = name
          
          marker.map = self.mapView
          print(1)
        }
      }
      guard let track = sessionInfo["track"] as? [Any] else {
        return
      }
      
      let path = GMSMutablePath()
      
      for coord in track {
        guard let coord = coord as? [String: Any] else {
          continue
        }
        guard let lat = coord["lat"] as? Double else {
          continue
        }
        guard let lng = coord["lng"] as? Double else {
          continue
        }
        let loc = CLLocationCoordinate2D(latitude: lat, longitude: lng)
        path.add(loc)
      }
      let polyline = GMSPolyline(path: path)
      polyline.strokeColor = .blue
      polyline.strokeWidth = 2
      
      polyline.map = self.mapView
    }
    
  }
  
  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
  }
}


extension MapViewController : CLLocationManagerDelegate {
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
