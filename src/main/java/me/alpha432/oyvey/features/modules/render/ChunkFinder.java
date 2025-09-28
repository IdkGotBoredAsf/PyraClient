package me.alpha432.oyvey.features.modules.render;

import com.google.common.eventbus.Subscribe;
import me.alpha432.oyvey.event.impl.Render3DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import me.alpha432.oyvey.util.render.RenderUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChunkFinder extends Module {
    // Settings
    private final Setting<Integer> threshold = register(new Setting<>("Threshold", 4, 1, 20));
    private final Setting<Boolean> highlightChunks = register(new Setting<>("HighlightChunks", true));
    private final Setting<Boolean> highlightBlocks = register(new Setting<>("HighlightBlocks", true));
    private final Setting<Boolean> flatMode = register(new Setting<>("FlatMode", false));
    private final Setting<Integer> red = register(new Setting<>("Red", 255, 0, 255));
    private final Setting<Integer> green = register(new Setting<>("Green", 0, 0, 255));
    private final Setting<Integer> blue = register(new Setting<>("Blue", 255, 0, 255));
    private final Setting<Integer> alpha = register(new Setting<>("Alpha", 80, 0, 255));

    // Data
    private final Set<ChunkPos> suspiciousChunks = new HashSet<>();
    private final Map<ChunkPos, Set<BlockPos>> suspiciousBlocks = new HashMap<>();

    public ChunkFinder() {
        super("ChunkFinder", "Highlights suspicious deepslate / rotated deepslate above Y=8", Category.RENDER, true, false, false);
    }

    @Override
    public void onEnable() {
        suspiciousChunks.clear();
        suspiciousBlocks.clear();
    }

    @Override
    public void onUpdate() {
        if (mc.world == null) return;

        suspiciousChunks.clear();
        suspiciousBlocks.clear();

        // ✅ Correct iteration for 1.21.5
        for (WorldChunk chunk : mc.world.getChunkManager().chunks.values()) {
            if (chunk != null) {
                scanChunk(chunk);
            }
        }
    }

    private void scanChunk(WorldChunk chunk) {
        ChunkPos pos = chunk.getPos();
        Set<BlockPos> found = new HashSet<>();

        int startX = pos.getStartX();
        int startZ = pos.getStartZ();

        // Use heightmap to determine top Y (WORLD_SURFACE is typical)
        int topY = mc.world.getTopY(Heightmap.Type.WORLD_SURFACE, startX, startZ);

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                for (int y = 8; y < topY; y++) {
                    BlockPos bp = new BlockPos(startX + dx, y, startZ + dz);
                    BlockState state = chunk.getBlockState(bp);
                    if (isRotatedDeepslate(state)) {
                        found.add(bp);
                    }
                }
            }
        }

        if (found.size() >= threshold.getValue()) {
            suspiciousChunks.add(pos);
            suspiciousBlocks.put(pos, found);
        }
    }

    private boolean isRotatedDeepslate(BlockState state) {
        if (state == null) return false;
        if (!state.contains(Properties.AXIS)) return false;
        if (state.get(Properties.AXIS) == Direction.Axis.Y) return false;
        return state.isOf(Blocks.DEEPSLATE) ||
               state.isOf(Blocks.POLISHED_DEEPSLATE) ||
               state.isOf(Blocks.DEEPSLATE_BRICKS) ||
               state.isOf(Blocks.DEEPSLATE_TILES) ||
               state.isOf(Blocks.CHISELED_DEEPSLATE);
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        Color color = new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue());

        if (highlightChunks.getValue()) {
            for (ChunkPos pos : suspiciousChunks) {
                int startX = pos.getStartX();
                int startZ = pos.getStartZ();
                int endX = pos.getEndX();
                int endZ = pos.getEndZ();

                if (flatMode.getValue()) {
                    // just a thin horizontal slice at Y = 64
                    Box box = new Box(startX, 64, startZ, endX + 1, 65, endZ + 1);
                    RenderUtil.drawBox(event.getMatrix(), box, color, 1f);
                } else {
                    // full vertical up to surface
                    int topY = mc.world.getTopY(Heightmap.Type.WORLD_SURFACE, startX, startZ);
                    Box box = new Box(startX, mc.world.getBottomY(), startZ, endX + 1, topY, endZ + 1);
                    RenderUtil.drawBox(event.getMatrix(), box, color, 1f);
                }
            }
        }

        if (highlightBlocks.getValue()) {
            for (Set<BlockPos> blocks : suspiciousBlocks.values()) {
                for (BlockPos bp : blocks) {
                    // Expand slightly so box is visible
                    Box box = new Box(bp).expand(0.002);
                    RenderUtil.drawBox(event.getMatrix(), box, color, 1f);
                }
            }
        }
    }
}
