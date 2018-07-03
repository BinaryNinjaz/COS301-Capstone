let page = 0; //0 being login, 1 being sign up

function googleLogin(){
	var provider = new firebase.auth.GoogleAuthProvider();
	provider.addScope('https://www.googleapis.com/auth/contacts.readonly');
	firebase.auth().useDeviceLanguage();
	provider.setCustomParameters({
	  'login_hint': 'user@example.com'
	});
	
	firebase.auth().signInWithPopup(provider).then(function(result) {
	var token = result.credential.accessToken;
        var user = result.user;
	document.location.href = "HomePage.html";
	}).catch(function(error) {
	  // Handle Errors here.
	  var errorCode = error.code;
	  var errorMessage = error.message;
	  var email = error.email;
	 var credential = error.credential;
	});
	
}
function onSignIn(googleUser) {
  var profile = googleUser.getBasicProfile();

	
	
	const email = profile.getEmail();
    firebase.auth().signInWithEmailAndPassword(email, password).then(function (user) { // user details correct
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


/* This function is used to make the page dynamic, in that if user presses the login button, it should render a login form rather than registration*/
function showLogin(){
	page = 0;
	document.getElementById("pageCon").innerHTML = "";
	document.getElementById("pageCon").innerHTML = "<div class='container'>"
		+'<div class="col-md-3 col-md-offset-1">'
			+'<div >'
                             +'<fieldset class="inputBlock">'
                                +'<h2>Log in to Harvest:</h2>'
                                    +'<div class="form-group">'
                                        +'<label style="text-align:left">Email</label>'
                                        +'<input type="text" class="form-control" id="username" required>'
                                    +'</div>'
                                    +'<div class="form-group">'
                                        +'<label style="text-align:left">Password</label>'
                                        +'<input type="password" class="form-control" id="password" required data-type="tooltip" title="This password has no requirements">'
                                    +'</div>'
				+'<button id="myInput" onclick="firebaseLogin()" class="btn btn-success">Log In</button>'
                                    +'<br>'
				    +'<button onclick="googleLogin()" class= "btn btn-google">Log in with Google</button>'                                    
                                    +'<br>'
					+'<button onclick="showRegister()" class="btn btn-primary">Don\'t have an account? Sign Up</button>'
				    +'<br>'
                                    +'<a  onclick="resetPassword()" href="javascript:;">Forgot password</a>'
                                +'</fieldset>'
                            +'</div>'
                    +'</div>'
	+'</div>';
}

/* This function is used to make the page dynamic, in that if user presses the registration button, it should render a registration form rather than  login*/
function showRegister(){
	page = 1;
    document.getElementById("pageCon").innerHTML = '<div class="container">'
							+'<div class="row">'
								+'<div class="col-md-3 col-md-offset-1">'
									+'<div align="center">'
										+'<fieldset class="inputBlock">'
											+'<h2>Sign up for Harvest</h2>'
											+'<div class="form-group">'
												+'<label style="text-align:left">First Name</label>'
												+'<input placeholder="" type="text" class="form-control" id="name" required>'
											+'</div>'
											+'<div class="form-group">'
												+'<label style="text-align:left">Surname</label>'
												+'<input placeholder="" type="text" class="form-control" id="surname" required>'
											+'</div>'
											+'<div class="form-group">'
												+'<label style="text-align:left">Email Address</label>'
												+'<input type="text" class="form-control" id="email" required data-type="tooltip"'
													   +'title="The email must be properly formatted, so joe@example.com">'
											+'</div>'
											+'<div class="form-group">'
												+'<label style="text-align:left">New Password</label>'
												+'<input placeholder="" type="password" class="form-control" id="password" required'
													   +'data-type="tooltip" title="There are absolutely no password requirements">'
											+'</div>'
											+'<div class="form-group">'
												+'<label style="text-align:left">Password Conformation</label>'
												+'<input placeholder="" type="password" class="form-control" id="passwordConf" required>'
											+'</div>'
											+'<div class="form-group" id="errorSpace"></div>'
											+'<button onclick="register()" class="btn btn-success">Create Account</button>'
											+'<br /> <button class="btn btn-primary" onclick="showLogin();">Have an account? Log In</button>'
										+'</fieldset>'

									+'</div>'
								+'</div>'
							+'</div>'
						+'</div>'
						}

/* This function connects to firebase, it checks if the user is already in the system - Teboho Mokoena */
function register() {
	const email = document.getElementById("email").value;
	const pass = document.getElementById("password").value;
    if(checkPass(pass, document.getElementById("passwordConf").value)){
        firebaseRegister(email, pass);
	}
}

function checkPass(pass1, pass2) {
    // if (pass1 === "" || pass2 === "") {
    //     document.getElementById("errorSpace").innerHTML = "<p class='errmsg'>Passwords cannot be empty</p>";
    //     return false;
    // }
	if(page === 0){
		return false;
	}
	if (pass1 === "" && pass2 === ""){
        document.getElementById("errorSpace").innerHTML = "<br>";
        return true;
	}
    if (pass1 !== pass2) {
        document.getElementById("errorSpace").innerHTML = "<p class='errmsg'>Passwords do not match</p>";
        document.getElementById("password").value = 0;
        document.getElementById("passwordConf").value = 0;
        return false;
    }
    if (pass1 === pass2) {
        document.getElementById("errorSpace").innerHTML = "<br>";
        return true;
    }
}

function resetPassword() {
    document.getElementById("ModalSpace").innerHTML = "" +
        "" +
        "<div id='resetModal' class='modal fade' role='dialog'>" +
        "<div class='modal-dialog'>" +
        "" +
        "<div class='modal-content'>" +
        "<div class='modal-header'>" +
        "<h4 class='modal-title'>Password Reset</h4>" +
        "</div>" +
        "<div class='modal-body'>" +
        "<div class='form-group'>" +
        "<label class='control-label'>Please enter your email address so we can send you instructions on resetting your password</label> " +
        "<input class='form-control' type='email' id='emailAddress'></div>" +
        "</div>" +
        "<div class='modal-footer'>" +
        "<div class='col-sm-2 col-sm-offset-5' style='padding: 2px'><button type='button' class='btn btn-warning' data-dismiss='modal'>Cancel</button></div>" +
        "<div class='col-sm-5' style='padding: 2px'><button type='button' class='btn btn-success' data-dismiss='modal' onclick='sendPasswordResetEmail(document.getElementById(\"emailAddress\").value)'>Send Password Reset Email</button></div>" +
        "</div>" +
        "</div>" +
        "" +
        "</div>" +
        "</div>"
    ;

    $('#resetModal  ').modal('show');
}


/* This section of code was added by Vincent, to listen for the Enter shortcut on the keyboard*/
var input = document.getElementById("password");
input.addEventListener("keyup", function(event) {
    event.preventDefault();
    if (event.keyCode === 13) {
        document.getElementById("myInput").click();
    }
});
/* What Vincent Added on 04/05/2018 ends here. */














