//
//  SessionViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/11.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit
import Firebase
import GoogleMaps
import Eureka

public class SessionViewController: UIViewController, GMSMapViewDelegate, TypedRowControllerType {
  public var row: RowOf<Session>!
  
  public typealias RowValue = Session
  
  public var onDismissCallback: ((UIViewController) -> Void)?
  
  var zoomLevel: Float = 15.0
  var collections = [(CLLocation, String)]()
  var orchards = [Orchard]()
  
  var orchardPolygons: [GMSPolygon] = []
  var trackLine: GMSPolyline?
  var pickUpMarkers: [GMSMarker] = []
  
  @IBOutlet weak var mapView: GMSMapView!
  
  public override func viewDidLoad() {
    super.viewDidLoad()
    
    mapView.isHidden = false
    mapView.mapType = GMSMapViewType.normal
    mapView.settings.compassButton = true
    mapView.delegate = self
    mapView.settings.myLocationButton = true
    mapView.padding = UIEdgeInsets(top: 42, left: 0, bottom: 101, right: 0)
    
    guard let session = row.value else {
      return
    }
    
    trackLine = session.track.gmsPolyline(mapView: mapView)
    pickUpMarkers = session.collections.gmsMarkers(mapView: mapView)
    
    orchardPolygons.removeAll(keepingCapacity: true)
    for (_, orchard) in Entities.shared.orchards {
      let poly = orchard.coords.gmsPolygon(mapView: mapView)
      orchardPolygons.append(poly)
    }
    
    if let coord = session.track.first {
      mapView.camera = GMSCameraPosition(target: coord,
                                         zoom: zoomLevel,
                                         bearing: .pi / 2,
                                         viewingAngle: .pi)
    }
    
  }
  
  public override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
  }
}
