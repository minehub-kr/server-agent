package kr.minehub.servers.agent.websocket;

public enum Commands {
    PING("ping"),
    RUN_COMMAND("run_command"),
    RUN_SHELL_COMMAND("run_shell_command"),
    GET_SERVER_METADATA("get_server_metadata"),
    GET_SERVER_PERFORMANCE("get_server_performance"),
    GET_PLAYERS("get_players"),
    GET_PLUGIN_VERSION("get_plugin_version"),
    GET_BUKKIT_VERSION("get_bukkit_version"),
    GET_BUKKIT_INFO("get_bukkit_info"),
    BUKKIT_LOG("bukkit_log"),

    ;

    String name;

    Commands(String name) {
        this.name = name;
    }

    public static Commands getActionByName(String actionName) {
        for (Commands action : Commands.values()) {
            if (action.name.equals(actionName)) {
                return action;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}
