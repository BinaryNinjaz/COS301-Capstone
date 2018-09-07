//
//  DataGenerator.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/09/06.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import CoreLocation

struct SessionGenerator {
  var foreman: Worker
  var orchard: Orchard
  var duration: (Date, Date)
  var curve: ((Double) -> Double)?
  
  init(_ foreman: Worker, _ orchard: Orchard, _ duration: (Date, Date)) {
    (self.foreman, self.orchard, self.duration) = (foreman, orchard, duration)
  }
  
  func generateSession() -> Session {
    var data = [Worker: [CollectionPoint]]()
    var track = [CLLocationCoordinate2D]()
    
    let workers = Entities
      .shared
      .workers
      .lazy
      .filter { $0.value.assignedOrchards.contains(self.orchard.id) }
      .map { $0.value }
    
    let start = Double(duration.0.stepsSince1970(step: nil))
    let end = Double(duration.1.stepsSince1970(step: nil))
    
    var x = start
    
    while x < end {
      if let curve = curve, curve(x) > 0.5 {
        continue
      } else if curve == nil && Double.random() > 0.5 {
        continue
      }
      
      let rp = orchard.randomPoint()
      var tracked = false
      for w in workers {
        if Double(abs(w.hashValue)) / Double(Int.max) * Double.random() > 0.5 {
          let point = CollectionPoint(
            location: orchard.randomPoint(),
            date: .random(between: duration.0, and: duration.1),
            selectedOrchard: orchard.id)
          data[w] = (data[w] ?? []) + [point]
          if !tracked {
            track.append(rp)
            tracked = true
          }
        }
      }
      
      x += Double((5..<10).random())
    }
    
    let result: [String: Any] = [
      "start_date": DateFormatter.rfc2822String(from: duration.0),
      "end_date": DateFormatter.rfc2822String(from: duration.1),
      "wid": foreman.id,
      "track": track.firbaseCoordRepresentation(),
      "collections": data.firebaseSessionRepresentation()
    ]
    
    return Session(json: result, id: "-" + Int(Date().timeIntervalSince1970 * 10000).description)
  }
}

struct SessionsGenerator {
  var period: (Date, Date)
  var foremen: [Worker]
  
  init(period: (Date, Date), foremen: [Worker]) {
    (self.period, self.foremen) = (period, foremen)
  }
  
  func generateSessions() -> [Session] {
    var start = period.0.startOfDay()
    let end = period.1.endOfDay()
    var result = [Session]()
    
    while start <= end {
      for foreman in foremen {
        guard let o = foreman.assignedOrchards.randomElement() else {
          continue
        }
        guard let orchard = Entities.shared.orchards.first(where: { $0.value.id == o })?.value else {
          continue
        }
        let startTime = start.random(hour: .range(6, 9), minute: .range(0, 60), second: .range(0, 60))
        let endTime = start.random(hour: .range(16, 20), minute: .range(0, 60), second: .range(0, 60))
        
        let creator = SessionGenerator.init(foreman, orchard, (startTime, endTime))
        result.append(creator.generateSession())
      }
      start = start.date(byAdding: .day, value: 1)
    }
    
    return result
  }
}

struct StoredGeneratedSessions {
  static var shared = SortedDictionary<Date, SortedSet<Session>>(>)
}
