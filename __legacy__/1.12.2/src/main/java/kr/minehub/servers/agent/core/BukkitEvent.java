package kr.minehub.servers.agent.core;

import kr.minehub.servers.agent.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.awt.*;
import java.net.URL;

/**
 * 1.12.2 patch:
 * Pre spigot era code patch
 */

public class BukkitEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (event.getPlayer().isOp()) {
            URL url = Main.core.authorization.createRequest();
            Player player = event.getPlayer();
            if (url == null) {
                player.sendMessage(ChatColor.RED + "[에러] " + ChatColor.RESET + "생성 중 오류 발생!");
                return;
            }

            event.getPlayer().sendMessage(ChatColor.YELLOW + "아래의 링크를 클릭하여 Stella-IT Accounts 로 로그인하세요.");
            event.getPlayer().sendMessage(ChatColor.AQUA + "" + ChatColor.UNDERLINE + url.toString());
        }
    }

}
