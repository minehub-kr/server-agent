package kr.minehub.servers.agent.core;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.json.simple.JSONObject;

import kr.minehub.servers.agent.Main;
import kr.minehub.servers.agent.utils.BukkitUtils;
import kr.minehub.servers.agent.websocket.Commands;
import kr.minehub.servers.agent.websocket.ConnectSession;

public class AgentListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        
        broadcastPayload(Commands.PLAYER_JOIN, BukkitUtils.getPlayerJSON(player));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();    

        broadcastPayload(Commands.PLAYER_LEAVE, BukkitUtils.getPlayerJSON(player));
    }

    public void broadcastPayload(Commands command, JSONObject data) {
        runBroadcast(buildPayload(command, data));
    }

    public void runBroadcast(JSONObject payload) {
        if (Main.core.server != null) {
            ConnectSession session = Main.core.server.getWebsocketSession();
            if (session != null) {
                session.broadcastPayload(payload);
            }
        }
    }

    public JSONObject buildPayload(Commands command, JSONObject data) {
        JSONObject payload = new JSONObject();

        payload.put("action", command.name());
        payload.put("data", data);

        return payload;
    }

}
