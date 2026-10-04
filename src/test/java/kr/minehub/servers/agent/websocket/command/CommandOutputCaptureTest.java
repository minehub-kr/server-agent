package kr.minehub.servers.agent.websocket.command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.config.Configuration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandOutputCaptureTest {
    @Test
    void capturesFormattedFeedbackFromCommandThreadOnly() throws Exception {
        Logger logger = (Logger) LogManager.getRootLogger();
        try (CommandOutputCapture capture = new CommandOutputCapture()) {
            logger.error("Seed: [{}]", 42);
            Thread unrelated = new Thread(() -> logger.error("Unrelated log"));
            unrelated.start();
            unrelated.join();

            assertEquals("Seed: [42]\n", capture.getOutput(""));
            assertEquals("Direct feedback\n", capture.getOutput("Direct feedback\n"));
        }
    }

    @Test
    void removesFilterEvenWhenCommandThrows() {
        Logger logger = (Logger) LogManager.getRootLogger();
        Configuration configuration = logger.getContext().getConfiguration();
        Filter previous = configuration.getFilter();
        CommandOutputCapture capture = new CommandOutputCapture();
        assertNotNull(configuration.getFilter());

        assertThrows(IllegalStateException.class, () -> {
            try (CommandOutputCapture ignored = capture) {
                throw new IllegalStateException("Command failed");
            }
        });

        assertSame(previous, configuration.getFilter());
        assertTrue(capture.isStopped());
    }
}
