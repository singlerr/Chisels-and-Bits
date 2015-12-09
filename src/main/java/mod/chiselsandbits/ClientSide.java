
package mod.chiselsandbits;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.ItemBlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.IntegerBox;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.items.ItemChisel;
import mod.chiselsandbits.items.ItemChiseledBit;
import mod.chiselsandbits.network.NetworkRouter;
import mod.chiselsandbits.network.packets.ChiselPacket;
import mod.chiselsandbits.render.SculptureModelGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.Type;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class ClientSide
{

	public final static ClientSide instance = new ClientSide();
	private final Random rand = new Random();

	public void preinit(
			final ChiselsAndBits mod )
	{
		ModelLoaderRegistry.registerLoader( new SculptureModelGenerator() );
	}

	public void init(
			final ChiselsAndBits chiselsandbits )
	{
		for ( ChiselMode mode : ChiselMode.values() )
		{
			KeyBinding binding = new KeyBinding( mode.string.toString(), 0, "itemGroup.chiselsandbits" );
			ClientRegistry.registerKeyBinding(binding);
			mode.binding  = binding;
		}
		
		MinecraftForge.EVENT_BUS.register( instance );
		net.minecraftforge.fml.common.FMLCommonHandler.instance().bus().register( instance );
	}

	public void postinit(
			final ChiselsAndBits mod )
	{
		final ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

		final String MODID = ChiselsAndBits.MODID;

		registerMesh( mesher, mod.itemChiselStone, 0, new ModelResourceLocation( new ResourceLocation( MODID, "chisel_stone" ), "inventory" ) );
		registerMesh( mesher, mod.itemChiselIron, 0, new ModelResourceLocation( new ResourceLocation( MODID, "chisel_iron" ), "inventory" ) );
		registerMesh( mesher, mod.itemChiselGold, 0, new ModelResourceLocation( new ResourceLocation( MODID, "chisel_gold" ), "inventory" ) );
		registerMesh( mesher, mod.itemChiselDiamond, 0, new ModelResourceLocation( new ResourceLocation( MODID, "chisel_diamond" ), "inventory" ) );
		registerMesh( mesher, mod.itemBitBag, 0, new ModelResourceLocation( new ResourceLocation( MODID, "bit_bag" ), "inventory" ) );
		registerMesh( mesher, mod.itemWrench, 0, new ModelResourceLocation( new ResourceLocation( MODID, "wrench_wood" ), "inventory" ) );

		if ( mod.itemPositiveprint != null )
		{
			ModelBakery.addVariantName( mod.itemPositiveprint, MODID + ":positiveprint", MODID + ":positiveprint_written" );
			mesher.register( mod.itemPositiveprint, new ItemMeshDefinition(){

				@Override
				public ModelResourceLocation getModelLocation(
						final ItemStack stack )
				{
					return new ModelResourceLocation( new ResourceLocation( MODID, stack.hasTagCompound() ? "positiveprint_written_preview" : "positiveprint" ), "inventory" );
				}

			} );
		}

		if ( mod.itemNegativeprint != null )
		{
			ModelBakery.addVariantName( mod.itemNegativeprint, MODID + ":negativeprint", MODID + ":negativeprint_written" );
			mesher.register( mod.itemNegativeprint, new ItemMeshDefinition(){

				@Override
				public ModelResourceLocation getModelLocation(
						final ItemStack stack )
				{
					return new ModelResourceLocation( new ResourceLocation( MODID, stack.hasTagCompound() ? "negativeprint_written_preview" : "negativeprint" ), "inventory" );
				}

			} );
		}

		if ( mod.itemBlockBit != null )
		{
			mesher.register( mod.itemBlockBit, new ItemMeshDefinition(){

				@Override
				public ModelResourceLocation getModelLocation(
						final ItemStack stack )
				{
					return new ModelResourceLocation( new ResourceLocation( MODID, "block_bit" ), "inventory" );
				}

			} );
		}

		for ( final BlockChiseled blk : mod.conversions.values() )
		{
			final Item item = Item.getItemFromBlock( blk );
			mesher.register( item, 0, new ModelResourceLocation( new ResourceLocation( MODID, "block_chiseled" ), "inventory" ) );
		}

		ChiselsAndBits.instance.config.allowBlockAlternatives = Minecraft.getMinecraft().gameSettings.allowBlockAlternatives;
	}

	private void registerMesh(
			final ItemModelMesher mesher,
			final Item item,
			final int i,
			final ModelResourceLocation loctaion )
	{
		if ( item != null )
		{
			mesher.register( item, i, loctaion );
		}
	}

	@SubscribeEvent
	public void interaction(
			final TickEvent.ClientTickEvent event )
	{
		// used to prevent hyper chisels.. its actually far worse then you might think...
		if ( event.side == Side.CLIENT && event.type == Type.CLIENT && event.phase == Phase.START && !Minecraft.getMinecraft().gameSettings.keyBindAttack.isKeyDown() )
		{
			ItemChisel.resetDelay();
		}
		
		if ( ! Minecraft.getMinecraft().gameSettings.keyBindAttack.isKeyDown() )
		{
			if ( loopDeath )
			{
				drawBlock = null;
				drawStart = null;
			}
			else
				loopDeath = true;
		}
		else
			loopDeath = false;
		
		for ( ChiselMode mode : ChiselMode.values() )
		{
			KeyBinding kb = (KeyBinding) mode.binding;
			if ( kb.isKeyDown() )
			{
				ItemChisel.changeChiselMode( ItemChisel.getChiselMode(), mode );
			}
		}
	}

	@SubscribeEvent
	@SideOnly( Side.CLIENT )
	public void updateConfig(
			final ActionPerformedEvent.Pre ev )
	{
		Minecraft.getMinecraft().addScheduledTask( new Runnable(){

			@Override
			public void run()
			{
				ChiselsAndBits.instance.config.allowBlockAlternatives = Minecraft.getMinecraft().gameSettings.allowBlockAlternatives;
			}

		} );
	}

	@SubscribeEvent
	@SideOnly( Side.CLIENT )
	public void drawHighlight(
			final DrawBlockHighlightEvent event )
	{
		final EntityPlayer player = event.player;
		final float partialTicks = event.partialTicks;
		final MovingObjectPosition mop = event.target;
		final World theWorld = player.worldObj;

		final double x = player.lastTickPosX + ( player.posX - player.lastTickPosX ) * partialTicks;
		final double y = player.lastTickPosY + ( player.posY - player.lastTickPosY ) * partialTicks;
		final double z = player.lastTickPosZ + ( player.posZ - player.lastTickPosZ ) * partialTicks;

		final ChiselMode chMode = ModUtil.isHoldingChiselTool( event.player );
		if ( chMode != null )
		{
			if ( mop.typeOfHit != MovingObjectType.BLOCK )
			{
				return;
			}

			final BlockPos blockpos = mop.getBlockPos();
			final IBlockState state = theWorld.getBlockState( blockpos );
			final Block block = state.getBlock();

			// this logic originated in the vanilla bounding box...
			if ( BlockChiseled.supportsBlock( state ) && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK )
			{
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate( 770, 771, 1, 0 );
				GlStateManager.color( 0.0F, 0.0F, 0.0F, 0.4F );
				GL11.glLineWidth( 2.0F );
				GlStateManager.disableTexture2D();
				GlStateManager.depthMask( false );

				if ( block.getMaterial() != Material.air && theWorld.getWorldBorder().contains( blockpos ) )
				{
					final VoxelBlob vb = new VoxelBlob();
					vb.fill( 1 ); // fill with.. something soild...

					final BlockChiseled chiselBlock = ChiselsAndBits.instance.getConversion( block.getMaterial() );
					RenderGlobal.func_181561_a( chiselBlock.getSelectedBoundingBox( player, blockpos, vb, chMode ).expand( 0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D ).offset( -x, -y, -z ) );
				}

				GlStateManager.depthMask( true );
				GlStateManager.enableTexture2D();
				GlStateManager.disableBlend();

				event.setCanceled( true );
			}
		}
		else if ( ModUtil.isHoldingPattern( event.player ) )
		{
			if ( mop.typeOfHit != MovingObjectType.BLOCK )
			{
				return;
			}

			final IBlockState s = theWorld.getBlockState( mop.getBlockPos() );
			if ( !( s.getBlock() instanceof BlockChiseled ) && !BlockChiseled.supportsBlock( s ) )
			{
				return;
			}

			if ( !event.currentItem.hasTagCompound() )
			{
				return;
			}

			final ItemStack item = ChiselsAndBits.instance.itemNegativeprint.getPatternedItem( event.currentItem );
			if ( !item.hasTagCompound() )
			{
				return;
			}

			final int rotations = ModUtil.getRotations( player, event.currentItem.getTagCompound().getByte( "side" ) );

			if ( item != null )
			{
				GlStateManager.depthFunc( GL11.GL_ALWAYS );
				showGhost( event.currentItem, item, mop.getBlockPos(), event, rotations, x, y, z, mop.sideHit, null );
				GlStateManager.depthFunc( GL11.GL_LEQUAL );
			}
		}
		else if ( ModUtil.isHoldingChiseledBlock( event.player ) )
		{
			if ( mop.typeOfHit != MovingObjectType.BLOCK )
			{
				return;
			}

			final ItemStack item = event.currentItem;
			if ( !item.hasTagCompound() )
			{
				return;
			}

			final int rotations = ModUtil.getRotations( player, item.getTagCompound().getByte( "side" ) );
			final BlockPos offset = mop.getBlockPos();

			final Block cb = theWorld.getBlockState( offset ).getBlock();

			if ( event.player.isSneaking() )
			{
				final BlockPos blockpos = mop.getBlockPos();
				final BlockPos partial = new BlockPos( Math.floor( 16 * ( mop.hitVec.xCoord - blockpos.getX() ) ), Math.floor( 16 * ( mop.hitVec.yCoord - blockpos.getY() ) ), Math.floor( 16 * ( mop.hitVec.zCoord - blockpos.getZ() ) ) );
				showGhost( event.currentItem, item, offset, event, rotations, x, y, z, mop.sideHit, partial );
			}
			else if ( cb.isReplaceable( theWorld, offset ) )
			{
				showGhost( event.currentItem, item, offset, event, rotations, x, y, z, mop.sideHit, null );
			}
			else if ( theWorld.isAirBlock( offset.offset( mop.sideHit ) ) )
			{
				showGhost( event.currentItem, item, offset.offset( mop.sideHit ), event, rotations, x, y, z, mop.sideHit, null );
			}
		}
	}

	private ItemStack previousItem;
	private int previousRotations;
	private Object previousModel;
	private IntegerBox modelBounds;
	private boolean isVisible = true;
	private BlockPos lastPartial;

	private void showGhost(
			final ItemStack refItem,
			final ItemStack item,
			final BlockPos blockPos,
			final DrawBlockHighlightEvent event,
			int rotations,
			final double x,
			final double y,
			final double z,
			final EnumFacing side,
			final BlockPos partial )
	{
		IBakedModel baked;

		if ( previousItem == refItem && previousRotations == rotations && previousModel != null && samePartial( lastPartial, partial ) )
		{
			baked = ( IBakedModel ) previousModel;
		}
		else
		{
			previousItem = refItem;
			previousRotations = rotations;

			final TileEntityBlockChiseled bc = new TileEntityBlockChiseled();
			bc.readChisleData( item.getSubCompound( "BlockEntityTag", false ) );
			VoxelBlob blob = bc.getBlob();
			while ( rotations-- > 0 )
			{
				blob = blob.spin( Axis.Y );
			}

			modelBounds = blob.getBounds();

			bc.setBlob( blob );

			final Block blk = Block.getBlockFromItem( item.getItem() );
			previousModel = baked = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel( bc.getItemStack( blk, null ) );

			if ( partial != null )
			{
				isVisible = ItemBlockChiseled.tryPlaceBlockAt( blk, item, event.player, event.player.getEntityWorld(), blockPos, side, partial, false );
			}
			else
			{
				isVisible = true;
			}
		}

		if ( !isVisible )
		{
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate( 0.5 + blockPos.getX() - x, 0.5 + blockPos.getY() - y, 0.5 + blockPos.getZ() - z );
		if ( partial != null )
		{
			final BlockPos t = ModUtil.getPartialOffset( side, partial, modelBounds );
			final double fullScale = 1.0 / VoxelBlob.dim;
			GlStateManager.translate( t.getX() * fullScale, t.getY() * fullScale, t.getZ() * fullScale );
		}
		GlStateManager.scale( 2.0F, 2.0F, 2.0F );

		GlStateManager.color( 1.0f, 1.0f, 1.0f, 1.0f );
		GlStateManager.enableBlend();
		GlStateManager.blendFunc( GL11.GL_SRC_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR );
		GlStateManager.disableTexture2D();
		ItemBlockChiseled.renderTransparentGhost = true;
		GlStateManager.colorMask( false, false, false, false );
		Minecraft.getMinecraft().getRenderItem().renderItem( item, baked );
		GlStateManager.colorMask( true, true, true, true );
		GlStateManager.depthFunc( GL11.GL_LEQUAL );
		Minecraft.getMinecraft().getRenderItem().renderItem( item, baked );
		ItemBlockChiseled.renderTransparentGhost = false;
		GlStateManager.enableTexture2D();

		GlStateManager.color( 1.0f, 1.0f, 1.0f, 1.0f );
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

	private boolean samePartial(
			final BlockPos lastPartial2,
			final BlockPos partial )
	{
		if ( lastPartial2 == partial )
		{
			return true;
		}

		if ( lastPartial2 == null || partial == null )
		{
			return false;
		}

		return partial.equals( lastPartial2 );
	}

	public EntityPlayer getPlayer()
	{
		return Minecraft.getMinecraft().thePlayer;
	}

	public boolean addHitEffects(
			final World world,
			final MovingObjectPosition target,
			final IBlockState state,
			final EffectRenderer effectRenderer )
	{
		final ItemStack hitWith = getPlayer().getCurrentEquippedItem();
		if ( hitWith != null && ( hitWith.getItem() instanceof ItemChisel || hitWith.getItem() instanceof ItemChiseledBit ) )
		{
			return true; // no
			// effects!
		}

		final BlockPos pos = target.getBlockPos();
		final Block block = state.getBlock();

		final int posX = pos.getX();
		final int posY = pos.getY();
		final int posZ = pos.getZ();
		final float boxOffset = 0.1F;

		double x = posX + rand.nextDouble() * ( block.getBlockBoundsMaxX() - block.getBlockBoundsMinX() - boxOffset * 2.0F ) + boxOffset + block.getBlockBoundsMinX();
		double y = posY + rand.nextDouble() * ( block.getBlockBoundsMaxY() - block.getBlockBoundsMinY() - boxOffset * 2.0F ) + boxOffset + block.getBlockBoundsMinY();
		double z = posZ + rand.nextDouble() * ( block.getBlockBoundsMaxZ() - block.getBlockBoundsMinZ() - boxOffset * 2.0F ) + boxOffset + block.getBlockBoundsMinZ();

		switch ( target.sideHit )
		{
			case DOWN:
				y = posY + block.getBlockBoundsMinY() - boxOffset;
				break;
			case EAST:
				x = posX + block.getBlockBoundsMaxX() + boxOffset;
				break;
			case NORTH:
				z = posZ + block.getBlockBoundsMinZ() - boxOffset;
				break;
			case SOUTH:
				z = posZ + block.getBlockBoundsMaxZ() + boxOffset;
				break;
			case UP:
				y = posY + block.getBlockBoundsMaxY() + boxOffset;
				break;
			case WEST:
				x = posX + block.getBlockBoundsMinX() - boxOffset;
				break;
			default:
				break;

		}
		
		EntityFX fx = effectRenderer.spawnEffectParticle( EnumParticleTypes.BLOCK_DUST.getParticleID(), x, y, z, 0.0D, 0.0D, 0.0D, new int[] { Block.getStateId( state ) } );
		
		if ( fx != null )
			fx.multiplyVelocity( 0.2F ).multipleParticleScaleBy( 0.6F );

		return true;
	}

	@SubscribeEvent
	public void wheelEvent(
			final MouseEvent me )
	{
		if ( me.isCanceled() || me.dwheel == 0 )
		{
			return;
		}

		final Minecraft mc = Minecraft.getMinecraft();
		final EntityPlayer player = mc.thePlayer;
		final ItemStack is = player.getHeldItem();

		if ( is != null && is.getItem() instanceof ItemChisel && player.isSneaking() )
		{
			ItemChisel.scrollOption( ItemChisel.getChiselMode(), me.dwheel );
			me.setCanceled( true );
		}
	}

	public static void placeSound(
			final World world,
			final BlockPos pos,
			final int metadata )
	{
		final IBlockState state = Block.getStateById( metadata );
		final Block block = state.getBlock();
		world.playSound( pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, block.stepSound.getPlaceSound(), ( block.stepSound.getVolume() + 1.0F ) / 16.0F, block.stepSound.getFrequency() * 0.9F, false );
	}

	public static void breakSound(
			final World world,
			final BlockPos pos,
			final int extractedState )
	{
		final IBlockState state = Block.getStateById( extractedState );
		final Block block = state.getBlock();
		world.playSound( pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, block.stepSound.getBreakSound(), ( block.stepSound.getVolume() + 1.0F ) / 16.0F, block.stepSound.getFrequency() * 0.9F, false );
	}

	public static HashMap<Integer,String> blockToTexture = new HashMap<Integer,String>();
	private static Field mapRegSprites = null;

	private static TextureAtlasSprite findTexture(TextureAtlasSprite texture, List<BakedQuad> faceQuads) throws IllegalArgumentException, IllegalAccessException, NullPointerException
	{
		if ( texture == null )
		{
			TextureMap map = Minecraft.getMinecraft().getTextureMapBlocks();
			
			if ( mapRegSprites == null )
			{
				mapRegSprites = ReflectionHelper.findField( map.getClass(), "mapRegisteredSprites", "field_110574_e");
				if( mapRegSprites == null )
					throw new RuntimeException("Unable to get sprite list.");
			}
			
			Map<?, ?> mapRegisteredSprites = (Map<?, ?>) mapRegSprites.get(map);			
			if( mapRegisteredSprites == null )
				throw new RuntimeException("Unable to lookup textures.");
			
			for (BakedQuad q : faceQuads )
			{
				int offsetSize = q.getVertexData().length / 4;
				
				int[] data = q.getVertexData();
				float UA = Float.intBitsToFloat(data[4]);
				float VA = Float.intBitsToFloat(data[5]);
				float UB = Float.intBitsToFloat(data[offsetSize+4]);
				float VB = Float.intBitsToFloat(data[offsetSize+5]);
				float UC = Float.intBitsToFloat(data[offsetSize*2+4]);
				float VC = Float.intBitsToFloat(data[offsetSize*2+5]);

				// use middle of a triangle instead of corners..
				float U = ( UA + UB + UC ) / 3.0f;
				float V = ( VA + VB + VC ) / 3.0f;
				
				Iterator<?> iterator1 = mapRegisteredSprites.values().iterator();
				while ( iterator1.hasNext())
				{
				    final TextureAtlasSprite sprite = (TextureAtlasSprite)iterator1.next();
				    if ( sprite.getMinU() <= U && U <= sprite.getMaxU() && sprite.getMinV() <= V && V <= sprite.getMaxV()  )
				    {
				    	texture = sprite;						
				    	return texture;
				    }				    
				}
			}
		}
		
		return texture;
	}
	
	public static TextureAtlasSprite findTexture(int BlockRef,IBakedModel originalModel)
	{
		TextureAtlasSprite texture = null;
		
		if ( originalModel != null )
		{
			// first try to get the real model...
			try
			{
				if ( originalModel instanceof ISmartBlockModel )
				{
					IBakedModel newModel = ( (ISmartBlockModel) originalModel ).handleBlockState(Block.getStateById(BlockRef));
					if ( newModel != null ) originalModel = newModel;
				}
			}
			catch(Exception err) {}
			
			// who knows if that worked.. now lets try to get a texture...
			try
			{
				texture = originalModel.getTexture();
			}
			catch( Exception err )
			{
				// didn't work? ok lets try scanning for the texture in the atlas...
				if ( blockToTexture.containsKey(BlockRef) )
				{
					String textureName = blockToTexture.get(BlockRef);
					if ( textureName != null )
						texture = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite( textureName );
				}
				else
				{
					try
					{
						for ( EnumFacing side : EnumFacing.VALUES)
							texture = findTexture( texture, originalModel.getFaceQuads(side));
						texture = findTexture( texture, originalModel.getGeneralQuads());
						
						blockToTexture.put(BlockRef, texture == null ? null : texture.getIconName() );
					}
					catch( Exception errr ) 
					{
						blockToTexture.put(BlockRef, null);
					}
				}
			}
		}
		
		// still no good? then just default to missing texture..
		if ( texture == null )
			texture = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
		
		return texture;
	}

	private BlockPos drawBlock;
	private BlockPos drawStart;
	private boolean loopDeath = false;
	
	public BlockPos getStartPos()
	{
		return drawStart;
	}
	
	public void pointAt(BlockPos pos, int x, int y, int z)
	{
		if ( drawBlock == null || drawBlock.equals(pos) )
		{
			drawBlock = pos;
			if ( drawStart == null )
				drawStart = new BlockPos( x, y, z );
		}
	}
	
	public boolean sameDrawBlock(BlockPos pos, int x, int y, int z)
	{
		if ( Minecraft.getMinecraft().gameSettings.keyBindAttack.isKeyDown() )
			return drawBlock != null && drawBlock.equals(pos);
		else
		{
			if ( drawBlock != null && drawBlock.equals(pos) )
			{
				final ChiselPacket pc = new ChiselPacket( pos, drawStart.getX(), drawStart.getY(), drawStart.getZ(), x, y, z, EnumFacing.UP, ChiselMode.DRAWN_REGION );
				final int extractedState = pc.doAction( Minecraft.getMinecraft().thePlayer );
				
				if ( extractedState != 0 )
				{
					ClientSide.breakSound( Minecraft.getMinecraft().theWorld, pos, extractedState );
					NetworkRouter.instance.sendToServer( pc );
				}
			}
			
			drawBlock = null;
			drawStart = null;
			return false;
		}
	}

	public boolean addBlockDestroyEffects(World world, BlockPos pos, IBlockState state, EffectRenderer effectRenderer)
	{
        if (!state.getBlock().isAir(world, pos))
        {
            state = state.getBlock().getActualState(state, world, pos);
            int StateID = Block.getStateId(state);
            
            int i = 4;

            for (int j = 0; j < i; ++j)
            {
                for (int k = 0; k < i; ++k)
                {
                    for (int l = 0; l < i; ++l)
                    {
                        double d0 = pos.getX() + (j + 0.5D) / i;
                        double d1 = pos.getY() + (k + 0.5D) / i;
                        double d2 = pos.getZ() + (l + 0.5D) / i;
                        effectRenderer.spawnEffectParticle(EnumParticleTypes.BLOCK_CRACK.getParticleID(), d0, d1, d2, d0 - pos.getX() - 0.5D, d1 - pos.getY() - 0.5D, d2 - pos.getZ() - 0.5D, StateID);
                    }
                }
            }
        }
        
        return true;
	}
	
}
