import Darwin

typealias Chromosome = (a: Double, b: Double, c: Double, d: Double)
var function: (Chromosome) -> (Double) -> Double = { c in
  return { x in
//    return c.a * x + c.b
    return c.a * sin(c.b * (x - c.c)) + c.d
  }
}

func initSinusoidalGuess(against data: [(x: Double, y: Double)], forPeriod period: Double) {
  var high = 0.0, low = Double.infinity
  for (_, y) in data {
    if y > high {
      high = y
    }
    if y < low {
      low = y
    }
  }
  
  let d = (high + low) / 2
  let a = high - d
  let b = 2.0 * .pi / period
  let c = asin((low - d) / a) - b
  
  // a * sin(b * x + c) + d
  
  Limit.b = b..<(b + 0.1)
  if a < 0 {
    Limit.a = a..<0
  } else {
    Limit.a = 0..<a
  }
  if b < 0 {
    Limit.b = b..<0
  } else {
    Limit.b = 0..<b
  }
  if c < 0 {
    Limit.c = c..<0
  } else {
    Limit.c = 0..<c
  }
}

enum Limit {
  static var a = 0.0..<100.0
  static var b = 0.0..<4.0
  static var c = 0.0..<100.0
  static var d = 0.0..<100.0
}

func mutate(_ chromosome: Chromosome) -> Chromosome {
  return (
    Bool.random() ? chromosome.a : Double.random(in: Limit.a),
    Bool.random() ? chromosome.b : Double.random(in: Limit.b),
    Bool.random() ? chromosome.c : Double.random(in: Limit.c),
    Bool.random() ? chromosome.d : Double.random(in: Limit.d)
  )
}

func cross(_ x: Chromosome, _ y: Chromosome) -> Chromosome {
  return (
    Bool.random() ? x.a : y.a,
    Bool.random() ? x.b : y.b,
    Bool.random() ? x.c : y.c,
    Bool.random() ? x.d : y.d
  )
}

func randomChromosome() -> Chromosome {
  return (
    Double.random(in: Limit.a),
    Double.random(in: Limit.b),
    Double.random(in: Limit.c),
    Double.random(in: Limit.d)
  )
}

func cross(_ xs: [Chromosome], withChance prob: Double) -> [Chromosome] {
  var result = xs
  for i in 0..<(xs.count / 2) {
    let p = Double.random(in: 0..<1)
    
    if p > prob {
      let a = xs[i * 2]
      let b = xs[i * 2 + 1]
      
      let c = cross(a, b)
      result.append(c)
    }
  }
  return result
}

func mutate(_ xs: [Chromosome], withChance prob: Double) -> [Chromosome] {
  var result = xs
  for i in 0..<xs.count {
    let p = Double.random(in: 0..<1)
    if p > prob {
      let a = xs[i]
      let c = mutate(a)
      result.append(c)
    }
  }
  return result
}

func select(
  from chromosomes: [Chromosome],
  withTournamentSize n: Int, 
  against data: [(x: Double, y: Double)]
) -> [Chromosome] {
  var result = [Chromosome]()
  result.reserveCapacity(n)
  var evals = [Double]()
  evals.reserveCapacity(n)
  
  for chromosome in chromosomes {
    let fitness = evaluateFitness(of: chromosome, against: data)
    for i in 0..<min(result.count, n) {
      if fitness < evals[i] {
        if i < result.count - 1 {
          evals.insert(fitness, at: i)
          result.insert(chromosome, at: i)
        } else {
          evals.append(fitness)
          result.append(chromosome)
        }
        
      }
    }
    if result.count < n {
      evals.append(fitness)
      result.append(chromosome)
    }
  }
  
  return result
}

func evaluateFitness(of chromosome: Chromosome, against data: [(x: Double, y: Double)]) -> Double {
  var error = 0.0
  let f = function(chromosome)
  
  for (xi, yi) in data {
    let y = f(xi)
    error += (yi - y) * (yi - y)
  }
  
  return error
}

func evolve(
  populationOfSize n: Int, 
  generations: Int, 
  against data: [(x: Double, y: Double)]
) -> Chromosome {
  var chromosomes = [Chromosome]()
  chromosomes.reserveCapacity(n)
  for _ in 0..<n {
    chromosomes.append(randomChromosome())
  }
  
  for _ in 0..<100 {
    let ms = mutate(chromosomes, withChance: 0.05)
    let cs = cross(ms, withChance: 0.25)
    let ts = select(from: cs, withTournamentSize: n, against: data)
    
    chromosomes = ts
  }

  guard var best = chromosomes.first else {
    fatalError("eh")
  }
  var bestE = evaluateFitness(of: best, against: data)

  for v in chromosomes.dropFirst() {
    let e = evaluateFitness(of: v, against: data)
    
    if e < bestE {
      bestE = e
      best = v
    }
  }
  
  return best
}

var data: [(Double, Double)] = [
  (1, 10),
  (2, 14),
  (3, 12),
  (4, 16),
  (5, 133),
  (6, 18),
  (7, 9),
  (8, 20),
  (9, 22),
  (10, 21),
  (11, 24),
  (12, 26),
  (13, 28),
  (14, 30),
  (15, 31),
]

var data2: [(Double, Double)] = [
  (1, 7.78),
  (2, 7.52),
  (3, 6.87),
  (4, 5.97),
  (5, 5.17),
  (6, 4.67),
  (7, 4.67),
  (8, 5.1),
  (9, 5.67),
  (10, 6.2),
  (11, 6.82),
  (12, 7.43),
]

initSinusoidalGuess(against: data2, forPeriod: 3)
let best = evolve(populationOfSize: 100, generations: 100, against: data2)


print(best)