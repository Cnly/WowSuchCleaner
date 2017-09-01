package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.Cnly.Crafter.Crafter.framework.commands.AbstractCrafterCommand;
import io.github.Cnly.Crafter.Crafter.framework.locales.ILocaleManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.Main;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.auction.AuctionConfig;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.gui.Vault;

public class VaultCommand extends AbstractCrafterCommand
{
    
    private Main main = Main.getInstance();
    private ILocaleManager localeManager = main.getLocaleManager();
    
    public VaultCommand()
    {
        this.setAction("vault");
        this.setPlayerNeeded(true);
        this.setPlayerNeededNotice(localeManager.getLocalizedString("commands.playerNeeded"));
        this.setHelp(localeManager.getLocalizedString("commands.vault.help").replace("{usage}", "/wsc vault"));
        this.setPermission("WowSuchCleaner.commands.vault");
        this.setPermissionNeededNotice(localeManager.getLocalizedString("commands.noPermission"));
    }

    @Override
    protected void executeCommand(CommandSender sender, String[] args)
    {
        new Vault(main).openFor((Player)sender);
    }
    
}
