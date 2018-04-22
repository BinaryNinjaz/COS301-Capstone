//
//  FirebaseConverter.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/22.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import CoreLocation

extension Array where Element == CLLocationCoordinate2D {
  func firbaseCoordRepresentation() -> [String: Any] {
    var result = [String: Any]()
    var id = 0
    for loc in self {
      let coord = ["lat": loc.latitude, "lng": loc.longitude]
      result[id.description] = coord
      id += 1
    }
    return result
  }
}

extension Dictionary where Key == Worker, Value == [CollectionPoint] {
  func firebaseSessionRepresentation() -> [String: Any] {
    var result = [String: Any]()
    
    for (key, collectionPoints) in self {
      var collections: [String: Any] = [:]
      var i = 0
      
      for collection in collectionPoints {
        collections[i.description] = [
          "coord": [
            "lat": collection.location.latitude,
            "lng": collection.location.longitude
          ],
          "date": collection.date.timeIntervalSince1970
        ]
        i += 1
      }
      print(key.id)
      result[key.id] = collections
    }
    
    return result
  }
}
