package kr.mcsv.client.core;

import com.stella_it.meiling.MeilingAuthorizationMethod;
import kr.mcsv.client.Main;
import kr.mcsv.client.utils.MCSVUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MCSVCommandHandler {
    public static List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> result = new ArrayList<>();
        String commandName = label.toLowerCase();

        int currentInput = args.length - 1;
        String currentArg = args[currentInput];

        if (commandName.equals("mcsv")) {
            if (currentInput == 0) {
                if (sender.hasPermission("mcsv.login")) {
                    result.add("login");
                }

                if (sender.hasPermission("mcsv.setup")) {
                    result.add("setup");
                }
            }
        }

        return MCSVUtils.searchList(result, currentArg);
    }

    public static boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = label.toLowerCase();
        String subCommand = (args.length >= 1) ? args[0] : "";

        if (commandName.equals("mcsv")) {
            if (subCommand.equals("")) {
                return sendMCSVInfo(sender);
            } else if (subCommand.equals("login")) {
                if (args.length == 1) {
                    return generateLoginLink(sender);
                } else if (args.length == 2) {
                    String authorizationCode = args[1];
                    sender.sendMessage("Test: "+authorizationCode);
                } else {
                    sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"올바르지 않은 인수입니다.");
                }
            } else if (subCommand.equals("setup")) {

            }
            return true;
        }
        return false;
    }

    public static boolean sendMCSVInfo(CommandSender sender) {
        if (sender.hasPermission("mcsv.info")) {
            sender.sendMessage(
                    ChatColor.GREEN + "[MCSV] " + ChatColor.RESET + "MCSV.KR 클라이언트 - 버전: " + Main.version
            );

            if (sender.hasPermission("mcsv.login")) {
                sender.sendMessage(
                        ChatColor.GREEN + "* " +
                                ChatColor.BOLD + "/mcsv " +
                                ChatColor.GRAY + "login " +
                                ChatColor.DARK_GRAY + ": " +
                                ChatColor.RESET + "MCSV.KR 플랫폼에 로그인합니다."
                );
            }

            if (sender.hasPermission("mcsv.setup")) {
                sender.sendMessage(
                        ChatColor.GREEN + "* " +
                                ChatColor.BOLD + "/mcsv " +
                                ChatColor.GRAY + "setup " +
                                ChatColor.DARK_GRAY + ": " +
                                ChatColor.RESET + "MCSV.KR 플랫폼에 이 서버를 등록합니다."
                );
            }

            sender.sendMessage(
                    "Copyright © "+
                            ChatColor.GREEN+"mcsv.kr platform "+
                            ChatColor.RESET+"and "+
                            ChatColor.AQUA+"Stella "+ChatColor.LIGHT_PURPLE+"IT"
            );
        } else {
            sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"권한이 없습니다.");
        }

        return true;
    }


    public static boolean generateLoginLink(CommandSender sender) {
        if (sender.hasPermission("mcsv.login")) {
            URL url = Main.client.createAuthorizationRequest(
                    MeilingAuthorizationMethod.AUTHORIZATION_CODE,
                    Main.clientScope.split(" ")
            );

            sender.sendMessage(
                    ChatColor.GREEN + "[MCSV] " + ChatColor.RESET + "MCSV.KR 클라이언트 - 로그인"
            );

            sender.sendMessage(
                    "아래 링크로 이동해, 인증을 진행해 주세요."
            );

            sender.sendMessage(
                    "" + ChatColor.AQUA + ChatColor.UNDERLINE + url.toString()
            );

        } else {
            sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"권한이 없습니다.");
        }

        return true;
    }
}
