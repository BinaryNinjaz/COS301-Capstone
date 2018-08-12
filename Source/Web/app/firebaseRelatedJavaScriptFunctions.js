"use strict";

function initializeFirebase() {
  const config = {
    apiKey: "AIzaSyBTTgAtocwDfror-XZLi4R5rUEphTUK4PU",
    authDomain: "harvest-ios-1522082524457.firebaseapp.com",
    databaseURL: "https://harvest-ios-1522082524457.firebaseio.com",
    projectId: "harvest-ios-1522082524457",
    storageBucket: "harvest-ios-1522082524457.appspot.com",
    messagingSenderId: "167365669216"
  };
  firebase.initializeApp(config);
}

// set up the database
initializeFirebase();

function refreshPage() {
  location.reload();
}

const title = document.getElementsByTagName("title")[0].innerHTML;
if (title !== "Harvest | Login and Register") {
  // This is automatic whenever there's a change to a users authorization
  firebase.auth().onAuthStateChanged(function (user) {
    if (user) {
      // user logged in
      // window.location.href = "HomePage.html";
    } else {
      // user logged out
      window.location.href = "index.html";
    }
  });
}

function retryUntilTimeout(succ, fail, timeout) {
  if (firebase.auth().currentUser) {
    succ()
  } else {
    if (timeout > 0 && timeout < 1000 * 60 * 5) {
      setTimeout(succ, timeout * 2);
    } else {
      alert("Network timeout. Try Reloading the page later.");
    }
    fail();
  }
}

function firebaseRegister(email, password) {
  firebase.auth()
    .createUserWithEmailAndPassword(email, password)
    .then(function (user) { // user details correct
      document.location.href = "HomePage.html";
    }).catch(function (error) { // some error occured
      const errorCode = error.code;
      const errorMessage = error.message;

      if (errorCode === 'auth/wrong-password') {
        alert('Wrong password.');
      } else {
        alert(errorMessage);
      }
      console.log(error);
  });
}

function firebaseLogin() {
  const email = document.getElementById("username").value;
  let password = document.getElementById("password").value;

  firebase.auth().signInWithEmailAndPassword(email, password).then(function (user) {
    // user logged in
    document.location.href = "HomePage.html";
  }).catch(function (error) {
    // log in failed
    const errorCode = error.code;
    const errorMessage = error.message;

    if (errorCode === 'auth/wrong-password') {
      alert('Wrong password.');
    } else {
      alert(errorMessage);
    }
  });
}

function signOut() {
  firebase.auth().signOut().then(function () {
    document.location.href = "index.html";
  });
}

function sendPasswordResetEmail(emailAddress) {
  firebase.auth().sendPasswordResetEmail(emailAddress).then(function () {
    // Email sent.
  }).catch(function (error) {
    // An error happened.
  });
}

function locationLookup(callback) {
  $.get('http://ip-api.com/json', (data, response) => {
    callback(data, response);
  });
}

const user = function() { return firebase.auth().currentUser };
const userID = function() {
  if (user() !== null) {
    return user().uid
  } else {
    return ""
  }
}

function getAdmin(callback) {
  const ref = firebase.database().ref('/' + userID() + '/admin');
  ref.once('value').then((snapshot) => {
    callback(snapshot);
  });
}

function getOrchards(callback) {
  const ref = firebase.database().ref('/' + userID() + '/orchards');
  ref.once('value').then((snapshot) => {
    callback(snapshot);
  });
}

function getWorkers(callback) {
  const ref = firebase.database().ref('/' + userID() + '/workers');
  ref.once('value').then((snapshot) => {
    callback(snapshot);
  });
}

function getFarms(callback) {
  const ref = firebase.database().ref('/' + userID() + '/farms');
  ref.once('value').then((snapshot) => {
    callback(snapshot);
  });
}

/// orchards must be set to {} before calling this function
function setOrchards(orchards, completion) {
  getOrchards((snapshot) => {
    snapshot.forEach((childSnapshot) => {
      orchards[childSnapshot.key] = childSnapshot.val();
    });
    completion();
  });
}

