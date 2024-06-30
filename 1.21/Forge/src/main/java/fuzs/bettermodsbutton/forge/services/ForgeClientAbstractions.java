package fuzs.bettermodsbutton.forge.services;

import fuzs.bettermodsbutton.config.ClientConfig;
import fuzs.bettermodsbutton.forge.client.gui.components.ModsButton;
import fuzs.bettermodsbutton.forge.config.ForgeClientConfig;
import fuzs.bettermodsbutton.services.ClientAbstractions;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraftforge.client.gui.ModListScreen;
import net.minecraftforge.common.ForgeI18n;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForgeClientAbstractions implements ClientAbstractions {

    @Override
    public ClientConfig getClientConfig() {
        return ForgeClientConfig.INSTANCE;
    }

    @Override
    public int getModListSize() {
        return ModList.get().size();
    }

    @Override
    public String getModListMessage(String fallback) {
        return ForgeI18n.parseMessage("fml.menu.loadingmods", this.getModListSize());
    }

    @NotNull
    @Override
    public Button getNewModsButton(Screen screen, @Nullable Button oldButton) {
        return new ModsButton(Button.builder(CommonComponents.EMPTY, (Button button) -> {
            screen.getMinecraft().setScreen(new ModListScreen(screen));
        }).pos(screen.width / 2 - 100, screen.height / 4 + 48 + 48).size(200, 20));
    }
}
