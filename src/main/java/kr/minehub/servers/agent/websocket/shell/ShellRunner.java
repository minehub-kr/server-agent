package kr.minehub.servers.agent.websocket.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShellRunner {
    String cmdline;
    ProcessBuilder builder;

    int exitVal = -1;
    
    String stdouterr = "";
    String shellExecutable = null;

    public ShellRunner(String cmdline) {
        this(null, cmdline);
    }

    public ShellRunner(String shellExecutable, String cmdline) {
        this.shellExecutable = shellExecutable;
        builder = new ProcessBuilder();

        String shell = getShellExecutable();
        builder.command(shell, (isWindows() ? "/c" : "-c"), cmdline);
        builder.directory(new File(System.getProperty("user.dir")));
        builder.redirectErrorStream(true);
    }

    public int run() throws IOException, InterruptedException {
        Process process = builder.start();

        try {
            process.getOutputStream().close();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append('\n');
                }
            }
            stdouterr = output.toString();
            exitVal = process.waitFor();
            return exitVal;
        } finally {
            process.destroy();
        }
    }

    public String getOutput() {
        return stdouterr;
    }

    public int getExitVal() {
        return exitVal;
    }

    public static boolean isWindows() {
        // Check if this platform is Micro$oft Window$
        return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }

    public String getShellExecutable() {
        if (this.shellExecutable != null) return this.shellExecutable;
        return isWindows() ? "cmd.exe" : "/bin/sh";
    }
    
}
