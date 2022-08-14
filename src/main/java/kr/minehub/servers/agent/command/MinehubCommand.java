package kr.minehub.servers.agent.command;

import com.stella_it.meiling.InvalidRefreshTokenException;
import com.stella_it.meiling.MeilingAuthorizationMethod;
import kr.minehub.servers.agent.Main;
import kr.minehub.servers.agent.api.MinehubAPI;
import kr.minehub.servers.agent.api.MinehubServer;
import kr.minehub.servers.agent.api.auth.MinehubAuthorization;
import kr.minehub.servers.agent.flows.OOBELogin;
import kr.minehub.servers.agent.log.AgentLogger;
import kr.minehub.servers.agent.utils.JSONUtils;
import kr.minehub.servers.agent.utils.SystemUtils;
import kr.minehub.servers.agent.utils.GeneralUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MinehubCommand {
    public static boolean hasPermission(CommandSender sender, String node) {
        return sender.hasPermission("minehub."+node);
    }

    public static List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> result = new ArrayList<>();
        String commandName = label.toLowerCase();

        int currentInput = args.length - 1;
        String currentArg = args[currentInput];

        if (commandName.equals("minehub")) {
            if (currentInput == 0) {
                for (MinehubCommandAction action : MinehubCommandAction.values()) {
                    if (action.hasPermission(sender)) {
                        result.add(action.getCommand());
                    }
                }
            }
        }

        return GeneralUtils.searchList(result, currentArg);
    }

    public static boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = label.toLowerCase();

        if (commandName.equals("minehub")) {
            if (args.length >= 1) {
                MinehubCommandAction action = MinehubCommandAction.getAction(args[0]);
                if (action == null) {
                    sender.sendMessage(
                        AgentLogger.error("올바르지 않은 명령입니다.")
                    );
                    return true;
                }

                switch (action) {
                    case REGISTER:
                    {
                        boolean isForced = false;
                        if (args.length > 2) {
                            isForced = args[1].toLowerCase().equals("confirm");
                        }

                        return registerServer(sender, isForced);
                    }
                    case LOGIN:
                    {
                        if (args.length == 1) {
                            return generateLoginLink(sender);
                        } else if (args.length == 2) {
                            String authorizationCode = args[1];
                            return authorizeUsingAuthcode(sender, authorizationCode);
                        } else {
                            sender.sendMessage(AgentLogger.error("올바르지 않은 인수입니다."));
                        }
                    }
                        break;
                    
                    case RENAME:
                    {
                        if (args.length > 2) {
                            String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                            return renameServer(sender, name);
                        } else {
                            sender.sendMessage(AgentLogger.error("이름이 입력되지 않았습니다."));
                        }
                    }
                        break;
                    case TOKEN:
                        return showToken(sender);
                    case DEBUG:
                        return runDebug(sender, args.length >= 2 ? args[1] : null);
                    case INFO:
                        return sendServerAgentStatus(sender);
                    case HELP:
                    default:
                        sender.sendMessage(
                            AgentLogger.log("Minehub 클라이언트 - 버전: " + Main.version)
                        );
                        sender.sendMessage(
                            MinehubCommandAction.getAllManual(sender, label, "")
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

    private static boolean sendServerAgentStatus(CommandSender sender) {
        if (MinehubCommand.hasPermission(sender, "info")) {
            sender.sendMessage(
                AgentLogger.log("Minehub ServerAgent - 버전: " + Main.version)
            );


            if (!MinehubAPI.checkOnline()) {
                sender.sendMessage(
                    AgentLogger.error("Minehub이 점검 중 이거나, 서버가 온라인이 아닙니다.")
                );

                return true;
            }

            if (MinehubCommand.hasPermission(sender, "checkLogin")) {
                String header = ChatColor.RESET + "로그인 상태: ";

                if (Main.core.authorization.isAuthorized()) {
                    sender.sendMessage(header+ChatColor.GREEN+"로그인 완료");
                } else {
                    sender.sendMessage(header+ChatColor.RED+"로그인 되지 않음");
                }
            }

            if (MinehubCommand.hasPermission(sender, "checkRegistered")) {
                String header = ChatColor.RESET + "등록 상태: ";

                if (Main.core.server.isRegistered()) {
                    sender.sendMessage(header+ChatColor.GREEN+"등록 완료");
                } else {
                    sender.sendMessage(header+ChatColor.RED+"미등록");
                }
            }

            if (MinehubCommand.hasPermission(sender, "getServerId")) {
                String header = ChatColor.RESET + "서버 아이디: ";

                if (Main.core.server.getServerId() != null) {
                    sender.sendMessage(header+ChatColor.GREEN+Main.core.server.getServerId());
                } else {
                    sender.sendMessage(header+ChatColor.RED+"미 발급");
                }
            }

            sender.sendMessage(GeneralUtils.getCopyrightString());
        } else {
            sender.sendMessage(AgentLogger.error("에러: 권한이 없습니다."));
        }

        return true;
    }


    private static boolean generateLoginLink(CommandSender sender) {
        if (MinehubCommand.hasPermission(sender, "login")) {
            OOBELogin.requestUserLogin(sender);
        } else {
            sender.sendMessage(AgentLogger.error("권한이 없습니다."));
        }

        return true;
    }


    private static boolean showToken(CommandSender sender) {
        if (MinehubCommand.hasPermission(sender, "token")) {
            if (!Main.core.authorization.isAuthorized()) {
                sender.sendMessage(AgentLogger.error("에러: 로그인 되어있지 않습니다!"));
                return true;
            }

            sender.sendMessage(
                AgentLogger.log("Minehub ServerAgent - 토큰 조회")
            );

            try {
                sender.sendMessage("Access Token: "+Main.core.authorization.getAccessToken());
                sender.sendMessage("Refresh Token: "+Main.core.authorization.getRefreshToken());
            } catch (InvalidRefreshTokenException e) {
                sender.sendMessage(AgentLogger.error("토큰 갱신에 실패했습니다!"));
                return true;
            }
            sender.sendMessage(AgentLogger.warn("경고: 이 토큰을 절대 제3자에게 노출해서는 절대 안 됩니다!"));

        } else {
            sender.sendMessage(AgentLogger.error("에러: 권한이 없습니다."));
        }

        return true;
    }


    private static boolean authorizeUsingAuthcode(CommandSender sender, String code) {
        if (MinehubCommand.hasPermission(sender, "login")) {
            if (!MinehubAPI.checkOnline()) {
                sender.sendMessage(
                       AgentLogger.error("에러: Minehub이 점검 중 이거나, 서버가 온라인이 아닙니다.")
                );

                return true;
            }

            sender.sendMessage(
                    AgentLogger.log("인증서버와 통신을 시작합니다")
            );

            boolean authSuccess = Main.core.authorization.authorize(MeilingAuthorizationMethod.AUTHORIZATION_CODE, code);

            if (!authSuccess) {
                sender.sendMessage(AgentLogger.error("에러: 올바르지 않은 코드이거나, 만료된 코드입니다. 처음부터 인증을 다시 시작하세요."));
                return true;
            }

            sender.sendMessage(
                    AgentLogger.log("인증에 성공했습니다.")
            );
            Main.core.save();

            return true;
        } else {
            sender.sendMessage(AgentLogger.error("에러: 권한이 없습니다."));
        }

        return true;
    }

    private static boolean registerServer(CommandSender sender, boolean forceRegister) {
        if (MinehubCommand.hasPermission(sender, "register")) {
            if (!MinehubAPI.checkOnline()) {
                sender.sendMessage(
                        AgentLogger.error("에러: Minehub이 점검 중 이거나, 서버가 온라인이 아닙니다.")
                );

                return true;
            }

            sender.sendMessage(
                    AgentLogger.log("Minehub에 서버 등록을 위한 통신을 시작합니다")
            );

            if (!Main.core.authorization.isAuthorized()) {
                sender.sendMessage(AgentLogger.error("에러: 로그인 되어있지 않습니다!"));
                return true;
            } else if (!forceRegister && Main.core.server.isRegistered()) {
                sender.sendMessage(AgentLogger.error("에러: 이미 이 서버는 등록되어 있습니다!"));
                sender.sendMessage(AgentLogger.error("에러: 강제로 새로 등록하려면, "+ChatColor.GREEN+"/minehub register confirm"+ChatColor.RESET+" 명령어를 대신 실행하세요."));
                return true;
            }

            // do register logic
            boolean success = Main.core.registerServer();

            if (!success) {
                sender.sendMessage(AgentLogger.error("에러: 서버 등록에 실패 했습니다! 다시 시도 하세요."));
                return true;
            }

            Main.core.save();
            sender.sendMessage(AgentLogger.log("서버 등록에 성공했습니다."));

        } else {
            sender.sendMessage(AgentLogger.error("권한이 없습니다."));
        }
        return true;
    }

    private static boolean renameServer(CommandSender sender, String name) {
        if (MinehubCommand.hasPermission(sender, "rename")) {
            if (!Main.core.authorization.isAuthorized()) {
                sender.sendMessage(AgentLogger.error("에러: 로그인 되어있지 않습니다!"));
                return true;
            }

            if (Main.core.server == null || !Main.core.server.isRegistered()) {
                sender.sendMessage(AgentLogger.error("에러: 서버가 등록되어 있지 않습니다!"));
                return true;
            }

            MinehubServer server = Main.core.server;
            MinehubAuthorization authorization = Main.core.authorization;

            boolean success = MinehubAPI.renameServer(server, authorization, name);

            if (success) {
                sender.sendMessage(AgentLogger.log("서버 이름이 "+name+"으로 변경되었습니다."));
            } else {
                sender.sendMessage(AgentLogger.error("에러: 서버 이름 변경 중 오류가 발생했습니다! 자세한 내용은 콘솔을 확인해 주세요."));
            }
        } else {
            sender.sendMessage(AgentLogger.error("권한이 없습니다."));
        }
        return true;
    }

    public static boolean runDebug(CommandSender sender, @Nullable String command) {
        if (command == null) {
            sender.sendMessage(AgentLogger.error("에러 - Debug 명령어가 감지되지 않았습니다."));
            return true;
        }

        String commandDetect = command.toLowerCase();
        switch(commandDetect) {
            case "metadata":
                sender.sendMessage(JSONUtils.createMetadataJSON().toJSONString());
                break;
            case "env":
                sender.sendMessage(SystemUtils.rawSystemEnvironmentJSON().toJSONString());
                break;
        }


        return true;
    }
}