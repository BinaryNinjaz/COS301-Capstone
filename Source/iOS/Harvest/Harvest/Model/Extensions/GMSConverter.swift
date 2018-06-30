//
//  GMSConverter.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/21.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import GoogleMaps

extension Array where Element == CLLocationCoordinate2D {
  func gmsPath() -> GMSPath {
    let path = GMSMutablePath()
    
    for point in self {
      path.add(point)
    }
    
    return path
  }
  
  func gmsPolyline(mapView: GMSMapView, color: UIColor = .blue, width: CGFloat = 1.0) -> GMSPolyline {
    let result = GMSPolyline(path: gmsPath())
    result.strokeColor = color
    result.strokeWidth = width
    result.map = mapView
    return result
  }
  
  func gmsPolygon(mapView: GMSMapView, color: UIColor = .blue, width: CGFloat = 3.0) -> GMSPolygon {
    let result = GMSPolygon(path: gmsPath())
    result.fillColor = color.withAlphaComponent(0.1)
    result.strokeColor = color.withAlphaComponent(0.5)
    result.strokeWidth = width
    result.map = mapView
    return result
  }
}

extension Dictionary where Key == Worker, Value == [CollectionPoint] {
  func gmsMarkers(mapView: GMSMapView) -> [GMSMarker] {
    var result: [GMSMarker] = []
    let formatter = DateFormatter()
    formatter.dateStyle = .none
    formatter.timeStyle = .short
    for (key, value) in self {
      for (i, point) in value.enumerated() {
        let marker = GMSMarker(position: point.location)
        marker.title = key.description
          + " - "
          + formatter.string(from: point.date)
          + " (\(i + 1) / \(value.count))"
        marker.map = mapView
        result.append(marker)
      }
    }
    return result
  }
}
