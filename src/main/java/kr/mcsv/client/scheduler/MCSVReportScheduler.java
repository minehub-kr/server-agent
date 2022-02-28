package kr.mcsv.client.scheduler;

import com.neovisionaries.ws.client.WebSocketException;
import com.stella_it.meiling.InvalidRefreshTokenException;
import kr.mcsv.client.Main;
import kr.mcsv.client.server.MCSVServer;
import kr.mcsv.client.websocket.MCSVWebsocketSession;
import org.bukkit.Bukkit;

import java.io.IOException;

public class MCSVReportScheduler {
    private MCSVServer server;
    public MCSVReportScheduler(MCSVServer server) {
        this.server = server;
    }

    int scheduleId = -1;
    int syncInterval = 30;

    public void start() {
        if (this.scheduleId < 0) {
            this.scheduleId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                    Main.plugin,
                    () -> {
                        this.runJob();
                    },0,20 * syncInterval
            );
        }
    }

    public void stop() {
        if (this.scheduleId > 0) {
            Main.plugin.getServer().getScheduler().cancelTask(this.scheduleId);
            this.scheduleId = -1;
        }
    }

    public void runJob() {
        // add jobs here

        new Thread(() -> {
            if (server != null) {
                server.updateMetadata();
            }
        }).run();
    }


}
