package com.ssomar.score.usedapi;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerRotation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PacketEventsTool {

    public static void registerParticleToggleListener(Set<UUID> hiddenPlayers) {
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListenerAbstract() {
            @Override
            public void onPacketSend(PacketSendEvent event) {
                if (event.getPacketType() != Play.Server.PARTICLE) return;
                Player recipient = (Player) event.getPlayer();
                if (recipient != null && hiddenPlayers.contains(recipient.getUniqueId())) {
                    event.setCancelled(true);
                }
            }
        });
    }

    public static  int sendTest(Player player) {
        int entityId = (int) (Math.random() * Integer.MAX_VALUE);


        WrapperPlayClientPlayerRotation clientPlayerRotation = new WrapperPlayClientPlayerRotation(100,100, true);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, clientPlayerRotation);

        return entityId;
    }
}
