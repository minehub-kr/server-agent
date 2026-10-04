package kr.minehub.servers.agent.websocket;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;
import kr.minehub.servers.agent.Main;
import kr.minehub.servers.agent.api.MinehubAPI;
import kr.minehub.servers.agent.api.MinehubServer;
import kr.minehub.servers.agent.api.auth.MinehubAuthorization;
import kr.minehub.servers.agent.core.AgentCore;
import java.net.URI;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

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

    @Test
    void shutdownDuringHandshakeClosesLateConnection() throws Exception {
        AgentCore previous = Main.core;
        MinehubServer server = mock(MinehubServer.class);
        when(server.getServerId()).thenReturn("test-server");
        ConnectSession session = new ConnectSession(server);
        WebSocket socket = mock(WebSocket.class);
        when(socket.connect()).thenAnswer(invocation -> {
            session.disconnect();
            return socket;
        });
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
             MockedStatic<MinehubAPI> api = mockStatic(MinehubAPI.class);
             MockedConstruction<WebSocketFactory> factories = mockConstruction(WebSocketFactory.class,
                     (factory, context) -> when(factory.createSocket(any(URI.class))).thenReturn(socket))) {
            bukkit.when(Bukkit::getLogger).thenReturn(Logger.getAnonymousLogger());
            api.when(MinehubAPI::getHostname).thenReturn("localhost");
            Main.core = mock(AgentCore.class);
            Main.core.authorization = mock(MinehubAuthorization.class);
            when(Main.core.authorization.getAccessToken()).thenReturn("test-token");

            assertNull(session.connect());

            verify(socket).disconnect();
            assertNull(session.ws);
            assertFalse(session.isConnecting());
        } finally {
            Main.core = previous;
        }
    }
}
