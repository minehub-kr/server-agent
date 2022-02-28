package kr.mcsv.client.websocket.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class MCSVShellRunner {
    String cmdline;
    ProcessBuilder builder;

    int exitVal = -1;
    
    String stdouterr = "";

    public MCSVShellRunner(String cmdline) {
        builder = new ProcessBuilder();

        String shell = getShellExecutable();
        builder.command(shell, (isWindows() ? "/c" : "-c"), cmdline);
        builder.directory(new File(System.getProperty("user.dir")));
    }

    public int run() throws IOException, InterruptedException {
        Process process = builder.start();

        StringBuilder output = new StringBuilder();

		BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

		String line;
		while ((line = reader.readLine()) != null) {
			output.append(line + "\n");
		}

        stdouterr = output.toString();

        int exitVal = process.waitFor();
        this.exitVal = exitVal;
        return exitVal;
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

    public static String getShellExecutable() {
        return isWindows() ? "cmd.exe" : "/bin/sh";
    }
    
}
