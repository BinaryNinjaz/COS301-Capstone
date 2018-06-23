const functions = require('firebase-functions');
const admin = require('firebase-admin');
//const cors = require('cors')({origin: true});
admin.initializeApp();

// Create and Deploy Your First Cloud Functions
// https://firebase.google.com/docs/functions/write-firebase-functions

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
