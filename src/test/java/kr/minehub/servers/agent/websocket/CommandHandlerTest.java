package kr.minehub.servers.agent.websocket;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import kr.minehub.servers.agent.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CommandHandlerTest {
    private JSONObject request(String command) {
        JSONObject data = new JSONObject();
        data.put("cmdline", command);
        JSONObject request = new JSONObject();
        request.put("data", data);
        return request;
    }

    @Test
    void returnsCapturedOutputFromScheduledCommand() throws Exception {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            BukkitScheduler scheduler = mock(BukkitScheduler.class);
            bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
            bukkit.when(Bukkit::getConsoleSender).thenReturn(mock(ConsoleCommandSender.class));
            when(scheduler.runTask(eq(Main.plugin), any(Runnable.class))).thenAnswer(invocation -> {
                invocation.<Runnable>getArgument(1).run();
                return mock(BukkitTask.class);
            });
            bukkit.when(() -> Bukkit.dispatchCommand(any(CommandSender.class), eq("help")))
                    .thenAnswer(invocation -> {
                        invocation.<CommandSender>getArgument(0).sendMessage("Help output");
                        return true;
                    });

            JSONObject response = CommandHandler.runBukkitCommand(request("help"));

            assertEquals("Help output\n", response.get("output"));
        }
    }

    @Test
    void reportsCommandExceptionInsteadOfWaitingForTimeout() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            BukkitScheduler scheduler = mock(BukkitScheduler.class);
            bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
            when(scheduler.runTask(eq(Main.plugin), any(Runnable.class))).thenAnswer(invocation -> {
                invocation.<Runnable>getArgument(1).run();
                return mock(BukkitTask.class);
            });
            CommandException failure = new CommandException("Invalid command");
            bukkit.when(() -> Bukkit.dispatchCommand(any(CommandSender.class), eq("invalid")))
                    .thenThrow(failure);

            IOException error = assertThrows(IOException.class,
                    () -> CommandHandler.runBukkitCommand(request("invalid"), 1, TimeUnit.SECONDS));

            assertSame(failure, error.getCause());
        }
    }

    @Test
    void cancelsQueuedCommandAfterTimeout() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            BukkitScheduler scheduler = mock(BukkitScheduler.class);
            BukkitTask task = mock(BukkitTask.class);
            bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
            when(scheduler.runTask(eq(Main.plugin), any(Runnable.class))).thenReturn(task);

            IOException error = assertThrows(IOException.class,
                    () -> CommandHandler.runBukkitCommand(request("help"), 1, TimeUnit.MILLISECONDS));

            assertTrue(error.getMessage().contains("timed out"));
            verify(task).cancel();
            org.mockito.ArgumentCaptor<Runnable> command =
                    org.mockito.ArgumentCaptor.forClass(Runnable.class);
            verify(scheduler).runTask(eq(Main.plugin), command.capture());
            command.getValue().run();
            bukkit.verify(() -> Bukkit.dispatchCommand(any(CommandSender.class), anyString()), never());
        }
    }

    @Test
    void cancelsQueuedCommandWhenInterrupted() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            BukkitScheduler scheduler = mock(BukkitScheduler.class);
            BukkitTask task = mock(BukkitTask.class);
            bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
            when(scheduler.runTask(eq(Main.plugin), any(Runnable.class))).thenReturn(task);

            Thread.currentThread().interrupt();
            try {
                assertThrows(InterruptedException.class,
                        () -> CommandHandler.runBukkitCommand(request("help")));
                verify(task).cancel();
            } finally {
                Thread.interrupted();
            }
        }
    }

    @Test
    void executesDirectlyOnServerThread() throws Exception {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
            bukkit.when(() -> Bukkit.dispatchCommand(any(CommandSender.class), eq("help")))
                    .thenReturn(true);

            CommandHandler.runBukkitCommand(request("help"));

            bukkit.verify(Bukkit::getScheduler, never());
        }
    }
}
