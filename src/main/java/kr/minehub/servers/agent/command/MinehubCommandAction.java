package kr.minehub.servers.agent.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public enum MinehubCommandAction {
    HELP("help", "", "이 도움말을 표시합니다."),
    LOGIN("login", "", "Minehub ServerAgent에 Stella IT Accounts로 로그인합니다."),
    REGISTER("register", "", "Minehub ServerAgent에 이 서버를 등록합니다."),
    RENAME("rename", "<name>", "Minehub ServerAgent에 등록된 이 서버의 이름을 변경합니다."),
    INFO("info", "", "ServerAgent의 현재 상태를 표시합니다."),
    DEBUG("debug", "<? command>", "ServerAgent를 디버깅 하기 위한 명령을 실행 합니다"),
    RECONNECT("reconnect", "", "오류 등으로 인해 Minehub RSM 서버와 연결이 끊긴 경우, 강제로 다시 연결을 시도합니다"),
    TOKEN("token", "", "(Debug) oAuth2 인증용 토큰을 표시합니다.");

    String cmdline;
    String usage;
    String explanation;

    MinehubCommandAction(String cmdline, String usage, String explanation) {
        this.cmdline = cmdline;
        this.usage = usage;
        this.explanation = explanation;
    }

    public static List<String> listAll(CommandSender sender) {
        List<String> all = new ArrayList<>();

        for (MinehubCommandAction action : MinehubCommandAction.values()) {
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

        for (MinehubCommandAction action : MinehubCommandAction.values()) {
            if (MinehubCommand.hasPermission(sender, action.getCommand())) {
                all += action.getManual(label, name)+"\n";
            }
        }

        return all;
    }

    public boolean hasPermission(CommandSender sender) {
        return MinehubCommand.hasPermission(sender, this.getCommand());
    }

    public String getCommand() {
        return cmdline;
    }

    public static MinehubCommandAction getAction(String string) {
        for (MinehubCommandAction action : MinehubCommandAction.values()) {
            if (action.getCommand().equalsIgnoreCase(string)) {
                return action;
            }
        }
        return null;
    }
}
