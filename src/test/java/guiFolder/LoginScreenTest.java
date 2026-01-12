package guiFolder;

import UserManagement.UserManager;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class LoginScreenTest {

    @Test
    void constructor_nullUserManager_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new LoginScreen(null));
    }

    @Test
    void attemptLogin_emptyUsername_setsErrorMessage() throws Exception {
        UserManager userManager = new DummyUserManager();
        LoginScreen loginScreen = new LoginScreen(userManager);

        TextField usernameField = new TextField("   ");
        PasswordField passwordField = new PasswordField();
        passwordField.setText("pass");
        Label errorLabel = new Label();

        call_attemptLogin(loginScreen, usernameField, passwordField, errorLabel);

        assertEquals("Username is required.", errorLabel.getText());
    }

    @Test
    void attemptLogin_emptyPassword_setsErrorMessage() throws Exception {
        UserManager userManager = new DummyUserManager();
        LoginScreen loginScreen = new LoginScreen(userManager);

        TextField usernameField = new TextField("user");
        PasswordField passwordField = new PasswordField();
        passwordField.setText("");
        Label errorLabel = new Label();

        call_attemptLogin(loginScreen, usernameField, passwordField, errorLabel);

        assertEquals("Password is required.", errorLabel.getText());
    }

    private void call_attemptLogin(
            LoginScreen screen,
            TextField usernameField,
            PasswordField passwordField,
            Label errorLabel
    ) throws Exception {
        Method m = LoginScreen.class.getDeclaredMethod(
                "attemptLogin",
                javafx.stage.Stage.class,
                TextField.class,
                PasswordField.class,
                Label.class
        );
        m.setAccessible(true);
        m.invoke(screen, null, usernameField, passwordField, errorLabel);
    }

    static class DummyUserManager extends UserManager {
        @Override
        public UserManagement.User loginUser(String username, String password) {
            fail("loginUser should NOT be called when fields are empty");
            return null;
        }
    }
}
