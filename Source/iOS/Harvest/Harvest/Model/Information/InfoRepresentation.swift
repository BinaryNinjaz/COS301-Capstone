//
//  InfoRepresentation.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/19.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

// swiftlint:disable function_body_length
import Eureka

extension Worker {
  func information(for formVC: FormViewController, onChange: @escaping () -> Void) {
    let form = formVC.form
    tempory = Worker(json: json()[id] ?? [:], id: id)
    
    let orchards = Entities.shared.orchards
    
    let orchardSection = SelectableSection<ListCheckRow<Orchard>>(
      "Assigned Orchard",
      selectionType: .multipleSelection)
    
    for (_, orchard) in orchards {
      orchardSection <<< ListCheckRow<Orchard>(orchard.description) { row in
        let farmName = Entities
          .shared.farms
          .first { _, v in v.id == orchard.assignedFarm }
          .map { $0.value.name }
          ?? orchard.assignedFarm
        row.title = farmName + " " + orchard.name
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
    
    let firstnameRow = NameRow { row in
      row.add(rule: RuleRequired(msg: "• First name must be filled in"))
      row.validationOptions = .validatesAlways
      row.title = "Worker Name"
      row.value = firstname
      row.placeholder = "John"
    }.onChange { (row) in
      self.tempory?.firstname = row.value ?? ""
      onChange()
    }.cellUpdate { (cell, _) in
      cell.textField.clearButtonMode = .whileEditing
    }.onRowValidationChanged { (cell, row) in
      if row.validationErrors.isEmpty {
        cell.backgroundColor = .white
      } else {
        cell.backgroundColor = .invalidInput
      }
    }
    
    let lastnameRow = NameRow { row in
      row.add(rule: RuleRequired(msg: "• Surname must be filled in"))
      row.validationOptions = .validatesAlways
      row.title = "Worker Surname"
      row.value = lastname
      row.placeholder = "Appleseed"
    }.onChange { (row) in
      self.tempory?.lastname = row.value ?? ""
      onChange()
    }.cellUpdate { (cell, _) in
      cell.textField.clearButtonMode = .whileEditing
    }.onRowValidationChanged { (cell, row) in
      if row.validationErrors.isEmpty {
        cell.backgroundColor = .white
      } else {
        cell.backgroundColor = .invalidInput
      }
    }
    
    let isForemanRow = SwitchRow("isForemanTag") { row in
      row.title = "Is a foreman?"
      row.value = kind == .foreman
    }.onChange { (row) in
      self.tempory?.kind = row.value ?? true ? .foreman : .worker
      onChange()
    }
    
    let phoneRow = PhoneRow { row in
      row.hidden = Condition.function(["isForemanTag"], { form in
        return !((form.rowBy(tag: "isForemanTag") as? SwitchRow)?.value ?? false)
      })
      row.add(rule: RuleRequired(msg: "• Phone numbers must exist for foreman"))
      row.validationOptions = .validatesAlways
      row.title = "Phone Number"
      row.value = phoneNumber
      row.placeholder = "012 3456789"
    }.onChange { row in
      self.tempory?.phoneNumber = row.value ?? ""
      onChange()
    }.onRowValidationChanged { (cell, row) in
      if row.validationErrors.isEmpty {
        cell.backgroundColor = .white
      } else {
        cell.backgroundColor = .invalidInput
      }
    }
    
    let idRow = TextRow { row in
      row.title = "ID Number"
      row.value = idNumber
      row.placeholder = "8001011234567"
    }.onChange { row in
      self.tempory?.idNumber = row.value ?? ""
      onChange()
    }
    
    let infoRow = TextAreaRow { row in
      row.value = details
      row.placeholder = "Any extra information"
    }.onChange { row in
      self.tempory?.details = row.value ?? ""
      onChange()
    }
    
    let deleteWorkerRow = ButtonRow { row in
      row.title = "Delete Worker"
    }.onCellSelection { (_, _) in
      HarvestDB.delete(worker: self) { (_, _) in
        formVC.navigationController?.popViewController(animated: true)
      }
    }.cellUpdate { (cell, _) in
      cell.textLabel?.textColor = .white
      cell.backgroundColor = .red
    }
    
    form
      +++ Section()
      <<< firstnameRow
      <<< lastnameRow
      <<< idRow
      
      +++ Section("Role")
      <<< isForemanRow
      <<< phoneRow
//      <<< emailRow
  
//      +++ Section("Contact")
//      <<< phoneRow
    
      +++ Section("Information")
      <<< infoRow
    
      +++ orchardSection
    
      +++ Section()
      <<< deleteWorkerRow
  }
}

extension Farm {
  func information(for formVC: FormViewController, onChange: @escaping () -> Void) {
    let form = formVC.form
    tempory = Farm(json: json()[id] ?? [:], id: id)
    
    let nameRow = NameRow { row in
      let uniqueNameRule = RuleClosure<String> { (_) -> ValidationError? in
        let notUnique = Entities.shared.farms.contains { $0.value.name == row.value && $0.value.id != self.id }
        return notUnique ? ValidationError(msg: "• Farm names must be unique") : nil
      }
      row.add(rule: RuleRequired(msg: "• Farm names must be filled in"))
      row.add(rule: uniqueNameRule)
      row.title = "Farm Name"
      row.value = name
      row.placeholder = "Name of the farm"
    }.onChange { row in
      self.tempory?.name = row.value ?? ""
      onChange()
    }.cellUpdate { (cell, _) in
      cell.textField.clearButtonMode = .whileEditing
    }.onRowValidationChanged { (cell, row) in
      if row.validationErrors.isEmpty {
        cell.backgroundColor = .white
      } else {
        cell.backgroundColor = .invalidInput
      }
    }
    
    let companyRow = NameRow { row in
      row.title = "Company Name"
      row.value = companyName
      row.placeholder = "Name of the company"
    }.onChange { row in
      self.tempory?.companyName = row.value ?? ""
      onChange()
    }.cellUpdate { (cell, _) in
      cell.textField.clearButtonMode = .whileEditing
    }
    
    let emailRow = EmailRow { row in
      row.title = "Farm Email"
      row.value = email
      row.placeholder = "Farms email address"
    }.onChange { row in
      self.tempory?.email = row.value ?? ""
      onChange()
    }.cellUpdate { (cell, _) in
      cell.textField.clearButtonMode = .whileEditing
    }
    let phoneRow = PhoneRow { row in
      row.title = "Contact Number"
      row.value = contactNumber
      row.placeholder = "Phone number of the farm"
    }.onChange { row in
      self.tempory?.contactNumber = row.value ?? ""
      onChange()
    }.cellUpdate { (cell, _) in
      cell.textField.clearButtonMode = .whileEditing
    }
    
    let provinceRow = NameRow { row in
      row.title = "Province"
      row.value = province
      row.placeholder = "Province location of the farm"
    }.onChange { row in
      self.tempory?.province = row.value ?? ""
      onChange()
    }.cellUpdate { (cell, _) in
      cell.textField.clearButtonMode = .whileEditing
    }
    let nearestTownRow = NameRow { row in
      row.title = "Nearest Town"
      row.value = nearestTown
      row.placeholder = "Town nearest to the farm"
    }.onChange { row in
      self.tempory?.nearestTown = row.value ?? ""
      onChange()
    }.cellUpdate { (cell, _) in
      cell.textField.clearButtonMode = .whileEditing
    }
    
    let detailsRow = TextAreaRow { row in
      row.title = "Details"
      row.value = details
      row.placeholder = "Any extra information"
    }.onChange { row in
      self.tempory?.details = row.value ?? ""
      onChange()
    }
    let o = Orchard(json: ["farm": id], id: "")
    let orchardRow = OrchardInFarmRow(tag: nil, orchard: o) { row in
      row.title = "Add Orchard to \(self.name)"
    }.cellUpdate { (cell, _) in
      cell.textLabel?.textColor = .addOrchard
      cell.textLabel?.textAlignment = .center
    }
    
    let orchardsSection = Section("Orchards in \(name)")
    
    for (_, orchard) in Entities.shared.orchards {
      let oRow = OrchardInFarmRow(tag: nil, orchard: orchard) { row in
        row.title = orchard.name
      }
      if orchard.assignedFarm == id {
        orchardsSection <<< oRow
      }
    }
    
    let deleteFarmRow = ButtonRow { row in
      row.title = "Delete Farm"
    }.onCellSelection { (_, _) in
      HarvestDB.delete(farm: self) { (_, _) in
        formVC.navigationController?.popViewController(animated: true)
      }
    }.cellUpdate { (cell, _) in
      cell.textLabel?.textColor = .white
      cell.backgroundColor = .red
    }
    
    form +++ Section("Farm")
      <<< nameRow
      <<< companyRow
      
      +++ Section("Contact")
      <<< emailRow
      <<< phoneRow
      
      +++ Section("Location")
      <<< provinceRow
      <<< nearestTownRow
      
      +++ Section("Details")
      <<< detailsRow
    
      +++ orchardsSection
      <<< orchardRow
    
      +++ Section()
      <<< deleteFarmRow
  }
}

public class DeletableMultivaluedSection: MultivaluedSection {
  var onRowsRemoved: ((IndexSet) -> Void)?
  
  required public init<S>(_ elements: S) where S: Sequence, S.Element == BaseRow {
    fatalError("init has not been implemented")
  }
  
  required public init() {
    fatalError("init() has not been implemented")
  }
  
  required public init(multivaluedOptions: MultivaluedOptions = MultivaluedOptions.Insert.union(.Delete),
                       header: String = "",
                       footer: String = "",
                       _ initializer: (MultivaluedSection) -> Void = { _ in }) {
    
    super.init(header: header, footer: footer, {section in initializer(section) })
    self.multivaluedOptions = multivaluedOptions
    guard multivaluedOptions.contains(.Insert) else { return }
  }
  
  func initialize() {
    let addRow = addButtonProvider(self)
    addRow.onCellSelection { cell, row in
      guard let tableView = cell.formViewController()?.tableView, let indexPath = row.indexPath else { return }
      cell.formViewController()?.tableView(tableView, commit: .insert, forRowAt: indexPath)
    }
    self <<< addRow
  }
  
  override public func rowsHaveBeenRemoved(_ rows: [BaseRow], at: IndexSet) {
    onRowsRemoved?(at)
  }
}

extension Orchard {
  func information(for formVC: FormViewController, onChange: @escaping () -> Void) {
    let form = formVC.form
    tempory = Orchard(json: json()[id] ?? [:], id: id)
    
    let farms = Entities.shared.farms
    
    let cultivarsRow = DeletableMultivaluedSection(
      multivaluedOptions: [.Insert, .Delete],
      header: "Cultivars",
      footer: "") { (section) in
        section.addButtonProvider = { sectionB in
          return ButtonRow {
            $0.title = "Add Another Cultivar"
          }
        }
        for (idx, cultivar) in cultivars.enumerated() {
          section <<< TextRow {
            $0.placeholder = "Cultivar"
            $0.value = cultivar
          }.onChange { row in
            self.tempory?.cultivars[idx] = row.value ?? ""
            onChange()
          }
        }
        
        section.multivaluedRowToInsertAt = { index in
          self.tempory?.cultivars.append("")
          return TextRow {
            $0.placeholder = "Cultivar"
          }.onChange { row in
            self.tempory?.cultivars[index] = row.value ?? ""
            onChange()
          }
        }
      }
    cultivarsRow.onRowsRemoved = { indexes in
      for i in indexes {
        self.tempory?.cultivars.remove(at: i)
      }
      onChange()
    }
    
    let nameRow = NameRow { row in
      row.add(rule: RuleRequired(msg: "• Orchard names must be filled in"))
      row.title = "Orchard Name"
      row.value = name
      row.placeholder = "Name of the orchard"
    }.onChange { row in
      self.tempory?.name = row.value ?? ""
      onChange()
    }.cellUpdate { (cell, _) in
      cell.textField.clearButtonMode = .whileEditing
    }.onRowValidationChanged { (cell, row) in
      if row.validationErrors.isEmpty {
        cell.backgroundColor = .white
      } else {
        cell.backgroundColor = .invalidInput
      }
    }
    
    let cropRow = TextRow { row in
      row.title = "Orchard Crop"
      row.value = crop
      row.placeholder = "Crop farmed on the orchard"
    }.onChange { row in
      self.tempory?.crop = row.value ?? ""
      onChange()
    }.cellUpdate { (cell, _) in
      cell.textField.clearButtonMode = .whileEditing
    }
    
    let bagMassRow = DecimalRow { row in
      row.title = "Bag Mass (kilogram)"
      row.value = bagMass.isNaN ? nil : bagMass
      row.placeholder = "Average mass of a bag"
    }.onChange { row in
      self.tempory?.bagMass = row.value ?? .nan
      onChange()
    }.cellUpdate { (cell, _) in
      cell.textField.clearButtonMode = .whileEditing
    }
    
    let dateRow = DateRow { row in
      row.title = "Date Planted"
      row.value = date
    }.onChange { row in
      self.tempory?.date = row.value ?? Date()
      onChange()
    }
    
    let irrigationRow = PushRow<IrrigationKind> { row in
      row.title = "Irrigation Kind"
      row.options = IrrigationKind.allCases
      row.value = irrigationKind
    }.onChange { row in
      self.tempory?.irrigationKind = row.value ?? .none
      onChange()
    }
    
    let widthRow = DecimalRow { row in
      row.title = "Tree Spacing (meter)"
      row.value = treeSpacing.isNaN ? nil : treeSpacing
      row.placeholder = "Horizontal Spacing"
    }.onChange { row in
      self.tempory?.treeSpacing = row.value ?? .nan
      onChange()
    }.cellUpdate { (cell, _) in
      cell.textField.clearButtonMode = .whileEditing
    }
    
    let heightRow = DecimalRow { row in
      row.title = "Row Spacing (meter)"
      row.value = rowSpacing.isNaN ? nil : rowSpacing
      row.placeholder = "Vertical Spacing"
    }.onChange { row in
      self.tempory?.rowSpacing = row.value ?? .nan
      onChange()
    }.cellUpdate { (cell, _) in
      cell.textField.clearButtonMode = .whileEditing
    }
    
    let detailsRow = TextAreaRow { row in
      row.value = details
      row.placeholder = "Any extra information"
    }.onChange { row in
      self.tempory?.details = row.value ?? ""
      onChange()
    }
    
    let farmSelection = PickerRow<Farm> { row in
      row.options = []
      var aFarm: Farm? = nil
      
      for (_, farm) in farms {
        let farm = farm
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
    
    let orchardAreaRow = OrchardAreaRow { row in
      row.title = "Orchard Location"
      row.value = self
    }.cellUpdate { (cell, _) in
      cell.detailTextLabel?.text = ""
    }
      
    orchardAreaRow.actuallyChanged = { (row) in
      self.tempory?.coords = row.value?.coords ?? []
      onChange()
    }
    
    let deleteOrchardRow = ButtonRow { row in
      row.title = "Delete Orchard"
    }.onCellSelection { (_, _) in
      HarvestDB.delete(orchard: self) { (_, _) in
        formVC.navigationController?.popViewController(animated: true)
      }
    }.cellUpdate { (cell, _) in
      cell.textLabel?.textColor = .white
      cell.backgroundColor = .red
    }
    
    form
      +++ Section("Orchard")
      <<< nameRow
      <<< cropRow
    
      +++ Section("Orchard Location")
      <<< orchardAreaRow
    
      +++ Section("Collection Details")
      <<< bagMassRow
      
      +++ Section("Plantation Details")
      <<< irrigationRow
      <<< dateRow
      
      +++ cultivarsRow
      
      +++ Section("Crop Dimensions")
      <<< widthRow
      <<< heightRow
    
      +++ Section("Information")
      <<< detailsRow
    
      +++ Section("Part of Farm")
      <<< farmSelection
    
      +++ Section()
      <<< deleteOrchardRow
  }
}

extension Session {
  func information(for formVC: FormViewController, onChange: @escaping () -> Void) {
    let form = formVC.form
    tempory = Session(json: json(), id: id)
    
    // FIXME Maybe allow changing these details or not? If change then
    // allow foreman select else disallow artificial changes
    
    let displayRow = LabelRow { row in
      row.title = "Foreman"
      row.value = foreman.description
    }
    
    let startDateRow = DateTimeRow { row in
      row.title = "Time Started"
      row.value = startDate
      row.baseCell.isUserInteractionEnabled = false
    }.onChange { row in
      self.tempory?.startDate = row.value ?? Date()
      onChange()
    }
    
    let endDateRow = DateTimeRow { row in
      row.title = "Time Ended"
      row.value = endDate
      row.baseCell.isUserInteractionEnabled = false
    }.onChange { row in
      self.tempory?.endDate = row.value ?? Date()
      onChange()
    }
    
    let sessionRow = SessionRow { row in
      row.title = "Tracked Path and Collection Points"
      row.value = self
    }.cellUpdate { (cell, _) in
      cell.detailTextLabel?.text = ""
    }
    
    form
      +++ Section("Foreman")
      <<< displayRow
    
      +++ Section("Duration")
      <<< startDateRow
      <<< endDateRow
    
      +++ Section("Tracking")
      <<< sessionRow
  }
}

extension HarvestUser {
  func information(for formVC: FormViewController, onChange: @escaping () -> Void) {
    let form = formVC.form
    temporary = HarvestUser(json: json())
    
    let firstnameRow = TextRow { row in
      row.title = "First Name"
      row.value = HarvestUser.current.firstname
      row.placeholder = ""
    }.cellUpdate { (cell, _) in
      cell.textField.clearButtonMode = .whileEditing
    }.onChange { row in
      self.temporary?.firstname = row.value ?? ""
      onChange()
    }
    
    let lastnameRow = TextRow { row in
      row.title = "Last Name"
      row.value = HarvestUser.current.lastname
      row.placeholder = ""
    }.cellUpdate { (cell, _) in
      cell.textField.clearButtonMode = .whileEditing
    }.onChange { row in
      self.temporary?.lastname = row.value ?? ""
      onChange()
    }
    
    form
      +++ Section("Name")
      <<< firstnameRow
      <<< lastnameRow
  }
}

extension EntityItem {
  func information(for formVC: FormViewController, onChange: @escaping () -> Void) {
    switch self {
    case let .worker(w): w.information(for: formVC, onChange: onChange)
    case let .orchard(o): o.information(for: formVC, onChange: onChange)
    case let .farm(f): f.information(for: formVC, onChange: onChange)
    case let .session(s): s.information(for: formVC, onChange: onChange)
    case .shallowSession: break
    case let .user(u): u.information(for: formVC, onChange: onChange)
    }
  }
}
