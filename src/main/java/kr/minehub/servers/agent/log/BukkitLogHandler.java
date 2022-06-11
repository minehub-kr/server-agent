package kr.minehub.servers.agent.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;

import java.io.Serializable;
import java.util.function.Consumer;

public class BukkitLogHandler {
    boolean initialized = false;
    org.apache.logging.log4j.core.Logger coreLogger = null;
    Appender appender = null;

    Consumer<LogEvent> consumer = null;

    private org.apache.logging.log4j.core.Logger getCoreLogger() {
        if (this.coreLogger == null) this.coreLogger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
        return this.coreLogger;
    }

    private void propagateEvent(LogEvent event) {
        if (consumer != null) {
            consumer.accept(event);
        }
    }

    private Appender getAppender() {
        if (this.appender == null) {
            this.appender = new Appender() {
                @Override public boolean isStarted() { return true; }
                @Override public boolean isStopped() {return false;}
                @Override public State getState() {return null;}
                @Override public void initialize() {}
                @Override public void start() { }
                @Override public void stop() { }
                @Override public void append(LogEvent e) {
                    propagateEvent(e);
                }
                @Override public ErrorHandler getHandler() { return null; }
                @Override public Layout<? extends Serializable> getLayout() { return null; }
                @Override public String getName() { return "UClogs"; }
                @Override public boolean ignoreExceptions() { return false; }
                @Override public void setHandler(ErrorHandler arg0) {}
            };
        }

        return this.appender;
    }
    
    public void start() {
        if (!this.initialized) {
            this.getAppender().start();
            this.getCoreLogger().addAppender(this.getAppender());
            this.initialized = true;
        }
    }

    public void stop() {
        if (this.initialized) {
            this.getCoreLogger().removeAppender(this.getAppender());
            this.getAppender().stop();
            this.initialized = false;
        }
    }

    public void setConsumer(Consumer<LogEvent> consumer) {
        this.consumer = consumer;
    }
}
