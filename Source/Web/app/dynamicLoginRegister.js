var page = 0; //0 being login, 1 being sign up
var contents=document.getElementById("pageCon");
var navBar = document.getElementsById("navHighlight");
function showLogin(){
	contents.innerHTML = "<div class='container'>"           
							+"<div class='row'>"
								+"<div class='col-md-3 col-md-offset-1'>"
									+"<div align='center'>"
										+"<fieldset class='inputBlock'>"
												+"<h2>Login to Harvest:</h2>"
													+"<div class='form-group'>"
														+"<label style='text-align:left'>Username/Email</label>"
														+"<input type='text' class='form-control' id='username' required>"
													+"</div>"
													+"<div class='form-group'>"
														+"<label style='text-align:left'>Password</label>"
														+"<input type='password' class='form-control' id='password' required>"
													+"</div>"
													
													+"<button onclick='firebaseLogin()' class='btn btn-success'>Log In</button>"
													+"<br><br>"  
													+"<a  onclick='#'>Forgot password</a>"          
										
																									
										+"</fieldset>"
									+"</div>"
								+"</div>"
							+"</div>"
						+"</div>";
	
}

function showRegister(){
	contents.innerHTML = '<div class="container">'
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
											+'</div'
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
											+'</div'
											+'<div class="form-group" id="errorSpace"><br></div>'
											+'<button onclick="register()" class="btn btn-success">Create Account</button>'
											
										+'</fieldset>'
										
									+'</div>'
								+'</div>'
							+'</div>'
						+'</div>'
}
