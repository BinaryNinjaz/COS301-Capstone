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

//var groupBy = ''; /* grouping variable */
//var period = '';	/* period time space variable */
//var startDate = '';	/* Begin date variable */
//var endDate = '';	/* End date variable */
//var id0 = '';	/* user ID variable */

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
//also draws initial graphs on start up
function initPage(){
    initOrchards(); //This function initiates the orchards immediately when the analytics page is accessed (for selction)
    initWorkers();//This function initiates the workers immediately when the analytics page is accessed (for selection)

    //Vincent started working on this function from here
    var ctx = document.getElementById("myChart").getContext('2d');
    var myChart = new Chart(ctx, {
            type: 'radar',
            data: {
                    //The size of the labels array will depend on the start date and the end date.
                    //The labels will be the dates, from the specified start till the specified end.
                    labels: ['Sunday','Monday','Tuesday','Wednesday','Thursady','Friday','Saturday'], //This will contain the dates plotted on each point. (give me the dates)
                    datasets: [{
                    label: "Number of Bags p/day", //These are the number of bags per day since the start date and the end date

                    //The following values in the data array are the number of bags collected each day. from start date to end date
                    data: [4, 0, 5, 0, 0, 0, 4], //The size of this will also depend on the start and the end date
                    backgroundColor: '#4CAF50' //Color of the area 
            }]
            }
    });
    
    google.charts.load("current", {packages:["corechart"]});
    google.charts.setOnLoadCallback(drawChart);
    function drawChart() {
      var data = google.visualization.arrayToDataTable([
        ["Period", "Number of Bags", { role: "style" } ], //This line explains the format of the array object
        ["06:00 - 07:00", 8, "#4CAF50"], //[label: which is the period of 1 hour, Number of bags, color of bar]
        ["07:00 - 08:00", 6, "#4CAF50"],
        ["08:00 - 09:00", 3, "#4CAF50"],
		["09:00 - 10:00", 7, "#4CAF50"],
        ["10:00 - 11:00", 5, "#4CAF50"],
        ["11:00 - 12:00", 9, "#4CAF50"],
		["12:00 - 13:00", 7, "#4CAF50"],
        ["13:00 - 14:00", 7, "#4CAF50"],
        ["14:00 - 15:00", 9, "#4CAF50"],
        ["15:00 - 16:00", 8, "#4CAF50"],
		["16:00 - 17:00", 11, "#4CAF50"],
        ["17:00 - 18:00", 15, "#4CAF50"]
      ]);

      var view = new google.visualization.DataView(data);
      view.setColumns([0, 1,
                       { calc: "stringify",
                         sourceColumn: 1,
                         type: "string",
                         role: "annotation" },
                       2]);
      var nameOfWorker; //This has to be assigned the name of the worker/the id ow the worker
      var options = {
        title: "Number of bags collected per hour by Teboho Mokoena", //Teboho Mokoena will be replaced with 'nameOfWorker' variable
        width: 1200, //Setting the width 
        height: 500, //Setting the height
        bar: {groupWidth: "95%"}, //This is the grouping width of the bar graph
        legend: { position: "none" }, //This will be determined by the UX designer
      };
      var chart = new google.visualization.BarChart(document.getElementById("curve_chart"));
      chart.draw(view, options);
  }
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

//this function returns the date of the monday of a given week
function getDateOfISOWeek(w, y) {
    var simple = new Date(y, 0, 1 + (w - 1) * 7);
    var dow = simple.getDay();
    var ISOweekStart = simple;
    if (dow <= 4){
        ISOweekStart.setDate(simple.getDate() - simple.getDay() + 1);
    }else{
        ISOweekStart.setDate(simple.getDate() + 8 - simple.getDay());
    }return ISOweekStart;
}

//takes information chosen by user for orchard filter to pass to orchard performance function
function filterOrchard(){
    var name = document.getElementById('orchardSelect').value;
    var week = document.getElementById('weekSelect').value; //format e.g: 2018-W17
    if(name!== '' && week!==''){
        var y = week.substring(0,4);
        var w = week.substring(6,8);
        var start = new Date(getDateOfISOWeek(w, y));
        var end = new Date(start.getFullYear(),start.getMonth(),start.getDate()+6);
        var id = getOrchardId(name);
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
//date to test function 2018/
function filterWorker(){
    var name = document.getElementById('workerSelect').value;
    var date = document.getElementById('workerDateSelect').value;
    if(name!== '' && date!== ''){
        var start = new Date(date);
        var end=new Date(date);
        end.setHours(18);
        end.setMinutes(0);
        start.setHours(6);
        start.setMinutes(0);
        var id = getWorkerId(name);
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

//converts a date to seconds since epoch
function dateToSeconds(date){ return date.getTime() / 1000 ; }

//starts post request for orchard
function orchardPerformance(start, end, id){
   const groupBy = 'orchard';
   const period = 'daily';
   const startDate = dateToSeconds(start);
   const endDate = dateToSeconds(end);
   //baseUrl is set to 'https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/timedGraphSessions'
   var keys = {};
   keys.id0 = id;
   keys.groupBy = groupBy;
   keys.period = period;
   keys.startDate = startDate;
   keys.endDate = endDate;
   keys.uid = userID();
   $.post(baseUrl, keys, (data, status) => {
        alterGraph(data,'orchard');
    });
}

//starts post request for worker
function workerPerformance(start, end, id){
   const groupBy = 'worker';
   const period = 'hourly';
   const startDate = dateToSeconds(start);
   const endDate = dateToSeconds(end);
   //baseUrl is set to 'https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/timedGraphSessions'
   var keys = {};
   keys.id0 = id;
   keys.groupBy = groupBy;
   keys.period = period;
   keys.startDate = startDate;
   keys.endDate = endDate;
   keys.uid = userID();
   $.post(baseUrl, keys, (data, status) => {
        alterGraph(data,'worker');
    });
}

//updates specific graph based on user input
function alterGraph(response,str){
    if(str==='orchard'){ //alter orchard graph
        console.log(response);
    }else if(str==='worker'){ //alter worker graph
        console.log(response);
    }else{ // can use this to make graphs blank (implemented later)
        
    }
}