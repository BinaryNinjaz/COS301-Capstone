import Swift

struct SortedArray<Element: Equatable> : Collection {
  typealias SortingElement = Element
  
  private var _store: [SortingElement]
  var areInIncreasingOrder: Comparator
  
  init(_ elements: [SortingElement] = [], areInIncreasingOrder: @escaping Comparator) {
    _store = elements.sorted(by: areInIncreasingOrder)
    self.areInIncreasingOrder = areInIncreasingOrder
  }
  
  var startIndex: Int {
    return 0
  }
  
  var endIndex: Int {
    return _store.count
  }
  
  func index(after: Int) -> Int {
    return after + 1
  }
  
  subscript(index: Int) -> SortingElement {
    return _store[index]
  }
  
  func index(of e: SortingElement) -> Int? {
    return _index(of: e)
  }
}

extension SortedArray: BidirectionalCollection {
  func index(before: Int) -> Int {
    return before - 1
  }
}

extension SortedArray: RandomAccessCollection {
  func index(_ i: Int, offsetBy: Int) -> Int {
    return i + offsetBy
  }
}

extension SortedArray where Element: Comparable {
  init(_ elements: [SortingElement] = []) {
    _store = elements.sorted()
    self.areInIncreasingOrder = (<)
  }
}

//extension SortedArray: RangeReplaceableCollection where Element: Comparable {
//  init() {
//    _store = []
//    areInIncreasingOrder = (<)
//  }
//}

extension SortedArray: SortedInsertableCollection, CustomStringConvertible {
  @discardableResult
  mutating func insert(_ e: SortingElement) -> Index {
    guard _store.count > 0 else {
      _store.append(e)
      return 0
    }
    
    let idx = insertionPoint(for: e).index
    
    if idx == endIndex {
      _store.append(e)
    } else {
      _store.insert(e, at: idx)
    }
    
    return idx
  }
  
  mutating func insert<S: Sequence>(elements: S) where S.Element == SortingElement {
    _store.append(contentsOf: elements)
    _store.sort(by: areInIncreasingOrder)
  }
  
  func contains(_ e: SortingElement) -> Bool {
    return _index(of: e) != nil
  }
  
  var description: String {
    return _store.description
  }
}

extension SortedArray: RangeRemovableCollection {
  mutating func removeSubrange(_ bounds: Range<Index>) {
    _store.removeSubrange(bounds)
  }
}
