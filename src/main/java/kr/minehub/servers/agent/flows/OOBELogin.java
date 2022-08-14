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

  public static void askUserToLogin(CommandSender sender) {
    sender.sendMessage(""+ChatColor.GREEN+"[Minehub] "+ChatColor.RESET+"Minehub ServerAgent가 로그인 되어있지 않습니다.");
    
    TextComponent message = new TextComponent();

    TextComponent link = new TextComponent(ChatColor.LIGHT_PURPLE+"/minehub "+ChatColor.AQUA+"login");
    link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("클릭하여 Stella IT Accounts 로 로그인 합니다.")));
    link.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/minehub login"));

    message.addExtra(link); 
    message.addExtra(ChatColor.RESET + " 명령어를 실행하거나 클릭 해 로그인 하세요.");

    sender.spigot().sendMessage(message);
  }

  public static void askUserToRegister(CommandSender sender) {
    sender.sendMessage(""+ChatColor.GREEN+"[Minehub] "+ChatColor.RESET+"이 서버가 Minehub RSM 시스템에 등록되어있지 않습니다.");
    
    TextComponent message = new TextComponent();

    TextComponent link = new TextComponent(ChatColor.LIGHT_PURPLE+"/minehub "+ChatColor.AQUA+"register");
    link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("클릭하여 이 서버를 등록합니다.")));
    link.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/minehub register"));

    message.addExtra(link); 
    message.addExtra(ChatColor.RESET + " 명령어를 실행하거나 클릭 해 로그인 하세요.");

    sender.spigot().sendMessage(message);
  }
}
