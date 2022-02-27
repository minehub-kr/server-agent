package kr.mcsv.client.websocket;

public enum MCSVWebsocketActions {
    PING("ping"),
    RUN_COMMAND("run_command"),
    RUN_SHELL_COMMAND("run_shell_command"),
    GET_SERVER_METADATA("get_server_metadata"),
    GET_PLAYERS("get_players"),
    GET_PLUGIN_VERSION("get_plugin_version"),
    GET_BUKKIT_VERSION("get_bukkit_version"),
    GET_BUKKIT_INFO("get_bukkit_info"),

    ;

    String name;

    MCSVWebsocketActions(String name) {
        this.name = name;
    }

    public static MCSVWebsocketActions getActionByName(String actionName) {
        for (MCSVWebsocketActions action : MCSVWebsocketActions.values()) {
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
