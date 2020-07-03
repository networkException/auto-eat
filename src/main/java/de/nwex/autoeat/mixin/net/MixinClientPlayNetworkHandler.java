package de.nwex.autoeat.mixin.net;

import static de.nwex.autoeat.chat.ChatBuilder.text;

import de.nwex.autoeat.chat.Chat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.util.ChatUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    private Long eatCoolDown = System.currentTimeMillis();

    @Inject(method = "onHealthUpdate", at = @At("HEAD"))
    public void onHealthUpdate(HealthUpdateS2CPacket packet, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null && client.getNetworkHandler() != null) {
            if (packet.getFood() != 20 && client.getNetworkHandler().getCommandDispatcher()
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

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    public void onChatMessage(GameMessageS2CPacket gameMessageS2CPacket, CallbackInfo ci) {
        String message = ChatUtil.stripTextFormat(gameMessageS2CPacket.getMessage().getString().replace("\n", ""));

        if (message.equals("Your appetite was sated.")) {
            System.out.println("Suppressed: " + message);

            ci.cancel();
        }
    }
}
