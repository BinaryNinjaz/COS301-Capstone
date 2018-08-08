//
//  Verification.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/06/16.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import Foundation

extension String {
  func isEmail() -> Bool {
    let emailRegex = try! NSRegularExpression(
      pattern: "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}")
    
    let urange = NSRange(location: 0, length: count)
    let match = emailRegex.rangeOfFirstMatch(in: self, range: urange)
    
    return match == urange
  }
  
  func isPhoneNumber() -> Bool {
    let phoneRegex = try! NSRegularExpression(
      pattern: "(?:\\+?(\\d{1,3}))?[-.\\s(]*(\\d{3})[-.\\s)]*(\\d{3})[-.\\s]*(\\d{4})(?:\\s*x(\\d+))?")
    
    let urange = NSRange(location: 0, length: count)
    let match = phoneRegex.rangeOfFirstMatch(in: self, range: urange)
    
    return match == urange
  }
}
