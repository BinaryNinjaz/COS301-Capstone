import Swift

// --------------------------------------------------------------------------- Protocols

protocol RangeRemovableCollection: Collection {
  mutating func removeSubrange(_ bounds: Range<Index>)
}

extension RangeRemovableCollection {
  mutating func removeSubrange<R: RangeExpression>(_ bounds: R) 
  where R.Bound == Index {
    let b = bounds.relative(to: self)
    removeSubrange(b)
  }
  
  mutating func remove(at: Index) {
    removeSubrange(at...at)
  }
  
  mutating func removeFirst() {
    remove(at: startIndex)
  }
  
  mutating func removeFirst(_ n: Int) {
    removeSubrange(startIndex..<index(startIndex, offsetBy: n))
  }
  
  mutating func removeAll() {
    removeSubrange(startIndex..<endIndex)
  }
}

extension RangeRemovableCollection where Self: BidirectionalCollection {
  mutating func removeLast() {
    remove(at: index(before: endIndex))
  }
  
  mutating func removeLast(_ n: Int) {
    removeSubrange(index(endIndex, offsetBy: -n)..<endIndex)
  }
}

protocol SortedCollection: Collection {
  associatedtype SortingElement
  typealias Comparator = (SortingElement, SortingElement) -> Bool
  typealias InsertionPoint = (containsExistingElement: Bool, index: Index)
  
  var areInIncreasingOrder: Comparator { get }
  func sortingElement(for element: Element) -> SortingElement
  func insertionPoint(for element: SortingElement) -> InsertionPoint
}

protocol SortedUniqueInsertableCollection: SortedCollection where SortingElement: Equatable {
  mutating func insert(unique element: Element) -> (Bool, Index)
}

protocol SortedInsertableCollection: SortedCollection {
  mutating func insert(_ element: Element) -> Index
}

extension SortedInsertableCollection {
  mutating func insert(unique element: Element) -> (Bool, Index) {
    let idx = insert(element)
    return (true, idx)
  }
}

extension SortedCollection where SortingElement == Element {
  func sortingElement(for element: Element) -> SortingElement {
    return element
  }
}

extension SortedCollection where Self: RandomAccessCollection, SortingElement: Equatable {
  func insertionPoint(for element: SortingElement) -> InsertionPoint {
    guard count > 0 else {
      return (false, startIndex)
    }
    guard count > 1 else {
      return areInIncreasingOrder(element, sortingElement(for: first!)) 
        ? (false, startIndex)
        : (element == sortingElement(for: first!), index(after: startIndex))
    }
    
    var start = startIndex
    var end = endIndex
    var mid = startIndex
    while start <= end {
      if start == end {
        if start >= endIndex {
          return (false, endIndex)
        }
        let loc = areInIncreasingOrder(element, sortingElement(for: self[start])) ? 0 : 1
        return (element == sortingElement(for: self[start]), index(start, offsetBy: loc))
      }
      
      let offset = distance(from: start, to: end) / 2
      mid = index(start, offsetBy: offset)
      
      if sortingElement(for: self[mid]) == element {
        return (true, mid)
      } else if areInIncreasingOrder(element, sortingElement(for: self[mid])) {
        let midOffset = distance(from: start, to: mid) - 1
        if midOffset < 0 {
          break
        }
        end = index(start, offsetBy: midOffset)
      } else {
        start = index(after: mid)
      }
    }
    return (false, mid)
  } 
}

extension SortedCollection where SortingElement: Equatable {
  func insertionPoint(for element: SortingElement) -> InsertionPoint {
    guard startIndex < endIndex else {
      return (false, startIndex)
    }
    
    var i = startIndex
    var beforeEnd = startIndex
    while i < endIndex {
      if areInIncreasingOrder(sortingElement(for: self[i]), element) {
        beforeEnd = i
        formIndex(after: &i)
      } else {
        return (sortingElement(for: self[i]) == element, i)
      }
    }
    return (sortingElement(for: self[beforeEnd]) == element, endIndex)
  }
}

extension SortedCollection {
  internal func _index(of item: SortingElement) -> Index? {
    let pos = insertionPoint(for: item)
    return pos.containsExistingElement ? pos.index : nil
  }
}