/// workers must be set to {} before calling this function.
/// This function also gets the admin as a worker.
function setWorkers(workers, completion) {
  getAdmin((adminSnapshot) => {
    const val = adminSnapshot.val();
    if (val !== null && val !== undefined) {
      const farmOwner = {name: val.firstname, surname: val.lastname};

      workers[val.uid] = farmOwner;
      getWorkers((snapshot) => {
        snapshot.forEach((childSnapshot) => {
          workers[childSnapshot.key] = childSnapshot.val();
        });
        completion();
      });
    }
  });
}

/// farms must be set to {} before calling this function
function setFarms(farms, completion) {
  getFarms((snapshot) => {
    snapshot.forEach((childSnapshot) => {
      farms[childSnapshot.key] = childSnapshot.val();
    });
    completion();
  });
}

function watchFarms(farms, completion) {
  const ref = firebase.database().ref('/' + userID() + '/farms');
  ref.on('child_added', (snapshot) => {
    farms[snapshot.key] = snapshot.val();
    completion();
  });
  ref.on('child_removed', (snapshot) => {
    delete farms[snapshot.key];
    completion();
  });
  ref.on('child_changed', (snapshot) => {
    farms[snapshot.key] = snapshot.val();
    completion();
  });
}

function watchOrchards(orchards, completion) {
  const ref = firebase.database().ref('/' + userID() + '/orchards');
  ref.on('child_added', (snapshot) => {
    orchards[snapshot.key] = snapshot.val();
    completion();
  });
  ref.on('child_removed', (snapshot) => {
    delete orchards[snapshot.key];
    completion();
  });
  ref.on('child_changed', (snapshot) => {
    orchards[snapshot.key] = snapshot.val();
    completion();
  });
}

function watchWorkers(workers, completion) {
  const ref = firebase.database().ref('/' + userID() + '/workers');
  ref.on('child_added', (snapshot) => {
    workers[snapshot.key] = snapshot.val();
    completion();
  });
  ref.on('child_removed', (snapshot) => {
    delete workers[snapshot.key];
    completion();
  });
  ref.on('child_changed', (snapshot) => {
    workers[snapshot.key] = snapshot.val();
    completion();
  });
}

function searchFarm(farm, searchText, full) {
  var result = {};
  if (farm === undefined) {
    return result;
  }

  const text = searchText.toLowerCase();

  if (farm.name !== undefined && stringContainsSubstring(farm.name.toLowerCase(), text)) {
    result["Name"] = farm.name;
  }

  if (farm.companyName !== undefined && stringContainsSubstring(farm.companyName.toLowerCase(), text)) {
    result["Company"] = farm.companyName;
  }

  if (farm.email !== undefined && stringContainsSubstring(farm.email.toLowerCase(), text)) {
    result["Email"] = farm.email;
  }

  if (farm.contactNumber !== undefined && stringContainsSubstring(farm.contactNumber.toLowerCase(), text)) {
    result["Phone Number"] = farm.contactNumber;
  }

  if (farm.province !== undefined && stringContainsSubstring(farm.province.toLowerCase(), text)) {
    result["Province"] = farm.province;
  }

  if (farm.town !== undefined && stringContainsSubstring(farm.town.toLowerCase(), text)) {
    result["Nearest Town"] = farm.town;
  }

  if (full && farm.further !== undefined && stringContainsSubstring(farm.further.toLowerCase(), text)) {
    result["Details"] = farm.further;
  }

  return result;
}

