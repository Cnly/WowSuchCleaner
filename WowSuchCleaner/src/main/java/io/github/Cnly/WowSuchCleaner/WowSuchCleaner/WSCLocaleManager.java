package io.github.Cnly.WowSuchCleaner.WowSuchCleaner;

import io.github.Cnly.Crafter.Crafter.framework.locales.CrafterLocaleManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

public class WSCLocaleManager extends CrafterLocaleManager
{
    
    public WSCLocaleManager(String locale, File localeDirectory, boolean copyDefault,
                            JavaPlugin jp)
    {
        super(locale, localeDirectory, copyDefault, jp);
    }
    
    @Override
    public void loadLocaleFile()
    {
    
        File localeFile = new File(this.localeDirectory, this.locale + ".yml");
    
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(localeFile), "utf-8"));
        }
        catch(UnsupportedEncodingException e)
        {
            throw new RuntimeException("Unsupported encoding: utf-8");
        }
        catch(FileNotFoundException e)
        {
            throw new RuntimeException("Locale file: " + this.locale + ".yml does not exist!");
        }
    
        YamlConfiguration yc = new YamlConfiguration();
        try
        {
            yc.load(reader);
        }
        catch(FileNotFoundException e)
        {
            throw new RuntimeException("Locale file: " + this.locale + ".yml not found!", e);
        }
        catch(IOException e)
        {
            throw new RuntimeException("IOException occurred while loading locale file: " + this.locale + ".yml!", e);
        }
        catch(InvalidConfigurationException e)
        {
            throw new RuntimeException("Locale file: " + this.locale + ".yml is not valid!", e);
        }
    
        for(String key : yc.getKeys(true))
        {
        
            Object o = yc.get(key);
        
            if(o instanceof ConfigurationSection)
                continue;
        
            this.stringMappings.put(key, ChatColor.translateAlternateColorCodes('&', (String)o));
        
        }
    
    }
    
}
