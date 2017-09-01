package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.region;

import io.github.Cnly.Crafter.Crafter.utils.regions.boxregions.ApproximateBoxRegion;
import org.bukkit.Location;

public class WSCApproxBoxRegion extends ApproximateBoxRegion
{
    public WSCApproxBoxRegion(String worldName, int maxX, int minX, int maxY, int minY, int maxZ, int minZ)
    {
        super(worldName, maxX, minX, maxY, minY, maxZ, minZ);
    }
    
    public WSCApproxBoxRegion(Location loc1, Location loc2)
    {
        super(loc1, loc2);
    }
    
    /**
     * Use identity hashcode because region objects are unique.
     * @return identity hashcode provided by {@code System.identityHashCode}
     */
    @Override
    public int hashCode()
    {
        return System.identityHashCode(this);
    }
    
    /**
     * Use identical comparison operator because region objects are unique.
     * @param obj object to compare with
     * @return if two objects are identical
     */
    @Override
    public boolean equals(Object obj)
    {
        return super.equals(obj);
    }
    
    public boolean isTheSameArea(ApproximateBoxRegion other)
    {
        if(this.getMaxX() != other.getMaxX())
        {
            return false;
        }
        else if(this.getMaxY() != other.getMaxY())
        {
            return false;
        }
        else if(this.getMaxZ() != other.getMaxZ())
        {
            return false;
        }
        else if(this.getMinX() != other.getMinX())
        {
            return false;
        }
        else if(this.getMinY() != other.getMinY())
        {
            return false;
        }
        else if(this.getMinZ() != other.getMinZ())
        {
            return false;
        }
        else
        {
            if(this.getWorldName() == null)
            {
                if(other.getWorldName() != null)
                {
                    return false;
                }
            }
            else if(!this.getWorldName().equals(other.getWorldName()))
            {
                return false;
            }
            return true;
        }
    }
    
}
