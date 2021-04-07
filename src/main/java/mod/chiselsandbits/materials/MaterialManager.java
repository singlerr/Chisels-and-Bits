package mod.chiselsandbits.materials;

import com.google.common.collect.*;
import net.minecraft.block.material.Material;

import java.util.Arrays;
import java.util.Map;

public class MaterialManager
{
    private static final MaterialManager INSTANCE = new MaterialManager();

    public static MaterialManager getInstance()
    {
        return INSTANCE;
    }

    private final Map<Material, String> materialNames = Maps.newHashMap();
    private final Multimap<String, Material> knownMaterials = HashMultimap.create();

    private MaterialManager()
    {
          this.registerMapping( "wood", Material.WOOD );
          this.registerMapping( "rock", Material.ROCK );
          this.registerMapping( "iron", Material.IRON );
          this.registerMapping( "cloth", Material.CARPET );
          this.registerMapping( "ice", Material.ICE );
          this.registerMapping( "packed_ice", Material.PACKED_ICE );
          this.registerMapping( "clay", Material.CLAY );
          this.registerMapping( "glass", Material.GLASS );
          this.registerMapping( "sand", Material.SAND );
          this.registerMapping( "ground", Material.EARTH );
          this.registerMapping( "grass", Material.EARTH );
          this.registerMapping( "snow", Material.SNOW_BLOCK );
          this.registerMapping( "fluid", Material.WATER );
          this.registerMapping( "leaves", Material.LEAVES );
    }

    public void registerMapping(final String name, final Material material) {
        if (knownMaterials.containsKey(name))
            throw new IllegalArgumentException(String.format("The material name: %s is already registered!", name));

        knownMaterials.put(name, material);
        materialNames.put(material, name);
    }

    public Multimap<String, Material> getKnownMaterials()
    {
        return knownMaterials;
    }

    public Map<Material, String> getMaterialNames()
    {
        return materialNames;
    }
}
