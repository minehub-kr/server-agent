package kr.minehub.servers.agent.websocket;

import com.neovisionaries.ws.client.WebSocket;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConnectSessionTest {
    @Test
    void suppressedReconnectDoesNotLeaveConnectingFlagSet() throws Exception {
        ConnectSession session = new ConnectSession(null);
        session.ws = mock(WebSocket.class);
        session.setPreventReconnect(true);

        assertNull(session.connect());
        assertFalse(session.isConnecting());
    }

    @Test
    void uncheckedConnectionFailureClearsConnectingFlag() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getLogger).thenReturn(Logger.getAnonymousLogger());
            ConnectSession session = new ConnectSession(null);

            assertThrows(NullPointerException.class, session::connect);

            assertFalse(session.isConnecting());
        }
    }

    @Test
    void explicitReconnectDisconnectsOldSocketAndReenablesConnecting() throws Exception {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getLogger).thenReturn(Logger.getAnonymousLogger());
            ConnectSession session = spy(new ConnectSession(null));
            WebSocket oldSocket = mock(WebSocket.class);
            when(oldSocket.isOpen()).thenReturn(true);
            session.ws = oldSocket;
            session.setPreventReconnect(true);
            doReturn(mock(WebSocket.class)).when(session).connect();

            session.forceReconnect();

            verify(oldSocket).disconnect();
            verify(session).connect();
            assertFalse(session.preventReconnect);
            assertFalse(session.isConnecting());
        }
    }
}
