const functions = require('firebase-functions');
const admin = require('firebase-admin');
//const cors = require('cors')({origin: true});
admin.initializeApp();

// ?startDate=[Double]&endDate=[Double]&uid=[String]
exports.sessionsWithinDates = functions.https.onRequest((req, res) => {
  const startDate = req.query.startDate;
  const endDate = req.query.endDate;
  const uid = req.query.uid;
  
  var result = {};
  
  var sessions = admin.database().ref('/' + uid + '/sessions');
  sessions.once('value').then((snapshot) => {
    snapshot.forEach((childSnapshot) => {
      const key = childSnapshot.key;
      const val = childSnapshot.val();
      
      if (startDate <= val.start_date && val.start_date <= endDate) {
        result[key] = val;
      }
    });
    res.send(result);
    return true;
  }).catch((error) => {
    
  });
});

// ?pageNo=[Int]&pageSize=[Int? = 50]&uid=[String]
exports.flattendSessions = functions.https.onRequest((req, res) => {
  const pageNo = req.query.pageNo;
  var pageSize = req.query.pageSize;
  if (pageSize === undefined) {
    pageSize = 50;
  }
  const uid = req.query.uid;
  
  var result = [];
  var count = 0;
  var sessions = admin.database().ref('/' + uid + '/sessions').orderByChild('start_date');
  sessions.once('value').then((snapshot) => {
    const total = snapshot.numChildren();
    
    snapshot.forEach((childSnapshot) => {
      if (count >= total - (pageNo - 1) * pageSize) {
        res.send(result);
        return;
      } else if (count >= total - pageNo * pageSize) {
        const key = childSnapshot.key;
        const val = childSnapshot.val();
        result.unshift({key: key, startDate: val.start_date});
      } 
      count++;
    });
    res.send(result);
    return true;
  }).catch((error) => {
    
  });
});
