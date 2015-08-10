package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.Cnly.Crafter.Crafter.framework.commands.AbstractCrafterCommand;
import io.github.Cnly.Crafter.Crafter.framework.locales.CrafterLocaleManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.Main;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.gui.LotShowcase;

public class ShowcaseCommand extends AbstractCrafterCommand
{
    
    private Main main = Main.getInstance();
    private CrafterLocaleManager localeManager = main.getLocaleManager();
    
    public ShowcaseCommand()
    {
        this.setAction("showcase");
        this.setPlayerNeeded(true);
        this.setPlayerNeededNotice(localeManager.getLocalizedString("commands.playerNeeded"));
        this.setHelp(localeManager.getLocalizedString("commands.showcase.help").replace("{usage}", "/wsc showcase"));
        this.setPermission("WowSuchCleaner.commands.showcase");
        this.setPermissionNeededNotice(localeManager.getLocalizedString("commands.noPermission"));
    }

    @Override
    protected void executeCommand(CommandSender sender, String[] args)
    {
        
        new LotShowcase(localeManager).openFor((Player)sender, 1);
        
    }
    
}
