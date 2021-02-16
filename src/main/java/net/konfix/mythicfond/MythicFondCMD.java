package net.konfix.mythicfond;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import net.milkbowl.vault.economy.EconomyResponse;
import net.konfix.mythicfond.utils.Chat;
import net.konfix.mythicfond.utils.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

@RequiredArgsConstructor
@CommandAlias("fond")
public class MythicFondCMD extends BaseCommand {

    private final MythicFond plugin;

    @Default
    @CommandPermission("MythicFond.view")
    public void sendDefault(CommandSender commandSender) {
        Chat.msg(commandSender, Message.FUNDS_MAIN.toString().replace("%amount%",
                new DecimalFormat("#,###").format(plugin.getFundController().getTotalFunds())));

        long total = plugin.getFundController().getTotalFunds();
        plugin.getFundController().getFundGoals().forEach((s, fundGoal) -> {
            long amount = fundGoal.getCost();

            float percentage = (((float)total) / amount) *100;
            if (percentage > 100) percentage = 100;
            Chat.msg(commandSender, fundGoal.getMessage()
                    .replace("%percent%", new DecimalFormat("###.##").format(percentage)));
            int completedBar = (int) percentage;
            int notCompleted = 100 - completedBar;

        });
    }

    @Subcommand("depozit")
    @CommandPermission("MythicFond.deposit")
    public void addFunds(CommandSender commandSender, long amount) {
        Player player = (Player) commandSender;
        long minAmount;
        try {
            minAmount = MythicFond.getInstance().getConfig().getLong("Settings.min-deposit");
        } catch (NullPointerException exception) {
          minAmount = 10000;
        }
        if (amount < minAmount) {
            Message.DEPOSIT_LOW.msg(player);
            return;
        }
        if (MythicFond.getEcon().has(player, amount)) {
            EconomyResponse economyResponse = MythicFond.getEcon().withdrawPlayer(player, amount);
            if (economyResponse.transactionSuccess()) {
                Chat.msg(player, Message.FUNDS_REMOVED.toString().replace("%amount%",
                        new DecimalFormat("#,###").format(amount)));
                plugin.getFundController().addMoney(player, amount);
            }
        } else {
            Message.FUNDS_NOT_ENOUGH.msg(player);
        }
    }

    @Subcommand("reload")
    @CommandPermission("MythicFond.reload")
    public void reloadFiles(CommandSender commandSender) {
        MythicFond.getInstance().getLang().reload();
        MythicFond.getInstance().reloadConfig();
        plugin.getFundController().reload();
        plugin.getFundController().checkCompleted();
        Message.CONFIGURATION_RELOAD.msg(commandSender);
    }

    @Subcommand("reset")
    @CommandPermission("MythicFond.reset")
    public void resetFunds(CommandSender commandSender) {
        MythicFond.getInstance().getFundController().setTotalFunds(0);
        Message.FUNDS_RESET.msg(commandSender);
    }
}
