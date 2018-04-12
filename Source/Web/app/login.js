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