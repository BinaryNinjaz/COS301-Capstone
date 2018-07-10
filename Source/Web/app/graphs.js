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

function yieldsRef() {
  return database.ref('/' + userID() + '/sessions');
}

firebase.auth().onAuthStateChanged(function (user) {
  if (user) {
    $(window).bind("load", function() {
      getWorkers();
    });
  } else {
    sessions = [];
  }
});

function workersRef() {
  return database.ref('/' + userID()  + '/workers');
}

var foremen = [];
var workers = [];
function getWorkers() {
  workersRef().once('value').then(function (snapshot) {
      snapshot.forEach(function (worker) {
        const w = worker.val();
        const k = worker.key;
        if (w.type === "Foreman") {
          foremen.push({key: k, value: w});
        } else {
          workers.push({key: k, value: w});
        }
      });
  });
}

function getCollectionsRef(sessionKey) {
  return database.ref('/' + userID() + '/sessions/'+
                sessionKey+'/collections');
}

function workerCollections(sessionKey,workerKey) {
  return database.ref('/' + userID() + '/sessions/'+
                sessionKey+'/collections/'+workerKey);
}


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

function initPage(){
    getWorkers();
    loadSessions();
    setTimeout(function(){ test(); },3000);
    //calculateBagsPerHour();
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
          var childData = child.val();
          sessionKey = child.key;
          var startDate = new Date(childData.start_date * 1000);
          var endDate = new Date(childData.end_date * 1000);
          sessions.push({start: startDate, end: endDate});  
          collections.push(populateCollection(sessionKey));
          console.log(collections[sessionCount]);
          ++sessionCount;
      });
  });
}

/*
Object returned from the function below will contain all of the workers' names of
a specific session and all the dates of the collectons that the workers made 
*/
function populateCollection(sessionKey) {
  var obj = [];
  const collectionsRef = getCollectionsRef(sessionKey);
  collectionsRef.once('value').then(function (snapshot) {
      snapshot.forEach(function (child) {
        var childData = child.val();
        workerKey = child.key;
        const worker = workerForKey(workerKey);
        if (worker !== undefined) {
            const name = worker.value.name + " " + worker.value.surname;
            var collectionDates = [];
            const workerCollRef = workerCollections(sessionKey,workerKey);
            var count = 0;
            workerCollRef.once('value').then(function (snapshot2) {
                snapshot2.forEach(function (child2) {
                    var childData2 = child2.val();
                    collectionDates[count] = {};
                    collectionDates[count] = new Date(childData2.date * 1000);
                    ++count;
                });
            });
            //console.log(obj);
            obj.push({worker: name, coll: collectionDates});
            //console.log(obj);
        }else{
            console.log("Worker is undefined..");
        }
      });
  }); 
  return obj;
}

var totalBagsPerHour = [];
var workerBagsPerHour = [];
function calculateBagsPerHour(){
   //yet to be implemented 
}

function test(){
    var count = 0;
    console.log("test..");
    for(var x in collections){
        var string = "Collection number: "+count+"\n";
        console.log(collections[x]);
        console.log(x);
        console.log(x[count]);
        for(var i = 0; i < x.length; i++){
            var temp = "Worker: "+x[i].worker+" Dates are below: \n";
            //console.log(temp);
            for(var d in x[i].coll){
                 temp+=d+" ";   
            }
            string+=temp;
        }
        //window.alert(string);
        ++count;
    }
}