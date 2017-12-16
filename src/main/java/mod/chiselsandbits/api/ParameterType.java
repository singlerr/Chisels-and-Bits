package mod.chiselsandbits.api;

/**
 * use these Enums to retrieve various settings or runtime values via
 * {@link IChiselAndBitsAPI}.getParameter
 */
public interface ParameterType<T extends Object>
{

	public ParamTypes getType();

	public static enum ParamTypes
	{
		BOOLEAN,
		FLOAT,
		DOUBLE,
		INTEGER
	};

	public static enum BooleanParam implements ParameterType<Boolean>
	{
		ENABLE_MCMP(/* enableMCMultipart */),
		ENABLE_DAMAGE_TOOLS(/* damageTools */),
		ENABLE_BIT_LIGHT_SOURCE(/* enableBitLightSource */);

		@Override
		public mod.chiselsandbits.api.ParameterType.ParamTypes getType()
		{
			return ParamTypes.BOOLEAN;
		}
	};

	public static enum FloatParam implements ParameterType<Float>
	{
		BLOCK_FULL_LIGHT_PERCENTAGE(/* bitLightPercentage */);

		@Override
		public mod.chiselsandbits.api.ParameterType.ParamTypes getType()
		{
			return ParamTypes.FLOAT;
		}
	}

	public static enum DoubleParam implements ParameterType<Double>
	{
		BIT_MAX_DRAWN_REGION_SIZE(/* maxDrawnRegionSize */);

		@Override
		public mod.chiselsandbits.api.ParameterType.ParamTypes getType()
		{
			return ParamTypes.DOUBLE;
		}
	}

	public static enum IntegerParam implements ParameterType<Integer>
	{
		BIT_BAG_MAX_STACK_SIZE(/* bagStackSize */);

		@Override
		public mod.chiselsandbits.api.ParameterType.ParamTypes getType()
		{
			return ParamTypes.INTEGER;
		}
	}

}
