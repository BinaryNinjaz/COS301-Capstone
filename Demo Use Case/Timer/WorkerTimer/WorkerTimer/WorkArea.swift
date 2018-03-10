//
//  WorkArea.swift
//  WorkerTimer
//
//  Created by Letanyan Arumugam on 2018/03/10.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import CoreLocation
import CoreGraphics
import GoogleMaps

struct WorkArea {
  var id: UInt32
  var title: String
  let area: GMSPolygon
  let rect: (start: CLLocationCoordinate2D, end: CLLocationCoordinate2D)
  var workingTime: TimeInterval
  var isSelected: Bool = false
  var color: UInt32
  
  init(id: UInt32,
       title: String,
       start: CLLocationCoordinate2D,
       end: CLLocationCoordinate2D) {
    workingTime = 0.0
    self.id = id
    self.title = title
    color = UIColor.random()
    
    rect = (start, end)
    
    let tl = start
    let bl = CLLocationCoordinate2D(latitude: start.latitude, longitude: end.longitude)
    let br = end
    let tr = CLLocationCoordinate2D(latitude: end.latitude, longitude: start.longitude)
    
    let path = GMSMutablePath()
    path.add(tl)
    path.add(bl)
    path.add(br)
    path.add(tr)
    
    area = GMSPolygon(path: path)
    area.isTappable = true
    area.userData = ["id": id, "title": title, "workingTime": workingTime]
    area.fillColor = UIColor.color(color, alpha: 0.5)
    area.title = title
  }
  
  func contains(_ location: CLLocationCoordinate2D) -> Bool {
    let lat = abs(rect.start.latitude) <= abs(location.latitude)
      && abs(location.latitude) <= abs(rect.end.latitude)
    let long = abs(rect.start.longitude) <= abs(location.longitude)
      && abs(location.longitude) <= abs(rect.end.longitude)
    return lat && long
  }
  
  mutating func tap() {
    isSelected = !isSelected
    if isSelected {
      area.fillColor = UIColor.color(color, alpha: 0.75)
    } else {
      area.fillColor = UIColor.color(color, alpha: 0.5)
    }
  }
}

extension CLLocationCoordinate2D {
  var cgPoint: CGPoint {
    return CGPoint(x: latitude, y: longitude)
  }
}

extension Array where Element == WorkArea {
  mutating func visit(location: CLLocationCoordinate2D, forDuration d: TimeInterval) -> WorkArea? {
    for (idx, el) in zip(indices, self) {
      guard el.contains(location) else {
        continue
      }
      self[idx].workingTime += d
      return self[idx]
    }
    return nil
  }
}
