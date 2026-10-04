package kr.minehub.servers.agent.websocket;

import kr.minehub.servers.agent.Main;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ConnectionWatchdogTest {
    @Test
    void schedulesNetworkRecoveryAsynchronouslyAndCancelsTaskZero() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            BukkitScheduler scheduler = mock(BukkitScheduler.class);
            bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
            when(scheduler.scheduleAsyncRepeatingTask(eq(Main.plugin), any(Runnable.class), eq(0L), eq(200L)))
                    .thenReturn(0);
            ConnectionWatchdog watchdog = new ConnectionWatchdog(null);

            watchdog.start();
            watchdog.stop();

            verify(scheduler).scheduleAsyncRepeatingTask(eq(Main.plugin), any(Runnable.class), eq(0L), eq(200L));
            verify(scheduler, never()).scheduleSyncRepeatingTask(any(), any(Runnable.class), anyLong(), anyLong());
            verify(scheduler).cancelTask(0);
        }
    }
}
