package de.nwex.autoeat.mixin.net;

import static de.nwex.autoeat.chat.ChatBuilder.text;

import de.nwex.autoeat.chat.Chat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HealthUpdateS2CPacket.class)
public class MixinHealthUpdatePacketS2CPacket {

    @Shadow
    private int food;
    private Long eatCoolDown = System.currentTimeMillis();

    @Inject(method = "read", at = @At("HEAD"))
    public void read(PacketByteBuf buf, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null && client.getNetworkHandler() != null) {
            if (food != 20 && client.getNetworkHandler().getCommandDispatcher()
                .getRoot()
                .getChildren().stream()
                .anyMatch(serverCommandSourceCommandNode -> serverCommandSourceCommandNode.getName().equals("eat"))
                && eatCoolDown + 2000 < System.currentTimeMillis()) {
                Chat.send("/eat");
                Chat.announce("Eat", text("Saturated"));

                eatCoolDown = System.currentTimeMillis();
            }
        }
    }
}
