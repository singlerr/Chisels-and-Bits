package mod.chiselsandbits.fabric.platform.network;

import com.google.common.collect.Maps;
import mod.chiselsandbits.platforms.core.dist.Dist;
import mod.chiselsandbits.platforms.core.dist.DistExecutor;
import mod.chiselsandbits.platforms.core.network.INetworkChannel;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public final class FabricNetworkChannel implements INetworkChannel
{

    private final ResourceLocation                             name;
    private final Map<Integer, NetworkMessageSpecification<?>> messageSpecifications = Maps.newHashMap();

    public FabricNetworkChannel(final ResourceLocation name)
    {
        this.name = name;

        ServerPlayNetworking.registerGlobalReceiver(name, (minecraftServer, serverPlayer, serverGamePacketListener, friendlyByteBuf, packetSender) -> {
            final int messageId = friendlyByteBuf.readVarInt();
            final NetworkMessageSpecification<?> spec = messageSpecifications.get(messageId);
            handleMessageReceive(
              spec,
              friendlyByteBuf,
              true,
              serverPlayer,
              minecraftServer::execute
            );
        });

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPlayNetworking.registerGlobalReceiver(name,
          (minecraft, clientPacketListener, friendlyByteBuf, packetSender) -> {
              final int messageId = friendlyByteBuf.readVarInt();
              final NetworkMessageSpecification<?> spec = messageSpecifications.get(messageId);
              handleMessageReceive(
                spec,
                friendlyByteBuf,
                false,
                Minecraft.getInstance().player,
                Minecraft.getInstance()::execute
              );
          }));
    }

    public <T> void handleMessageReceive(
      final NetworkMessageSpecification<T> specification,
      final FriendlyByteBuf friendlyByteBuf,
      final boolean server,
      final Player player,
      final Consumer<Runnable> executor
    )
    {
        final T message = specification.creator.apply(friendlyByteBuf);
        specification.executionHandler.execute(message, server, player, executor);
    }

    @SuppressWarnings("unchecked")
    public <T> void handleMessageSerialization(
      final T message,
      final FriendlyByteBuf friendlyByteBuf
    )
    {
        final NetworkMessageSpecification<T> spec = (NetworkMessageSpecification<T>) getSpec(message.getClass());
        friendlyByteBuf.writeVarInt(spec.id);
        spec.serializer.accept(message, friendlyByteBuf);
    }

    @Override
    public <T> void register(
      final int id,
      final Class<T> msgClass,
      final BiConsumer<T, FriendlyByteBuf> serializer,
      final Function<FriendlyByteBuf, T> creator,
      final MessageExecutionHandler<T> executionHandler)
    {
        final NetworkMessageSpecification<T> spec = new NetworkMessageSpecification<>(id, msgClass, serializer, creator, executionHandler);
        this.messageSpecifications.put(
          spec.id,
          spec
        );
    }

    @Override
    public void sendToServer(final Object msg)
    {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            final FriendlyByteBuf buf = PacketByteBufs.create();

            handleMessageSerialization(
              msg,
              buf
            );

            ClientPlayNetworking.send(name, buf);
        });
    }

    @Override
    public void sendToPlayer(final Object msg, final ServerPlayer player)
    {
        final FriendlyByteBuf buf = PacketByteBufs.create();

        handleMessageSerialization(
          msg,
          buf
        );

        ServerPlayNetworking.send(player, name, buf);
    }

    @SuppressWarnings("unchecked")
    private <T> NetworkMessageSpecification<T> getSpec(final Class<T> networkMessageClass)
    {
        return (NetworkMessageSpecification<T>) messageSpecifications.values().stream().filter(
            spec -> spec.msgClass.equals(networkMessageClass)
          )
          .findFirst()
          .orElseThrow();
    }

    private static final class NetworkMessageSpecification<T>
    {
        private final int                            id;
        private final Class<T>                       msgClass;
        private final BiConsumer<T, FriendlyByteBuf> serializer;
        private final Function<FriendlyByteBuf, T>   creator;
        private final MessageExecutionHandler<T>     executionHandler;

        private NetworkMessageSpecification(
          final int id,
          final Class<T> msgClass,
          final BiConsumer<T, FriendlyByteBuf> serializer,
          final Function<FriendlyByteBuf, T> creator, final MessageExecutionHandler<T> executionHandler)
        {
            this.id = id;
            this.msgClass = msgClass;
            this.serializer = serializer;
            this.creator = creator;
            this.executionHandler = executionHandler;
        }
    }
}
