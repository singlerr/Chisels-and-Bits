package mod.chiselsandbits.integration;

import java.util.ArrayList;
import java.util.List;

import mod.chiselsandbits.integration.JEI.IntegerationJEI;
import mod.chiselsandbits.integration.mcmultipart.IntegrationMCMP;

public class Integration extends IntegrationBase
{

	public List<IntegrationBase> integrations = new ArrayList<IntegrationBase>();

	public static final IntegerationJEI jei = new IntegerationJEI();
	public static final IntegrationMCMP mcmp = new IntegrationMCMP();

	// last.
	public static final Integration instance = new Integration();

	private Integration()
	{
		integrations.add( new IntegrationVersionChecker() );
		integrations.add( jei );
		integrations.add( mcmp );
	}

	@Override
	public void preinit()
	{
		for ( final IntegrationBase i : integrations )
		{
			i.preinit();
		}
	}

	@Override
	public void init()
	{
		for ( final IntegrationBase i : integrations )
		{
			i.init();
		}
	}

	@Override
	public void postinit()
	{
		for ( final IntegrationBase i : integrations )
		{
			i.postinit();
		}
	}
}
