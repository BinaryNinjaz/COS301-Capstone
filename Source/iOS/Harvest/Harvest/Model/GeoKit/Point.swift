import Swift

public struct Point<Number: BinaryFloatingPoint> {
  public var x: Number
  public var y: Number
  
  public init(_ x: Number, _ y: Number) {
    (self.x, self.y) = (x, y)
  }
}

extension Point : Equatable {
  public static func ==(lhs: Point, rhs: Point) -> Bool {
    return lhs.x == rhs.x && lhs.y == rhs.y
  }
}

extension Point : CustomStringConvertible {
  public var description: String {
    return "(\(x) \(y))"
  }
}
