package guiFolder;

import UserManagement.User;
import UserManagement.UserManager;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class RegisterScreenTest {


    @Test
    void updateDisable_disablesWhenUsernameMissing() throws Exception {
        Button registerButton = new Button();
        TextField usernameField = new TextField("   ");
        PasswordField passwordField = new PasswordField();
        passwordField.setText("123456");
        ComboBox<String> ministryBox = new ComboBox<>();

        Supplier<User.Role> getRole = () -> User.Role.CITIZEN;

        call_updateDisable(registerButton, usernameField, passwordField, getRole, ministryBox);

        assertTrue(registerButton.isDisable());
    }

    @Test
    void updateDisable_disablesWhenPasswordMissing() throws Exception {
        Button registerButton = new Button();
        TextField usernameField = new TextField("user");
        PasswordField passwordField = new PasswordField();
        passwordField.setText("");
        ComboBox<String> ministryBox = new ComboBox<>();

        Supplier<User.Role> getRole = () -> User.Role.CITIZEN;

        call_updateDisable(registerButton, usernameField, passwordField, getRole, ministryBox);

        assertTrue(registerButton.isDisable());
    }

    @Test
    void updateDisable_disablesWhenRoleIsNull() throws Exception {
        Button registerButton = new Button();
        TextField usernameField = new TextField("user");
        PasswordField passwordField = new PasswordField();
        passwordField.setText("123456");
        ComboBox<String> ministryBox = new ComboBox<>();

        Supplier<User.Role> getRole = () -> null;

        call_updateDisable(registerButton, usernameField, passwordField, getRole, ministryBox);

        assertTrue(registerButton.isDisable());
    }

    @Test
    void updateDisable_enablesForCitizenWhenFieldsOk() throws Exception {
        Button registerButton = new Button();
        TextField usernameField = new TextField("user");
        PasswordField passwordField = new PasswordField();
        passwordField.setText("123456");
        ComboBox<String> ministryBox = new ComboBox<>();

        Supplier<User.Role> getRole = () -> User.Role.CITIZEN;

        call_updateDisable(registerButton, usernameField, passwordField, getRole, ministryBox);

        assertFalse(registerButton.isDisable());
    }

    @Test
    void updateDisable_ministryMemberNeedsMinistrySelection() throws Exception {
        Button registerButton = new Button();
        TextField usernameField = new TextField("user");
        PasswordField passwordField = new PasswordField();
        passwordField.setText("123456");
        ComboBox<String> ministryBox = new ComboBox<>();
        ministryBox.setValue(null);

        Supplier<User.Role> getRole = () -> User.Role.MINISTRYMEMBER;

        call_updateDisable(registerButton, usernameField, passwordField, getRole, ministryBox);
        assertTrue(registerButton.isDisable());

        ministryBox.setValue("Ministry of Health");
        call_updateDisable(registerButton, usernameField, passwordField, getRole, ministryBox);
        assertFalse(registerButton.isDisable());
    }

    @Test
    void setShown_setsVisibleAndManaged() throws Exception {
        TextField node = new TextField();

        call_setShown(node, false);
        assertFalse(node.isVisible());
        assertFalse(node.isManaged());

        call_setShown(node, true);
        assertTrue(node.isVisible());
        assertTrue(node.isManaged());
    }

    @Test
    void attemptRegister_emptyUsername_setsErrorMessage() throws Exception {
        RegisterScreen screen = new RegisterScreen(new DummyUserManager());

        TextField usernameField = new TextField("   ");
        PasswordField passwordField = new PasswordField();
        passwordField.setText("123456");

        ComboBox<String> ministryBox = new ComboBox<>();
        Label errorLabel = new Label();

        Supplier<User.Role> getRole = () -> User.Role.CITIZEN;

        call_attemptRegister(screen, usernameField, passwordField, ministryBox, getRole, errorLabel);

        assertEquals("Username is required.", errorLabel.getText());
    }

    @Test
    void attemptRegister_shortPassword_setsErrorMessage() throws Exception {
        RegisterScreen screen = new RegisterScreen(new DummyUserManager());

        TextField usernameField = new TextField("user");
        PasswordField passwordField = new PasswordField();
        passwordField.setText("123"); // < 6

        ComboBox<String> ministryBox = new ComboBox<>();
        Label errorLabel = new Label();

        Supplier<User.Role> getRole = () -> User.Role.CITIZEN;

        call_attemptRegister(screen, usernameField, passwordField, ministryBox, getRole, errorLabel);

        assertEquals("Password must be at least 6 characters.", errorLabel.getText());
    }

    @Test
    void attemptRegister_ministryMemberWithoutMinistry_setsErrorMessage() throws Exception {
        RegisterScreen screen = new RegisterScreen(new DummyUserManager());

        TextField usernameField = new TextField("user");
        PasswordField passwordField = new PasswordField();
        passwordField.setText("123456");

        ComboBox<String> ministryBox = new ComboBox<>();
        ministryBox.setValue(null);

        Label errorLabel = new Label();
        Supplier<User.Role> getRole = () -> User.Role.MINISTRYMEMBER;

        call_attemptRegister(screen, usernameField, passwordField, ministryBox, getRole, errorLabel);

        assertEquals("Please select a ministry.", errorLabel.getText());
    }

    private void call_updateDisable(
            Button registerButton,
            TextField usernameField,
            PasswordField passwordField,
            Supplier<User.Role> getRole,
            ComboBox<String> ministryBox
    ) throws Exception {
        Method m = RegisterScreen.class.getDeclaredMethod(
                "updateDisable",
                Button.class,
                TextField.class,
                PasswordField.class,
                Supplier.class,
                ComboBox.class
        );
        m.setAccessible(true);
        m.invoke(null, registerButton, usernameField, passwordField, getRole, ministryBox);
    }

    private void call_setShown(javafx.scene.Node node, boolean shown) throws Exception {
        Method m = RegisterScreen.class.getDeclaredMethod("setShown", javafx.scene.Node.class, boolean.class);
        m.setAccessible(true);
        m.invoke(null, node, shown);
    }

    private void call_attemptRegister(
            RegisterScreen screen,
            TextField usernameField,
            PasswordField passwordField,
            ComboBox<String> ministryBox,
            Supplier<User.Role> getRole,
            Label errorLabel
    ) throws Exception {
        Method m = RegisterScreen.class.getDeclaredMethod(
                "attemptRegister",
                javafx.stage.Stage.class,
                TextField.class,
                PasswordField.class,
                ComboBox.class,
                Supplier.class,
                Label.class
        );
        m.setAccessible(true);
        m.invoke(screen, null, usernameField, passwordField, ministryBox, getRole, errorLabel);
    }


    static class DummyUserManager extends UserManager {
        @Override
        public boolean registerUser(String username, String password, User.Role role) {
            fail("registerUser should not be called in these validation tests");
            return false;
        }

        @Override
        public boolean registerUser(String username, String password, User.Role role, String ministry) {
            fail("registerUser should not be called in these validation tests");
            return false;
        }
    }
}
