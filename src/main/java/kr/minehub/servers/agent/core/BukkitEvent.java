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
            if (!Main.core.authorization.isAuthorized()) {
                OOBELogin.requestUserLogin(player);
            }
        }
    }

}
