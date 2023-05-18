import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChatServerTest {

    @Test
    void testSendMessage() {
        ChatServer chatServer = new ChatServer();

        // Test case 1: Sending a message to a valid recipient
        String sender = "Desiree";
        String recipient = "Daniel";
        String message = "Hello, Daniel!";
        assertTrue(chatServer.MessageSent(message));

        // Test case 2: Sending a message to an invalid recipient
        String invalidRecipient = "Charlie";
        assertFalse(chatServer.MessageSent(message));
    }
    @Test
    void testGetUserCount() {
        ChatServer chatServer = new ChatServer();

        // Test case 1: No users connected
        assertEquals(0, chatServer.getUserCount());

        // Test case 2: One user connected
        chatServer.connectUser("Desiree");
        assertEquals(1, chatServer.getUserCount());

        // Test case 3: Multiple users connected
        chatServer.connectUser("Daniel");
        chatServer.connectUser("JJ");
        assertEquals(3, chatServer.getUserCount());

//         Test case 4: Disconnecting users
        chatServer.disconnectUser("Desiree");
        assertEquals(2, chatServer.getUserCount());
        chatServer.disconnectUser("Daniel");
        assertEquals(1, chatServer.getUserCount());
        chatServer.disconnectUser("JJ");
        assertEquals(0, chatServer.getUserCount());
    }
}
