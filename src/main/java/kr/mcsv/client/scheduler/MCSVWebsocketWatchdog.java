package kr.mcsv.client.scheduler;

import com.neovisionaries.ws.client.WebSocketException;
import com.stella_it.meiling.InvalidRefreshTokenException;
import kr.mcsv.client.Main;
import kr.mcsv.client.log.MCSVLogTemplate;
import kr.mcsv.client.server.MCSVServer;
import kr.mcsv.client.websocket.MCSVWebsocketSession;
import org.bukkit.Bukkit;

import java.io.IOException;

public class MCSVWebsocketWatchdog {
    private MCSVServer server;
    public MCSVWebsocketWatchdog(MCSVServer server) {
        this.server = server;
    }

    int scheduleId = -1;
    int syncInterval = 10;

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
                MCSVWebsocketSession session = server.getWebsocketSession();

                if (session != null) {
                    if (!session.isConnected()) {
                        try {
                            Bukkit.getLogger().warning(MCSVLogTemplate.warn(
                                "WebsocketWatchdog: MCSV.KR 과 웹소켓 세션이 연결되어있지 않습니다. 연결을 재시도 합니다."
                            ));
                            session.connect();
                            Bukkit.getLogger().info(MCSVLogTemplate.log(
                                "WebsocketWatchdog: MCSV.KR 과 웹소켓 세션이 복구되었습니다."
                            ));
                        } catch (Exception e) {
                            Bukkit.getLogger().severe(MCSVLogTemplate.error(
                                "WebsocketWatchdog: MCSV.KR 과의 웹소켓 세션 복구 중 예외가 발생했습니다. 스택트레이스를 참조해 주세요."
                            ));

                            e.printStackTrace();
                        }
                    }
                }
            }
        }).run();
    }


}
