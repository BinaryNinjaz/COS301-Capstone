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
  var id: Int
  var title: String
  let area: GMSPolygon
  let rect: (start: CLLocationCoordinate2D, end: CLLocationCoordinate2D)
  var workingTime: TimeInterval
  var isSelected = false
  var isIn = false {
    didSet {
      area.fillColor = isIn
        ? UIColor.color(color, alpha: 0.5)
        : UIColor.color(color, alpha: 0.1)
    }
  }
  var color: UInt32
  
  init(id: Int,
       title: String,
       start: CLLocationCoordinate2D,
       end: CLLocationCoordinate2D) {
    
    workingTime = 0.0
    self.id = id
    self.title = title
    color = UIColor.random()
    
    rect = (start, end)
    
    // Create Polygon
    let tl = start
    let bl = CLLocationCoordinate2D(latitude: start.latitude,
                                    longitude: end.longitude)
    let br = end
    let tr = CLLocationCoordinate2D(latitude: end.latitude,
                                    longitude: start.longitude)
    
    let path = GMSMutablePath()
    path.add(tl)
    path.add(bl)
    path.add(br)
    path.add(tr)
    
    area = GMSPolygon(path: path)
    area.isTappable = true
    area.userData = ["id": id, "title": title, "workingTime": workingTime]
    area.fillColor = UIColor.color(color, alpha: 0.1)
    area.strokeColor = UIColor.color(color, alpha: 0.9)
    area.strokeWidth = 2.5
    area.title = title
    // -------------
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
      area.fillColor = UIColor.color(color, alpha: 0.1)
    }
  }
}

extension CLLocationCoordinate2D {
  var cgPoint: CGPoint {
    return CGPoint(x: latitude, y: longitude)
  }
}

extension Array where Element == WorkArea {
  /// updates location work time and returns the updated workArea
  mutating func visit(
    location: CLLocationCoordinate2D,
    forDuration d: TimeInterval
  ) -> [WorkArea] {
    var result = [WorkArea]()
    for (idx, el) in zip(indices, self) {
      guard el.contains(location) else {
        continue
      }
      self[idx].workingTime += d
      result.append(self[idx])
    }
    return result
  }
  
  func contains(workArea: WorkArea) -> WorkArea? {
    for e in self {
      if e.id == workArea.id {
        return e
      }
    }
    return nil
  }
}

extension UserDefaults {
  func set(_ wa: WorkArea) {
    set(wa.title, forKey: wa.id.description + "Title")
    
    set(wa.rect.start.latitude, forKey: wa.id.description + "StartLat")
    set(wa.rect.start.longitude, forKey: wa.id.description + "StartLong")
    
    set(wa.rect.end.latitude, forKey: wa.id.description + "EndLat")
    set(wa.rect.end.longitude, forKey: wa.id.description + "EndLong")
  }
  
  func setWorkAreaCount(_ count: Int) {
    set(count, forKey: "WorkAreaCount")
  }
  
  func workAreaCount() -> Int {
    return integer(forKey: "WorkAreaCount")
  }
  
  func workArea(forKey key: Int) -> WorkArea? {
    guard let t = string(forKey: key.description + "Title") else {
      return nil
    }
    
    let startLat = double(forKey: key.description + "StartLat")
    let startLong = double(forKey: key.description + "StartLong")
    
    let endLat = double(forKey: key.description + "EndLat")
    let endLong = double(forKey: key.description + "EndLong")
    
    return WorkArea(id: key,
                    title: t,
                    start: CLLocationCoordinate2D(latitude: startLat, longitude: startLong),
                    end: CLLocationCoordinate2D(latitude: endLat, longitude: endLong))
    
  }
  
  func removeWorkArea(forKey key: Int) {
    removeObject(forKey: key.description + "Title")
    
    removeObject(forKey: key.description + "StartLat")
    removeObject(forKey: key.description + "StartLong")
    
    removeObject(forKey: key.description + "EndLat")
    removeObject(forKey: key.description + "EndLong")
  }
}
