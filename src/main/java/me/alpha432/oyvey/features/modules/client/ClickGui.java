package me.alpha432.oyvey.features.modules.client;

import com.google.common.eventbus.Subscribe;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.impl.ClientEvent;
import me.alpha432.oyvey.features.commands.Command;
import me.alpha432.oyvey.features.gui.OyVeyGui;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import org.lwjgl.glfw.GLFW;

/**
 * Modernized ClickGUI module with smoother behavior and cleaner color management.
 */
public class ClickGui extends Module {
    private static ClickGui INSTANCE = new ClickGui();

    // General settings
    public Setting<String> prefix = str("Prefix", ".");
    public Setting<Boolean> rainbow = bool("Rainbow", false);

    // Base colors
    public Setting<Integer> red = num("Red", 0, 0, 255);
    public Setting<Integer> green = num("Green", 120, 0, 255);
    public Setting<Integer> blue = num("Blue", 255, 0, 255);
    public Setting<Integer> alpha = num("Alpha", 200, 0, 255);

    // Gradient / secondary colors
    public Setting<Integer> topRed = num("SecondRed", 60, 0, 255);
    public Setting<Integer> topGreen = num("SecondGreen", 180, 0, 255);
    public Setting<Integer> topBlue = num("SecondBlue", 255, 0, 255);
    public Setting<Integer> hoverAlpha = num("HoverAlpha", 240, 0, 255);

    // Rainbow controls
    public Setting<Integer> rainbowHue = num("HueSpeed", 240, 0, 600);
    public Setting<Float> rainbowBrightness = num("Brightness", 200f, 1f, 255f);
    public Setting<Float> rainbowSaturation = num("Saturation", 200f, 1f, 255f);

    public ClickGui() {
        super("ClickGui", "Opens a modern smoother ClickGUI", Category.CLIENT, true, false, false);
        setBind(GLFW.GLFW_KEY_RIGHT_SHIFT);
        rainbowHue.setVisibility(v -> rainbow.getValue());
        rainbowBrightness.setVisibility(v -> rainbow.getValue());
        rainbowSaturation.setVisibility(v -> rainbow.getValue());
        setInstance();
    }

    public static ClickGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGui();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Subscribe
    public void onSettingChange(ClientEvent event) {
        if (event.getStage() == 2 && event.getSetting().getFeature().equals(this)) {
            if (event.getSetting().equals(this.prefix)) {
                OyVey.commandManager.setPrefix(this.prefix.getPlannedValue());
                Command.sendMessage("Prefix set to §b%s", OyVey.commandManager.getPrefix());
            }
            updateColors();
        }
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) return;
        mc.setScreen(new OyVeyGui(true)); // pass a flag to use new smooth design
    }

    @Override
    public void onLoad() {
        updateColors();
        OyVey.commandManager.setPrefix(this.prefix.getValue());
    }

    @Override
    public void onTick() {
        if (!(mc.currentScreen instanceof OyVeyGui)) {
            disable();
        }
    }

    private void updateColors() {
        OyVey.colorManager.setColor(
            this.red.getPlannedValue(),
            this.green.getPlannedValue(),
            this.blue.getPlannedValue(),
            this.alpha.getPlannedValue()
        );
    }
}
