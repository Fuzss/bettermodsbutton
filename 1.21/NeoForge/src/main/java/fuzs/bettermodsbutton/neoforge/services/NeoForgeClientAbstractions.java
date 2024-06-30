package fuzs.bettermodsbutton.neoforge.services;

import fuzs.bettermodsbutton.config.ClientConfig;
import fuzs.bettermodsbutton.neoforge.config.NeoForgeClientConfig;
import fuzs.bettermodsbutton.services.ClientAbstractions;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.neoforged.fml.ModList;
import net.neoforged.fml.i18n.FMLTranslations;
import net.neoforged.neoforge.client.gui.ModListScreen;
import net.neoforged.neoforge.client.gui.widget.ModsButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NeoForgeClientAbstractions implements ClientAbstractions {

    @Override
    public ClientConfig getClientConfig() {
        return NeoForgeClientConfig.INSTANCE;
    }

    @Override
    public int getModListSize() {
        return ModList.get().size();
    }

    @Override
    public String getModListMessage(String fallback) {
        return FMLTranslations.parseMessageWithFallback("fml.menu.loadingmods", () -> fallback, this.getModListSize());
    }

    @NotNull
    @Override
    public Button getNewModsButton(Screen screen, @Nullable Button oldButton) {
        return oldButton != null ?
                oldButton :
                new ModsButton(Button.builder(CommonComponents.EMPTY, (Button button) -> {
                    screen.getMinecraft().setScreen(new ModListScreen(screen));
                }).pos(screen.width / 2 - 100, screen.height / 4 + 48 + 48).size(200, 20));
    }
}
