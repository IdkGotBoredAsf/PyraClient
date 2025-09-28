package me.alpha432.oyvey.features.modules.client;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.impl.Render2DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.util.TextUtil;

import java.awt.*;

public class HudModule extends Module {
    public HudModule() {
        super("Hud", "Displays client info on screen", Category.CLIENT, true, false, false);
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        String text = TextUtil.text("{global} %s {} %s", OyVey.NAME, OyVey.VERSION);

        // Example: use the color manager for dynamic HUD color
        Color hudColor = new Color(
            OyVey.colorManager.getColor().getRed(),
            OyVey.colorManager.getColor().getGreen(),
            OyVey.colorManager.getColor().getBlue(),
            255
        );

        event.getContext().drawTextWithShadow(
            mc.textRenderer,
            text,
            4, 4,  // position
            hudColor.getRGB()
        );
    }
}
