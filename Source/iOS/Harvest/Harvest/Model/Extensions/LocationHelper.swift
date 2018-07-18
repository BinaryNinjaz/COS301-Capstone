//
//  LocationHelper.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/06/25.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import CoreLocation

extension Orchard {
  func contains(_ point: CLLocationCoordinate2D) -> Bool {
    let poly = Poly(coords.map { Point($0.longitude, $0.latitude) })
    let point = Point(point.longitude, point.latitude)
    return poly.contains(point)
  }
}

extension CLLocationCoordinate2D {
  func toRadians() -> (lat: Double, lng: Double) {
    return (latitude.toRadians(), longitude.toRadians())
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
