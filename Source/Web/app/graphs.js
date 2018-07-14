/* Vincent Added comments to the following code below */
const baseUrl = 'https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/timedGraphSessions';
const database = firebase.database();	/* Pointing to database on firebase cloud */
const user = function() { return firebase.auth().currentUser }; /* Function which authenticates user */

/* Function returns the user ID of the selected user */
const userID = function() {
  if (user() !== null) {
    return user().uid ;
  } else {
    return "";
  }
}

var foremen = []; /* Array containing a list of Foremen names */
var workers = []; /* Array containing a list of workers names */
var orchards = []; /* Array containing a list of Orchard names */

/*firebase.auth().onAuthStateChanged(function (user) {
  if (user) {
    $(window).bind("load", function() {
      initPage();
    });
  }
});*/

/* Function returns a pointer to the list of workers of the particular user ID */
function workersRef() {
  return database.ref('/' + userID()  + '/workers');
}

/* Function returns a pointer to the list of orchards of the particular user ID */
function orchardsRef() {
  return database.ref('/' + userID()  + '/orchards');
}

/* Function returns a worker pointed to by the callback parameter */
function getWorkers(callback) {
  const ref = firebase.database().ref('/' + userID() + '/workers');
  ref.once('value').then((snapshot) => {
    callback(snapshot);
  });
}

/* Function returns an orchard pointed to by the callback parameter */
function getOrchards(_callback) {
  const ref = firebase.database().ref('/' + userID() + '/orchards');
  ref.once('value').then((snapshot) => {
    _callback(snapshot);
  });
}

/* Function returns a foremen, given a particular key */
function foremanForKey(key) {
  for (var k in foremen) {
    if (foremen[k].key === key) {
      return foremen[k];
    }
  }
  return {value: {name: "Farm", surname: "Owner"}}; //The return value is a JSON object
}

/* Function returns a worker, given a particular key */
function workerForKey(key) {
  for (var k in workers) {
    if (workers[k].key === key) {
      return workers[k];
    }
  }
  return undefined;
}

//calls functions that populate the drop down lists and worker/orchard arrays
function initPage(){
    initOrchards(); //This function initiates the orchards immediately when the analytics page is accessed (for selction)
    initWorkers();	//This function initiates the workers immediately when the analytics page is accessed (for selection)
}

/* This function loads all available orchards in the database, for graph filtering */
function initOrchards(){
    var orchardSelect = document.getElementById('orchardSelect');
    getOrchards((orchardsSnap) => {
        orchards=[];
        orchardsSnap.forEach((orchard) => {
          const val = orchard.val();
          const k = orchard.key;
          orchards.push({key: k, value: val});
          var option = document.createElement("option");
          option.text = val.name;
          orchardSelect.options.add(option);
        });
    }); 
}

/* This function loads all available workers in the database, for graph filtering */
function initWorkers(){
   var workerSelect = document.getElementById('workerSelect');
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
            var wName = w.name + ' ' + w.surname;
            var option = document.createElement("option");
            option.text = wName;
            workerSelect.options.add(option);
          }
        });
    });
}

//takes information chosen by user for orchard filter to pass to orchard performance function
function filterOrchard(){
    var name = document.getElementById('orchardSelect').value;
    var start = document.getElementById('startDateSelect').value;
    var end = document.getElementById('endDateSelect').value;
    if(name!== '' && start!== '' && end !== ''){
        var id = getOrchardId(orchardName);
        orchardPerformance(start, end, id);
    }else{
        window.alert("Some fields in the orchard filter appear to be blank. \n"
        +"Please enter them to continue."); //Appropriate error message when loading fails
    }
}

/* This function returns the Orchard ID when given the name of the orchard */
function getOrchardId(name){
    var id='';
    for (var k in orchards) {
        if(name===orchards[k].value.name){
            return orchards[k].value.name;
        }
    }
    return id;
}

//takes information chosen by user for worker filter to pass to worker performance function
function filterWorker(){
    var name = document.getElementById('workerSelect').value;
    var date = document.getElementById('workerDateSelect').value;
    //doing something here to get start and end variables from date
    var start='';
    var end ='';
    if(name!== '' && date!== ''){
        var id = getWorkerId(orchardName);
        workerPerformance(start, end, id);
    }else{
        window.alert("Some fields in the worker filter appear to be blank. \n"
        +"Please enter them to continue."); //Appropriate error message when loading of workers fails 
    }
}

/* This function returns the worker ID, given the name of a particular worker */
function getWorkerId(name){
    var id='';
    for (var k in workers) {
        var fullname = workers[k].value.name+' '+workers[k].value.surname;
        if(name===fullname){
            return workers[k].key;
        }
    }
    return id;
}

var groupBy; /* grouping variable */
var period;	/* period time space variable */
var startDate;	/* grouping variable */
var endDate;	/* grouping variable */
var uid;	/* grouping variable */

//converts a date to seconds since epoch
function dateToSeconds(date){ return Math.floor( date.getTime() / 1000 ) }

//updates orchard graph based on user input
//still needs further implemention
function orchardPerformance(start, end, id){
   groupBy = 'orchard';
   period = 'daily';
   startDate = dateToSeconds(start);
   endDate = dateToSeconds(end);
   uid = id;
   var params = constructParams(groupBy,period,startDate,endDate,uid);
   var response = sendPostRequest(params);
   //implementation will go here
}

//updates worker graph based on user input
//still needs further implemention
function workerPerformance(start, end, id){
   groupBy = 'worker';
   period = 'hourly';
   startDate = dateToSeconds(start);
   endDate = dateToSeconds(end);
   uid = id;
   var params = constructParams(groupBy,period,startDate,endDate,uid);
   var response = sendPostRequest(params);
   //implementation will go here
}

//creates the parameter string for the http post request
function constructParams(groupBy,period,startDate,endDate,uid){
    var params = '';
    params = params +'groupBy='+groupBy;
    params = params +'&period='+period;
    params = params +'&startDate='+startDate;
    params = params +'&endDate='+endDate;
    params = params +'&uid='+uid;
    return params;
}

function sendPostRequest(params){
    var http = new XMLHttpRequest();
    http.open('POST', baseUrl, true);
    
    http.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');

    http.onreadystatechange = function() {//Call a function when the state changes
        if(http.readyState == 4 && http.status == 200) {
            return http.responseText;
        }
    }
    http.send(params);
}