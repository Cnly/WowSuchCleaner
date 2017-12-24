package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.region;

import org.bukkit.Location;

public class WorldRegion extends WSCApproxBoxRegion
{
    
    public WorldRegion(String worldName)
    {
        super(worldName, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE);
    }
    
    @Override
    public boolean isInRegion(Location l)
    {
        return l.getWorld().getName().equals(this.getWorldName());
    }
    
}
