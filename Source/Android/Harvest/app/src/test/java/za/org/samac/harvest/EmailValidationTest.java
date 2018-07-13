package za.org.samac.harvest;
import android.widget.EditText;

import org.junit.Test;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class EmailValidationTest {
    @Test
    public void emailValidator_CorrectEmailSimple_ReturnsTrue() {
        assertThat(LoginActivity.validateForm("a@gmail.com", "a"), is(true));
    }
}