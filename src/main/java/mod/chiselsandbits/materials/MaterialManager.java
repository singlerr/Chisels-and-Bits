package mod.chiselsandbits.materials;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import net.minecraft.block.material.Material;

import java.util.Map;

public class MaterialManager
{
    private static final MaterialManager INSTANCE = new MaterialManager();
    private final Map<Material, String>      materialNames     = Maps.newHashMap();
    private final Multimap<String, Material> knownMaterials    = HashMultimap.create();
    private final Map<Material, Material>    materialRemapping = Maps.newHashMap();
    private MaterialManager()
    {
        this.registerMapping("wood", Material.WOOD);
        this.registerMapping("rock", Material.ROCK);
        this.registerMapping("iron", Material.IRON);
        this.registerMapping("cloth", Material.CARPET);
        this.registerMapping("ice", Material.ICE);
        this.registerMapping("packed_ice", Material.PACKED_ICE);
        this.registerMapping("clay", Material.CLAY);
        this.registerMapping("glass", Material.GLASS);
        this.registerMapping("sand", Material.SAND);
        this.registerMapping("ground", Material.EARTH);
        this.registerMapping("grass", Material.ORGANIC);
        this.registerMapping("snow", Material.SNOW_BLOCK);
        this.registerMapping("fluid", Material.WATER);
        this.registerMapping("leaves", Material.LEAVES);
        this.registerMapping("plant", Material.PLANTS);

        this.registerRemapping(Material.SPONGE, Material.CLAY);
        this.registerRemapping(Material.ANVIL, Material.IRON);
        this.registerRemapping(Material.GOURD, Material.PLANTS);
        this.registerRemapping(Material.CACTUS, Material.PLANTS);
        this.registerRemapping(Material.CORAL, Material.ROCK);
        this.registerRemapping(Material.WEB, Material.PLANTS);
        this.registerRemapping(Material.TNT, Material.ROCK);
    }

    public void registerMapping(final String name, final Material material)
    {
        if (knownMaterials.containsKey(name))
        {
            throw new IllegalArgumentException(String.format("The material name: %s is already registered!", name));
        }

        knownMaterials.put(name, material);
        materialNames.put(material, name);
    }

    public void registerRemapping(final Material material, final Material target)
    {
        if (materialRemapping.containsKey(material))
        {
            throw new IllegalArgumentException("Can not remap a material twice!");
        }

        if (knownMaterials.containsValue(material))
        {
            throw new IllegalArgumentException("Can not remap a material which is registered as a working material.");
        }

        if (!knownMaterials.containsValue(target))
        {
            throw new IllegalArgumentException("Can not remap a material to a material which is not registered as a working material.");
        }

        this.materialRemapping.put(material, target);
    }

    public static MaterialManager getInstance()
    {
        return INSTANCE;
    }

    public Multimap<String, Material> getKnownMaterials()
    {
        return knownMaterials;
    }

    public Map<Material, String> getMaterialNames()
    {
        return materialNames;
    }

    public Material remapMaterialIfNeeded(final Material material)
    {
        return materialRemapping.getOrDefault(material, material);
    }
}
