import Swift

// --------------------------------------------------------------------------- Protocols

protocol RangeRemovableCollection : Collection {
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

protocol SortedCollection : Collection {
  associatedtype SortingElement
  typealias Comparator = (SortingElement, SortingElement) -> Bool
  typealias InsertionPoint = (containsExistingElement: Bool, index: Index)
  
  var areInIncreasingOrder: Comparator { get }
  func sortingElement(for element: Element) -> SortingElement
  func insertionPoint(for element: SortingElement) -> InsertionPoint
}

protocol SortedUniqueInsertableCollection : SortedCollection where SortingElement: Equatable {
  mutating func insert(unique element: Element) -> (Bool, Index)
}

protocol SortedInsertableCollection : SortedUniqueInsertableCollection {
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
  func insertionPoint(for element: SortingElement) -> InsertionPoint {
    guard startIndex < endIndex else {
      return (false, startIndex)
    }
    var i = startIndex
    while i < endIndex {
      if areInIncreasingOrder(sortingElement(for: self[i]), element) {
        formIndex(after: &i)
      } else {
        return (false, i)
      }
    }
    return (false, endIndex)
  }
}

extension SortedCollection {
  internal func _index(of item: SortingElement) -> Index? {
    let pos = insertionPoint(for: item)
    return pos.containsExistingElement ? pos.index : nil
  }
}

// --------------------------------------------------------------------------- Sorted Array


// --------------------------------------------------------------------------- Sorted Set

// --------------------------------------------------------------------------- Sorted Dictionary


// --------------------------------------------------------------------------- Lib Conformers

//extension Range: SortedCollection
//where Bound: Strideable, Bound.Stride: SignedInteger {
//  typealias SortingElement = Bound
//
//  var areInIncreasingOrder: (Element, Element) -> Bool {
//    return (<)
//  }
//}
//
//extension ClosedRange: SortedCollection
//where Bound: Strideable, Bound.Stride: SignedInteger {
//  typealias SortingElement = Bound
//
//  var areInIncreasingOrder: (Element, Element) -> Bool {
//    return (<)
//  }
//}

extension ReversedCollection: SortedCollection
where Base: SortedCollection {
  typealias SortingElement = Base.SortingElement
  
  func sortingElement(for element: Element) -> SortingElement {
    return _base.sortingElement(for: element)
  }
  
  var areInIncreasingOrder: Comparator {
    return {
      return self._base.areInIncreasingOrder($1, $0) 
    }
  }
}

extension Slice: SortedCollection
where Base: SortedCollection {
  typealias SortingElement = Base.SortingElement
  
  func sortingElement(for element: Element) -> SortingElement {
    return base.sortingElement(for: element)
  }
  
  var areInIncreasingOrder: Comparator {
    return base.areInIncreasingOrder
  }
}

// --------------------------------------------------------------------------- Sortable Type

struct DictionaryPair<Word: Comparable, Definition> : Comparable, CustomStringConvertible {
  var word: Word
  var definition: Definition
  
  static func ==(lhs: DictionaryPair, rhs: DictionaryPair) -> Bool {
    return lhs.word == rhs.word
  }
  
  static func <(lhs: DictionaryPair, rhs: DictionaryPair) -> Bool {
    return lhs.word < rhs.word
  }
  
  var description: String {
    return "\(word): \(definition)"
  }
}

extension DictionaryPair : Hashable where Word: Hashable {
  var hashValue: Int {
    return word.hashValue
  }
}

func count<C: SortedCollection>(_ c: C) -> Int {
  guard c.count > 0 else {
    return 0
  }
  var lastVisited = c.sortingElement(for: c.first!)
  var r = 1
  
  for x in c.lazy.dropFirst() {
    let y = c.sortingElement(for: x)
    guard c.areInIncreasingOrder(lastVisited, y) else {
      fatalError()
    }
    lastVisited = y
    r += 1
  }
  return r
}

// --------------------------------------------------------------------------- Main

// Array

//import Darwin
//var x = SortedArray<UInt32>()
//x.insert(UInt32.max)
//for _ in 1...10000 {
//  x.insert(arc4random())
//}
//
//
//let i = Float(clock()) / Float(CLOCKS_PER_SEC)
//print(x.contains(UInt32.max))
//let j = Float(clock()) / Float(CLOCKS_PER_SEC)
//print(j - i)

// Set

//let a = DictionaryPair(word: "ABBA", definition: 134782)
//let b = DictionaryPair(word: "AARBOR", definition: 34782)
//let c = DictionaryPair(word: "BING", definition: 134782)
//let d = DictionaryPair(word: "BONG", definition: 34782)
//let e = DictionaryPair(word: "DING", definition: 1342)
//let f = DictionaryPair(word: "RING", definition: 1382)
//let g = DictionaryPair(word: "ABACUS", definition: 1342)
//let h = DictionaryPair(word: "ACUTE", definition: 1382)
//
//var xs = SortedSet<DictionaryPair<String, Int>>()
//xs.insert(unique: a)
//xs.insert(unique: b)
//xs.insert(unique: c)
//xs.insert(unique: d)
//xs.insert(unique: e)
//xs.insert(unique: f)
//xs.insert(unique: g)
//xs.insert(unique: h)

//xs.removeLast(5)
//print(count(xs))
//print(xs)


// Dictionary

//let arr = SortedArray([11, 1, 2])
//
//var dict = SortedDictionary<String, Int>()
//
//dict["B"] = 1
//dict["A"] = 11
//dict["G"] = 1
//dict["R"] = 12
//dict["D"] = 1
//dict["E"] = 10
//
//dict["R"] = 8
//dict["D"] = 2
//
//dict.insert(unique: ("R", 10))
//
//print(dict)
//
//dict.removeLast(3)
//
//print(dict)
//
//print(count(dict[SortedDictionary.Index(boxed: 0)..<SortedDictionary.Index(boxed: 3)]))
//
//for (k, v) in dict {
//  print(k, v)
//}
//
//let e = dict.map { $0.value }.elementsEqual(arr)
//print(dict.map { $0.value })
//print(e)
