package kr.minehub.servers.agent.utils;

import org.bukkit.Location;
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
}
