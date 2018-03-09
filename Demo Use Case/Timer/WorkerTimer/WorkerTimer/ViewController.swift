//
//  ViewController.swift
//  WorkerTimer
//
//  Created by Letanyan Arumugam on 2018/03/09.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit

class ViewController: UIViewController {
  @IBOutlet weak var timerButton: UIButton!
  var startTime: Date? = nil
  var endTime: Date? = nil
  @IBOutlet weak var timerLabel: UILabel!
  var timer: Timer! = nil
  
  @IBAction func startButtonPressed(_ sender: UIButton) {
    timerButton.isSelected = !timerButton.isSelected
    guard let st = startTime else {
      startTime = Date()
      endTime = nil
      timer = Timer(timeInterval: 1, repeats: true) { _ in
        guard let st = self.startTime else {
          return
        }
        let duration = Date().timeIntervalSince(st)
        self.timerLabel.text = self.formatTimeInterval(duration)
      }
      timer.fire()
      RunLoop.current.add(timer, forMode: RunLoopMode.defaultRunLoopMode)
      timerButton.backgroundColor = .red
      return
    }
    if (timer.isValid) {
      timer.invalidate()
      timer = nil
    }
    
    endTime = Date()
    startTime = nil
    let duration = endTime!.timeIntervalSince(st)
    timerButton.backgroundColor = .green
    
    timerLabel.text = formatTimeInterval(duration)
  }
  
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
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    // Do any additional setup after loading the view, typically from a nib.
    
    timerButton.setTitle("STOP", for: .selected)
    timerButton.setTitle("START", for: .normal)
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
  }


}

