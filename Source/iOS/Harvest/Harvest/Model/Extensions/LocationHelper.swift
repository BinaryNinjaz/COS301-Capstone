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
