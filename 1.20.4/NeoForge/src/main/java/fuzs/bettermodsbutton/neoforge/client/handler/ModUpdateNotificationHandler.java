package fuzs.bettermodsbutton.neoforge.client.handler;

import fuzs.bettermodsbutton.ClientAbstractions;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.neoforge.client.gui.TitleScreenModUpdateIndicator;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public class ModUpdateNotificationHandler {
    @Nullable
    public static TitleScreenModUpdateIndicator modUpdateNotification;

    public static void setModUpdateNotification(Screen screen, @Nullable Button button) {
        TitleScreenModUpdateIndicator indicator = makeModUpdateNotification(screen, button);
        if (indicator != null) {
            if (screen instanceof TitleScreen) {
                try {
                    Field modUpdateNotificationField = TitleScreen.class.getDeclaredField("modUpdateNotification");
                    modUpdateNotificationField.setAccessible(true);
                    MethodHandles.lookup().unreflectSetter(modUpdateNotificationField).invoke(screen, indicator);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            } else {
                modUpdateNotification = indicator;
            }
        }
    }

    @Nullable
    private static TitleScreenModUpdateIndicator makeModUpdateNotification(Screen screen, @Nullable Button button) {
        if (ClientAbstractions.getClientConfig().getUpdateNotification().get() && button != null) {
            TitleScreenModUpdateIndicator indicator = new TitleScreenModUpdateIndicator(button);
            indicator.resize(screen.getMinecraft(), screen.width, screen.height);
            indicator.init();
            return indicator;
        } else if (button == null) {
            // condition is configured precisely so that we can disable the indiciator here
            return new TitleScreenModUpdateIndicator(null);
        }
        return null;
    }

    public static void onAfterRenderScreen(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (screen.getClass() == PauseScreen.class) {
            // this will still be null if we are on an empty pause screen (from pressing F3 + Esc)
            if (modUpdateNotification != null) {
                modUpdateNotification.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }
    }

    public static void onScreenClosing(Screen screen) {
        if (screen.getClass() == PauseScreen.class) {
            modUpdateNotification = null;
        }
    }
}
