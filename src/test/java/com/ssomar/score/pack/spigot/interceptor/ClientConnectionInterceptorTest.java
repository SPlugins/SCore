package com.ssomar.score.pack.spigot.interceptor;

import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClientConnectionInterceptorTest {

    /**
     * A server with Geyser (or a second port) has several listening channels and the same handler is added
     * to each of them: "is not a @Sharable handler, so can't be added or removed multiple times" at shutdown
     * (dinobossyt, 2026-09-21, https://mclo.gs/om14ZrN).
     */
    @Test
    void serverHandlerCanBeAddedToSeveralListeningChannels() {
        ClientConnectionInterceptor.ServerHandler handler = new ClientConnectionInterceptor.ServerHandler(channel -> { });
        EmbeddedChannel java = new EmbeddedChannel();
        EmbeddedChannel geyser = new EmbeddedChannel();

        assertDoesNotThrow(() -> {
            java.pipeline().addFirst(handler);
            geyser.pipeline().addFirst(handler);
        });
        assertNotNull(java.pipeline().context(handler));
        assertNotNull(geyser.pipeline().context(handler));

        assertDoesNotThrow(() -> {
            java.pipeline().remove(handler);
            geyser.pipeline().remove(handler);
        });
    }

    @Test
    void everyAcceptedClientChannelGoesThroughTheConsumer() {
        List<Channel> seen = new ArrayList<>();
        ClientConnectionInterceptor.ServerHandler handler = new ClientConnectionInterceptor.ServerHandler(seen::add);
        EmbeddedChannel listening = new EmbeddedChannel(handler);

        EmbeddedChannel client1 = new EmbeddedChannel();
        EmbeddedChannel client2 = new EmbeddedChannel();
        listening.writeInbound(client1);
        listening.writeInbound(client2);
        // the initializer runs when the accepted channel is registered
        client1.pipeline().fireChannelRegistered();
        client2.pipeline().fireChannelRegistered();

        assertEquals(2, seen.size());
    }
}
