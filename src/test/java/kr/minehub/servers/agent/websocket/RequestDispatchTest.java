package kr.minehub.servers.agent.websocket;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import kr.minehub.servers.agent.Main;
import kr.minehub.servers.agent.utils.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    @Test
    void readsPlayersAndWorldInfoInsideScheduledServerTask() throws Exception {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
             MockedStatic<BukkitUtils> utils = mockStatic(BukkitUtils.class)) {
            AtomicBoolean inServerTask = new AtomicBoolean();
            BukkitScheduler scheduler = mock(BukkitScheduler.class);
            bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
            when(scheduler.callSyncMethod(eq(Main.plugin), any(Callable.class))).thenAnswer(invocation -> {
                FutureTask<Object> task = new FutureTask<>(invocation.<Callable<Object>>getArgument(1));
                inServerTask.set(true);
                try {
                    task.run();
                } finally {
                    inServerTask.set(false);
                }
                return task;
            });
            bukkit.when(Bukkit::getOnlinePlayers).thenAnswer(invocation -> {
                assertTrue(inServerTask.get());
                return Collections.emptyList();
            });
            utils.when(BukkitUtils::getBukkitInfoJSON).thenAnswer(invocation -> {
                assertTrue(inServerTask.get());
                return new JSONObject();
            });

            for (String action : new String[] { "get_players", "get_bukkit_info" }) {
                JSONObject request = new JSONObject();
                request.put("action", action);
                assertNotNull(new CommandHandler(null).processWebsocket(request).get("data"));
            }
            verify(scheduler, times(2)).callSyncMethod(eq(Main.plugin), any(Callable.class));
        }
    }
}
