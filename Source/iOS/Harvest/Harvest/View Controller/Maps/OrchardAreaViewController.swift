//
//  OrchardAreaViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/05/04.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit
import Firebase
import GoogleMaps
import Eureka

public class OrchardAreaViewController:
UIViewController, GMSMapViewDelegate, TypedRowControllerType, CLLocationManagerDelegate {
  public var row: RowOf<Orchard>!
  
  public typealias RowValue = Orchard
  
  public var onDismissCallback: ((UIViewController) -> Void)?
  
  var zoomLevel: Float = 15.0
  
  var orchardPolygon: GMSPolygon? = nil
  var collections = [CLLocationCoordinate2D]()
  
  @IBOutlet weak var mapView: GMSMapView!
  @IBOutlet weak var removeAllButton: UIButton!
  @IBOutlet weak var removeLastButton: UIButton!
  
  var actuallyChanged: ((RowOf<Orchard>) -> ())? = nil
  
  func updatePolygon() {
    orchardPolygon?.map = nil
    var data = row.value?.json()[row.value?.id ?? ""] ?? [:]
    data["coords"] = collections.firbaseCoordRepresentation()
    row.value = Orchard(json: data, id: row.value?.id ?? "")
    orchardPolygon = row.value?.coords.gmsPolygon(mapView: mapView)
    actuallyChanged?(row)
  }
  
  @IBAction func removeAllCoords(_ sender: Any) {
    if !collections.isEmpty {
      collections.removeAll()
      updatePolygon()
    } else {
      let alert = UIAlertController.alertController(
        title: "No Points",
        message: "There are no more points in the orchard to delete")
      
      present(alert, animated: true, completion: nil)
    }
    
  }
  
  @IBAction func removeLastCoord(_ sender: Any) {
    if !collections.isEmpty {
      collections.removeLast()
      updatePolygon()
    } else {
      let alert = UIAlertController.alertController(
        title: "No Points",
        message: "There are no more points in the orchard to delete")
      
      present(alert, animated: true, completion: nil)
    }
    
  }
  
  public override func viewDidLoad() {
    super.viewDidLoad()
    
    removeAllButton.apply(gradient: .red)
    removeLastButton.apply(gradient: .orange)
    
    mapView.isHidden = false
    mapView.mapType = GMSMapViewType.normal
    mapView.settings.compassButton = true
    mapView.delegate = self
    mapView.settings.myLocationButton = true
    mapView.padding = UIEdgeInsets(top: 42, left: 0, bottom: 101, right: 0)
    
    guard let orchard = row.value else {
      return
    }
    
    collections = orchard.coords
    orchardPolygon = collections.gmsPolygon(mapView: mapView)
    
    if let path = orchardPolygon?.path {
      let bounds = GMSCoordinateBounds(path: path)
      mapView.animate(with: GMSCameraUpdate.fit(bounds, withPadding: 15.0))
    } else {
      let locationManager = CLLocationManager()
      locationManager.delegate = self
      locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
      locationManager.requestLocation()
    }
  }
  
  public override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
  }
  
  public func mapView(_ mapView: GMSMapView, didLongPressAt coordinate: CLLocationCoordinate2D) {
    if #available(iOS 10.0, *) {
      let gen = UISelectionFeedbackGenerator()
      gen.prepare()
      gen.selectionChanged()
    } else {
      // Fallback on earlier versions
    }
    
    collections.append(coordinate)
    updatePolygon()
  }
  
  public func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
    guard let f = locations.first else {
      return
    }
    mapView.camera = GMSCameraPosition(target: f.coordinate,
                                       zoom: zoomLevel,
                                       bearing: .pi / 2,
                                       viewingAngle: .pi)
    
    manager.stopUpdatingLocation()
  }
  
  public func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
    print(error)
  }
}
