//
//  PhoneyFormatter.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/19.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import PhoneNumberKit

enum Phoney {
  static func formatted(number: String?) -> String? {
    guard let number = number else {
      return nil
    }
    
    let kit = PhoneNumberKit()
    let code = Locale.current.regionCode ?? "ZA"
    guard let phoneNumber = try? kit.parse(number, withRegion: code, ignoreType: true) else {
      return nil
    }
    
    return kit.format(phoneNumber, toType: PhoneNumberFormat.international)
  }
}
