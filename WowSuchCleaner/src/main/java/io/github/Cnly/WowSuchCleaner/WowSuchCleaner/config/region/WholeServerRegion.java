package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.region;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * A special region object that stands for server default.
 */
public class WholeServerRegion extends WSCApproxBoxRegion
{
    public WholeServerRegion()
    {
        super("", Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE);
    }
    
    @Override
    public boolean isInRegion(Player p)
    {
        return true;
    }
    
    @Override
    public boolean isInRegion(Location l)
    {
        return true;
    }
    
}
