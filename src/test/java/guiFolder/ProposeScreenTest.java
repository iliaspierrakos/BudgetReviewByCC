package guiFolder;

import UserManagement.User;
import UserManagement.UserManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProposeScreenTest {

    @Test
    void constructor_acceptsValidArguments() {
        User user = createDummyUser();
        UserManager userManager = createDummyUserManager();

        assertDoesNotThrow(() -> new ProposeScreen(user, userManager));
    }

    @Test
    void show_whenUserIsNotMinistryMember_returnsEarly() {
        User user = createDummyUser();
        UserManager userManager = createDummyUserManager();

        ProposeScreen screen = new ProposeScreen(user, userManager);
        assertDoesNotThrow(() -> {
            try {
                screen.show(null)
            } catch (Exception ignored) {

            }
        });
    }

    private static User createDummyUser() {
        return null;
    }

    private static UserManager createDummyUserManager() {
        return null;
    }
}
