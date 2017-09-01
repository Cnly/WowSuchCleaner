package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.commands.region;

import io.github.Cnly.Crafter.Crafter.framework.commands.AbstractCrafterCommand;
import io.github.Cnly.Crafter.Crafter.framework.commands.ArgumentLengthValidator;
import io.github.Cnly.Crafter.Crafter.framework.locales.ILocaleManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.ActiveCleaner;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.Main;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.RegionalConfigManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.SharedConfigManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.region.RegionConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class RegionRemoveCommand extends AbstractCrafterCommand
{
    
    private Main main = Main.getInstance();
    private FileConfiguration config = main.getConfig();
    private ILocaleManager localeManager = main.getLocaleManager();
    private SharedConfigManager sharedConfigManager = main.getSharedConfigManager();
    private RegionalConfigManager regionalConfigManager = main.getRegionalConfigManager();
    private ActiveCleaner activeCleaner = main.getActiveCleaner();
    
    public RegionRemoveCommand()
    {
        this.setAction("remove");
        this.setArgumentOffset(1);
        this.setArgumentValidator(new ArgumentLengthValidator().setExactLength(1));
        this.setPlayerNeeded(false);
        this.setHelp(localeManager.getLocalizedString("commands.region.remove.help").replace("{usage}", "/wsc region remove <region name>"));
        this.setPermission("WowSuchCleaner.commands.region.remove");
        this.setPermissionNeededNotice(localeManager.getLocalizedString("commands.noPermission"));
    }
    
    @Override
    protected void executeCommand(CommandSender sender, String[] args)
    {
    
        String regionName = args[0];
    
        if(regionName.length() == 0)
        {
            sender.sendMessage(localeManager.getLocalizedString("commands.region.emptyRegionName"));
            return;
        }
    
        RegionConfig regionConfig = regionalConfigManager.removeRegion(regionName);
        if(null == regionConfig)
        {
            sender.sendMessage(localeManager.getLocalizedString("commands.region.remove.noSuchRegion"));
            return;
        }
        
        activeCleaner.regionRemoved(regionConfig);
        
        sender.sendMessage(localeManager.getLocalizedString("commands.region.remove.success"));
        
    }
    
}
