import Swift

public struct Ellip<Number: BinaryFloatingPoint> {
  public var center: Point<Number>
  public var majorAxis: Number
  public var minorAxis: Number
  
  public init(center: Point<Number>, majorAxis: Number, minorAxis: Number) {
    self.center = center
    (self.majorAxis, self.minorAxis) = (majorAxis, minorAxis)
  }
  
  public init(center: Point<Number>, radius: Number) {
    self.center = center
    (self.majorAxis, self.minorAxis) = (radius, radius)
  }
}

extension Ellip: Equatable {
  public static func == (lhs: Ellip, rhs: Ellip) -> Bool {
    return lhs.minorAxis == rhs.minorAxis
      && lhs.majorAxis == rhs.majorAxis
      && lhs.center == rhs.center
  }
}

extension Ellip {
  public func contains(_ p: Point<Number>) -> Bool {
    return (p.x - center.x).square() / majorAxis
      + (p.y - center.y).square() / minorAxis <= 1
  }
}
