package me.alpha432.oyvey.features.modules.render;

import com.google.common.eventbus.Subscribe;
import me.alpha432.oyvey.event.impl.Render3DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.chunk.Chunk;

import java.awt.*;

public class ChunkDeepslateHighlighter extends Module {
    public ChunkDeepslateHighlighter() {
        super("ChunkDeepslateHighlighter", "Highlights chunk layer if rotated deepslate/deepslate is found above Y=8", Category.RENDER, true, false, false);
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        // Get the player's current chunk
        BlockPos playerPos = mc.player.getBlockPos();
        Chunk chunk = mc.world.getChunk(playerPos);

        // Scan chunk for deepslate / rotated deepslate above Y=8
        boolean found = false;
        int targetY = -1;

        outerLoop:
        for (int x = 0; x < 16; x++) {
            for (int y = 9; y < mc.world.getTopY(); y++) { // Start at y=9
                for (int z = 0; z < 16; z++) {
                    BlockPos checkPos = chunk.getPos().getStartPos().add(x, y, z);
                    Block block = mc.world.getBlockState(checkPos).getBlock();

                    if (block == Blocks.DEEPSLATE || block.toString().toLowerCase().contains("deepslate")) {
                        found = true;
                        targetY = y;
                        break outerLoop;
                    }
                }
            }
        }

        if (found && targetY > 8) {
            // Highlight a thin layer across the entire chunk at the Y level
            BlockPos chunkStart = chunk.getPos().getStartPos();
            BlockPos chunkEnd = chunkStart.add(15, 0, 15);

            Box layer = new Box(
                chunkStart.getX(), targetY, chunkStart.getZ(),
                chunkEnd.getX() + 1, targetY + 0.05, chunkEnd.getZ() + 1
            );

            RenderUtil.drawBox(event.getMatrix(), layer, Color.CYAN, 2f);
        }
    }
}
