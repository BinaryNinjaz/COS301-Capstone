//const baseUrl = 'https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/flattendSessions?';
//var pageNo = 0;
//var pageSize = 8;
const database = firebase.database();
const user = function() { return firebase.auth().currentUser };
const userID = function() {
  if (user() !== null) {
    return user().uid ;
  } else {
    return "";
  }
}

function getWorkers(callback) {
  const ref = firebase.database().ref('/' + userID() + '/workers');
  ref.once('value').then((snapshot) => {
    callback(snapshot);
  });
}

function yieldsRef() {
  return firebase.database().ref('/' + userID() + '/sessions');
}

firebase.auth().onAuthStateChanged(function (user) {
  if (user) {
    $(window).bind("load", function() {
      initPage();
    });
  } else {
    sessions = [];
  }
});

function foremanForKey(key) {
  for (var k in foremen) {
    if (foremen[k].key === key) {
      return foremen[k];
    }
  }
  return {value: {name: "Farm", surname: "Owner"}};
}

function workerForKey(key) {
  for (var k in workers) {
    if (workers[k].key === key) {
      return workers[k];
    }
  }
  return undefined;
}

var foremen = [];
var workers = []
function initPage() {
  getWorkers((workersSnap) => {
    foremen = [];
    workers = [];
    workersSnap.forEach((worker) => {
      const w = worker.val();
      const k = worker.key;
      if (w.type === "Foreman") {
        foremen.push({key: k, value: w});
      } else {
        workers.push({key: k, value: w});
      }
    });
    loadSessions();
  });
}

/*
The collections for a specific session are linked to a session based on its index
For example: sessions[0] has its collection data at collections[0]
- sessionCount therefore counts the number of collections as well
*/
var sessions = [];
var collections = [];
var sessionCount = 0;
function loadSessions() {
  yieldsRef().once('value').then(function (snapshot) {
      snapshot.forEach(function (child) {
          sessionKey = child.key;
          collectionsRef = yieldsRef()+'/'+sessionKey+'/collections';
          sessions[sessionCount].start = child.val().start_date;
          sessions[sessionCount].end = child.val().end_date;
          collections[sessionCount] = populateCollection(collectionsRef);
          ++sessionCount;
      });
  });
}

function populateCollection(ref) {
  ref().once('value').then(function (snapshot) {
      
  });  
}