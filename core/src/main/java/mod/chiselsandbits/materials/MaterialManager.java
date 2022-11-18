package mod.chiselsandbits.materials;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import net.minecraft.world.level.material.Material;

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
        this.registerMapping("rock", Material.STONE);
        this.registerMapping("iron", Material.METAL);
        this.registerMapping("cloth", Material.CLOTH_DECORATION);
        this.registerMapping("ice", Material.ICE);
        this.registerMapping("packed_ice", Material.ICE_SOLID);
        this.registerMapping("clay", Material.CLAY);
        this.registerMapping("glass", Material.GLASS);
        this.registerMapping("sand", Material.SAND);
        this.registerMapping("ground", Material.DIRT);
        this.registerMapping("grass", Material.GRASS);
        this.registerMapping("snow", Material.SNOW);
        this.registerMapping("fluid", Material.WATER);
        this.registerMapping("leaves", Material.LEAVES);
        this.registerMapping("plant", Material.PLANT);
        this.registerMapping("wool", Material.WOOL);
        this.registerMapping("nether_wood", Material.NETHER_WOOD);
        this.registerMapping("froglight", Material.FROGLIGHT);
        this.registerMapping("amethyst", Material.AMETHYST);

        this.registerRemapping(Material.SPONGE, Material.CLAY);
        this.registerRemapping(Material.HEAVY_METAL, Material.METAL);
        this.registerRemapping(Material.VEGETABLE, Material.PLANT);
        this.registerRemapping(Material.CACTUS, Material.PLANT);
        this.registerRemapping(Material.WEB, Material.PLANT);
        this.registerRemapping(Material.EXPLOSIVE, Material.STONE);
        this.registerRemapping(Material.LAVA, Material.WATER);
        this.registerRemapping(Material.MOSS, Material.PLANT);
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
