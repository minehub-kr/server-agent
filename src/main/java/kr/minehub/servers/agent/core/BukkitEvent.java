package kr.minehub.servers.agent.core;

import kr.minehub.servers.agent.Main;
import kr.minehub.servers.agent.flows.OOBELogin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class BukkitEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.isOp()) {
            AgentCore core = Main.core;
            if (core != null) {
                if (!core.authorization.isAuthorized()) {
                    OOBELogin.requestUserLogin(player);
                } else {
                    if (core.server == null || !core.server.isRegistered()) {
                        OOBELogin.askUserToRegister(player);
                    }
                }
            }

        }
    }

}
