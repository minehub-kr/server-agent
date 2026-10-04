package kr.minehub.servers.agent.websocket.command;

import java.util.UUID;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BukkitCommandDispatcherTest {
    @Test
    void keepsSpigotFallbackWhenPaperApiIsUnavailable() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            BukkitCommandDispatcher dispatcher = new BukkitCommandDispatcher();

            assertSame(dispatcher, dispatcher.getCommandSender());
            assertSame(dispatcher, dispatcher.getCommandSender());
        }
    }

    @Test
    void capturesStringAndRawMessagesInOrder() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            BukkitCommandDispatcher dispatcher = new BukkitCommandDispatcher();

            dispatcher.sendMessage(new String[] { "first", "second" });
            dispatcher.sendMessage(UUID.randomUUID(), "third");
            dispatcher.sendRawMessage("fourth");

            assertEquals("first\nsecond\nthird\nfourth\n", dispatcher.getOutput());
        }
    }

    @Test
    void capturesBungeeComponentsInsteadOfSendingThemToConsole() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            ConsoleCommandSender console = mock(ConsoleCommandSender.class);
            bukkit.when(Bukkit::getConsoleSender).thenReturn(console);
            BukkitCommandDispatcher dispatcher = new BukkitCommandDispatcher();

            dispatcher.spigot().sendMessage(new TextComponent("component"));
            dispatcher.spigot().sendMessage(UUID.randomUUID(),
                    new TextComponent[] { new TextComponent("one"), new TextComponent("two") });

            assertTrue(dispatcher.getOutput().contains("component"));
            assertTrue(dispatcher.getOutput().contains("one"));
            assertTrue(dispatcher.getOutput().contains("two"));
            verifyNoInteractions(console);
        }
    }
}
