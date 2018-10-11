//
//  InfoRepresentation.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/19.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

// swiftlint:disable function_body_length
import Eureka
import SCLAlertView
import FirebaseDatabase
import FirebaseAuth

extension UIViewController {
  func prebuiltGraph(
    title: String,
    stat: Stat
  ) -> ButtonRow {
    return ButtonRow { row in
      row.title = title
    }.cellUpdate { cell, _ in
      cell.textLabel?.textAlignment = .left
      cell.textLabel?.textColor = .addOrchard
    }.onCellSelection { _, _ in
      guard let vc = self.storyboard?.instantiateViewController(withIdentifier: "statsViewController") else {
        return
      }
      
      guard let svc = vc as? StatsViewController else {
        return
      }
      
      svc.stat = stat
      
      self.navigationController?.pushViewController(svc, animated: true)
    }
  }
}

extension Worker {
  func information(for formVC: FormViewController, onChange: @escaping () -> Void) {
    let form = formVC.form
    tempory = Worker(json: json()[id] ?? [:], id: id)
    
    let assignedOrchards = MultipleSelectorRow<Orchard> { row in
      row.title = "Assigned Orchards"
      self.tempory?.assignedOrchards = []
      var opts = [Orchard]()
      var vals = Set<Orchard>()
      for (_, orchard) in Entities.shared.orchards {
        opts.append(orchard)
        if self.assignedOrchards.contains(orchard.id) {
          vals.insert(orchard)
        }
      }
      row.options = opts
      row.value = vals
    }.onChange { row in
      self.tempory?.assignedOrchards = Array(row.value ?? []).map { $0.id }
      onChange()
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
      row.title = "Worker Surname"
      row.value = lastname
      row.placeholder = "Appleseed"
    }.onChange { (row) in
      self.tempory?.lastname = row.value ?? ""
      onChange()
    }.cellUpdate { (cell, _) in
      cell.textField.clearButtonMode = .whileEditing
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
      let validPhone = RuleClosure<String> { (_) -> ValidationError? in
        let valid = Phoney.formatted(number: row.value) != nil
        return valid ? nil : ValidationError(msg: "• Phone numbers must be validly formatted")
      }
      row.add(rule: RuleRequired(msg: "• Foremen must have phone numbers. So they can sign in."))
      row.add(rule: validPhone)
      row.validationOptions = .validatesAlways
      row.title = "Phone Number"
      row.value = Phoney.formatted(number: phoneNumber)
      row.placeholder = Phoney.formatted(number: "0123456789")
    }.onChange { row in
      self.tempory?.phoneNumber = Phoney.formatted(number: row.value) ?? ""
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
      let alert = SCLAlertView(appearance: .warningAppearance)
      alert.addButton("Cancel", action: {})
      alert.addButton("Delete") {
        HarvestDB.delete(worker: self) { (_, _) in
          formVC.navigationController?.popViewController(animated: true)
        }
      }
      
      alert.showWarning("Are You Sure You Want to Delete \(self.name)?", subTitle: """
        You will not be able to get back any information about this worker.
        Any work done by this worker will no longer have any statistics associated with them.
        """)
    }.cellUpdate { (cell, _) in
      cell.textLabel?.textColor = .white
      cell.backgroundColor = .red
    }
    
    let performanceSection = Section("Performance")
    if kind == .worker {
      for stat in StatStore.shared.store where stat.grouping == .worker {
        var newStat = stat
        newStat.ids = [self.id]
        performanceSection <<< formVC.prebuiltGraph(title: stat.name, stat: newStat)
      }
    } else {
      for stat in StatStore.shared.store where stat.grouping == .foreman {
        var newStat = stat
        newStat.ids = [self.id]
        performanceSection <<< formVC.prebuiltGraph(title: stat.name, stat: newStat)
      }
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
    
      +++ Section("Assigne Orchards")
      <<< assignedOrchards
    
    _ = id != "" ? (form +++ performanceSection) : form
    
    form
      +++ Section("Further Information")
      <<< infoRow
      
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
    
    let performanceSection = Section("Performance")
    for stat in StatStore.shared.store where stat.grouping == .farm {
      var newStat = stat
      newStat.ids = [self.id]
      performanceSection <<< formVC.prebuiltGraph(title: stat.name, stat: newStat)
    }
    
    let detailsRow = TextAreaRow { row in
      row.title = "Details"
      row.value = details
      row.placeholder = "Any extra information"
    }.onChange { row in
      self.tempory?.details = row.value ?? ""
      onChange()
    }
    
    let orchardRow = ButtonRow { row in
      row.title = "Create New Orchard"
    }.cellUpdate { (cell, _) in
      cell.textLabel?.textColor = .addOrchard
      cell.textLabel?.textAlignment = .center
    }.onCellSelection { _, _ in
      if let temp = self.tempory, self != temp {
        HarvestDB.save(farm: temp)
        self.makeChangesPermanent()
        Entities.shared.addItem(.farm(temp))
      }
      let storyboard = UIStoryboard(name: "Main", bundle: Bundle.main)
      let vc = storyboard.instantiateViewController(withIdentifier: "entityViewController")
      guard let evc = vc as? EntityViewController else {
        fatalError("We should never get here. We instantiated from entityViewController")
      }
      evc.entity = EntityItem.orchard(Orchard(json: ["farm": self.id], id: ""))
      formVC.navigationController?.pushViewController(evc, animated: true)
    }
    
    let orchardsSection = Section("Assigned Orchards")
    
    for (_, orchard) in Entities.shared.orchards {
      let oRow = OrchardInFarmRow(tag: nil, orchard: orchard) { row in
        row.title = orchard.name
      }
      if orchard.assignedFarm == id, id != "" {
        orchardsSection <<< oRow
      }
    }
    
    let deleteFarmRow = ButtonRow { row in
      row.title = "Delete Farm"
    }.onCellSelection { (_, _) in
      let alert = SCLAlertView(appearance: .warningAppearance)
      alert.addButton("Cancel", action: {})
      alert.addButton("Delete") {
        HarvestDB.delete(farm: self) { (_, _) in
          formVC.navigationController?.popViewController(animated: true)
        }
      }
      
      alert.showWarning("Are You Sure You Want to Delete \(self.name)?", subTitle: """
        You will not be able to get back any information about this farm.
        """)
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
    
      +++ orchardsSection
      <<< orchardRow
      
      +++ performanceSection
      
      +++ Section("Further Information")
      <<< detailsRow
    
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
  // swiftlint:disable cyclomatic_complexity
  func information(for formVC: FormViewController, onChange: @escaping () -> Void) {
    let form = formVC.form
    tempory = Orchard(json: json()[id] ?? [:], id: id)
    
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
      let uniqueNameRule = RuleClosure<String> { (_) -> ValidationError? in
        let notUnique = Entities.shared.orchards.contains {
          $0.value.name == row.value
            && $0.value.assignedFarm == self.tempory?.assignedFarm
            && $0.value.id != self.id
        }
        return notUnique ? ValidationError(msg: "• Orchard names in the same farm must have different names.") : nil
      }
      row.add(rule: RuleRequired(msg: "• Orchard names must be filled in"))
      row.add(rule: uniqueNameRule)
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
      row.value = bagMass
      row.placeholder = "Average mass of a bag"
    }.onChange { row in
      self.tempory?.bagMass = row.value
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
      row.value = treeSpacing
      row.placeholder = "Horizontal Spacing"
    }.onChange { row in
      self.tempory?.treeSpacing = row.value
      onChange()
    }.cellUpdate { (cell, _) in
      cell.textField.clearButtonMode = .whileEditing
    }
    
    let heightRow = DecimalRow { row in
      row.title = "Row Spacing (meter)"
      row.value = rowSpacing
      row.placeholder = "Vertical Spacing"
    }.onChange { row in
      self.tempory?.rowSpacing = row.value
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
    
    let orchardAreaRow = OrchardAreaRow { row in
      row.title = "Orchard Location"
      row.value = tempory
    }.cellUpdate { (cell, _) in
      let lat = self.tempory?.coords.first?.latitude ?? 0.0
      let lng = self.tempory?.coords.first?.longitude ?? 0.0
      let lt = String(format: "%.4f", lat)
      let lg = String(format: "%.4f", lng)
      cell.detailTextLabel?.text = "\(lt), \(lg)"
    }
    
    let farmSelection = PushRow<Farm> { row in
      row.title = "Assigned Farm"
      row.add(rule: RuleRequired(msg: "• An orchard must be assigned to a farm."))
      row.options = []
      var aFarm: Farm?
      
      for (_, farm) in Entities.shared.farms {
        let farm = farm
        row.options?.append(farm)
        if farm.id == assignedFarm {
          aFarm = farm
        }
      }
      row.value = aFarm
      if aFarm == nil {
        self.tempory?.assignedFarm = row.value?.id ?? ""
      }
    }.onChange { (row) in
      self.tempory?.assignedFarm = row.value?.id ?? ""
      orchardAreaRow.value = self.tempory
      onChange()
    }.onRowValidationChanged { (cell, row) in
      if row.validationErrors.isEmpty {
        cell.backgroundColor = .white
      } else {
        cell.backgroundColor = .invalidInput
      }
    }
      
    orchardAreaRow.actuallyChanged = { (row) in
      self.tempory?.coords = row.value?.coords ?? []
      self.tempory?.inferArea = self.tempory?.coords.isEmpty == true
      onChange()
    }
    
    let performanceSection = Section("Performance")
    for stat in StatStore.shared.store where stat.grouping == .orchard {
      var newStat = stat
      newStat.ids = [self.id]
      performanceSection <<< formVC.prebuiltGraph(title: stat.name, stat: newStat)
    }
    
    let deleteOrchardRow = ButtonRow { row in
      row.title = "Delete Orchard"
    }.onCellSelection { (_, _) in
      let alert = SCLAlertView(appearance: .warningAppearance)
      
      alert.addButton("Cancel", action: {})
      alert.addButton("Delete") {
        HarvestDB.delete(orchard: self) { (_, _) in
          formVC.navigationController?.popViewController(animated: true)
        }
      }
      
      alert.showWarning("Are You Sure You Want to Delete \(self.name)?", subTitle: """
        You will not be able to get back any information about this farm.
        Any work done in this orchard will no longer display any statistics.
        """)
    }.cellUpdate { (cell, _) in
      cell.textLabel?.textColor = .white
      cell.backgroundColor = .red
    }
    
    let assignedWorkersRow = MultipleSelectorRow<Worker> { row in
      row.title = "Assigned Workers"
      self.tempory?.assignedWorkers = []
      var opts = [Worker]()
      var vals = Set<Worker>()
      for (_, worker) in Entities.shared.workers {
        opts.append(worker)
        if worker.assignedOrchards.contains(id) {
          vals.insert(worker)
          self.tempory?.assignedWorkers.append((worker.id, .assigned))
        } else {
          self.tempory?.assignedWorkers.append((worker.id, .unassigned))
        }
        row.options = opts
        row.value = vals
      }
    }.onChange { row in
      var idx = 0
      for (assignedWorker, status) in self.tempory?.assignedWorkers ?? [] {
        if [.remove, .unassigned].contains(status)
        && row.value?.contains(where: { $0.id == assignedWorker }) ?? false {
          self.tempory?.assignedWorkers[idx] = (assignedWorker, .add)
        } else if [.add, .assigned].contains(status)
        && !(row.value?.contains(where: { $0.id == assignedWorker }) ?? false) {
          self.tempory?.assignedWorkers[idx] = (assignedWorker, .remove)
        }
        
        idx += 1
      }
      onChange()
    }
    
    form
      +++ Section("Orchard")
      <<< nameRow
      <<< cropRow
    
      +++ Section("Orchard Location")
      <<< orchardAreaRow
      
      +++ Section("Assigned Farm")
      <<< farmSelection
    
      +++ Section("Collection Details")
      <<< bagMassRow
      
      +++ Section("Plantation Details")
      <<< irrigationRow
      <<< dateRow
      
      +++ cultivarsRow
      
      +++ Section("Crop Dimensions")
      <<< widthRow
      <<< heightRow
    
      +++ Section("Assigned Workers")
      <<< assignedWorkersRow
      
    if id != "" {
      form +++ performanceSection
    }
    
    form
      +++ Section("Further Information")
      <<< detailsRow
      
      +++ Section()
      <<< deleteOrchardRow
  }
}

extension Session {
  func information(for formVC: FormViewController, onChange: @escaping () -> Void) {
    let form = formVC.form
    tempory = Session(json: json(), id: id)
    
    let displayRow = LabelRow { row in
      row.title = "Foreman"
      row.value = foreman.description
    }
    
    let startDateRow = DateTimeRow { row in
      row.title = "Time Started"
      row.value = startDate
      row.baseCell.isUserInteractionEnabled = false
    }
    
    let endDateRow = DateTimeRow { row in
      row.title = "Time Ended"
      row.value = endDate
      row.baseCell.isUserInteractionEnabled = false
    }
    
    let sessionRow = SessionRow { row in
      row.title = "Tracked Path and Collection Points"
      row.value = self
    }.cellUpdate { (cell, _) in
      cell.detailTextLabel?.text = ""
    }
    
    let chartRow = DonutChartRow("") { row in
      row.value = self
      if #available(iOS 11, *) {
        row.baseCell.userInteractionEnabledWhileDragging = true
      }
    }
    
    let deleteSessionRow = ButtonRow { row in
      row.title = "Delete Session"
    }.onCellSelection { (_, _) in
      let alert = SCLAlertView(appearance: .warningAppearance)
      alert.addButton("Cancel", action: {})
      alert.addButton("Delete") {
        HarvestDB.delete(session: self) { (_, _) in
          let vcs = formVC.navigationController?.viewControllers
          if let vc = vcs?[(vcs?.count ?? 0) - 2] as? SessionSelectionViewController {
            vc.sessions.removeSession(withId: self.id)
            vc.tableView.reloadData()
          }
          formVC.navigationController?.popViewController(animated: true)
        }
      }
      
      alert.showWarning("Are You Sure You Want to Delete this Session?", subTitle: """
      You will not be able to get back any information about this session.
      """)
    }.cellUpdate { (cell, _) in
      cell.textLabel?.textColor = .white
      cell.backgroundColor = .red
    }
    
    form
      +++ Section()
      <<< displayRow
    
      +++ Section("Duration")
      <<< startDateRow
      <<< endDateRow
    
      +++ Section("Tracking")
      <<< sessionRow
    
      +++ Section("Worker Performance Summary")
      <<< chartRow
    
      +++ Section()
      <<< deleteSessionRow
  }
}

extension HarvestUser {
  func information(for formVC: FormViewController, onChange: @escaping () -> Void) {
    let form = formVC.form
    temporary = HarvestUser(json: json())
    
    let organisationNameRow = TextRow { row in
      row.title = "Organisation Name"
      row.value = self.organisationName
      row.placeholder = "Name of the Organisation"
    }.cellUpdate { (cell, _) in
      cell.textField.clearButtonMode = .whileEditing
    }.onChange { row in
      self.temporary?.organisationName = row.value ?? ""
      onChange()
    }
    
    let emailRow = EmailRow { row in
      row.title = "Email"
      row.value = self.accountIdentifier
      row.placeholder = "Your Account Email Address"
    }.onChange { row in
      self.temporary?.accountIdentifier = row.value ?? self.accountIdentifier
      onChange()
    }.cellUpdate { (cell, _) in
      cell.textField.clearButtonMode = .whileEditing
    }
    
    let firstnameRow = TextRow { row in
      row.title = "First Name"
      row.value = self.firstname
      row.placeholder = ""
    }.cellUpdate { (cell, _) in
      cell.textField.clearButtonMode = .whileEditing
    }.onChange { row in
      self.temporary?.firstname = row.value ?? ""
      onChange()
    }
    
    let lastnameRow = TextRow { row in
      row.title = "Surname"
      row.value = self.lastname
      row.placeholder = ""
    }.cellUpdate { (cell, _) in
      cell.textField.clearButtonMode = .whileEditing
    }.onChange { row in
      self.temporary?.lastname = row.value ?? ""
      onChange()
    }
    
    let deleteAccountRow = ButtonRow { row in
      row.title = "Delete Account"
    }.onCellSelection { _, _ in
      let confirmationAlert = SCLAlertView(appearance: .warningAppearance)
      confirmationAlert.addButton("Cancel", action: {})
      confirmationAlert.addButton("Resgin") {
        guard let user = Auth.auth().currentUser else {
          SCLAlertView().showError("An Error Occurred", subTitle: "User is not currently signed in")
          return
        }
        HarvestDB.delete(harvestUser: self) { succ in
          if succ {
            user.delete { (error) in
              guard error == nil else {
                SCLAlertView().showError("An Error Occurred", subTitle: error!.localizedDescription)
                return
              }
            }
            HarvestDB.signOut()
            if let vc = formVC
              .storyboard?
              .instantiateViewController(withIdentifier: "signInOptionViewController") {
              formVC.present(vc, animated: true, completion: nil)
            }
          }
        }
      }
      confirmationAlert.showWarning("Are You Sure?", subTitle: "Are you sure you want to delete your account")
      
    }.cellUpdate { (cell, _) in
      cell.textLabel?.textColor = .white
      cell.backgroundColor = .red
    }
    
    let newPasswordRow = ButtonRow { row in
      row.title = "Update Password"
    }.onCellSelection { _, _ in
      let alert = SCLAlertView(appearance: .warningAppearance)
      
      let newPasswordTextField = alert.addTextField("New Password")
      let confirmPasswordTextField = alert.addTextField("Confirm New Password")
      
      newPasswordTextField.placeholder = "New Password"
      confirmPasswordTextField.placeholder = "Confirm New Password"
      newPasswordTextField.isSecureTextEntry = true
      confirmPasswordTextField.isSecureTextEntry = true
      
      alert.addButton("Cancel", action: {})
      alert.addButton("Update") {
        guard let newPassword = newPasswordTextField.text,
          let confirmPassword = confirmPasswordTextField.text,
          newPassword.count >= 6 && confirmPassword.count >= 6 else {
            SCLAlertView().showError(
              "Password Not Long Enough",
              subTitle: "Password length must be at least 6 characters long")
            return
        }
        
        guard newPassword == confirmPassword else {
          SCLAlertView().showError(
            "Mismatching passwords",
            subTitle: """
          Your passwords are not matching. Please provide the same password in both \
          password prompts
          """)
          return
        }
        
        guard let user = Auth.auth().currentUser else {
          SCLAlertView().showError(
            "An Error Occurred",
            subTitle: "User is not signed in")
          return
        }
        
        user.updatePassword(to: newPassword) { (error) in
          if let err = error {
            SCLAlertView().showError("An Error Occurred", subTitle: err.localizedDescription)
            return
          }
          SCLAlertView.showSuccessToast(message: "Password Updated")
        }
      }
      
      alert.showEdit(
        "Update Password",
        subTitle: "Enter the new password you want to change to.")
      
    }.cellUpdate { cell, _ in
      cell.textLabel?.textColor = .white
      cell.backgroundColor = .addOrchard
    }
    
    let loginDetailsSection = Section("Login Details")
    loginDetailsSection <<< emailRow
    if Auth.auth().currentUser?.providerID == "Firebase" {
      loginDetailsSection <<< newPasswordRow
    }
    
    form
      +++ loginDetailsSection
      
      +++ Section("Account Details")
      <<< organisationNameRow
      <<< firstnameRow
      <<< lastnameRow
    
    form
      +++ Section("Account Management")
      <<< deleteAccountRow
  }
}

extension EntityItem {
  func information(for formVC: FormViewController, onChange: @escaping () -> Void) {
    switch self {
    case let .worker(w): w.information(for: formVC, onChange: onChange)
    case let .orchard(o): o.information(for: formVC, onChange: onChange)
    case let .farm(f): f.information(for: formVC, onChange: onChange)
    case let .session(s): s.information(for: formVC, onChange: onChange)
    case let .user(u): u.information(for: formVC, onChange: onChange)
    }
  }
}
