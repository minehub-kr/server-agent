package kr.mcsv.client.command;

import com.stella_it.meiling.InvalidRefreshTokenException;
import com.stella_it.meiling.MeilingAuthorizationMethod;
import kr.mcsv.client.Main;
import kr.mcsv.client.api.MCSVAPI;
import kr.mcsv.client.server.MCSVServer;
import kr.mcsv.client.utils.MCSVLowLevelUtils;
import kr.mcsv.client.utils.MCSVUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MCSVCommand {
    public static boolean hasPermission(CommandSender sender, String node) {
        return sender.hasPermission("mcsv."+node);
    }

    public static List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> result = new ArrayList<>();
        String commandName = label.toLowerCase();

        int currentInput = args.length - 1;
        String currentArg = args[currentInput];

        if (commandName.equals("mcsv")) {
            if (currentInput == 0) {
                for (MCSVCommandAction action : MCSVCommandAction.values()) {
                    if (action.hasPermission(sender)) {
                        result.add(action.getCommand());
                    }
                }
            }
        }

        return MCSVUtils.searchList(result, currentArg);
    }

    public static boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = label.toLowerCase();

        if (commandName.equals("mcsv")) {
            if (args.length >= 1) {
                MCSVCommandAction action = MCSVCommandAction.getAction(args[0]);
                if (action == null) {
                    sender.sendMessage(
                            ChatColor.GREEN + "[MCSV] " + ChatColor.RESET + "올바르지 않은 명령입니다."
                    );
                    return true;
                }

                switch (action) {
                    case SETUP:
                    {
                        boolean isForced = false;
                        if (args.length > 2) {
                            isForced = args[1].toLowerCase().equals("confirm");
                        }

                        return startSetup(sender, isForced);
                    }
                    case LOGIN:
                    {
                        if (args.length == 1) {
                            return generateLoginLink(sender);
                        } else if (args.length == 2) {
                            String authorizationCode = args[1];
                            return authorizeUsingAuthcode(sender, authorizationCode);
                        } else {
                            sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"올바르지 않은 인수입니다.");
                        }
                    }
                        break;
                    case TOKEN:
                        return showToken(sender);
                    case DEBUG:
                        return runDebug(sender, args.length >= 2 ? args[1] : null);
                    case INFO:
                        return sendMCSVInfo(sender);
                    case HELP:
                    default:
                        sender.sendMessage(
                                ChatColor.GREEN + "[MCSV] " + ChatColor.RESET + "MCSV.KR 클라이언트 - 버전: " + Main.version
                        );
                        sender.sendMessage(
                                MCSVCommandAction.getAllManual(sender, "mcsv", "")
                        );
                        return true;
                }
            } else {
                // fallback to help.
                return onCommand(sender, command, label, new String[]{"help"});
            }
            return true;
        }
        return false;
    }

    private static boolean sendMCSVInfo(CommandSender sender) {
        if (MCSVCommand.hasPermission(sender, "info")) {
            sender.sendMessage(
                    ChatColor.GREEN + "[MCSV] " + ChatColor.RESET + "MCSV.KR 클라이언트 - 버전: " + Main.version
            );


            if (!MCSVAPI.checkOnline()) {
                sender.sendMessage(
                        ChatColor.RED + "[MCSV] " + ChatColor.RESET + "MCSV.KR 플랫폼이 점검 중 이거나, 서버가 온라인이 아닙니다."
                );

                return true;
            }

            if (MCSVCommand.hasPermission(sender, "checkLogin")) {
                String header = ChatColor.RESET + "로그인 상태: ";

                if (Main.core.authorization.isAuthorized()) {
                    sender.sendMessage(header+ChatColor.GREEN+"로그인 완료");
                } else {
                    sender.sendMessage(header+ChatColor.RED+"로그인 되지 않음");
                }
            }

            if (MCSVCommand.hasPermission(sender, "checkRegistered")) {
                String header = ChatColor.RESET + "등록 상태: ";

                if (Main.core.server.isRegistered()) {
                    sender.sendMessage(header+ChatColor.GREEN+"등록 완료");
                } else {
                    sender.sendMessage(header+ChatColor.RED+"미등록");
                }
            }

            sender.sendMessage(MCSVUtils.getCopyrightString());
        } else {
            sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"권한이 없습니다.");
        }

        return true;
    }


    private static boolean generateLoginLink(CommandSender sender) {
        if (MCSVCommand.hasPermission(sender, "login")) {
            URL url = Main.core.authorization.createRequest();
            if (url == null) {
                sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"생성 중 오류 발생!");
                return true;
            }

            sender.sendMessage(
                    ChatColor.GREEN + "[MCSV] " + ChatColor.RESET + "MCSV.KR 클라이언트 - 로그인"
            );

            sender.sendMessage(
                    "아래 링크로 이동해, 인증을 진행해 주세요."
            );

            sender.sendMessage(
                    "" + ChatColor.AQUA + ChatColor.UNDERLINE + url.toString()
            );

            sender.sendMessage("");
            sender.sendMessage("로그인을 진행 한 후, 코드를 복사해 명령어(/mcsv login 코드)를 실행 해 주세요.");

        } else {
            sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"권한이 없습니다.");
        }

        return true;
    }


    private static boolean showToken(CommandSender sender) {
        if (MCSVCommand.hasPermission(sender, "token")) {
            if (!Main.core.authorization.isAuthorized()) {
                sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"로그인 되어있지 않습니다!");
                return true;
            }

            sender.sendMessage(
                    ChatColor.GREEN + "[MCSV] " + ChatColor.RESET + "MCSV.KR 클라이언트 - 토큰 조회"
            );

            try {
                sender.sendMessage("Access Token: "+Main.core.authorization.getAccessToken());
                sender.sendMessage("Refresh Token: "+Main.core.authorization.getRefreshToken());
            } catch (InvalidRefreshTokenException e) {
                sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"토큰 갱신에 실패했습니다!");
                return true;
            }
            sender.sendMessage(ChatColor.GOLD+"[경고] "+ChatColor.RESET+"이 토큰을 절대 제3자에게 노출해서는 절대 안 됩니다!");

        } else {
            sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"권한이 없습니다.");
        }

        return true;
    }


    private static boolean authorizeUsingAuthcode(CommandSender sender, String code) {
        if (MCSVCommand.hasPermission(sender, "login")) {
            if (!MCSVAPI.checkOnline()) {
                sender.sendMessage(
                        ChatColor.RED + "[MCSV] " + ChatColor.RESET + "MCSV.KR 플랫폼이 점검 중 이거나, 서버가 온라인이 아닙니다."
                );

                return true;
            }

            sender.sendMessage(
                    ChatColor.GREEN + "[MCSV] " + ChatColor.RESET + "인증서버와 통신을 시작합니다"
            );

            boolean authSuccess = Main.core.authorization.authorize(MeilingAuthorizationMethod.AUTHORIZATION_CODE, code);

            if (!authSuccess) {
                sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"올바르지 않은 코드이거나, 만료된 코드입니다. 처음부터 인증을 다시 시작하세요.");
                return true;
            }

            sender.sendMessage(
                    ChatColor.GREEN + "[MCSV] " + ChatColor.RESET + "인증에 성공했습니다."
            );
            Main.core.save();

            return true;
        } else {
            sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"권한이 없습니다.");
        }

        return true;
    }

    private static boolean startSetup(CommandSender sender, boolean forceRegister) {
        if (MCSVCommand.hasPermission(sender, "setup")) {
            if (!MCSVAPI.checkOnline()) {
                sender.sendMessage(
                        ChatColor.RED + "[MCSV] " + ChatColor.RESET + "MCSV.KR 플랫폼이 점검 중 이거나, 서버가 온라인이 아닙니다."
                );

                return true;
            }

            sender.sendMessage(
                    ChatColor.GREEN + "[MCSV] " + ChatColor.RESET + "MCSV.KR 플랫폼 중앙 서버에 서버 등록을 위한 통신을 시작합니다"
            );

            if (!Main.core.authorization.isAuthorized()) {
                sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"로그인 되어있지 않습니다!");
                return true;
            } else if (!forceRegister && Main.core.server.isRegistered()) {
                sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"이미 이 서버는 등록되어 있습니다!");
                sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"강제로 새로 등록하려면, "+ChatColor.GREEN+"/mcsv setup confirm"+ChatColor.RESET+" 을 대신 실행하세요.");
                return true;
            }

            // do register logic
            boolean success = Main.core.registerServer();

            if (!success) {
                sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"서버 등록에 실패 했습니다! 다시 시도 하세요.");
                return true;
            }

            Main.core.save();
            sender.sendMessage(ChatColor.GREEN+"[등록] "+ChatColor.RESET+"서버 등록에 성공했습니다.");

        } else {
            sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"권한이 없습니다.");
        }
        return true;
    }

    public static boolean runDebug(CommandSender sender, @Nullable String command) {
        if (command == null) {
            sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"Debug 명령어가 감지되지 않았습니다.");
            return true;
        }

        String commandDetect = command.toLowerCase();
        switch(commandDetect) {
            case "metadata":
                sender.sendMessage(MCSVUtils.createMetadataJSON().toJSONString());
                break;
            case "env":
                sender.sendMessage(MCSVLowLevelUtils.rawSystemEnvironmentJSON().toJSONString());
        }


        return true;
    }
}
