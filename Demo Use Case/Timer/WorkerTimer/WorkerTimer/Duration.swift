//
//  Duration.swift
//  WorkerTimer
//
//  Created by Letanyan Arumugam on 2018/03/09.
//

import Foundation

func formatTimeInterval(_ duration: TimeInterval) -> String {
  let formatter = NumberFormatter()
  
  if duration < 60 {
    let ssec = formatter.string(from: duration as NSNumber)!
    
    return ssec + " secounds"
  } else if duration < 3600 {
    let min = duration / 60
    let smin = formatter.string(from: min as NSNumber)!
    let ssmin = smin == "1" ? "1 minute" : smin + " minutes"
    
    return ssmin
  } else {
    let hr = duration / 3600
    let min = ((duration / 3600) - trunc(duration / 3600)) * 60
    
    let shr = formatter.string(from: hr as NSNumber)!
    let smin = formatter.string(from: min as NSNumber)!
    
    let sshr = shr == "1" ? "1 hour" : shr + "hours"
    let ssmin = smin == "1" ? "1 minute" : smin + " minutes"
    
    return sshr + " and " + ssmin
  }
}
