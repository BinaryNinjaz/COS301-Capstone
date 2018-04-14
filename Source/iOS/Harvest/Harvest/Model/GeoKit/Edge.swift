import Swift

public struct Edge<Number: BinaryFloatingPoint> {
  public var start: Point<Number>
  public var end: Point<Number>
  
  public init(_ start: Point<Number>, _ end: Point<Number>) {
    (self.start, self.end) = (start, end)
  }
}

extension Edge {
  public func length() -> Number {
    let a = start
    let b = end
    
    return ((a.x - b.x).square() + (a.y - b.y).square()).squareRoot()
  }
  
  public func min() -> (x: Number, y: Number) {
    return (Swift.min(start.x, end.x), Swift.min(start.y, end.y))
  }
  
  public func max() -> (x: Number, y: Number) {
    return (Swift.max(start.x, end.x), Swift.max(start.y, end.y))
  }
  
  public func gradient() -> Number {
    let dy = end.y - start.y
    let dx = end.x - start.x
    
    return dx == 0 ? Number.infinity : dy / dx
  }
  
  public func offset() -> Number? {
    let m = gradient()
    return m == .infinity ? nil : m * -start.x + start.y
  }
  
  public func intersection(onLineXEqual x: Number) -> Point<Number>? {
    let m = gradient()
    guard let c = offset() else {
      return nil
    }
    
    return Point(x, m * x + c)
  }
}

extension Edge : Equatable {
  public static func ==(lhs: Edge, rhs: Edge) -> Bool {
    return lhs.start == rhs.start && lhs.end == rhs.end
  }
}

extension Edge : CustomStringConvertible {
  public var description: String {
    return "[\(start) \(end)]"
  }
}

public func createEdges<N>(from points: [Point<N>]) -> [Edge<N>] {
  var start: Point<N>? = nil
  
  var result = [Edge<N>]()
  
  for p in points {
    guard let s = start else {
      start = p
      continue
    }
    let edge = Edge(s, p)
    start = p
    result.append(edge)
  }
  if !result.isEmpty {
    result.append(Edge(start!, points.first!))
  }
  return result
}
