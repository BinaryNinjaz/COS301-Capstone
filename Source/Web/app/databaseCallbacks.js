const user = function() { return firebase.auth().currentUser };
const userID = function() { 
  if (user() !== null) {
    return user().uid
  } else {
    return ""
  }
}

function getWorkers(callback) {
  const ref = firebase.database().ref('/' + userID() + '/workers');
  ref.once('value').then((snapshot) => {
    callback(snapshot);
  });
}