package kr.mcsv.client.websocket.command;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public class MCSVWebsocketCommandDispatcher implements ConsoleCommandSender {
    String messageBuffer = "";
    ConsoleCommandSender sender = Bukkit.getConsoleSender();

    public MCSVWebsocketCommandDispatcher() {
        
    }

    public String getOutput() {
        return messageBuffer;
    }

    @Override
    public void sendMessage(String message) {
        messageBuffer += message + "\n";
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
        return sender.spigot();
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
