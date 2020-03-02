package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.Cnly.Crafter.Crafter.framework.commands.AbstractCrafterCommand;
import io.github.Cnly.Crafter.Crafter.framework.locales.ILocaleManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.Main;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.data.auction.AuctionDataManager;

public class AuctionCommand extends AbstractCrafterCommand {

    private Main main = Main.getInstance();
    private ILocaleManager localeManager = main.getLocaleManager();
    private AuctionDataManager auctionDataManager = main.getAuctionDataManager();

    public AuctionCommand() {
        this.setAction("auction");
        this.setPlayerNeeded(true);
        this.setPlayerNeededNotice(localeManager.getLocalizedString("commands.playerNeeded"));
        this.setHelp(localeManager.getLocalizedString("commands.auction.help").replace("{usage}", "/wsc auction"));
        this.setPermission("WowSuchCleaner.commands.auction");
        this.setPermissionNeededNotice(localeManager.getLocalizedString("commands.noPermission"));
    }

    @Override
    protected void executeCommand(CommandSender sender, String[] args) {

        Player p = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();

        if (!auctionDataManager.addLot(item, p.getLocation())) {
            p.sendMessage(localeManager.getLocalizedString("commands.auction.notAuctionable"));
        } else {
            p.sendMessage(localeManager.getLocalizedString("commands.auction.success"));
            p.getInventory().setItemInMainHand(null);
        }

    }

}
