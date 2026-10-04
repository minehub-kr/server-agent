package kr.minehub.servers.agent.websocket;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class FileCommandTest {
    @TempDir
    Path directory;

    private JSONObject request(String... fields) {
        JSONObject data = new JSONObject();
        for (int index = 0; index < fields.length; index += 2) {
            data.put(fields[index], fields[index + 1]);
        }
        JSONObject request = new JSONObject();
        request.put("data", data);
        return request;
    }

    @Test
    void movesFileWithoutLosingContents() throws Exception {
        Path source = Files.write(directory.resolve("source.txt"), "original".getBytes(StandardCharsets.UTF_8));
        Path destination = directory.resolve("destination.txt");

        JSONObject response = CommandHandler.moveLocalFile(request(
                "from", source.toString(), "to", destination.toString()));

        assertEquals(true, response.get("success"));
        assertFalse(Files.exists(source));
        assertEquals("original", new String(Files.readAllBytes(destination), StandardCharsets.UTF_8));
    }

    @Test
    void preservesBothFilesWhenDestinationAlreadyExists() throws Exception {
        Path source = Files.write(directory.resolve("source.txt"), new byte[] { 1 });
        Path destination = Files.write(directory.resolve("destination.txt"), new byte[] { 2 });

        assertThrows(IOException.class, () -> CommandHandler.moveLocalFile(request(
                "from", source.toString(), "to", destination.toString())));

        assertArrayEquals(new byte[] { 1 }, Files.readAllBytes(source));
        assertArrayEquals(new byte[] { 2 }, Files.readAllBytes(destination));
    }

    @Test
    void uploadsToBareFilename() throws Exception {
        Path destination = Paths.get("minehub-upload-test-" + UUID.randomUUID());
        try {
            CommandHandler.uploadFile(request("path", destination.toString(),
                    "contents", Base64.getEncoder().encodeToString(new byte[] { 42 })));
            assertArrayEquals(new byte[] { 42 }, Files.readAllBytes(destination));
        } finally {
            Files.deleteIfExists(destination);
        }
    }

    @Test
    void downloadsToBareFilename() throws Exception {
        Path source = Files.write(directory.resolve("download.txt"), new byte[] { 42 });
        Path destination = Paths.get("minehub-download-test-" + UUID.randomUUID());
        try {
            CommandHandler.downloadFile(request("url", source.toUri().toString(),
                    "path", destination.toString()));
            assertArrayEquals(new byte[] { 42 }, Files.readAllBytes(destination));
        } finally {
            Files.deleteIfExists(destination);
        }
    }

    @Test
    void rejectsInvalidContentsBeforeCreatingFileOrParent() {
        Path destination = directory.resolve("new-parent/file.txt");
        assertThrows(IOException.class, () -> CommandHandler.uploadFile(request(
                "path", destination.toString(), "contents", "not-base64!")));
        assertFalse(Files.exists(destination.getParent()));
    }

    @Test
    void reportsMissingUploadContentsWithoutChangingExistingFile() throws Exception {
        Path destination = Files.write(directory.resolve("existing.txt"), new byte[] { 7 });
        assertThrows(IOException.class, () -> CommandHandler.uploadFile(request("path", destination.toString())));
        assertArrayEquals(new byte[] { 7 }, Files.readAllBytes(destination));
    }
}
