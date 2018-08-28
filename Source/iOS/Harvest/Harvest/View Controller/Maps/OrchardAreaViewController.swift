//
//  OrchardAreaViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/05/04.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import UIKit
import Firebase
import GoogleMaps
import Eureka
import SCLAlertView

public class OrchardAreaViewController:
UIViewController, GMSMapViewDelegate, TypedRowControllerType, CLLocationManagerDelegate {
  public var row: RowOf<Orchard>!
  
  public typealias RowValue = Orchard
  
  public var onDismissCallback: ((UIViewController) -> Void)?
  
  var zoomLevel: Float = 15.0
  
  var orchardPolygon: GMSPolygon?
  var coordinates = [CLLocationCoordinate2D]()
  
  @IBOutlet weak var mapView: GMSMapView!
  @IBOutlet weak var removeAllButton: UIButton!
  @IBOutlet weak var removeLastButton: UIButton!
  @IBOutlet weak var instructionLabel: UILabel!
  
  var actuallyChanged: ((RowOf<Orchard>) -> Void)?
  
  var markers: [GMSMarker]?
  
  var shouldShowMarkers = false
  
  func updatePolygon() {
    orchardPolygon?.map = nil
    var data = row.value?.json()[row.value?.id ?? ""] ?? [:]
    data["coords"] = coordinates.firbaseCoordRepresentation()
    row.value = Orchard(json: data, id: row.value?.id ?? "")
    orchardPolygon = row.value?.coords.gmsPolygon(mapView: mapView, color: row.value?.color ?? UIColor.red)
    
    markers?.forEach { $0.map = nil }
    if shouldShowMarkers {
      markers = row.value?.coords.gmsPolygonMarkers(mapView: mapView)
    }
    
    actuallyChanged?(row)
  }
  
  @IBAction func removeAllCoords(_ sender: Any) {
    if !coordinates.isEmpty {
      coordinates.removeAll()
      updatePolygon()
    } else {
      SCLAlertView().showInfo("No More Points", subTitle: "There are no more points in the orchard to delete")
    }
  }
  
  @IBAction func removeLastCoord(_ sender: Any) {
    if !coordinates.isEmpty {
      coordinates.removeLast()
      updatePolygon()
    } else {
      SCLAlertView().showInfo("No More Points", subTitle: "There are no more points in the orchard to delete")
    }
  }
  
  public override func viewDidLoad() {
    super.viewDidLoad()
    
    navigationItem.rightBarButtonItem = UIBarButtonItem(
      title: "Hybrid",
      style: .plain,
      target: self,
      action: #selector(toggleMapType))
    
    mapView.isHidden = false
    mapView.mapType = GMSMapViewType.hybrid
    mapView.isMyLocationEnabled = true
    mapView.settings.myLocationButton = true
    mapView.settings.rotateGestures = true
    mapView.settings.compassButton = true
    mapView.delegate = self
    mapView.settings.myLocationButton = true
    mapView.padding = UIEdgeInsets(top: 42, left: 0, bottom: 101, right: 0)
    
    guard let orchard = row.value else {
      return
    }
    
    coordinates = orchard.coords
    orchardPolygon = coordinates.gmsPolygon(mapView: mapView, color: orchard.color)
  }
  
  public override func viewDidAppear(_ animated: Bool) {
    updatePolygon()
    if let path = orchardPolygon?.path, !coordinates.isEmpty {
      let bounds = GMSCoordinateBounds(path: path)
      mapView.animate(with: GMSCameraUpdate.fit(bounds, withPadding: 15.0))
    } else {
      let locationManager = CLLocationManager()
      locationManager.delegate = self
      locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
      locationManager.requestLocation()
      if let loc = mapView.myLocation {
        mapView.animate(toLocation: loc.coordinate)
      }
    }
  }
  
  public override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
  }
  
  @objc func toggleMapType() {
    guard let mapView = mapView else {
      return
    }
    
    SCLAlertView.toggleMapType(for: mapView, from: navigationItem.rightBarButtonItem)
  }
  
  public func mapView(_ mapView: GMSMapView, didTapAt coordinate: CLLocationCoordinate2D) {
    if #available(iOS 10.0, *) {
      let gen = UISelectionFeedbackGenerator()
      gen.prepare()
      gen.selectionChanged()
    } else {
      // Fallback on earlier versions
    }
    shouldShowMarkers = true
    coordinates.append(coordinate)
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
    mapView.animate(to: mapView.camera)
    manager.stopUpdatingLocation()
  }
  
  public func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
    print(error)
  }
  
  public override func viewDidLayoutSubviews() {
    super.viewDidLayoutSubviews()
    
    instructionLabel.setOriginX(8)
    instructionLabel.setWidth(view.frame.width - 16)
    
    removeAllButton.setOriginX(8)
    removeAllButton.setWidth((instructionLabel.frame.width - 16) / 2)
    
    removeLastButton.setWidth(removeAllButton.frame.width)
    
    removeLastButton.setOriginX(removeAllButton.frame.width + removeAllButton.frame.origin.x + 8)
    
    removeAllButton.apply(gradient: .deleteAllButton)
    removeLastButton.apply(gradient: .deleteButton)
  }
}
