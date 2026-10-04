package kr.minehub.servers.agent.utils;

import org.bukkit.Location;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BukkitUtilsTest {
    public interface ModernWorld extends World {
        int getMinHeight();
    }

    private <T extends World> T prepareWorld(T world) {
        when(world.getName()).thenReturn("test-world");
        when(world.getWorldType()).thenReturn(WorldType.NORMAL);
        when(world.getEnvironment()).thenReturn(World.Environment.NORMAL);
        when(world.getSpawnLocation()).thenReturn(new Location(world, 0, 64, 0));
        return world;
    }

    @Test
    void reportsNegativeMinimumHeightFromModernWorld() {
        ModernWorld world = prepareWorld(mock(ModernWorld.class));
        when(world.getMinHeight()).thenReturn(-64);

        JSONObject result = BukkitUtils.getWorldJSON(world);

        assertEquals(-64, result.get("minHeight"));
    }

    @Test
    void reportsCustomMinimumHeightInsteadOfHardcodingOverworldHeight() {
        ModernWorld world = prepareWorld(mock(ModernWorld.class));
        when(world.getMinHeight()).thenReturn(-128);

        assertEquals(-128, BukkitUtils.getWorldJSON(world).get("minHeight"));
    }

    @Test
    void fallsBackToZeroWhenOldWorldApiHasNoMinimumHeight() {
        World world = prepareWorld(mock(World.class));

        assertEquals(0, BukkitUtils.getWorldJSON(world).get("minHeight"));
    }

    @Test
    void serializesPlayerWithoutAddressOrLocationWorld() {
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(java.util.UUID.randomUUID());
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(player.getLocation()).thenReturn(new Location(null, 1, 2, 3));

        JSONObject result = BukkitUtils.getPlayerJSON(player);

        assertTrue(result.containsKey("ip"));
        assertNull(result.get("ip"));
        JSONObject location = (JSONObject) result.get("location");
        assertNull(location.get("world"));
        assertEquals(1.0, location.get("x"));
    }

    @Test
    void serializesWorldWithoutLegacyWorldType() {
        World world = prepareWorld(mock(World.class));
        when(world.getWorldType()).thenReturn(null);

        JSONObject result = BukkitUtils.getWorldJSON(world);

        assertTrue(result.containsKey("worldType"));
        assertNull(result.get("worldType"));
    }
}
