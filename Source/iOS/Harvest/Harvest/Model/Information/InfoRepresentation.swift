//
//  InfoRepresentation.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/19.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Eureka

protocol InformationRepresentable {
  func information(for form: Form)
}

extension Worker : InformationRepresentable {
  func information(using orchards: [Orchard], for form: Form) {
    let orchardSection = SelectableSection<ListCheckRow<Orchard>>(
      "Assigned Orchard",
      selectionType: .singleSelection(enableDeselection: true))
    
    for orchard in orchards {
      orchardSection <<< ListCheckRow<Orchard>(orchard.name) { row in
        row.title = orchard.name
        row.selectableValue = orchard
        row.value = orchard.id == assignedOrchard ? orchard : nil
      }
    }
    
    
    form
      +++ Section()
      <<< TextRow() { row in
        row.title = "Worker Name"
        row.value = firstname
      }
      <<< TextRow() { row in
        row.title = "Worker Surname"
        row.value = lastname
      }
      
      +++ Section("Role")
      <<< SwitchRow("isForemanTag") { row in
        row.title = "Is a foreman?"
        row.value = kind == .foreman
      }
      <<< EmailRow() { row in
        row.hidden = Condition.function(["isForemanTag"], { form in
          return !((form.rowBy(tag: "isForemanTag") as? SwitchRow)?.value ?? false)
        })
        row.title = "Email"
        row.value = email
      }
    
      +++ Section("Information")
      <<< TextAreaRow() { row in
        row.value = details
      }
    
      +++ orchardSection
  }
  
  func information(for form: Form) {
    information(using: [], for: form)
  }
}

extension Farm : InformationRepresentable {
  func information(for form: Form) {
    form +++ Section("Farm")
      <<< TextRow() { row in
        row.title = "Farm Name"
        row.value = name
    }
      <<< TextRow() { row in
        row.title = "Details"
        row.value = details
    }
  }
}

extension Orchard : InformationRepresentable {
  func information(using farms: [Farm], for form: Form) {
    let farmSelection = SelectableSection<ListCheckRow<Farm>>(
      "Assigned Farm",
      selectionType: .singleSelection(enableDeselection: true))
    
    for farm in farms {
      farmSelection <<< ListCheckRow<Farm>(farm.name) { row in
        row.title = farm.name
        row.selectableValue = farm
        row.value = farm.id == assignedFarm ? farm : nil
      }
    }
    
    form
      +++ Section("Orchard")
      <<< TextRow() { row in
        row.title = "Orchard Name"
        row.value = name
      }
      <<< TextRow() { row in
        row.title = "Orchard Crop"
        row.value = crop
      }
    
      +++ Section("Orchard Location")
    
      +++ Section("Collection Details")
      <<< DecimalRow() { row in
        row.title = "Mean Bag Mass"
        row.value = bagMass
      }
      
      +++ Section("Plantation Details")
      <<< DateRow() { row in
        row.title = "Date Planted"
        row.value = date
      }
      
      +++ Section("Crop Dimensions")
      <<< DecimalRow() { row in
        row.title = "width"
        row.value = xDim
      }
      <<< DecimalRow() { row in
        row.title = "height"
        row.value = yDim
      }
      <<< TextRow() { row in
        row.title = "Distance measurement unit (eg. m)"
        row.value = distanceUnit == "" ? "m" : distanceUnit
      }
    
      +++ Section("Information")
      <<< TextAreaRow { row in
        row.value = details
      }
    
      +++ farmSelection
  }
  
  func information(for form: Form) {
    information(using: [], for: form)
  }
}

extension EntityItem : InformationRepresentable {
  func information(for form: Form) {
    switch self {
    case let .worker(w): w.information(for: form)
    case let .orchard(o): o.information(for: form)
    case let .farm(f): f.information(for: form)
    case .userInfo: break
    }
  }
  
  func information(using other: [EntityItem], for form: Form) {
    switch self {
    case let .worker(w): w.information(using: other.orchards(), for: form)
    case let .orchard(o): o.information(using: other.farms(), for: form)
    case let .farm(f): f.information(for: form)
    case .userInfo: break
    }
  }
}

extension Array where Element == EntityItem {
  func orchards() -> [Orchard] {
    return compactMap {
      if case let .orchard(o) = $0 {
        return o
      } else {
        return nil
      }
    }
  }
  
  func workers() -> [Worker] {
    return compactMap {
      if case let .worker(w) = $0 {
        return w
      } else {
        return nil
      }
    }
  }
  
  func farms() -> [Farm] {
    return compactMap {
      if case let .farm(f) = $0 {
        return f
      } else {
        return nil
      }
    }
  }
}
