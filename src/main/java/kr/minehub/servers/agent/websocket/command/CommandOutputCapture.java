package kr.minehub.servers.agent.websocket.command;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ObjectMessage;
import org.apache.logging.log4j.message.ParameterizedMessage;

/** Captures synchronous console feedback on servers without Paper's sender API. */
public final class CommandOutputCapture extends AbstractFilter implements AutoCloseable {
    private final Configuration configuration;
    private final Thread commandThread = Thread.currentThread();
    private final StringBuilder output = new StringBuilder();

    public CommandOutputCapture() {
        configuration = ((Logger) LogManager.getRootLogger()).getContext().getConfiguration();
        start();
        // Global filters run before asynchronous logging queues, preserving command boundaries.
        configuration.addFilter(this);
    }

    @Override
    public synchronized Result filter(Logger logger, Level level, Marker marker,
                                      Message message, Throwable error) {
        if (Thread.currentThread() == commandThread && message != null
                && level.isMoreSpecificThan(logger.getLevel())) {
            output.append(message.getFormattedMessage()).append('\n');
        }
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object message, Throwable error) {
        return filter(logger, level, marker, message == null ? null : new ObjectMessage(message), error);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object... parameters) {
        return filter(logger, level, marker,
                message == null ? null : new ParameterizedMessage(message, parameters), null);
    }

    public synchronized String getOutput(String directOutput) {
        // Prefer sender feedback so commands that also log their output aren't duplicated.
        return directOutput.isEmpty() ? output.toString() : directOutput;
    }

    @Override
    public void close() {
        configuration.removeFilter(this);
        stop();
    }
}
