package kr.minehub.servers.agent.websocket;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestDispatchTest {
    @Test
    void reportsUnknownActionWithoutNullPointerException() throws Exception {
        JSONObject request = new JSONObject();
        request.put("action", "not-a-supported-action");

        JSONObject response = new CommandHandler(null).processWebsocket(request);

        assertEquals("invalid_action", response.get("error"));
        assertEquals("not-a-supported-action", response.get("action"));
    }

    @Test
    void rejectsNullAndNonStringActions() throws Exception {
        for (Object action : new Object[] { null, 42 }) {
            JSONObject request = new JSONObject();
            request.put("action", action);
            assertEquals("invalid_action", new CommandHandler(null).processWebsocket(request).get("error"));
        }
    }
}
