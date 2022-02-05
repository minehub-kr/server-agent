package kr.mcsv.client.scheduler;

import kr.mcsv.client.Main;
import kr.mcsv.client.server.MCSVServer;
import org.bukkit.Bukkit;

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

        if (server != null) {
            server.updateMetadata();
        }
    }


}
