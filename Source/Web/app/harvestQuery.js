
function isEmptyObject(obj) {
  return Object.keys(obj).length === 0 && obj.constructor === Object;
}

function ignoredWord(word) {
  return arrayContainsEntity(["is", "in", "with", "by", "on", "over", "from", "and"], word.toLowerCase());
}

function propertyWords() {
  return [
    "crop", "name", "company", "email", "phone number", "province", "nearest town",
    "details", "crop", "cultivar", "irrigation type", "id", "type", "assigned orchard"
  ];
}

function propertyWord(word) {
  return arrayContainsString(propertyWords(), word.toLowerCase());
}

function getRequestedPropertiesFromQuery(query) {
  const properties = propertyWords();
  var result = [];
  for (const pidx in properties) {
    const prop = properties[pidx];
    if (stringContainsSubstring(query, prop)) {
      result.push(prop);
    }
  }
  return result;
}

function arrayContainsString(array, string) {
  for (const i in array) {
    if (array[i] === string) {
      return true;
    }
  }
  return false;
}

function buildFormalQuery(queryText) {
  const passes = queryText.trim().split(/\s*[Oo][Rr]\s*/);
  var tokens = [];
  for (const i in passes) {
    const pass = passes[i];
    tokens.push(pass.split(/\s+/g));
  }
  return tokens;
}

function unionOfObjects(objectA, objectB) {
  if (isEmptyObject(objectA)) {
    return objectB;
  }
  if (isEmptyObject(objectB)) {
    return objectA;
  }
  var result = {};

  for (const keyA in objectA) {
    for (const keyB in objectB) {
      if (!arrayContainsString(keyA.split("/"), keyB)) {
        const temp = objectA[keyA];
        result[keyA + "/" + keyB] = temp + "/" + objectB[keyB];
        delete result[keyA];
      } else if (arrayContainsString(objectA[keyA].split("/"), objectB[keyB])) {
        result[keyA] = objectA[keyA];
      }
    }
  }

  // filter compound duplicates from compound words
  for (const rKey in result) {
    if (!stringContainsSubstring(rKey, "/")) {
      for (const r2Key in result) {
        if (rKey !== r2Key && stringContainsSubstring(r2Key, rKey)) {
          delete result[r2Key];
        }
      }
    }
  }

  return result;
}

function mergeObjects(objectA, objectB) {
  var result = {};
  for (const a in objectA) {
    result[a] = objectA[a];
  }
  for (const b in objectB) {
    result[b] = objectB[b];
  }
  return result;
}

function queryEntity(option, ekey, entity, farms, orchards, workers, queryText, full) {
  var result = {};

  const query = buildFormalQuery(queryText);
  var requestedProperties = [];
  for (const aQueryIdx in query) {
    const aQuery = query[aQueryIdx];
    var subResult = {};
    var missed = false;
    for (const queryParamIdx in aQuery) {
      const queryParam = aQuery[queryParamIdx];
      var subSubResult;
      if (ignoredWord(queryParam) || propertyWord(queryParam)) {
        continue;
      }
      if (option === "worker") {
        subSubResult = searchWorker(entity, orchards, queryParam, full);
      } else if (option === "orchard") {
        subSubResult = searchOrchard(entity, ekey, farms, orchards, workers, queryParam, full);
      } else if (option === "farm") {
        subSubResult = searchFarm(entity, queryParam, full);
      } else if (option === "session") {
        subSubResult = searchSession(entity, queryParam, farms, orchards, workers);
      }
      if (!isEmptyObject(subSubResult)) {
        subResult = unionOfObjects(subResult, subSubResult);
      } else {
        subResult = {};
        missed = true;
        break;
      }
    }
    if (!missed && !isEmptyObject(subResult)) {
      result = mergeObjects(result, subResult);
    }
  }
  const requested = getRequestedPropertiesFromQuery(queryText);
  if (requested.length > 0) {
    for (const key in result) {
      if (!arrayContainsString(requested, key.toLowerCase())) {
        delete result[key];
      }
    }
  }

  return result;
}

function queryWorker(worker, orchards, queryText, full) {
  return queryEntity('worker', '', worker, {}, orchards, {}, queryText, full);
}

function queryOrchard(orchard, okey, farms, orchards, workers, queryText, full) {
  return queryEntity('orchard', okey, orchard, farms, orchards, workers, queryText, full);
}

function queryFarm(farm, queryText, full) {
  return queryEntity('farm', '', farm, {}, {}, {}, queryText, full);
}

function querySession(session, queryText, full) {
  return queryEntity('session', '', session, farms, orchards, workers, queryText, full);
}