function searchOrchard(orchard, farms, searchText, full) {
  var result = {};
  if (orchard === undefined) {
    return result;
  }

  const text = searchText.toLowerCase();

  const farm = farms[orchard.farm];
  const farmResults = searchFarm(farm, text, full);
  for (const key in Object.keys(farmResults)) {
    result["Farm " + key] = farmResults[key];
  }

  if (orchard.name !== undefined && stringContainsSubstring(orchard.name.toLowerCase(), text)) {
    result["Name"] = orchard.name;
  }

  if (orchard.crop !== undefined && stringContainsSubstring(orchard.crop.toLowerCase(), text)) {
    result["Crop"] = orchard.crop;
  }

  for (const i in orchard.cultivars) {
    if (orchard.cultivars[i] !== undefined && stringContainsSubstring(orchard.cultivars[i].toLowerCase(), text)) {
      result["Cultivar"] = orchard.cultivars[i];
      break;
    }
  }

  if (orchard.irrigation !== undefined && stringContainsSubstring(orchard.irrigation.toLowerCase(), text)) {
    result["Irrigation Type"] = orchard.irrigation;
  }

  if (full && orchard.further !== undefined && stringContainsSubstring(orchard.further.toLowerCase(), text)) {
    result["Details"] = orchard.further;
  }

  return result;
}

function searchWorker(worker, searchText, full, orchards) {
  var result = {};
  if (worker === undefined) {
    return result;
  }

  const text = searchText.toLowerCase();

  const name = worker.name + " " + worker.surname;

  if (stringContainsSubstring(name.toLowerCase(), text)) {
    result["Name"] = name;
  }

  if (worker.idNumber !== undefined && stringContainsSubstring(worker.idNumber.toLowerCase(), text)) {
    result["ID"] = worker.idNumber;
  }
  if (worker.phoneNumber !== undefined && stringContainsSubstring(worker.phoneNumber.toLowerCase(), text)) {
    result["Phone Number"] = worker.phoneNumber;
  }
  if (full && worker.info !== undefined && stringContainsSubstring(worker.info.toLowerCase(), text)) {
    result["Details"] = worker.info;
  }
  if (full && worker.type !== undefined && stringContainsSubstring(worker.type.toLowerCase(), text)) {
    result["Type"] = worker.type;
  }
  if (full && worker.orchards !== undefined) {
    for (const idx in worker.orchards) {
      const orchardId = worker.orchards[idx];
      const orchard = orchards[orchardId];
      if (orchard.name !== undefined && stringContainsSubstring(orchard.name.toLowerCase(), text)) {
        result["Assigned Orchard"] = orchard.name;
      }
    }
  }

  return result;
}

function orchardAtPoint(orchards, x, y) {
  for (const orchardId in orchards) {
    const orchard = orchards[orchardId];
    var xs = [];
    var ys = [];
    for (const pointId in orchard.coords) {
      const point = orchard.coords[pointId];
      xs.push(point.lng);
      ys.push(point.lat);
    }
    if (polygonContainsPoint(xs, ys, x, y)) {
      return {value: orchard, key: orchardId};
    }
  }
  return undefined;
}

function arrayContainsEntity(array, item) {
  for (var i in array) {
    if (array[i] === item) {
      return true;
    }
  }
  return false;
}

function searchSession(session, searchText, farms, orchards, workers) {
  var result = {};

  const text = searchText.toLowerCase();

  const foreman = workers[session.wid];
  if (foreman !== undefined) {
    const foremanResults = searchWorker(foreman, text, false);
    for (const key in foremanResults) {
      result["Foreman " + key] = foremanResults[key];
    }
  }
  for (const workerId in session.collections) {
    const worker = workers[workerId];
    if (worker !== undefined) {
      const workerResults = searchWorker(worker, text, false);
      for (const key in workerResults) {
        result["Worker " + key] = workerResults[key];
      }
    }

    var orchardsForSession = [];
    var points = session.collections[workerId];
    for (const point in points) {
      const o = orchardAtPoint(orchards, point.lng, point.lat);
      if (o !== undefined && !arrayContainsEntity(orchardsForSession, o.key)) {
        orchardsForSession.push(o.key);
        const orchardsResult = searchOrchard(o, farms, text, false);
        for (const key in orchardsResult) {
          result["Orchard " + key] = orchardsResult[key];
        }
      }
    }
  }

  return result;
}
