package kr.minehub.servers.agent.websocket;

import com.neovisionaries.ws.client.WebSocket;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SessionListenerTest {
    @Test
    void returnsParseableErrorWithOriginalActionAndRecipient() throws Exception {
        SessionListener listener = new SessionListener(null);
        listener.handler = mock(CommandHandler.class);
        when(listener.handler.processWebsocket(any(JSONObject.class)))
                .thenThrow(new IOException("Command failed", new IllegalArgumentException("Invalid argument")));
        WebSocket websocket = mock(WebSocket.class);

        listener.onTextMessage(websocket,
                "{\"from\":\"dashboard\",\"payload\":{\"action\":\"run_command\"}}");

        ArgumentCaptor<String> text = ArgumentCaptor.forClass(String.class);
        verify(websocket).sendText(text.capture());
        JSONObject response = (JSONObject) new JSONParser().parse(text.getValue());
        assertEquals("dashboard", response.get("to"));
        JSONObject payload = (JSONObject) response.get("payload");
        assertEquals("run_command", payload.get("action"));
        assertEquals("java_exception", payload.get("error"));
        JSONObject exception = (JSONObject) payload.get("exception");
        assertEquals("java.lang.IllegalArgumentException: Invalid argument", exception.get("cause"));
    }
}
