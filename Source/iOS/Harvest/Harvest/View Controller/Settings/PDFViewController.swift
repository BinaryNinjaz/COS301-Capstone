//
//  PDFViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/08/10.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import UIKit

class PDFViewController: UIViewController {
  weak var webView: UIWebView?
  var pdfPath: String?
  
  override func viewDidAppear(_ animated: Bool) {
    webView?.removeFromSuperview()
    
    webView = UIWebView(frame: view.frame)
    
    let targetURL = Bundle.main.url(forResource: pdfPath, withExtension: "pdf")!
    let request = URLRequest(url: targetURL)
    webView?.loadRequest(request)
    
    if let webView = webView {
      view.addSubview(webView)
    }
  }
  
  override func viewDidLoad() {
    super.viewDidLoad()
  }
  
  func loadPDF(named: String) {
    pdfPath = named
  }
  
  override func viewWillLayoutSubviews() {
    webView?.setOriginX(0)
    webView?.setOriginY(0)
    webView?.setWidth(view.frame.width)
    webView?.setHeight(view.frame.height)
  }
  
}
