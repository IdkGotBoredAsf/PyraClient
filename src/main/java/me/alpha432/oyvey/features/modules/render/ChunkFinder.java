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
import net.minecraft.world.chunk.WorldChunk;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChunkFinder extends Module {
    // ===== Settings =====
    private final Setting<Integer> threshold = register(new Setting<>("Threshold", 4, 1, 20));
    private final Setting<Boolean> highlightChunks = register(new Setting<>("HighlightChunks", true));
    private final Setting<Boolean> highlightBlocks = register(new Setting<>("HighlightBlocks", true));
    private final Setting<Boolean> flatMode = register(new Setting<>("FlatMode", false));
    private final Setting<Integer> r = register(new Setting<>("Red", 255, 0, 255));
    private final Setting<Integer> g = register(new Setting<>("Green", 0, 0, 255));
    private final Setting<Integer> b = register(new Setting<>("Blue", 255, 0, 255));
    private final Setting<Integer> a = register(new Setting<>("Alpha", 80, 0, 255));

    // ===== Data =====
    private final Set<ChunkPos> suspiciousChunks = new HashSet<>();
    private final Map<ChunkPos, Set<BlockPos>> suspiciousBlocks = new HashMap<>();

    public ChunkFinder() {
        super("ChunkFinder", "Highlights suspicious deepslate above Y8", Category.RENDER, true, false, false);
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

        for (WorldChunk chunk : mc.world.getChunkManager().getLoadedChunksIterable()) {
            scanChunk(chunk);
        }
    }

    private void scanChunk(WorldChunk chunk) {
        ChunkPos pos = chunk.getPos();
        Set<BlockPos> found = new HashSet<>();

        int startX = pos.getStartX();
        int startZ = pos.getStartZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 8; y < mc.world.getTopY(); y++) {
                    BlockPos bp = new BlockPos(startX + x, y, startZ + z);
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
        Color color = new Color(r.getValue(), g.getValue(), b.getValue(), a.getValue());

        // Draw suspicious chunks
        if (highlightChunks.getValue()) {
            for (ChunkPos pos : suspiciousChunks) {
                int startX = pos.getStartX();
                int startZ = pos.getStartZ();
                int endX = pos.getEndX();
                int endZ = pos.getEndZ();

                if (flatMode.getValue()) {
                    // Flat highlight at Y=64
                    Box box = new Box(startX, 64, startZ, endX + 1, 65, endZ + 1);
                    RenderUtil.drawBox(event.getMatrix(), box, color, 1f);
                } else {
                    // Full chunk column
                    Box box = new Box(startX, mc.world.getBottomY(), startZ,
                                      endX + 1, mc.world.getTopY(), endZ + 1);
                    RenderUtil.drawBox(event.getMatrix(), box, color, 1f);
                }
            }
        }

        // Draw suspicious blocks
        if (highlightBlocks.getValue()) {
            for (Set<BlockPos> blocks : suspiciousBlocks.values()) {
                for (BlockPos bp : blocks) {
                    Box box = new Box(bp).expand(0.002);
                    RenderUtil.drawBox(event.getMatrix(), box, color, 1f);
                }
            }
        }
    }
}
