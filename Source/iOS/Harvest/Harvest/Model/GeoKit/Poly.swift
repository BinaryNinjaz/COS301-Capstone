import Swift

public struct Poly<Number: BinaryFloatingPoint> {
  public var edges: [Edge<Number>]
  
  public init(_ coords: Point<Number>...) {
    self.edges = createEdges(from: coords)
  }
  
  public init(_ coords: [Point<Number>]) {
    self.edges = createEdges(from: coords)
  }
}

extension Poly : Equatable {
  public static func ==(lhs: Poly, rhs: Poly) -> Bool {
    return lhs.edges == rhs.edges
  }
}

extension Poly {
  
  func min() -> (x: Number, y: Number)? {
    guard var result = edges.first?.min() else {
      return nil
    }
    
    for e in edges.dropFirst() {
      let temp = e.min()
      result = (Swift.min(result.x, temp.x), Swift.min(result.y, temp.y))
    }
    
    return result
  }
  
  func max() -> (x: Number, y: Number)? {
    guard var result = edges.first?.max() else {
      return nil
    }
    
    for e in edges.dropFirst() {
      let temp = e.max()
      result = (Swift.max(result.x, temp.x), Swift.max(result.y, temp.y))
    }
    
    return result
  }
  
  func intersectionPoints(onLineXEqual x: Number) -> [Point<Number>] {
    return edges
      .compactMap { $0.intersection(onLineXEqual: x) }
      .sorted { $0.y < $1.y }
  }
  
  func intersectionSegments(onLineXEqual x: Number) -> [Edge<Number>] {
    var result = [Edge<Number>]()
    
    var a: Point<Number>? = nil
    
    for p in intersectionPoints(onLineXEqual: x) {
      guard let s = a else {
        a = p
        continue
      }
      
      result.append(Edge(s, p))
      a = nil
    }
    
    return result
  }
  
  public func contains(_ p: Point<Number>) -> Bool {
    let segments = intersectionSegments(onLineXEqual: p.x)
    
    for segment in segments {
      let miny = segment.min().y
      let maxy = segment.max().y
      
      if miny <= p.y && p.y <= maxy {
        return true
      }
    }
    return false
  }
}

extension Poly : CustomStringConvertible {
  public var description: String {
    return edges.description
  }
}
