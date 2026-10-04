package kr.minehub.servers.agent.websocket.command;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.UnsafeValues;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public class BukkitCommandDispatcher implements ConsoleCommandSender {
    private final StringBuilder messageBuffer = new StringBuilder();
    private final ConsoleCommandSender sender = Bukkit.getConsoleSender();
    private CommandSender commandSender;
    private final Spigot spigot = new Spigot() {
        @Override
        public void sendMessage(BaseComponent component) {
            sendMessage(new BaseComponent[] { component });
        }

        @Override
        public void sendMessage(BaseComponent... components) {
            BukkitCommandDispatcher.this.sendMessage(BaseComponent.toLegacyText(components));
        }

        @Override
        public void sendMessage(UUID sender, BaseComponent component) {
            sendMessage(component);
        }

        @Override
        public void sendMessage(UUID sender, BaseComponent... components) {
            sendMessage(components);
        }
    };

    public BukkitCommandDispatcher() {
        
    }

    public synchronized String getOutput() {
        return messageBuffer.toString();
    }

    public CommandSender getCommandSender() {
        if (commandSender != null) {
            return commandSender;
        }

        Method createSender;
        try {
            // Discover Paper's API at runtime so the JAR still loads on Spigot.
            createSender = Server.class.getMethod("createCommandSender", Consumer.class);
        } catch (NoSuchMethodException e) {
            commandSender = this;
            return commandSender;
        }

        try {
            ClassLoader loader = Server.class.getClassLoader();
            Class<?> componentType = Class.forName("net.kyori.adventure.text.Component", true, loader);
            Class<?> serializerType = Class.forName(
                    "net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer", true, loader);
            Object serializer = getPaperSerializer(loader);
            Method serialize = serializerType.getMethod("serialize", componentType);
            Consumer<Object> feedback = component -> {
                try {
                    sendMessage((String) serialize.invoke(serializer, component));
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Failed to capture command feedback", e);
                }
            };
            commandSender = (CommandSender) createSender.invoke(Bukkit.getServer(), feedback);
            return commandSender;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to create Paper command sender", e);
        }
    }

    private Object getPaperSerializer(ClassLoader loader) throws ReflectiveOperationException {
        try {
            Class<?> components = Class.forName("io.papermc.paper.text.PaperComponents", true, loader);
            return components.getMethod("legacySectionSerializer").invoke(null);
        } catch (ClassNotFoundException e) {
            // Older Paper exposes the serializer, including Minecraft translations, here.
            return UnsafeValues.class.getMethod("legacyComponentSerializer").invoke(Bukkit.getUnsafe());
        }
    }

    @Override
    public synchronized void sendMessage(String message) {
        messageBuffer.append(message).append('\n');
    }

    @Override
    public void sendMessage(String[] messages) {
        for (String message : messages) {
            this.sendMessage(message);
        }
    }

    @Override
    public void sendMessage(UUID sender, String message) {
        this.sendMessage(message);
    }

    @Override
    public void sendMessage(UUID sender, String[] messages) {
        this.sendMessage(messages);
    }

    @Override
    public Server getServer() {
        return Bukkit.getServer();
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public Spigot spigot() {
        return spigot;
    }

    @Override
    public boolean isPermissionSet(String name) {
        return sender.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        return sender.isPermissionSet(perm);
    }

    @Override
    public boolean hasPermission(String name) {
        return sender.hasPermission(name);
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return sender.hasPermission(perm);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return sender.addAttachment(plugin, name, value);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return sender.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        return sender.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return sender.addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        sender.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        sender.recalculatePermissions();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return sender.getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return sender.isOp();
    }

    @Override
    public void setOp(boolean value) {
        sender.setOp(value);
    }

    @Override
    public boolean isConversing() {
        return false;
    }

    @Override
    public void acceptConversationInput(String input) {
        // TODO: Implement Conversation later.
        
    }

    @Override
    public boolean beginConversation(Conversation conversation) {
        // TODO: Implement Conversation later.
        return false;
    }

    @Override
    public void abandonConversation(Conversation conversation) {
        // TODO: Implement Conversation later.
        
    }

    @Override
    public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details) {
        // TODO: Implement Conversation later.
        
    }

    @Override
    public void sendRawMessage(String message) {
        this.sendMessage(message);

    }

    @Override
    public void sendRawMessage(UUID sender, String message) {
        this.sendMessage(message);
    }
    
}
