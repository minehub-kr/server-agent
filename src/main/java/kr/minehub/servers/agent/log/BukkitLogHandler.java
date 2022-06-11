package kr.minehub.servers.agent.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.json.simple.JSONObject;

import kr.minehub.servers.agent.utils.JSONUtils;
import kr.minehub.servers.agent.websocket.ConnectSession;

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
            this.appender = new AbstractAppender("minehub-svagent", null, PatternLayout.createDefaultLayout(), false, null) {
                @Override
                public void append(LogEvent event) {
                    // TODO Auto-generated method stub
                    propagateEvent(event);
                }       
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

    private void setConsumer(Consumer<LogEvent> consumer) {
        this.consumer = consumer;
    }

    public void registerWebsocket(ConnectSession session) {
        this.setConsumer((e) -> {
            if (session.isConnected()) {
                session.sendLog(JSONUtils.logEventToJSON(e));
            }
        });
    }

    public void unregisterWebsocket() {
        this.setConsumer(null);
    }
}
