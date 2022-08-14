package kr.minehub.servers.agent.flows;

import java.net.URL;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import kr.minehub.servers.agent.Main;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class OOBELogin {
  public static void requestUserLogin(CommandSender sender) {
    URL url = Main.core.authorization.createRequest();
    if (url == null) {
        sender.sendMessage(ChatColor.RED + "[에러] " + ChatColor.RESET + "생성 중 오류 발생!");
        return;
    }

    sender.sendMessage(ChatColor.YELLOW + "아래의 텍스트를 클릭하여 Stella-IT Accounts 로 로그인하세요.");
    TextComponent message = new TextComponent(ChatColor.AQUA + "[" + ChatColor.RESET + "Click Here" + ChatColor.AQUA + "]");
    message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("클릭하여 Stella-IT Accounts 로 로그인")));
    message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url.toString()));

    sender.spigot().sendMessage(message);
  }
  
}
