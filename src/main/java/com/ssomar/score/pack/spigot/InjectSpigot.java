package com.ssomar.score.pack.spigot;

import com.ssomar.score.pack.api.InjectPlatform;
import com.ssomar.score.pack.api.Injector;
import com.ssomar.score.pack.spigot.interceptor.ClientConnectionInterceptor;
import com.ssomar.score.utils.logging.Utils;
import io.netty.channel.ChannelPipeline;

import java.util.ArrayList;
import java.util.List;

public class InjectSpigot implements InjectPlatform {
    public static InjectSpigot INSTANCE = new InjectSpigot();
    // Read by the netty threads for each new connection while plugins register / unregister on the main thread
    private final List<Injector> injectors = new java.util.concurrent.CopyOnWriteArrayList<>();
    private boolean hasInitialized = false;

    private ClientConnectionInterceptor connectionInterceptor = new ClientConnectionInterceptor();

    private InjectSpigot() {
    }

    private boolean isRunningPaper() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public void registerInjector(Injector injector) {
        if (injectors.contains(injector)) {
            return;
        }
        try {
            connectionInterceptor.install((channel) -> {
                ChannelPipeline pipeline = channel.pipeline();
                injectors.forEach(pipeline::addFirst);
            });

            injectors.add(injector);

            Utils.sendConsoleMsg("Injector &e" + injector.getClass().getName() + " &7registered (TPack selfhosting)");
        }
        catch (Exception e) {
            e.printStackTrace();
            Utils.sendConsoleMsg("&cFailed to register injector &e" + injector.getClass().getName());
        }

    }

    public void unregisterAllInjectors() {
        // Copy to avoid ConcurrentModificationException
        List<Injector> injectorsCopy = new ArrayList<>(this.injectors);
        injectorsCopy.forEach(this::unregisterInjector);
    }

    /**
     * Unregisters an injector and removes it from all active channels
     *
     * @param injector The injector to unregister
     * @return true if successfully unregistered, false otherwise
     */
    public boolean unregisterInjector(Injector injector) {
        if (injector == null || !injectors.contains(injector)) {
            return false;
        }

        boolean removed = injectors.remove(injector);

        if (removed && connectionInterceptor != null) {
            // New connections read the injectors list, so removing it from the list is enough for them.
            // (It used to install() a second server handler here, which threw at shutdown on servers with
            // several listening channels, e.g. with Geyser.)
            if (injectors.isEmpty()) connectionInterceptor.uninstall();
            Utils.sendConsoleMsg("Injector &e" + injector.getClass().getName() + " &7unregistered (TPack selfhosting)");
        }

        return removed;
    }
}