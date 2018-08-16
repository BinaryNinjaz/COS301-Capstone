//
//  AppIntroViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/08/05.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import UIKit
import FSPagerView

class CarouselViewController: UIViewController, FSPagerViewDataSource, FSPagerViewDelegate {
  override var prefersStatusBarHidden: Bool {
    return true
  }
  
  private var images: [UIImage] = []
  func showTutorial() {
    images = [#imageLiteral(resourceName: "TutorialWorkflow"), #imageLiteral(resourceName: "TutorialInfo"), #imageLiteral(resourceName: "TutorialCollections"), #imageLiteral(resourceName: "TutorialSessions"), #imageLiteral(resourceName: "TutorialStats")]
  }
  
  func showIntro() {
    images = [#imageLiteral(resourceName: "HarvestIntro"), #imageLiteral(resourceName: "HarvestManage"), #imageLiteral(resourceName: "HarvestTrack"), #imageLiteral(resourceName: "HarvestAnalyse")]
  }
  
  var currentIndex = 0 {
    didSet {
      if currentIndex == images.count - 1 {
        nextButton.setTitle("Done", for: .normal)
        skipButton.isHidden = true
      } else {
        nextButton.setTitle("Next  ⇢", for: .normal)
        skipButton.isHidden = false
      }
    }
  }
  @IBOutlet weak var pagerView: FSPagerView! {
    didSet {
      self.pagerView.register(FSPagerViewCell.self, forCellWithReuseIdentifier: "cell")
    }
  }
  @IBOutlet weak var pageControler: FSPageControl! {
    didSet {
      pageControler.numberOfPages = images.count
    }
  }
  @IBOutlet weak var nextButton: UIButton!
  @IBOutlet weak var skipButton: UIButton!
  
  override func viewDidLoad() {
    super.viewDidLoad()
    pagerView.transformer = FSPagerViewTransformer(type: .linear)
    pageControler.contentHorizontalAlignment = .center
  }
  
  func numberOfItems(in pagerView: FSPagerView) -> Int {
    return images.count
  }
  
  func pagerView(_ pagerView: FSPagerView, cellForItemAt index: Int) -> FSPagerViewCell {
    let cell = pagerView.dequeueReusableCell(withReuseIdentifier: "cell", at: index)
    
    cell.imageView?.image = images[index]
    cell.imageView?.contentMode = .scaleAspectFit
    cell.imageView?.layer.shadowRadius = 0
    cell.contentView.layer.shadowRadius = 0
    
    return cell
  }
  
  func pagerView(_ pagerView: FSPagerView, shouldSelectItemAt index: Int) -> Bool {
    return false
  }
  
  func pagerView(_ pagerView: FSPagerView, shouldHighlightItemAt index: Int) -> Bool {
    return false
  }
  
  func pagerViewDidScroll(_ pagerView: FSPagerView) {
    guard pageControler.currentPage != pagerView.currentIndex else {
      return
    }
    pageControler.currentPage = pagerView.currentIndex
    currentIndex = pagerView.currentIndex
  }
  
  @IBAction func nextButtonTouchUp(_ sender: UIButton) {
    if currentIndex == images.count - 1 {
      completeIntro()
    } else {
      currentIndex = min(currentIndex + 1, images.count - 1)
      pagerView.scrollToItem(at: currentIndex, animated: true)
    }
  }
  
  @IBAction func skipButtonTouchUp(_ sender: UIButton) {
    completeIntro()
  }
  
  func completeIntro() {
    if !UserDefaults.standard.bool(forKey: "Launched") {
      UserDefaults.standard.set(true, forKey: "Launched")
    }
    
    dismiss(animated: true, completion: nil)
  }
}
