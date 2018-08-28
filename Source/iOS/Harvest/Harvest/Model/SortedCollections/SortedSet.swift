import Swift

struct SortedSet<Element: Hashable> : Collection, RandomAccessCollection {
  typealias SortingElement = Element
  
  var _set: Set<SortingElement>
  var _ref: SortedArray<SortingElement>
  var areInIncreasingOrder: Comparator
  
  init(areInIncreasingOrder: @escaping Comparator) {
    _set = Set<SortingElement>()
    _ref = SortedArray<SortingElement>(areInIncreasingOrder: areInIncreasingOrder)
    self.areInIncreasingOrder = areInIncreasingOrder
  }
  
  var startIndex: Int {
    return 0
  }
  
  var endIndex: Int {
    return _ref.endIndex
  }
  
  func index(after: Int) -> Int {
    return after + 1
  }
  
  subscript(i: Int) -> SortingElement {
    return _ref[i]
  }
  
  func contains(_ e: SortingElement) -> Bool {
    return _set.contains(e)
  }
}

extension SortedSet: RangeReplaceableCollection where Element: Comparable {
  init() {
    _set = []
    _ref = SortedArray<Element>()
    areInIncreasingOrder = (<)
  }
}

extension SortedSet: SortedUniqueInsertableCollection, CustomStringConvertible {
  @discardableResult
  mutating func insert(unique e: SortingElement) -> (Bool, Index) {
    let (inserted, e) = _set.insert(e)
    
    if inserted {
      let i = _ref.insert(e)
      return (false, i)
    }
    return (true, _ref.index(of: e)!)
  }
  
  var description: String {
    return _ref.description
  }
}

//extension SortedSet : RangeRemovableCollection {
//  mutating func removeSubrange(_ bounds: Range<Int>) {
//    for ref_offset in bounds {
//      guard let idx = _set.index(of: _ref[ref_offset]) else {
//        continue
//      }
//      _set.remove(at: idx)
//    }
//    _ref.removeSubrange(bounds)
//  }
//}
