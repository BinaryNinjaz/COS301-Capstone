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
      selectionType: .multipleSelection)
    
    for (_, orchardEntity) in orchards {
      let orchard = orchardEntity.orchard!
      orchardSection <<< ListCheckRow<Orchard>(orchard.name) { row in
        row.title = orchard.name
        row.selectableValue = orchard
        row.value = assignedOrchards.contains(orchard.id) ? orchard : nil
      }.onChange { (row) in
        if let sel = row.value {
          guard let idx = self.tempory?.assignedOrchards.index(of: orchard.id) else {
            self.tempory?.assignedOrchards.append(orchard.id)
            onChange()
            return
          }
          self.tempory?.assignedOrchards[idx] = sel.id
          onChange()
        } else {
          guard let idx = self.tempory?.assignedOrchards.index(of: orchard.id) else {
            return
          }
          self.tempory?.assignedOrchards.remove(at: idx)
          onChange()
        }
      }
    }
    
    let firstnameRow = NameRow() { row in
      row.title = "Worker Name"
      row.value = firstname
      row.placeholder = "Firstname"
    }.onChange { (row) in
      self.tempory?.firstname = row.value ?? ""
      onChange()
    }.cellUpdate { (cell, row) in
      cell.textField.textAlignment = .left
      cell.titleLabel?.textColor = .titleLabel
      cell.textField.clearButtonMode = .whileEditing
    }
    let lastnameRow = NameRow() { row in
      row.title = "Worker Surname"
      row.value = lastname
      row.placeholder = "Surname"
    }.onChange { (row) in
      self.tempory?.lastname = row.value ?? ""
      onChange()
    }.cellUpdate { (cell, row) in
      cell.textField.textAlignment = .left
      cell.titleLabel?.textColor = .titleLabel
      cell.textField.clearButtonMode = .whileEditing
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
      row.placeholder = "henry@gmail.com"
    }.onChange { row in
      self.tempory?.email = row.value ?? ""
      onChange()
    }.cellUpdate { (cell, row) in
      cell.textField.textAlignment = .left
      cell.titleLabel?.textColor = .titleLabel
      cell.textField.clearButtonMode = .whileEditing
    }
    
    let infoRow = TextAreaRow() { row in
      row.value = details
      row.placeholder = "Any extra information"
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
    
    let nameRow = NameRow() { row in
      row.title = "Farm Name"
      row.value = name
      row.placeholder = "Name of the farm"
    }.onChange { row in
      self.tempory?.name = row.value ?? ""
      onChange()
    }.cellUpdate { (cell, row) in
      cell.textField.textAlignment = .left
      cell.titleLabel?.textColor = .titleLabel
      cell.textField.clearButtonMode = .whileEditing
    }
    let detailsRow = TextAreaRow() { row in
      row.title = "Details"
      row.value = details
      row.placeholder = "Any extra information"
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
    
    let farms = Entities.shared.items(for: .farm)!
    
    let nameRow = NameRow() { row in
      row.title = "Orchard Name"
      row.value = name
      row.placeholder = "Name of the orchard"
    }.onChange { row in
      self.tempory?.name = row.value ?? ""
      onChange()
    }.cellUpdate { (cell, row) in
      cell.textField.textAlignment = .left
      cell.titleLabel?.textColor = .titleLabel
      cell.textField.clearButtonMode = .whileEditing
    }
    
    let cropRow = TextRow() { row in
      row.title = "Orchard Crop"
      row.value = crop
      row.placeholder = "Crop farmed on the orchard"
    }.onChange { row in
      self.tempory?.crop = row.value ?? ""
      onChange()
    }.cellUpdate { (cell, row) in
      cell.textField.textAlignment = .left
      cell.titleLabel?.textColor = .titleLabel
      cell.textField.clearButtonMode = .whileEditing
    }
    
    let bagMassRow = DecimalRow() { row in
      row.title = "Bag Mass"
      row.value = bagMass
      row.placeholder = "Average mass of a bag"
    }.onChange { row in
      self.tempory?.bagMass = row.value ?? 0.0
      onChange()
    }.cellUpdate { (cell, row) in
      cell.textField.textAlignment = .left
      cell.titleLabel?.textColor = .titleLabel
      cell.textField.clearButtonMode = .whileEditing
      if let dt = cell.textField.text {
        cell.textField.text = Double(dt) == 0.0 ? "" : dt
      }
    }.onCellSelection { (cell, row) in
      if let dt = cell.textField.text {
        cell.textField.text = Double(dt) == 0.0 ? "" : dt
      }
    }
    
    let dateRow = DateRow() { row in
      row.title = "Date Planted"
      row.value = date
    }.onChange { row in
      self.tempory?.date = row.value ?? Date()
      onChange()
    }
    
    let widthRow = DecimalRow() { row in
      row.title = "Width"
      row.value = xDim
      row.placeholder = "Horizontal Spacing"
    }.onChange { row in
      self.tempory?.xDim = row.value ?? 0.0
      onChange()
    }.cellUpdate { (cell, row) in
      cell.textField.textAlignment = .left
      cell.titleLabel?.textColor = .titleLabel
      cell.textField.clearButtonMode = .whileEditing
      if let dt = cell.textField.text {
        cell.textField.text = Double(dt) == 0.0 ? "" : dt
      }
    }.onCellSelection { (cell, row) in
      if let dt = cell.textField.text {
        cell.textField.text = Double(dt) == 0.0 ? "" : dt
      }
    }
    
    let heightRow = DecimalRow() { row in
      row.title = "Height"
      row.value = yDim
      row.placeholder = "Vertical Spacing"
    }.onChange { row in
      self.tempory?.yDim = row.value ?? 0.0
      onChange()
    }.cellUpdate { (cell, row) in
      cell.textField.textAlignment = .left
      cell.titleLabel?.textColor = .titleLabel
      cell.textField.clearButtonMode = .whileEditing
      if let dt = cell.textField.text {
        cell.textField.text = Double(dt) == 0.0 ? "" : dt
      }
    }.onCellSelection { (cell, row) in
      if let dt = cell.textField.text {
        cell.textField.text = Double(dt) == 0.0 ? "" : dt
      }
    }
    
    let unitRow = TextRow() { row in
      row.title = "Distance unit"
      row.value = distanceUnit == "" ? "m" : distanceUnit
      row.placeholder = "Measurement unit (eg. m)"
    }.onChange { row in
      self.tempory?.distanceUnit = row.value ?? ""
      onChange()
    }.cellUpdate { (cell, row) in
      cell.textField.textAlignment = .left
      cell.titleLabel?.textColor = .titleLabel
      cell.textField.clearButtonMode = .whileEditing
      cell.textField.autocapitalizationType = .none
    }
    
    let detailsRow = TextAreaRow { row in
      row.value = details
      row.placeholder = "Any extra information"
    }.onChange { row in
      self.tempory?.details = row.value ?? ""
      onChange()
    }
    
    let farmSelection = PickerRow<Farm>() { row in
      row.options = []
      var aFarm: Farm? = nil
      
      for (_, farmEntity) in farms {
        let farm = farmEntity.farm!
        row.options.append(farm)
        if farm.id == assignedFarm {
          aFarm = farm
        }
      }
      row.value = aFarm
    }.onChange { (row) in
      self.tempory?.assignedFarm = row.value?.id ?? ""
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
    
      +++ Section("Farm Selection")
      <<< farmSelection
  }
}

extension Session {
  func information(for form: Form, onChange: @escaping () -> ()) {
    tempory = Session(json: json(), id: id)
    
    let display = NameRow() { row in
      row.title = "Foreman"
      row.value = self.display
      row.placeholder = "Name of the foreman"
    }.onChange { row in
      self.tempory?.display = row.value ?? ""
      onChange()
    }.cellUpdate { (cell, row) in
      cell.textField.textAlignment = .left
      cell.titleLabel?.textColor = .titleLabel
      cell.textField.clearButtonMode = .whileEditing
    }
    
    let startDate = DateTimeRow() { row in
      row.title = "Time Started"
      row.value = self.startDate
    }.onChange { row in
      self.tempory?.startDate = row.value ?? Date()
      onChange()
    }
    
    let endDate = DateTimeRow() { row in
      row.title = "Time Ended"
      row.value = self.endDate
    }.onChange { row in
      self.tempory?.endDate = row.value ?? Date()
      onChange()
    }
    
    form
      +++ Section("Foreman")
      <<< display
    
      +++ Section("Duration")
      <<< startDate
      <<< endDate
    
      +++ Section("Tracking")
      <<< SessionRow()
  }
}

extension EntityItem {
  func information(for form: Form, onChange: @escaping () -> ()) {
    switch self {
    case let .worker(w): w.information(for: form, onChange: onChange)
    case let .orchard(o): o.information(for: form, onChange: onChange)
    case let .farm(f): f.information(for: form, onChange: onChange)
    case let .session(s): s.information(for: form, onChange: onChange)
    }
  }
}
