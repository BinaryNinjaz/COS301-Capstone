package za.org.samac.harvest;
import android.widget.EditText;

import org.junit.Test;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class InputValidationTest {
    String correctEmail = "ramsey@gmail.com";
    String correctPassword = "ramsey";
    String correctFirstName = "Aaron";
    String correctSurname = "Ramsey";

    @Test
    public void loginValidator_CorrectFields_ReturnsTrue() {
        assertThat(SignIn_Farmer.validateForm(correctEmail, correctPassword), is(true));
    }
}