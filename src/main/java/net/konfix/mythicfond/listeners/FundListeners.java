package net.konfix.mythicfond.listeners;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.konfix.mythicfond.MythicFond;
import net.konfix.mythicfond.data.FundGoal;
import net.konfix.mythicfond.utils.Message;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

@RequiredArgsConstructor
public class FundListeners implements Listener {

    private final MythicFond plugin;

    @EventHandler
    public void onCommandProcess(PlayerCommandPreprocessEvent event) {
        List<FundGoal> incompleteGoals = getIncompleteGoals();
        if (incompleteGoals.isEmpty()) return;

        for (FundGoal incompleteGoal : incompleteGoals) {
            List<String> commands = incompleteGoal.getBlockedCommands();
            if (commands.contains(event.getMessage().toLowerCase().replace("/", ""))) {
                event.setCancelled(true);
                Message.FUNDS_COMMANDS.msg(event.getPlayer());
                return;
            }
        }
    }

    public List<FundGoal> getIncompleteGoals() {
        List<FundGoal> fundGoals = Lists.newArrayList();
        plugin.getFundController().getGoals().forEach((s, aBoolean) -> {
            if (!aBoolean) fundGoals.add(plugin.getFundController().getFundGoals().get(s));
        });

        return fundGoals;
    }

}
