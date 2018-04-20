//
//  InfoRepresentation.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/19.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Eureka

extension Worker {
  func information(for form: Form, onChange: @escaping () -> ()) {
    tempory = Worker(json: json()[id] ?? [:], id: id)
    
    let orchards = Entities.shared.items(for: .orchard)!
    
    let orchardSection = SelectableSection<ListCheckRow<Orchard>>(
      "Assigned Orchard",
      selectionType: .singleSelection(enableDeselection: true))
    
    for (_, orchardEntity) in orchards {
      let orchard = orchardEntity.orchard!
      orchardSection <<< ListCheckRow<Orchard>(orchard.name) { row in
        row.title = orchard.name
        row.selectableValue = orchard
        row.value = orchard.id == assignedOrchard ? orchard : nil
      }.onChange { (row) in
        self.tempory?.assignedOrchard = row.value?.id ?? ""
        onChange()
      }
    }
    
    let firstnameRow = TextRow() { row in
      row.title = "Worker Name"
      row.value = firstname
    }.onChange { (row) in
      self.tempory?.firstname = row.value ?? ""
      onChange()
    }
    let lastnameRow = TextRow() { row in
      row.title = "Worker Surname"
      row.value = lastname
    }.onChange { (row) in
      self.tempory?.lastname = row.value ?? ""
      onChange()
    }
    
    let isForemanRow = SwitchRow("isForemanTag") { row in
      row.title = "Is a foreman?"
      row.value = kind == .foreman
    }.onChange { (row) in
      self.tempory?.kind = row.value ?? true ? .foreman : .worker
      onChange()
    }
    let emailRow = EmailRow() { row in
      row.hidden = Condition.function(["isForemanTag"], { form in
        return !((form.rowBy(tag: "isForemanTag") as? SwitchRow)?.value ?? false)
      })
      row.title = "Email"
      row.value = email
    }.onChange { row in
      self.tempory?.email = row.value ?? ""
      onChange()
    }
    
    let infoRow = TextAreaRow() { row in
      row.value = details
    }.onChange { row in
      self.tempory?.details = row.value ?? ""
      onChange()
    }
    
    form
      +++ Section()
      <<< firstnameRow
      <<< lastnameRow
      
      +++ Section("Role")
      <<< isForemanRow
      <<< emailRow
    
      +++ Section("Information")
      <<< infoRow
    
      +++ orchardSection
  }
}

extension Farm {
  func information(for form: Form, onChange: @escaping () -> ()) {
    tempory = Farm(json: json()[id] ?? [:], id: id)
    
    let nameRow = TextRow() { row in
      row.title = "Farm Name"
      row.value = name
    }.onChange { row in
      self.tempory?.name = row.value ?? ""
      onChange()
    }
    let detailsRow = TextAreaRow() { row in
      row.title = "Details"
      row.value = details
    }.onChange { row in
      self.tempory?.details = row.value ?? ""
      onChange()
    }
    
    form +++ Section("Farm")
      <<< nameRow
      
      +++ Section("Details")
      <<< detailsRow
  }
}

extension Orchard {
  func information(for form: Form, onChange: @escaping () -> ()) {
    tempory = Orchard(json: json()[id] ?? [:], id: id)
    
    let farmSelection = SelectableSection<ListCheckRow<Farm>>(
      "Assigned Farm",
      selectionType: .singleSelection(enableDeselection: true))
    
    let farms = Entities.shared.items(for: .farm)!
    
    for (_, farmEntity) in farms {
      let farm = farmEntity.farm!
      farmSelection <<< ListCheckRow<Farm>(farm.name) { row in
        row.title = farm.name
        row.selectableValue = farm
        row.value = farm.id == assignedFarm ? farm : nil
      }.onChange { (row) in
        self.tempory?.assignedFarm = row.value?.id ?? ""
        onChange()
      }
    }
    
    let nameRow = TextRow() { row in
      row.title = "Orchard Name"
      row.value = name
    }.onChange { row in
      self.tempory?.name = row.value ?? ""
      onChange()
    }
    
    let cropRow = TextRow() { row in
      row.title = "Orchard Crop"
      row.value = crop
    }.onChange { row in
      self.tempory?.crop = row.value ?? ""
      onChange()
    }
    
    let bagMassRow = DecimalRow() { row in
      row.title = "Mean Bag Mass"
      row.value = bagMass
    }.onChange { row in
      self.tempory?.bagMass = row.value ?? 0.0
      onChange()
    }
    
    let dateRow = DateRow() { row in
      row.title = "Date Planted"
      row.value = date
    }.onChange { row in
      self.tempory?.date = row.value ?? Date()
      onChange()
    }
    
    let widthRow = DecimalRow() { row in
      row.title = "width"
      row.value = xDim
    }.onChange { row in
      self.tempory?.xDim = row.value ?? 0.0
      onChange()
    }
    
    let heightRow = DecimalRow() { row in
      row.title = "height"
      row.value = yDim
    }.onChange { row in
      self.tempory?.yDim = row.value ?? 0.0
      onChange()
    }
    
    let unitRow = TextRow() { row in
      row.title = "Distance measurement unit (eg. m)"
      row.value = distanceUnit == "" ? "m" : distanceUnit
    }.onChange { row in
      self.tempory?.distanceUnit = row.value ?? ""
      onChange()
    }
    
    let detailsRow = TextAreaRow { row in
      row.value = details
    }.onChange { row in
      self.tempory?.details = row.value ?? ""
      onChange()
    }
    
    form
      +++ Section("Orchard")
      <<< nameRow
      <<< cropRow
    
      +++ Section("Orchard Location")
    
      +++ Section("Collection Details")
      <<< bagMassRow
      
      +++ Section("Plantation Details")
      <<< dateRow
      
      +++ Section("Crop Dimensions")
      <<< widthRow
      <<< heightRow
      <<< unitRow
    
      +++ Section("Information")
      <<< detailsRow
    
      +++ farmSelection
  }
}

extension EntityItem {
  func information(for form: Form, onChange: @escaping () -> ()) {
    switch self {
    case let .worker(w): w.information(for: form, onChange: onChange)
    case let .orchard(o): o.information(for: form, onChange: onChange)
    case let .farm(f): f.information(for: form, onChange: onChange)
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
