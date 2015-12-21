package mod.chiselsandbits.integration;

import mod.chiselsandbits.ChiselsAndBits;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class Integration
{

	public static final IntegerationJEI jei = new IntegerationJEI();

	private Integration()
	{

	}

	private static void initVersionChecker()
	{
		final NBTTagCompound compound = new NBTTagCompound();
		compound.setString( "curseProjectName", "chisels-bits" );
		compound.setString( "curseFilenameParser", "chiselsandbits-[].jar" );
		FMLInterModComms.sendRuntimeMessage( ChiselsAndBits.MODID, "VersionChecker", "addCurseCheck", compound );
	}

	public static void preinit()
	{
		initVersionChecker();
	}

	public static void init()
	{
		jei.init();
	}

}
