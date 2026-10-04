package kr.minehub.servers.agent.websocket;

import kr.minehub.servers.agent.Main;
import kr.minehub.servers.agent.log.AgentLogger;
import kr.minehub.servers.agent.api.MinehubServer;
import org.bukkit.Bukkit;

public class ConnectionWatchdog {
    private MinehubServer server;
    public ConnectionWatchdog(MinehubServer server) {
        this.server = server;
    }

    int scheduleId = -1;
    int syncInterval = 10;

    public void start() {
        if (this.scheduleId < 0) {
            this.scheduleId = Bukkit.getScheduler().scheduleAsyncRepeatingTask(
                    Main.plugin,
                    this::runJob,
                    0L,
                    20L * syncInterval
            );
        }
    }

    public void stop() {
        if (this.scheduleId >= 0) {
            Bukkit.getScheduler().cancelTask(this.scheduleId);
            this.scheduleId = -1;
        }
    }

    public void runJob() {
        if (server == null) return;
        ConnectSession session = server.getWebsocketSession();
        if (session == null || session.preventReconnect || session.isConnected()) return;

        if (session.isConnecting()) {
            Bukkit.getLogger().warning(AgentLogger.warn(
                    "WebsocketWatchdog: Minehub과의 연결이 아직 진행 중입니다. 계속 연결이 되지 않는다면 서버를 재시작하세요."
            ));
            return;
        }

        try {
            Bukkit.getLogger().warning(AgentLogger.warn(
                    "WebsocketWatchdog: Minehub과 웹소켓 세션이 연결되어있지 않습니다. 연결을 재시도 합니다."
            ));
            if (session.connect() != null) {
                Bukkit.getLogger().info(AgentLogger.log(
                        "WebsocketWatchdog: Minehub과 웹소켓 세션이 복구되었습니다."
                ));
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe(AgentLogger.error(
                    "WebsocketWatchdog: Minehub과의 웹소켓 세션 복구 중 예외가 발생했습니다. 아래 표기되는 Stacktrace를 참조해 주세요."
            ));
            e.printStackTrace();
        }
    }
}
