package kr.minehub.servers.agent.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.bukkit.Bukkit;
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

    private boolean constructorExists(Class<?>... args) {
        try {
            AbstractAppender.class.getDeclaredConstructor(args);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private Appender getAppender() {
        if (this.appender == null) {
            String name = "minehub-svagent";
            if (constructorExists(String.class, Filter.class, Layout.class, boolean.class, Property[].class)) {
                Bukkit.getLogger().info(
                        "BukkitLogHandler: Log4J 에 연결 성공. 최신 constructor가 존재합니다."
                );
                this.appender = new AbstractAppender(name, null, PatternLayout.createDefaultLayout(), false, Property.EMPTY_ARRAY) {
                    @Override
                    public void append(LogEvent event) {
                        // TODO Auto-generated method stub
                        propagateEvent(event);
                    }
                };
            } else if (constructorExists(String.class, Filter.class, Layout.class, boolean.class)) {
                Bukkit.getLogger().info(
                        "BukkitLogHandler: Log4J 에 연결 성공. constructor가 구 버전입니다. 호환성 모드로 시작합니다. (type 1)"
                );
                this.appender = new AbstractAppender(name, null, PatternLayout.createDefaultLayout(), false) {
                    @Override
                    public void append(LogEvent event) {
                        // TODO Auto-generated method stub
                        propagateEvent(event);
                    }
                };
            } else if (constructorExists(String.class, Filter.class, Layout.class)) {
                Bukkit.getLogger().info(
                        "BukkitLogHandler: Log4J 에 연결 성공. constructor가 구 버전입니다. 호환성 모드로 시작합니다. (type 2)"
                );
                this.appender = new AbstractAppender(name, null, PatternLayout.createDefaultLayout()) {
                    @Override
                    public void append(LogEvent event) {
                        // TODO Auto-generated method stub
                        propagateEvent(event);
                    }
                };
            } else {
                return null;
            }
        }

        return this.appender;
    }

    public void start() {
        if (this.getAppender() == null) {
            return;
        }

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
