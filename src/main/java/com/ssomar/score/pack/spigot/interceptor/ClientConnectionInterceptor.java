package com.ssomar.score.pack.spigot.interceptor;

import io.netty.channel.*;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClientConnectionInterceptor {

    /**
     * Added to EVERY listening channel of the server (there are several with Geyser, a second port...), so it
     * must be sharable: a non sharable handler added to a second pipeline throws a ChannelPipelineException.
     */
    @ChannelHandler.Sharable
    static final class ServerHandler extends ChannelInboundHandlerAdapter {

        private final ChannelInitializer<Channel> beginInitProtocol;

        ServerHandler(final Consumer<Channel> channelConsumer) {
            this.beginInitProtocol = new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) {
                    try {
                        channelConsumer.accept(channel);
                    } catch (Exception e) {
                        throw new RuntimeException("Cannot inject incoming channel " + channel, e);
                    }
                }
            };
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Channel channel = (Channel) msg;

            // Prepare to initialize ths channel
            channel.pipeline().addFirst(beginInitProtocol);
            ctx.fireChannelRead(msg);
        }
    }

    private ServerHandler serverHandler;

    /** Installed once: the consumer is called for every new client channel until {@link #uninstall()}. */
    public synchronized void install(Consumer<Channel> channelConsumer) {
        if (serverHandler != null) return;
        final ServerHandler handler = new ServerHandler(channelConsumer);
        for (final ChannelFuture channelFuture : this.getChannels()) {
            channelFuture.channel().pipeline().addFirst(handler);
        }
        serverHandler = handler;
    }

    /** Removes the handler from the listening channels. Never throws: it is called while the plugin is disabled. */
    public synchronized void uninstall() {
        if (serverHandler == null) return;
        final ServerHandler handler = serverHandler;
        serverHandler = null;
        try {
            for (final ChannelFuture channelFuture : this.getChannels()) {
                try {
                    ChannelPipeline pipeline = channelFuture.channel().pipeline();
                    if (pipeline.context(handler) != null) pipeline.remove(handler);
                } catch (Exception ignored) {
                    // channel already closed (server shutdown)
                }
            }
        } catch (Exception ignored) {
        }
    }

    private Method getMethodByReturnType(Class<?> clazz, Class<?> returnType) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter((method) -> method.getReturnType().equals(returnType))
                .findFirst()
                .orElse(null);
    }

    private Field getFieldByTypeAndPredicate(Class<?> clazz, Class<?> type, Function<Field, Boolean> predicate) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter((field) -> field.getType().equals(type) && predicate.apply(field))
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private List<ChannelFuture> getChannels() {
        try {
            final Class<?> minecraftServerClass = Class.forName("net.minecraft.server.MinecraftServer");

            // Get CraftServer class (handles both versioned and unversioned packages)
            Class<?> craftServerClass = getCraftServerClass();
            final Object craftServerObject = craftServerClass.cast(Bukkit.getServer());
            final Method craftServerGetServerMethod = craftServerClass.getDeclaredMethod("getServer");

            final Object minecraftServer = craftServerGetServerMethod.invoke(craftServerObject);

            // Get ServerConnection - try Mojang mapped name first, then Spigot mapped
            Object serverConnection = getServerConnection(minecraftServer, minecraftServerClass);

            // Get channels field
            Field channelsField = getFieldByTypeAndPredicate(
                    serverConnection.getClass(),
                    List.class,
                    (field) -> Modifier.isPrivate(field.getModifiers())
            );

            // If not found in declared fields, search all fields including inherited
            if (channelsField == null) {
                channelsField = findChannelsField(serverConnection.getClass());
            }

            if (channelsField == null) {
                throw new RuntimeException("Could not find channels field in ServerConnection");
            }

            channelsField.setAccessible(true);
            return (List<ChannelFuture>) channelsField.get(serverConnection);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> getCraftServerClass() throws ClassNotFoundException {
        // Try unversioned first (Paper 1.20.5+ with Mojang mappings)
        try {
            return Class.forName("org.bukkit.craftbukkit.CraftServer");
        } catch (ClassNotFoundException ignored) {}

        // Fall back to versioned package
        String serverVersion = getServerVersion();
        return Class.forName("org.bukkit.craftbukkit." + serverVersion + ".CraftServer");
    }

    private Object getServerConnection(Object minecraftServer, Class<?> minecraftServerClass)
            throws InvocationTargetException, IllegalAccessException {

        // Try Mojang mapped class name first (Paper 1.20.5+)
        Class<?> serverConnectionClass = null;
        try {
            serverConnectionClass = Class.forName("net.minecraft.server.network.ServerConnectionListener");
        } catch (ClassNotFoundException ignored) {}

        // Fall back to Spigot mapped class name
        if (serverConnectionClass == null) {
            try {
                serverConnectionClass = Class.forName("net.minecraft.server.network.ServerConnection");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Could not find ServerConnection or ServerConnectionListener class", e);
            }
        }

        // Try to find method by return type
        Method getConnectionMethod = getMethodByReturnType(minecraftServerClass, serverConnectionClass);

        if (getConnectionMethod != null) {
            return getConnectionMethod.invoke(minecraftServer);
        }

        // If method not found, try to find field by type
        for (Field field : minecraftServerClass.getDeclaredFields()) {
            if (field.getType().equals(serverConnectionClass)) {
                field.setAccessible(true);
                return field.get(minecraftServer);
            }
        }

        // Last resort: search by class name pattern in fields
        for (Field field : minecraftServerClass.getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(minecraftServer);
            if (value != null) {
                String className = value.getClass().getName();
                if (className.contains("ServerConnection")) {
                    return value;
                }
            }
        }

        throw new RuntimeException("Could not find ServerConnection/ServerConnectionListener in MinecraftServer");
    }

    private Field findChannelsField(Class<?> clazz) {
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (field.getType().equals(List.class) && Modifier.isPrivate(field.getModifiers())) {
                    // Check if it's likely the channels field (not connections)
                    field.setAccessible(true);
                    return field;
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return null;
    }

    private String getServerVersion() {
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }
}