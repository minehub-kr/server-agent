package kr.mcsv.client.websocket;

public enum MCSVWebsocketActions {
    RUN_COMMAND("run_command"),
    RUN_SHELL_COMMAND("run_shell_command"),
    GET_SERVER_METADATA("get_server_metadata"),
    GET_PLAYERS("get_players"),

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
}
