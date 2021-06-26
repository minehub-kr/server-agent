package kr.mcsv.client.command;

import kr.mcsv.client.utils.MCSVUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public enum MCSVCommandAction {
    HELP("help", "", "이 도움말을 표시합니다."),
    SETUP("setup", "", "mcsv.kr platform에 이 서버를 등록합니다."),
    LOGIN("login", "", "mcsv.kr platform 으로 로그인합니다."),
    INFO("info", "", "mcsv.kr client 정보를 표시합니다."),
    TOKEN("token", "", "(Debug) oAuth2 인증용 토큰을 표시합니다.");

    String cmdline;
    String usage;
    String explanation;

    MCSVCommandAction(String cmdline, String usage, String explanation) {
        this.cmdline = cmdline;
        this.usage = usage;
        this.explanation = explanation;
    }

    public static List<String> listAll(CommandSender sender) {
        List<String> all = new ArrayList<>();

        for (MCSVCommandAction action : MCSVCommandAction.values()) {
            if (action.hasPermission(sender)) {
                all.add(action.getCommand());
            }
        }

        return all;
    }

    public String getManual(String label, String name) {
        return ChatColor.LIGHT_PURPLE+"/"+label+" "+ChatColor.AQUA+name+" "+ChatColor.YELLOW+this.cmdline+" "+ChatColor.GRAY+this.usage+ChatColor.RESET+" : "+this.explanation;
    }

    public static String getAllManual(CommandSender sender, String label, String name) {
        String all = "";

        for (MCSVCommandAction action : MCSVCommandAction.values()) {
            if (MCSVCommand.hasPermission(sender, action.getCommand())) {
                all += action.getManual(label, name)+"\n";
            }
        }

        return all;
    }

    public boolean hasPermission(CommandSender sender) {
        return MCSVCommand.hasPermission(sender, this.getCommand());
    }

    public String getCommand() {
        return cmdline;
    }

    public static MCSVCommandAction getAction(String string) {
        for (MCSVCommandAction action : MCSVCommandAction.values()) {
            if (action.getCommand().equalsIgnoreCase(string)) {
                return action;
            }
        }
        return null;
    }
}
