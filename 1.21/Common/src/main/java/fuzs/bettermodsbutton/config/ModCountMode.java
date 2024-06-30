package fuzs.bettermodsbutton.config;

import fuzs.bettermodsbutton.service.ClientAbstractions;
import org.jetbrains.annotations.Nullable;

public enum ModCountMode {
    COMPACT, ADAPTIVE, NONE;

    @Nullable
    public String getString(boolean isCompact) {
        if (this == NONE) {
            return null;
        } else if (isCompact || this == COMPACT) {
            return String.valueOf(ClientAbstractions.INSTANCE.getModListSize());
        } else {
            return ClientAbstractions.INSTANCE.getModListMessage("%s Loaded");
        }
    }
}
