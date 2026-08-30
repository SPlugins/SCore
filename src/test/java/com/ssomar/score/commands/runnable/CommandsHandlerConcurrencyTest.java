package com.ssomar.score.commands.runnable;

import com.ssomar.score.commands.runnable.player.PlayerRunCommand;
import com.ssomar.score.utils.placeholders.StringPlaceholder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The delayed command registries are written from several threads (Folia region threads for
 * join/quit, the global scheduler for the delayed task itself). This reproduces that pattern:
 * before the thread-safety fix it threw NullPointerException / ConcurrentModificationException.
 */
class CommandsHandlerConcurrencyTest {

    private static PlayerRunCommand command(UUID receiver) {
        ActionInfo aInfo = new ActionInfo("test", new StringPlaceholder());
        aInfo.setReceiverUUID(receiver);
        return new PlayerRunCommand("SENDMESSAGE test", 0, aInfo);
    }

    @Test
    void concurrentAddAndRemoveLeavesNoLeakAndThrowsNothing() throws Exception {
        final CommandsHandler handler = new CommandsHandler();
        final int threads = 8;
        final int perThread = 400;
        final UUID[] receivers = {UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()};

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        List<Future<?>> futures = new ArrayList<>();

        for (int t = 0; t < threads; t++) {
            final int seed = t;
            futures.add(pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) {
                        UUID receiver = receivers[(seed + i) % receivers.length];
                        PlayerRunCommand cmd = command(receiver);
                        handler.addDelayedCommand(cmd);
                        // read paths used by the join/quit listeners
                        handler.getDelayedCommandsWithPlayerReceiver(receiver);
                        handler.getDelayedPlayerCommands();
                        // the same command can be removed twice concurrently (quit + delayed task)
                        handler.removeDelayedCommand(cmd.getUuid(), receiver, false);
                        handler.removeDelayedCommand(cmd.getUuid(), receiver, false);
                    }
                } catch (Throwable e) {
                    failure.compareAndSet(null, e);
                }
                return null;
            }));
        }
        start.countDown();
        for (Future<?> f : futures) f.get(60, TimeUnit.SECONDS);
        pool.shutdownNow();

        assertNull(failure.get(), () -> "concurrent access threw " + failure.get());
        assertTrue(handler.getDelayedCommandsByRcUuid().isEmpty(), "delayedCommandsByRcUuid leaked entries");
        assertTrue(handler.getDelayedPlayerCommands().isEmpty(), "delayedCommandsByReceiverUuid leaked entries");
    }

    @Test
    void removingAnUnknownCommandIsANoOp() {
        CommandsHandler handler = new CommandsHandler();
        UUID receiver = UUID.randomUUID();
        assertDoesNotThrow(() -> handler.removeDelayedCommand(UUID.randomUUID(), receiver, false));
        assertDoesNotThrow(() -> handler.removeAllDelayedCommands(receiver));
    }
}
