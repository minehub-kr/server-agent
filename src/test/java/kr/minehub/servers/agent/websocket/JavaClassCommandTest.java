package kr.minehub.servers.agent.websocket;

import java.nio.file.Path;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class JavaClassCommandTest {
    @TempDir
    Path directory;

    public static class EntryPoint {
        public static JSONObject main(String[] arguments) {
            JSONObject result = new JSONObject();
            result.put("argumentCount", arguments.length);
            result.put("argument", arguments[0]);
            return result;
        }
    }

    @Test
    void passesStringArrayAsSingleReflectionArgument() throws Exception {
        JSONObject data = new JSONObject();
        data.put("url", directory.toUri().toString());
        data.put("mainClass", EntryPoint.class.getName());
        JSONObject request = new JSONObject();
        request.put("data", data);

        JSONObject response = CommandHandler.loadJavaClass(request);

        assertEquals(true, response.get("success"));
        JSONObject output = (JSONObject) response.get("output");
        assertEquals(1, output.get("argumentCount"));
        assertEquals("", output.get("argument"));
    }
}
