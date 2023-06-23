package main.airdrop;

import main.airdrop.SupplySignals.SupplyDrop;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TimerPlaceholder extends PlaceholderExpansion {
    @Override
    public boolean canRegister() {
        return super.canRegister();
    }


    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.equalsIgnoreCase("timer")) {
            int times2 = SupplyDrop.getTimes2();
            int minutes_all = times2 / 60;
            int hours_all = minutes_all / 60;
            int hours_min = hours_all * 60;
            int minutes = minutes_all - hours_min;
            int minutes_sec = minutes_all * 60;
            int seconds = times2 - minutes_sec;
            String timeString = hours_all + ":" + minutes + ":" + seconds;
            return timeString;
        }
        return null;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "airdrop";
    }

    @Override
    public @NotNull String getAuthor() {
        return "BotClity";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }
}
