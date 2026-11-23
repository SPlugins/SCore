package com.ssomar.score.utils.safebreak;

import com.ssomar.score.SCore;
import com.ssomar.score.config.GeneralConfig;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Blocks broken by commands (BREAK...) whose break event runs an activator that breaks other blocks by commands, and so on.
 * The commands of the activators (and the console commands like score run-block-command) don't keep the flag
 * isEventFromCustomBreakCommand, so nothing stopped a PLAYER_BLOCK_BREAK activator that runs
 * "score run-block-command ... BREAK" on the blocks around: each broken block ran it again on its neighbours, without an
 * end, until the server stopped responding (ethpick.yml, Discord 1557797708612247646).
 * <p>
 * A chain starts with an action (ActionInfo) and follows the commands it runs, the console commands they run and the
 * actions run by the break events of the blocks they break. The blocks broken by the commands of the action itself (depth 1)
 * are not limited. Deeper, a block is broken once per chain, and past MAX_DEPTH levels or MAX_BLOCKS blocks the chain stops.
 * A chain that ends by itself (a trunk felled log by log...) is far below.
 */
public class BreakChain {

    public static final int MAX_DEPTH = 64;
    public static final int MAX_BLOCKS = 512;

    private static final ThreadLocal<BreakChain> CURRENT = new ThreadLocal<>();

    private final int depth;
    private final Shared shared;

    private static class Shared {
        private final AtomicInteger blocks = new AtomicInteger();
        private final AtomicBoolean warned = new AtomicBoolean();
        private Set<Long> broken;

        private synchronized boolean firstBreak(long key) {
            if (broken == null) broken = new HashSet<>();
            return broken.add(key);
        }
    }

    private BreakChain(int depth, Shared shared) {
        this.depth = depth;
        this.shared = shared;
    }

    /** The chain of the code that runs now: the commands of an action, or the break event of a block broken by a command. */
    @Nullable
    public static BreakChain current() {
        return CURRENT.get();
    }

    /** The chain of a new action: the current one, or a new chain. */
    public static BreakChain currentOrNew() {
        BreakChain current = CURRENT.get();
        return current != null ? current : new BreakChain(0, new Shared());
    }

    /** One more block broken by a command: the next link of the current chain. */
    public static BreakChain next() {
        BreakChain parent = currentOrNew();
        return new BreakChain(parent.depth + 1, parent.shared);
    }

    /** false: the block must not be broken (already broken by this chain, or the chain is too long). */
    public boolean allow(Block block) {
        long key = ((long) block.getX() & 0x3FFFFFFL) << 38 | ((long) block.getZ() & 0x3FFFFFFL) << 12 | ((long) block.getY() & 0xFFFL);
        boolean first = shared.firstBreak(key);
        /* The blocks broken by the commands of the action itself: as before */
        if (depth < 2) return true;
        if (!GeneralConfig.getInstance().isBreakChains()) return false;
        if (!first) return false;
        if (depth > MAX_DEPTH || shared.blocks.incrementAndGet() > MAX_BLOCKS) {
            if (!shared.warned.getAndSet(true)) {
                SCore.plugin.getLogger().warning("Blocks broken by commands stopped at " + block.getWorld().getName() + " " + block.getX() + " " + block.getY() + " " + block.getZ()
                        + " (" + shared.blocks.get() + " blocks, " + depth + " levels): an activator triggered by these breaks breaks other blocks again (score run-block-command ... BREAK ?), it would never end.");
            }
            return false;
        }
        return true;
    }

    /** Makes chain the current one and returns the previous one, to give back to {@link #exit(BreakChain)}. */
    @Nullable
    public static BreakChain enter(@Nullable BreakChain chain) {
        BreakChain previous = CURRENT.get();
        if (chain == null) CURRENT.remove();
        else CURRENT.set(chain);
        return previous;
    }

    public static void exit(@Nullable BreakChain previous) {
        if (previous == null) CURRENT.remove();
        else CURRENT.set(previous);
    }

    /** The runnable runs later (scheduler) in this chain. */
    public static Runnable wrap(@Nullable BreakChain chain, Runnable runnable) {
        if (chain == null) return runnable;
        return () -> {
            BreakChain previous = enter(chain);
            try {
                runnable.run();
            } finally {
                exit(previous);
            }
        };
    }
}
