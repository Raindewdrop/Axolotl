package awa.hyw.Axolotl.injection.patch;

import awa.hyw.Axolotl.module.impl.misc.HealthBypass;
import awa.hyw.patchify.CallbackInfo;
import awa.hyw.patchify.annotation.Inject;
import awa.hyw.patchify.annotation.Patch;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;

@Patch(Connection.class)
public class ConnectionPatch {
    @Inject(method = "channelRead0", desc = "(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V")
    public static void channelRead0(Connection instance, ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        if (HealthBypass.INSTANCE.getEnabled() && HealthBypass.INSTANCE.onPacket(packet)) {
            ci.cancel();
        }
    }
}

