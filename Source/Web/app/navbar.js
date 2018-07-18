window.onload = function () {
	let succ = () => {
		name = firebase.auth().currentUser.email;
		document.getElementById("emailInNav").innerHTML = name + " <span class=\"glyphicon glyphicon-cog\"></span>";
	};
	let fail = () => {
		
	};
	retryUntilTimeout(succ, fail, 1000);
};