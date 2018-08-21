
function buildFormalQuery(queryText) {
  const passes = queryText.split(/\s*[Oo][Rr]\s*/);
  var tokens = [];
  for (const i in passes) {
    const pass = passes[i];
    tokens.push(pass.split(/\s*/));
  }
  return tokens;
}

function unionOfObjects(objectA, objectB) {
  var result = {};
  if (objectA == {}) {
    return objectB;
  } 
  if(objectB == {}) {
    return objectA;
  }
  for (const keyA in objectA) {
    for (const keyB in objectB) {
      if (keyA == keyB) {
        result[keyA] = objectA[keyA];
      }
    }
  }
  return result;
}

function queryWorker(worker, orchards, queryText) {
  var result = [];
  
  const query = buildFormalQuery(queryText);
  
  for (const aQueryIdx in query) {
    const aQuery = query[aQueryIndex];
    var subResult = {};
    for (const queryParamIdx in aQuery) {
      const queryParam = aQuery[queryParamIdx];
      const subSubResult = searchWorker(worker, orchards, aQuery, false);
      result = unionOfObjects(subSubResult, subResult);
    }
    result.push(subResult);
  }
  
  return result;
}