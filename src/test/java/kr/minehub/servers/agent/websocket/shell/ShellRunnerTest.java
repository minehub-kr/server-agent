package kr.minehub.servers.agent.websocket.shell;

import java.io.IOException;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

class ShellRunnerTest {
    @Test
    void drainsLargeStderrOutputWithoutDeadlock() {
        assumeFalse(ShellRunner.isWindows());
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            ShellRunner runner = new ShellRunner(
                    "i=0; while [ $i -lt 5000 ]; do printf 'error-output-line-012345678901234567890123456789\\n' >&2; "
                            + "i=$((i+1)); done; printf 'finished\\n'; exit 7");
            assertEquals(7, runner.run());
            assertEquals(7, runner.getExitVal());
            assertTrue(runner.getOutput().startsWith("error-output-line"));
            assertTrue(runner.getOutput().endsWith("finished\n"));
        });
    }

    @Test
    void usesExplicitShellExecutable() {
        ShellRunner runner = new ShellRunner("minehub-nonexistent-shell-executable", "echo test");
        assertThrows(IOException.class, runner::run);
    }
}
