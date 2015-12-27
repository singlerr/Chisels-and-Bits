package mod.chiselsandbits.integration;

import mod.chiselsandbits.ChiselsAndBits;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class IntegrationVersionChecker extends IntegrationBase
{
	@Override
	public void init()
	{
		final NBTTagCompound compound = new NBTTagCompound();
		compound.setString( "curseProjectName", "chisels-bits" );
		compound.setString( "curseFilenameParser", "chiselsandbits-[].jar" );
		FMLInterModComms.sendRuntimeMessage( ChiselsAndBits.MODID, "VersionChecker", "addCurseCheck", compound );
	}
}
