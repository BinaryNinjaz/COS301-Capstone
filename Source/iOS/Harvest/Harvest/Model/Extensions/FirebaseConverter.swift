//
//  FirebaseConverter.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/22.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import CoreLocation

extension Array where Element == CLLocationCoordinate2D {
  func firbaseCoordRepresentation() -> [Any] {
    var result = [Any]()
    var id = 0
    for loc in self {
      let coord = ["lat": loc.latitude, "lng": loc.longitude]
      result.append(coord)
      id += 1
    }
    return result
  }
}

extension Dictionary where Key == Worker, Value == [CollectionPoint] {
  func firebaseSessionRepresentation() -> [String: Any] {
    var result = [String: Any]()
    
    for (key, collectionPoints) in self {
      var collections: [Any] = []
      
      for collection in collectionPoints {
        collections.append([
          "coord": [
            "lat": collection.location.latitude,
            "lng": collection.location.longitude
          ],
          "date": DateFormatter.rfc2822String(from: collection.date)
        ])
      }
      result[key.id] = collections
    }
    
    return result
  }
}

extension Array where Element == (uid: String, name: String) {
  func firebaseWorkingForRepresentation() -> [String: Any] {
    var result = [String: Any]()
    
    for (i, n) in self {
      result[i] = n
    }
    
    return result
  }
}
