package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.commands;

import org.bukkit.command.CommandSender;

import io.github.Cnly.Crafter.Crafter.framework.commands.AbstractCrafterCommand;
import io.github.Cnly.Crafter.Crafter.framework.locales.ILocaleManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.Main;

public class ReloadCommand extends AbstractCrafterCommand
{
    
    private Main main = Main.getInstance();
    private ILocaleManager localeManager = main.getLocaleManager();
    
    public ReloadCommand()
    {
        this.setAction("reload");
        this.setHelp(localeManager.getLocalizedString("commands.reload.help").replace("{usage}", "/wsc reload"));
        this.setPermission("WowSuchCleaner.commands.reload");
        this.setPermissionNeededNotice(localeManager.getLocalizedString("commands.noPermission"));
    }
    
    @Override
    protected void executeCommand(CommandSender sender, String[] args)
    {
        
        main.onDisable();
        main.reloadConfig();
        main.onEnable();
        
        main = Main.getInstance();
        localeManager = main.getLocaleManager();
        
        sender.sendMessage(localeManager.getLocalizedString("commands.reload.reloaded"));
        
    }
    
}
