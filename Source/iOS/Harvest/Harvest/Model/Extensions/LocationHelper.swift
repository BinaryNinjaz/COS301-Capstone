//
//  LocationHelper.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/06/25.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import CoreLocation

extension Orchard {
  func contains(_ point: CLLocationCoordinate2D) -> Bool {
    let poly = Poly(coords.map { Point($0.longitude, $0.latitude) })
    let point = Point(point.longitude, point.latitude)
    return poly.contains(point)
  }
  
  func modifiedArea(withRespectTo points: [CLLocationCoordinate2D]) -> Bool {
    guard inferArea else {
      return false
    }
    let ps = coords + points
    let temp = coords
    coords = ps.convexHull()
    return temp != coords
  }
}

extension Tracker {
  func modifyOrchardAreas() {
    var modifiedOrchards = [String: [CLLocationCoordinate2D]]()
    
    for (_, pickups) in collections {
      for pickup in pickups {
        guard let selectedOrchard = pickup.selectedOrchard else {
          continue
        }
        if modifiedOrchards[selectedOrchard] == nil {
          modifiedOrchards[selectedOrchard] = [pickup.location]
        } else {
          modifiedOrchards[selectedOrchard]?.append(pickup.location)
        }
      }
    }
    
    for (orchardId, pickups) in modifiedOrchards {
      guard let orchard = Entities.shared.orchards.first(where: { $0.value.id == orchardId }) else {
        continue
      }
      
      if orchard.value.modifiedArea(withRespectTo: pickups) {
        HarvestDB.save(orchard: orchard.value)
      }
    }
  }
}

extension CLLocationCoordinate2D {
  func toRadians() -> (lat: Double, lng: Double) {
    return (latitude.toRadians(), longitude.toRadians())
  }
  
  var x: Double {
    return latitude
  }
  
  var y: Double {
    return longitude
  }
}

extension Double {
  func toRadians() -> Double {
    return self * .pi / 180.0
  }
  
  func squared() -> Double {
    return self * self
  }
}

extension CLLocationCoordinate2D {
  func distance(to loc: CLLocationCoordinate2D) -> Double {
    let dLat = (loc.latitude - latitude).toRadians()
    let dLng = (loc.longitude - longitude).toRadians()
    let a = sin(dLat / 2).squared()
      + sin(dLng / 2).squared() * cos(latitude.toRadians()) * cos(loc.latitude.toRadians())
    let c = 2 * atan2(sqrt(a), sqrt(1 - a))
    let d = c * 6371e3
    return d
  }
}

extension Array where Element == CLLocationCoordinate2D {
  func euclideanDistance() -> Double {
    return zip(dropFirst(), dropLast())
      .lazy
      .map { $0.distance(to: $1) }
      .reduce(0, +)
  }
}

extension Array where Element == CLLocationCoordinate2D {
  func convexHull() -> [CLLocationCoordinate2D] {
    guard count >= 3 else {
      return []
    }
    var result = [CLLocationCoordinate2D]()
    var points = self
    points.swapAt(0, points.index(of: points.min { $0.y < $1.y || ($0.y == $1.y && $0.x < $1.x) }!)!)
    let pivot = points[0]
    points.sort { a, b in
      let order = ccw(pivot, a, b)
      if order == .colinear {
        return Harvest.euclideanDistance(pivot, a) < Harvest.euclideanDistance(pivot, b)
      }
      return order == .counterclockwise
    }
    
    result.append(contentsOf: points[0..<3])
    
    for i in 3..<count {
      while let last = result.last,
        let secondLast = result.dropLast().last,
        ccw(secondLast, last, points[i]) != .counterclockwise {
        result.removeLast()
      }
      result.append(points[i])
    }
    return result
  }
}

enum Orientation {
  case colinear
  case clockwise
  case counterclockwise
}

private func ccw(
  _ p: CLLocationCoordinate2D,
  _ q: CLLocationCoordinate2D,
  _ r: CLLocationCoordinate2D
) -> Orientation {
  let t = (r.x - q.x) * (q.y - p.y) - (r.y - q.y) * (q.x - p.x)
  switch t {
  case let x where x < 0: return .counterclockwise
  case let x where x > 0: return .clockwise
  default: return .colinear
  }
}

private func euclideanDistance(_ a: CLLocationCoordinate2D, _ b: CLLocationCoordinate2D) -> Double {
  let dx = a.x - b.x
  let dy = a.y - b.y
  return dx * dx + dy * dy
}

extension Dictionary where Value == [CollectionPoint] {
  func convexHull() -> [CLLocationCoordinate2D] {
    var result = [CLLocationCoordinate2D]()
    for (_, ps) in self {
      result.append(contentsOf: ps.map { $0.location })
    }
    return result.convexHull()
  }
}
