package kr.minehub.servers.agent.core;

import kr.minehub.servers.agent.Main;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.awt.*;
import java.net.URL;

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
            event.getPlayer().sendMessage(ChatColor.YELLOW + "아래의 텍스트를 클릭하여 Stella-IT Accounts 로 로그인하세요.");
            TextComponent message = new TextComponent(ChatColor.AQUA + "[" + ChatColor.RESET + "Click Here" + ChatColor.AQUA + "]");
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("클릭하여 Stella-IT Accounts 로 로그인")));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url.toString()));

            event.getPlayer().spigot().sendMessage(message);
        }
    }

}
