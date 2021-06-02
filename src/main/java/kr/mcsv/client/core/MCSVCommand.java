package kr.mcsv.client.core;

import com.stella_it.meiling.InvalidRefreshTokenException;
import com.stella_it.meiling.MeilingAuthorization;
import com.stella_it.meiling.MeilingAuthorizationMethod;
import kr.mcsv.client.Main;
import kr.mcsv.client.utils.MCSVUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MCSVCommand {
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
                    return authorizeUsingAuthcode(sender, authorizationCode);
                } else {
                    sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"올바르지 않은 인수입니다.");
                }
            } else if (subCommand.equals("setup")) {
                boolean isForced = false;
                if (args.length > 2) {
                    isForced = args[1].toLowerCase().equals("confirm");
                }

                return startSetup(sender, isForced);
            } else if (subCommand.equals("token")) {
                return showToken(sender);
            }
            return true;
        }
        return false;
    }

    private static boolean sendMCSVInfo(CommandSender sender) {
        if (sender.hasPermission("mcsv.info")) {
            sender.sendMessage(
                    ChatColor.GREEN + "[MCSV] " + ChatColor.RESET + "MCSV.KR 클라이언트 - 버전: " + Main.version
            );

            if (sender.hasPermission("mcsv.checkLogin")) {
                String header = ChatColor.RESET + "로그인 상태: ";

                if (Main.core.isAuthorized()) {
                    sender.sendMessage(header+ChatColor.GREEN+"로그인 완료");
                } else {
                    sender.sendMessage(header+ChatColor.RED+"로그인 되지 않음");
                }
            }

            if (sender.hasPermission("mcsv.checkRegistered")) {
                String header = ChatColor.RESET + "등록 상태: ";

                if (Main.core.isRegistered()) {
                    sender.sendMessage(header+ChatColor.GREEN+"등록 완료");
                } else {
                    sender.sendMessage(header+ChatColor.RED+"미등록");
                }
            }

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

            if (sender.hasPermission("mcsv.token")) {
                sender.sendMessage(
                        ChatColor.GREEN + "* " +
                                ChatColor.BOLD + "/mcsv " +
                                ChatColor.GRAY + "token " +
                                ChatColor.DARK_GRAY + ": " +
                                ChatColor.RESET + "MCSV.KR 플랫폼에 로그인 하기 위한 토큰을 조회합니다."
                );
            }

            sender.sendMessage(MCSVCore.getCopyrightString());
        } else {
            sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"권한이 없습니다.");
        }

        return true;
    }


    private static boolean generateLoginLink(CommandSender sender) {
        if (sender.hasPermission("mcsv.login")) {
            URL url = Main.core.createAuthorizationRequest();
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
        if (sender.hasPermission("mcsv.token")) {
            if (!Main.core.isAuthorized()) {
                sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"로그인 되어있지 않습니다!");
                return true;
            }

            sender.sendMessage(
                    ChatColor.GREEN + "[MCSV] " + ChatColor.RESET + "MCSV.KR 클라이언트 - 토큰 조회"
            );

            try {
                sender.sendMessage("Access Token: "+Main.core.getAccessToken());
                sender.sendMessage("Refresh Token: "+Main.core.getRefreshToken());
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
        if (sender.hasPermission("mcsv.login")) {
            sender.sendMessage(
                    ChatColor.GREEN + "[MCSV] " + ChatColor.RESET + "인증서버와 통신을 시작합니다"
            );

            boolean authSuccess = Main.core.authorizeUsingAuthCode(code);

            if (!authSuccess) {
                sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"올바르지 않은 코드이거나, 만료된 코드입니다. 처음부터 인증을 다시 시작하세요.");
                return true;
            }

            sender.sendMessage(
                    ChatColor.GREEN + "[MCSV] " + ChatColor.RESET + "인증에 성공했습니다."
            );

            return true;
        } else {
            sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"권한이 없습니다.");
        }

        return true;
    }

    private static boolean startSetup(CommandSender sender, boolean forceRegister) {
        if (sender.hasPermission("mcsv.setup")) {
            if (!Main.core.isAuthorized()) {
                sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"로그인 되어있지 않습니다!");
                return true;
            } else if (!forceRegister && Main.core.isRegistered()) {
                sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"이미 이 서버는 등록되어 있습니다!");
                sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"강제로 새로 등록하려면, "+ChatColor.GREEN+"/mcsv setup confirm"+ChatColor.RESET+" 을 대신 실행하세요.");
                return true;
            }

            // do register logic
            boolean isSuccess = Main.core.registerServer();

            if (!isSuccess) {
                sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"서버 등록에 실패 했습니다! 다시 시도 하세요.");
                return true;
            }

            sender.sendMessage(ChatColor.GREEN+"[등록] "+ChatColor.RESET+"서버 등록에 성공했습니다.");

        } else {
            sender.sendMessage(ChatColor.RED+"[에러] "+ChatColor.RESET+"권한이 없습니다.");
        }
        return true;
    }
}
