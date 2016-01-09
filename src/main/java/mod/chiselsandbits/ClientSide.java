package mod.chiselsandbits;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.google.common.base.Stopwatch;

import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.ChiselTypeIterator;
import mod.chiselsandbits.chiseledblock.ItemBlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseledTESR;
import mod.chiselsandbits.chiseledblock.data.BitIterator;
import mod.chiselsandbits.chiseledblock.data.BitLocation;
import mod.chiselsandbits.chiseledblock.data.IntegerBox;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.gui.ChiselsAndBitsMenu;
import mod.chiselsandbits.gui.SpriteIconPositioning;
import mod.chiselsandbits.helpers.ChiselModeManager;
import mod.chiselsandbits.helpers.ChiselToolType;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.integration.mcmultipart.MCMultipartProxy;
import mod.chiselsandbits.interfaces.IItemScrollWheel;
import mod.chiselsandbits.interfaces.IPatternItem;
import mod.chiselsandbits.items.ItemChisel;
import mod.chiselsandbits.items.ItemChiseledBit;
import mod.chiselsandbits.network.NetworkRouter;
import mod.chiselsandbits.network.packets.PacketChisel;
import mod.chiselsandbits.network.packets.PacketRotateVoxelBlob;
import mod.chiselsandbits.registry.ModItems;
import mod.chiselsandbits.render.GeneratedModelLoader;
import mod.chiselsandbits.render.chiseledblock.tesr.ChisledBlockRenderChunkTESR;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
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
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.Type;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientSide
{

	private static final Random RANDOM = new Random();
	public static final ClientSide instance = new ClientSide();

	private final HashMap<ChiselMode, SpriteIconPositioning> chiselModeIcons = new HashMap<ChiselMode, SpriteIconPositioning>();
	private KeyBinding rotateCCW;
	private KeyBinding rotateCW;
	private KeyBinding modeMenu;
	private Stopwatch rotateTimer;

	public void preinit(
			final ChiselsAndBits mod )
	{
		ModelLoaderRegistry.registerLoader( new GeneratedModelLoader() );
	}

	public void init(
			final ChiselsAndBits chiselsandbits )
	{
		ClientRegistry.bindTileEntitySpecialRenderer( TileEntityBlockChiseledTESR.class, new ChisledBlockRenderChunkTESR() );

		for ( final ChiselMode mode : ChiselMode.values() )
		{
			final KeyBinding binding = new KeyBinding( mode.string.toString(), 0, "itemGroup.chiselsandbits" );
			ClientRegistry.registerKeyBinding( binding );
			mode.binding = binding;
		}

		modeMenu = new KeyBinding( "mod.chiselsandbits.other.mode", 56, "itemGroup.chiselsandbits" );
		ClientRegistry.registerKeyBinding( modeMenu );

		rotateCCW = new KeyBinding( "mod.chiselsandbits.other.rotate.ccw", 0, "itemGroup.chiselsandbits" );
		ClientRegistry.registerKeyBinding( rotateCCW );

		rotateCW = new KeyBinding( "mod.chiselsandbits.other.rotate.cw", 0, "itemGroup.chiselsandbits" );
		ClientRegistry.registerKeyBinding( rotateCW );

		ChiselsAndBits.registerWithBus( instance );
	}

	public void postinit(
			final ChiselsAndBits mod )
	{
		final ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

		final String modId = ChiselsAndBits.MODID;

		final ModItems modItems = ChiselsAndBits.getItems();

		registerMesh( mesher, modItems.itemChiselStone, 0, new ModelResourceLocation( new ResourceLocation( modId, "chisel_stone" ), "inventory" ) );
		registerMesh( mesher, modItems.itemChiselIron, 0, new ModelResourceLocation( new ResourceLocation( modId, "chisel_iron" ), "inventory" ) );
		registerMesh( mesher, modItems.itemChiselGold, 0, new ModelResourceLocation( new ResourceLocation( modId, "chisel_gold" ), "inventory" ) );
		registerMesh( mesher, modItems.itemChiselDiamond, 0, new ModelResourceLocation( new ResourceLocation( modId, "chisel_diamond" ), "inventory" ) );
		registerMesh( mesher, modItems.itemBitBag, 0, new ModelResourceLocation( new ResourceLocation( modId, "bit_bag" ), "inventory" ) );
		registerMesh( mesher, modItems.itemWrench, 0, new ModelResourceLocation( new ResourceLocation( modId, "wrench_wood" ), "inventory" ) );

		if ( modItems.itemPositiveprint != null )
		{
			ModelBakery.addVariantName( modItems.itemPositiveprint, modId + ":positiveprint", modId + ":positiveprint_written" );
			mesher.register( modItems.itemPositiveprint, new ItemMeshDefinition() {

				@Override
				public ModelResourceLocation getModelLocation(
						final ItemStack stack )
				{
					return new ModelResourceLocation( new ResourceLocation( modId, stack.hasTagCompound() ? "positiveprint_written_preview" : "positiveprint" ), "inventory" );
				}

			} );
		}

		if ( modItems.itemNegativeprint != null )
		{
			ModelBakery.addVariantName( modItems.itemNegativeprint, modId + ":negativeprint", modId + ":negativeprint_written" );
			mesher.register( modItems.itemNegativeprint, new ItemMeshDefinition() {

				@Override
				public ModelResourceLocation getModelLocation(
						final ItemStack stack )
				{
					return new ModelResourceLocation( new ResourceLocation( modId, stack.hasTagCompound() ? "negativeprint_written_preview" : "negativeprint" ), "inventory" );
				}

			} );
		}

		if ( modItems.itemMirrorprint != null )
		{
			ModelBakery.addVariantName( modItems.itemMirrorprint, modId + ":mirrorprint", modId + ":mirrorprint_written" );
			mesher.register( modItems.itemMirrorprint, new ItemMeshDefinition() {

				@Override
				public ModelResourceLocation getModelLocation(
						final ItemStack stack )
				{
					return new ModelResourceLocation( new ResourceLocation( modId, stack.hasTagCompound() ? "mirrorprint_written_preview" : "mirrorprint" ), "inventory" );
				}

			} );
		}

		if ( modItems.itemBlockBit != null )

		{
			mesher.register( modItems.itemBlockBit, new ItemMeshDefinition() {

				@Override
				public ModelResourceLocation getModelLocation(
						final ItemStack stack )
				{
					return new ModelResourceLocation( new ResourceLocation( modId, "block_bit" ), "inventory" );
				}

			} );
		}

		for (

		final BlockChiseled blk : ChiselsAndBits.getBlocks().getConversions().values() )

		{
			final Item item = Item.getItemFromBlock( blk );
			mesher.register( item, 0, new ModelResourceLocation( new ResourceLocation( modId, "block_chiseled" ), "inventory" ) );
		}

		ChiselsAndBits.getConfig().allowBlockAlternatives = Minecraft.getMinecraft().gameSettings.allowBlockAlternatives;

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
	void registerIconTextures(
			final TextureStitchEvent.Pre ev )
	{
		for ( final ChiselMode mode : ChiselMode.values() )
		{
			final SpriteIconPositioning sip = new SpriteIconPositioning();

			final ResourceLocation sprite = new ResourceLocation( "chiselsandbits", "icons/" + mode.name().toLowerCase() );
			final ResourceLocation png = new ResourceLocation( "chiselsandbits", "textures/icons/" + mode.name().toLowerCase() + ".png" );

			sip.sprite = ev.map.registerSprite( sprite );

			try
			{
				final IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource( png );
				final BufferedImage bi = TextureUtil.readBufferedImage( iresource.getInputStream() );

				int bottom = 0;
				int right = 0;
				sip.left = bi.getWidth();
				sip.top = bi.getHeight();

				for ( int x = 0; x < bi.getWidth(); x++ )
				{
					for ( int y = 0; y < bi.getHeight(); y++ )
					{
						final int color = bi.getRGB( x, y );
						final int a = color >> 24 & 0xff;
						if ( a > 0 )
						{
							sip.left = Math.min( sip.left, x );
							right = Math.max( right, x );

							sip.top = Math.min( sip.top, y );
							bottom = Math.max( bottom, y );
						}
					}
				}

				sip.height = bottom - sip.top + 1;
				sip.width = right - sip.left + 1;

				sip.left /= bi.getWidth();
				sip.width /= bi.getWidth();
				sip.top /= bi.getHeight();
				sip.height /= bi.getHeight();
			}
			catch ( final IOException e )
			{
				sip.height = 1;
				sip.width = 1;
				sip.left = 0;
				sip.top = 0;
			}

			chiselModeIcons.put( mode, sip );
		}
	}

	public SpriteIconPositioning getIconForMode(
			final ChiselMode mode )
	{
		return chiselModeIcons.get( mode );
	}

	@SubscribeEvent
	public void onRenderGUI(
			final RenderGameOverlayEvent.Post event )
	{
		final ChiselToolType tool = getHeldToolType();
		if ( event.type == ElementType.ALL && tool != null )
		{
			final boolean wasVisible = ChiselsAndBitsMenu.instance.isVisible();

			if ( modeMenu.isKeyDown() )
			{
				ChiselsAndBitsMenu.instance.raiseVisibility();
			}
			else
			{
				if ( ChiselsAndBitsMenu.instance.switchTo != null )
				{
					ChiselModeManager.changeChiselMode( tool, ChiselModeManager.getChiselMode( tool ), ChiselsAndBitsMenu.instance.switchTo );
					ChiselsAndBitsMenu.instance.switchTo = null;
				}

				ChiselsAndBitsMenu.instance.decreaseVisibility();
			}

			if ( ChiselsAndBitsMenu.instance.isVisible() )
			{
				ChiselsAndBitsMenu.instance.configure( event.resolution.getScaledWidth(), event.resolution.getScaledHeight() );

				if ( wasVisible == false )
				{
					ChiselsAndBitsMenu.instance.mc.inGameHasFocus = false;
					ChiselsAndBitsMenu.instance.mc.mouseHelper.ungrabMouseCursor();
				}

				if ( ChiselsAndBitsMenu.instance.mc.inGameHasFocus )
				{
					KeyBinding.unPressAllKeys();
				}

				final int k1 = Mouse.getX() * event.resolution.getScaledWidth() / ChiselsAndBitsMenu.instance.mc.displayWidth;
				final int l1 = event.resolution.getScaledHeight() - Mouse.getY() * event.resolution.getScaledHeight() / ChiselsAndBitsMenu.instance.mc.displayHeight - 1;

				net.minecraftforge.client.ForgeHooksClient.drawScreen( ChiselsAndBitsMenu.instance, k1, l1, event.partialTicks );
			}
			else
			{
				if ( wasVisible )
				{
					ChiselsAndBitsMenu.instance.mc.setIngameFocus();
				}
			}
		}

		if ( event.type == ElementType.HOTBAR && ChiselsAndBits.getConfig().enableToolbarIcons )
		{
			final Minecraft mc = Minecraft.getMinecraft();
			final GuiIngame sc = mc.ingameGUI;

			for ( int slot = 0; slot < 9; ++slot )
			{
				final ItemStack stack = mc.thePlayer.inventory.mainInventory[slot];
				if ( stack != null && stack.getItem() instanceof ItemChisel )
				{
					final ChiselMode mode = ChiselMode.getMode( stack );

					final int x = event.resolution.getScaledWidth() / 2 - 90 + slot * 20 + 2;
					final int y = event.resolution.getScaledHeight() - 16 - 3;

					GlStateManager.color( 1, 1, 1, 1.0f );
					Minecraft.getMinecraft().getTextureManager().bindTexture( TextureMap.locationBlocksTexture );
					final TextureAtlasSprite sprite = chiselModeIcons.get( mode ).sprite;

					GlStateManager.enableBlend();
					sc.drawTexturedModalRect( x + 1, y + 1, sprite, 8, 8 );
					GlStateManager.disableBlend();
				}
			}
		}
	}

	public ChiselToolType getHeldToolType()
	{
		final EntityPlayer player = getPlayer();

		if ( player == null )
		{
			return null;
		}

		final ItemStack is = player.getCurrentEquippedItem();

		if ( is != null && is.getItem() instanceof ItemChisel )
		{
			return ChiselToolType.CHISEL;
		}

		if ( is != null && is.getItem() instanceof ItemChiseledBit )
		{
			return ChiselToolType.BIT;
		}

		return null;
	}

	@SubscribeEvent
	public void interaction(
			final TickEvent.ClientTickEvent event )
	{
		// used to prevent hyper chisels.. its actually far worse then you might
		// think...
		if ( event.side == Side.CLIENT && event.type == Type.CLIENT && event.phase == Phase.START && !Minecraft.getMinecraft().gameSettings.keyBindAttack.isKeyDown() )
		{
			ItemChisel.resetDelay();
		}

		if ( !getToolKey().isKeyDown() )
		{
			if ( loopDeath )
			{
				drawStart = null;
				lastTool = ChiselToolType.CHISEL;
			}
			else
			{
				loopDeath = true;
			}
		}
		else
		{
			loopDeath = false;
		}

		if ( rotateCCW.isKeyDown() )
		{
			if ( rotateTimer == null || rotateTimer.elapsed( TimeUnit.MILLISECONDS ) > 200 )
			{
				rotateTimer = Stopwatch.createStarted();
				final PacketRotateVoxelBlob p = new PacketRotateVoxelBlob();
				p.wheel = 1;
				NetworkRouter.instance.sendToServer( p );
			}
		}

		if ( rotateCW.isKeyDown() )
		{
			if ( rotateTimer == null || rotateTimer.elapsed( TimeUnit.MILLISECONDS ) > 200 )
			{
				rotateTimer = Stopwatch.createStarted();
				final PacketRotateVoxelBlob p = new PacketRotateVoxelBlob();
				p.wheel = -1;
				NetworkRouter.instance.sendToServer( p );
			}
		}

		for ( final ChiselMode mode : ChiselMode.values() )
		{
			final KeyBinding kb = (KeyBinding) mode.binding;
			if ( kb.isKeyDown() )
			{
				final ChiselToolType tool = getHeldToolType();
				ChiselModeManager.changeChiselMode( tool, ChiselModeManager.getChiselMode( tool ), mode );
			}
		}
	}

	@SubscribeEvent
	@SideOnly( Side.CLIENT )
	public void updateConfig(
			final ActionPerformedEvent.Pre ev )
	{
		Minecraft.getMinecraft().addScheduledTask( new Runnable() {

			@Override
			public void run()
			{
				ChiselsAndBits.getConfig().allowBlockAlternatives = Minecraft.getMinecraft().gameSettings.allowBlockAlternatives;
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

		ChiselToolType tool = getHeldToolType();
		final ChiselMode chMode = ChiselModeManager.getChiselMode( tool );
		if ( chMode == ChiselMode.DRAWN_REGION )
		{
			tool = lastTool;
		}

		if ( tool != null && chMode != null )
		{
			if ( mop.typeOfHit != MovingObjectType.BLOCK )
			{
				return;
			}

			if ( mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK )
			{
				final BitLocation location = new BitLocation( mop, true, getDrawnTool() );

				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate( 770, 771, 1, 0 );
				GlStateManager.color( 0.0F, 0.0F, 0.0F, 0.4F );
				GL11.glLineWidth( 2.0F );
				GlStateManager.disableTexture2D();
				GlStateManager.depthMask( false );

				if ( theWorld.getWorldBorder().contains( location.blockPos ) )
				{
					// this logic originated in the vanilla bounding box...
					final IBlockState state = theWorld.getBlockState( location.blockPos );

					final TileEntity te = theWorld.getTileEntity( location.blockPos );
					final boolean isMultipart = MCMultipartProxy.proxyMCMultiPart.isMultiPartTileEntity( te );
					final boolean isChisel = getDrawnTool() == ChiselToolType.CHISEL;
					final TileEntityBlockChiseled data = ModUtil.getChiseledTileEntity( te, false );

					final VoxelBlob vb = data != null ? data.getBlob() : new VoxelBlob();

					if ( isChisel && data == null )
					{
						vb.fill( 1 );
					}

					final BitLocation other = getStartPos();
					if ( chMode == ChiselMode.DRAWN_REGION && other != null )
					{
						final ChiselTypeIterator oneEnd = new ChiselTypeIterator( VoxelBlob.dim, location.bitX, location.bitY, location.bitZ, VoxelBlob.NULL_BLOB, ChiselMode.SINGLE, EnumFacing.UP );
						final ChiselTypeIterator otherEnd = new ChiselTypeIterator( VoxelBlob.dim, other.bitX, other.bitY, other.bitZ, VoxelBlob.NULL_BLOB, ChiselMode.SINGLE, EnumFacing.UP );

						final AxisAlignedBB a = oneEnd.getBoundingBox( VoxelBlob.NULL_BLOB, false ).offset( location.blockPos.getX(), location.blockPos.getY(), location.blockPos.getZ() );
						final AxisAlignedBB b = otherEnd.getBoundingBox( VoxelBlob.NULL_BLOB, false ).offset( other.blockPos.getX(), other.blockPos.getY(), other.blockPos.getZ() );

						final AxisAlignedBB bb = a.union( b );

						final double maxChiseSize = ChiselsAndBits.getConfig().maxDrawnRegionSize + 0.001;
						if ( bb.maxX - bb.minX <= maxChiseSize && bb.maxY - bb.minY <= maxChiseSize && bb.maxZ - bb.minZ <= maxChiseSize )
						{
							drawSelectionBoundingBoxIfExists( bb, BlockPos.ORIGIN, player, partialTicks );

							if ( !getToolKey().isKeyDown() )
							{
								final PacketChisel pc = new PacketChisel( lastTool == ChiselToolType.BIT, location, other, EnumFacing.UP, chMode );

								if ( pc.doAction( getPlayer() ) > 0 )
								{
									NetworkRouter.instance.sendToServer( pc );
									ClientSide.placeSound( theWorld, location.blockPos, 0 );
								}

								drawStart = null;
								lastTool = ChiselToolType.CHISEL;
							}
						}
					}
					else
					{
						if ( theWorld.isAirBlock( location.blockPos ) || te instanceof TileEntityBlockChiseled || BlockBitInfo.supportsBlock( state ) || isMultipart )
						{
							final ChiselTypeIterator i = new ChiselTypeIterator( VoxelBlob.dim, location.bitX, location.bitY, location.bitZ, vb, chMode, mop.sideHit );
							final AxisAlignedBB bb = i.getBoundingBox( vb, isChisel );
							drawSelectionBoundingBoxIfExists( bb, location.blockPos, player, partialTicks );
						}
					}
				}

				GlStateManager.depthMask( true );
				GlStateManager.enableTexture2D();
				GlStateManager.disableBlend();

				event.setCanceled( true );
			}
		}
	}

	private void drawSelectionBoundingBoxIfExists(
			final AxisAlignedBB bb,
			final BlockPos blockPos,
			final EntityPlayer player,
			final float partialTicks )
	{
		if ( bb != null )
		{
			final double x = player.lastTickPosX + ( player.posX - player.lastTickPosX ) * partialTicks;
			final double y = player.lastTickPosY + ( player.posY - player.lastTickPosY ) * partialTicks;
			final double z = player.lastTickPosZ + ( player.posZ - player.lastTickPosZ ) * partialTicks;

			RenderGlobal.drawSelectionBoundingBox( bb.expand( 0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D ).offset( -x + blockPos.getX(), -y + blockPos.getY(), -z + blockPos.getZ() ) );
		}
	}

	@SubscribeEvent
	@SideOnly( Side.CLIENT )
	public void drawLast(
			final RenderWorldLastEvent event )
	{
		// important and used for tesr / block rendering.
		++lastRenderedFrame;

		if ( Minecraft.getMinecraft().gameSettings.hideGUI )
		{
			return;
		}

		// now render the ghosts...
		final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		final float partialTicks = event.partialTicks;
		final MovingObjectPosition mop = Minecraft.getMinecraft().objectMouseOver;
		final World theWorld = player.worldObj;
		final ItemStack currentItem = player.getCurrentEquippedItem();

		final double x = player.lastTickPosX + ( player.posX - player.lastTickPosX ) * partialTicks;
		final double y = player.lastTickPosY + ( player.posY - player.lastTickPosY ) * partialTicks;
		final double z = player.lastTickPosZ + ( player.posZ - player.lastTickPosZ ) * partialTicks;

		if ( mop == null )
		{
			return;
		}

		if ( ModUtil.isHoldingPattern( player ) )
		{
			if ( mop.typeOfHit != MovingObjectType.BLOCK )
			{
				return;
			}

			final IBlockState s = theWorld.getBlockState( mop.getBlockPos() );
			if ( !( s.getBlock() instanceof BlockChiseled ) && !BlockBitInfo.supportsBlock( s ) )
			{
				return;
			}

			if ( !currentItem.hasTagCompound() )
			{
				return;
			}

			final ItemStack item = ChiselsAndBits.getItems().itemNegativeprint.getPatternedItem( currentItem );
			if ( !item.hasTagCompound() )
			{
				return;
			}

			final int rotations = ModUtil.getRotations( player, currentItem.getTagCompound().getByte( "side" ) );

			if ( item != null )
			{
				Object cacheRef = s.getBlock() instanceof BlockChiseled ? theWorld.getTileEntity( mop.getBlockPos() ) : s;
				if ( cacheRef instanceof TileEntityBlockChiseled )
				{
					cacheRef = ( (TileEntityBlockChiseled) cacheRef ).getBlobStateReference();
				}

				GlStateManager.depthFunc( GL11.GL_ALWAYS );
				showGhost( currentItem, item, mop.getBlockPos(), player, rotations, x, y, z, mop.sideHit, null, cacheRef );
				GlStateManager.depthFunc( GL11.GL_LEQUAL );
			}
		}
		else if ( ModUtil.isHoldingChiseledBlock( player ) )
		{
			if ( mop.typeOfHit != MovingObjectType.BLOCK )
			{
				return;
			}

			final ItemStack item = currentItem;
			if ( !item.hasTagCompound() )
			{
				return;
			}

			final int rotations = ModUtil.getRotations( player, item.getTagCompound().getByte( "side" ) );
			final BlockPos offset = mop.getBlockPos();

			if ( player.isSneaking() )
			{
				final BlockPos blockpos = mop.getBlockPos();
				final BlockPos partial = new BlockPos( Math.floor( 16 * ( mop.hitVec.xCoord - blockpos.getX() ) ), Math.floor( 16 * ( mop.hitVec.yCoord - blockpos.getY() ) ), Math.floor( 16 * ( mop.hitVec.zCoord - blockpos.getZ() ) ) );
				showGhost( currentItem, item, offset, player, rotations, x, y, z, mop.sideHit, partial, null );
			}
			else
			{
				boolean canMerge = false;
				if ( currentItem.hasTagCompound() )
				{
					final TileEntityBlockChiseled tebc = ModUtil.getChiseledTileEntity( theWorld, offset, true );

					if ( tebc != null )
					{
						final TileEntityBlockChiseled tmp = new TileEntityBlockChiseled();
						tmp.readChisleData( currentItem.getSubCompound( "BlockEntityTag", false ) );
						VoxelBlob blob = tmp.getBlob();

						int xrotations = ModUtil.getRotations( player, currentItem.getTagCompound().getByte( "side" ) );
						while ( xrotations-- > 0 )
						{
							blob = blob.spin( Axis.Y );
						}

						canMerge = tebc.canMerge( blob );
					}
				}

				BlockPos newOffset = offset;
				final Block block = theWorld.getBlockState( newOffset ).getBlock();
				if ( !canMerge && !player.isSneaking() && !block.isReplaceable( theWorld, newOffset ) )
				{
					newOffset = offset.offset( mop.sideHit );
				}

				final TileEntity newTarget = theWorld.getTileEntity( newOffset );

				if ( theWorld.isAirBlock( newOffset ) || theWorld.getBlockState( newOffset ).getBlock().isReplaceable( theWorld, newOffset ) || newTarget instanceof TileEntityBlockChiseled
						|| MCMultipartProxy.proxyMCMultiPart.isMultiPartTileEntity( newTarget ) )
				{
					final TileEntityBlockChiseled test = ModUtil.getChiseledTileEntity( newTarget, false );
					showGhost( currentItem, item, newOffset, player, rotations, x, y, z, mop.sideHit, null, test == null ? null : test.getBlobStateReference() );
				}
			}
		}
	}

	private ItemStack previousItem;
	private int previousRotations;
	private Object previousModel;
	private Object previousCacheRef;
	private IntegerBox modelBounds;
	private boolean isVisible = true;
	private BlockPos lastPartial;
	private BlockPos lastPos;

	private void showGhost(
			final ItemStack refItem,
			final ItemStack item,
			final BlockPos blockPos,
			final EntityPlayer player,
			final int rotationCount,
			final double x,
			final double y,
			final double z,
			final EnumFacing side,
			final BlockPos partial,
			final Object cacheRef )
	{
		IBakedModel baked;

		if ( previousCacheRef == cacheRef && samePos( lastPos, blockPos ) && previousItem == refItem && previousRotations == rotationCount && previousModel != null && samePos( lastPartial, partial ) )
		{
			baked = (IBakedModel) previousModel;
		}
		else
		{
			int rotations = rotationCount;

			previousItem = refItem;
			previousRotations = rotations;
			previousCacheRef = cacheRef;
			lastPos = blockPos;

			final TileEntityBlockChiseled bc = new TileEntityBlockChiseled();
			bc.readChisleData( item.getSubCompound( "BlockEntityTag", false ) );
			VoxelBlob blob = bc.getBlob();
			while ( rotations-- > 0 )
			{
				blob = blob.spin( Axis.Y );
			}

			modelBounds = blob.getBounds();

			fail: if ( refItem.getItem() == ChiselsAndBits.getItems().itemNegativeprint )
			{
				final VoxelBlob pattern = blob;

				if ( cacheRef instanceof VoxelBlobStateReference )
				{
					blob = ( (VoxelBlobStateReference) cacheRef ).getVoxelBlob();
				}
				else if ( cacheRef instanceof IBlockState )
				{
					blob = new VoxelBlob();
					blob.fill( Block.getStateId( (IBlockState) cacheRef ) );
				}
				else
				{
					break fail;
				}

				final BitIterator it = new BitIterator();
				while ( it.hasNext() )
				{
					if ( it.getNext( pattern ) == 0 )
					{
						it.setNext( blob, 0 );
					}
				}
			}

			bc.setBlob( blob );

			final Block blk = Block.getBlockFromItem( item.getItem() );
			previousModel = baked = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel( bc.getItemStack( blk, null ) );

			if ( refItem.getItem() instanceof IPatternItem )
			{
				isVisible = true;
			}
			else
			{
				isVisible = ItemBlockChiseled.tryPlaceBlockAt( blk, item, player, player.getEntityWorld(), blockPos, side, partial, false );
			}
		}

		if ( !isVisible )

		{
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate( blockPos.getX() - x, blockPos.getY() - y, blockPos.getZ() - z );
		if ( partial != null )

		{
			final BlockPos t = ModUtil.getPartialOffset( side, partial, modelBounds );
			final double fullScale = 1.0 / VoxelBlob.dim;
			GlStateManager.translate( t.getX() * fullScale, t.getY() * fullScale, t.getZ() * fullScale );
		}

		GlStateManager.bindTexture( Minecraft.getMinecraft().getTextureMapBlocks().getGlTextureId() );
		GlStateManager.color( 1.0f, 1.0f, 1.0f, 0.1f );
		GlStateManager.enableBlend();
		GlStateManager.enableTexture2D();
		GlStateManager.blendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );
		GlStateManager.colorMask( false, false, false, false );

		renderModel( baked );
		GlStateManager.colorMask( true, true, true, true );
		GlStateManager.depthFunc( GL11.GL_LEQUAL );
		renderModel( baked );

		GlStateManager.color( 1.0f, 1.0f, 1.0f, 1.0f );
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

	private void renderQuads(
			final WorldRenderer renderer,
			final List<BakedQuad> quads )
	{
		int i = 0;
		for ( final int j = quads.size(); i < j; ++i )
		{
			final BakedQuad bakedquad = quads.get( i );
			final int color = 0xaaffffff;
			net.minecraftforge.client.model.pipeline.LightUtil.renderQuadColor( renderer, bakedquad, color );
		}
	}

	private void renderModel(
			final IBakedModel model )
	{
		final Tessellator tessellator = Tessellator.getInstance();
		final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin( 7, DefaultVertexFormats.ITEM );

		for ( final EnumFacing enumfacing : EnumFacing.values() )
		{
			renderQuads( worldrenderer, model.getFaceQuads( enumfacing ) );
		}

		renderQuads( worldrenderer, model.getGeneralQuads() );
		tessellator.draw();
	}

	private boolean samePos(
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

		double x = posX + RANDOM.nextDouble() * ( block.getBlockBoundsMaxX() - block.getBlockBoundsMinX() - boxOffset * 2.0F ) + boxOffset + block.getBlockBoundsMinX();
		double y = posY + RANDOM.nextDouble() * ( block.getBlockBoundsMaxY() - block.getBlockBoundsMinY() - boxOffset * 2.0F ) + boxOffset + block.getBlockBoundsMinY();
		double z = posZ + RANDOM.nextDouble() * ( block.getBlockBoundsMaxZ() - block.getBlockBoundsMinZ() - boxOffset * 2.0F ) + boxOffset + block.getBlockBoundsMinZ();

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

		final EntityFX fx = effectRenderer.spawnEffectParticle( EnumParticleTypes.BLOCK_DUST.getParticleID(), x, y, z, 0.0D, 0.0D, 0.0D, new int[] { Block.getStateId( state ) } );

		if ( fx != null )
		{
			fx.multiplyVelocity( 0.2F ).multipleParticleScaleBy( 0.6F );
		}

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

		final EntityPlayer player = ClientSide.instance.getPlayer();
		final ItemStack is = player.getHeldItem();

		if ( me.dwheel != 0 && is != null && is.getItem() instanceof IItemScrollWheel && player.isSneaking() )
		{
			( (IItemScrollWheel) is.getItem() ).scroll( player, is, me.dwheel );
			me.setCanceled( true );
		}
	}

	public static void placeSound(
			final World world,
			final BlockPos pos,
			final int stateID )
	{
		final IBlockState state = Block.getStateById( stateID );
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

	private BitLocation drawStart;
	private boolean loopDeath = false;
	private int lastRenderedFrame = Integer.MIN_VALUE;

	public int getLastRenderedFrame()
	{
		return lastRenderedFrame;
	}

	public BitLocation getStartPos()
	{
		return drawStart;
	}

	public void pointAt(
			final ChiselToolType type,
			final BitLocation pos )
	{
		if ( drawStart == null )
		{
			drawStart = pos;
			lastTool = type;
		}
	}

	ChiselToolType lastTool = ChiselToolType.CHISEL;

	KeyBinding getToolKey()
	{
		if ( lastTool == ChiselToolType.CHISEL )
		{
			return Minecraft.getMinecraft().gameSettings.keyBindAttack;
		}
		else
		{
			return Minecraft.getMinecraft().gameSettings.keyBindUseItem;
		}
	}

	public boolean addBlockDestroyEffects(
			final World world,
			final BlockPos pos,
			IBlockState state,
			final EffectRenderer effectRenderer )
	{
		if ( !state.getBlock().isAir( world, pos ) )
		{
			state = state.getBlock().getActualState( state, world, pos );
			final int StateID = Block.getStateId( state );

			final int i = 4;

			for ( int j = 0; j < i; ++j )
			{
				for ( int k = 0; k < i; ++k )
				{
					for ( int l = 0; l < i; ++l )
					{
						final double d0 = pos.getX() + ( j + 0.5D ) / i;
						final double d1 = pos.getY() + ( k + 0.5D ) / i;
						final double d2 = pos.getZ() + ( l + 0.5D ) / i;
						effectRenderer.spawnEffectParticle( EnumParticleTypes.BLOCK_CRACK.getParticleID(), d0, d1, d2, d0 - pos.getX() - 0.5D, d1 - pos.getY() - 0.5D, d2 - pos.getZ() - 0.5D, StateID );
					}
				}
			}
		}

		return true;
	}

	public TextureAtlasSprite getMissingIcon()
	{
		return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
	}

	public String getModeKey()
	{
		return GameSettings.getKeyDisplayString( modeMenu.getKeyCode() );
	}

	public ChiselToolType getDrawnTool()
	{
		return lastTool;
	}

}
