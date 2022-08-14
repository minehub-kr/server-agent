
package kr.minehub.servers.agent.flows;

/**
 * 1.12.2 patch:
 * Pre spigot era code patch
 */

import java.net.URL;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class OOBELogin {
  public static void requestUserLogin(CommandSender sender) {
    URL url = Main.core.authorization.createRequest();
    if (url == null) {
        sender.sendMessage(ChatColor.RED + "[에러] " + ChatColor.RESET + "생성 중 오류 발생!");
        return;
    }

    sender.sendMessage(ChatColor.YELLOW + "아래의 링크를 클릭하여 Stella-IT Accounts 로 로그인하세요.");
    sender.sendMessage(ChatColor.AQUA + "" + ChatColor.UNDERLINE + url.toString());
  }

  public static void askUserToLogin(CommandSender sender) {
    sender.sendMessage(""+ChatColor.GREEN+"[Minehub] "+ChatColor.RESET+"Minehub ServerAgent가 로그인 되어있지 않습니다.");
    sender.sendMessage(""+ChatColor.LIGHT_PURPLE + "/minehub "+ChatColor.AQUA+"login "+ChatColor.RESET+"명령어를 실행 해 로그인하세요.");
  }
}
