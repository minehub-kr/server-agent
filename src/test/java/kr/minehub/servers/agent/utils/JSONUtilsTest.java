package kr.minehub.servers.agent.utils;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JSONUtilsTest {
    @Test
    void reportsUnreadableDirectoryAsIoFailure() {
        File directory = mock(File.class);
        when(directory.exists()).thenReturn(true);
        when(directory.isDirectory()).thenReturn(true);
        when(directory.getAbsolutePath()).thenReturn("/unreadable-directory");
        when(directory.listFiles()).thenReturn(null);

        IOException error = assertThrows(IOException.class, () -> JSONUtils.fileToJSON(directory, true));

        assertTrue(error.getMessage().contains("/unreadable-directory"));
    }
}
