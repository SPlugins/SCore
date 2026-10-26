package com.ssomar.score.commands.activator;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class ActivatorCommandsRegistryTest {

    /** A command map like SimpleCommandMap#register: "prefix:label" always, "label" when it is free. */
    static class FakeCommandMap implements CommandMap {
        final Map<String, Command> known = new HashMap<>();

        @Override
        public void registerAll(String fallbackPrefix, List<Command> commands) {
            for (Command c : commands) register(fallbackPrefix, c);
        }

        @Override
        public boolean register(String label, String fallbackPrefix, Command command) {
            label = label.toLowerCase(Locale.ENGLISH);
            known.put(fallbackPrefix + ":" + label, command);
            boolean registered = !known.containsKey(label);
            if (registered) known.put(label, command);
            command.register(this);
            return registered;
        }

        @Override
        public boolean register(String fallbackPrefix, Command command) {
            return register(command.getName(), fallbackPrefix, command);
        }

        @Override
        public boolean dispatch(CommandSender sender, String cmdLine) {
            return false;
        }

        @Override
        public void clearCommands() {
            known.clear();
        }

        @Override
        public Command getCommand(String name) {
            return known.get(name);
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String cmdLine) {
            return null;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String cmdLine, Location location) {
            return null;
        }

        @Override
        public Map<String, Command> getKnownCommands() {
            return known;
        }
    }

    static class OtherCommand extends Command {
        OtherCommand(String name) {
            super(name);
        }

        @Override
        public boolean execute(CommandSender sender, String label, String[] args) {
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    private static void want(ActivatorCommandsRegistry registry, String owner, String... detailed) throws Exception {
        Field f = ActivatorCommandsRegistry.class.getDeclaredField("wantedByOwner");
        f.setAccessible(true);
        ((Map<String, Map<String, List<List<String>>>>) f.get(registry)).put(owner, ActivatorCommandsRegistry.parse(Arrays.asList(detailed)));
    }

    @Test
    void parseReadsLabelsAndArguments() {
        Map<String, List<List<String>>> map = ActivatorCommandsRegistry.parse(Arrays.asList("/Promo SUMMER", "promo winter", "/promo", "  ", "/minecraft:give", "/daily"));
        assertEquals(new HashSet<>(Arrays.asList("promo", "daily")), map.keySet());
        assertEquals(Arrays.asList(Collections.singletonList("SUMMER"), Collections.singletonList("winter")), map.get("promo"));
        assertTrue(map.get("daily").isEmpty());
    }

    @Test
    void completeProposesTheNextWord() {
        List<List<String>> chains = Arrays.asList(Arrays.asList("code", "SUMMER"), Arrays.asList("code", "WINTER"), Collections.singletonList("list"));
        assertEquals(Arrays.asList("code", "list"), ActivatorCommand.complete(chains, new String[]{""}));
        assertEquals(Collections.singletonList("code"), ActivatorCommand.complete(chains, new String[]{"c"}));
        assertEquals(Arrays.asList("SUMMER", "WINTER"), ActivatorCommand.complete(chains, new String[]{"CODE", ""}));
        assertEquals(Collections.singletonList("WINTER"), ActivatorCommand.complete(chains, new String[]{"code", "w"}));
        assertTrue(ActivatorCommand.complete(chains, new String[]{"list", ""}).isEmpty());
        assertTrue(ActivatorCommand.complete(Collections.<List<String>>emptyList(), new String[]{""}).isEmpty());
    }

    @Test
    void registersOnceForSeveralActivatorsAndPlugins() throws Exception {
        ActivatorCommandsRegistry registry = new ActivatorCommandsRegistry();
        FakeCommandMap map = new FakeCommandMap();
        want(registry, "ExecutableItems", "/promo summer", "/promo winter");
        want(registry, "ExecutableEvents", "/promo spring");
        assertTrue(registry.apply(map, map.known));
        Command promo = map.known.get("promo");
        assertTrue(promo instanceof ActivatorCommand);
        assertSame(promo, map.known.get("executableitems:promo"));
        assertEquals(3, ((ActivatorCommand) promo).getArgumentChains().size());
        /* same commands again: nothing to send to the players */
        assertFalse(registry.apply(map, map.known));
        assertTrue(promo.execute(null, "promo", new String[0]));
    }

    @Test
    void neverReplacesACommandItDidNotRegister() throws Exception {
        ActivatorCommandsRegistry registry = new ActivatorCommandsRegistry();
        FakeCommandMap map = new FakeCommandMap();
        OtherCommand spawn = new OtherCommand("spawn");
        map.register("essentials", spawn);
        want(registry, "ExecutableItems", "/spawn", "/promo");
        registry.apply(map, map.known);
        assertSame(spawn, map.known.get("spawn"));
        assertNull(map.known.get("executableitems:spawn"));
        assertEquals(Collections.singleton("promo"), registry.getRegisteredLabels());
    }

    @Test
    void unregistersWhatIsNoLongerWanted() throws Exception {
        ActivatorCommandsRegistry registry = new ActivatorCommandsRegistry();
        FakeCommandMap map = new FakeCommandMap();
        want(registry, "ExecutableItems", "/promo");
        want(registry, "ExecutableEvents", "/promo", "/daily");
        registry.apply(map, map.known);
        assertNotNull(map.known.get("daily"));

        /* EI reload without the item: EE still wants /promo */
        want(registry, "ExecutableItems");
        assertFalse(registry.apply(map, map.known));
        assertTrue(map.known.get("promo") instanceof ActivatorCommand);

        want(registry, "ExecutableEvents");
        assertTrue(registry.apply(map, map.known));
        assertTrue(map.known.isEmpty(), map.known.toString());
        assertTrue(registry.getRegisteredLabels().isEmpty());
    }

    @Test
    void requestSyncWithoutSchedulerDoesNothing() {
        ActivatorCommandsRegistry registry = new ActivatorCommandsRegistry();
        Supplier<Collection<String>> supplier = () -> Collections.singletonList("/promo");
        registry.requestSync("ExecutableItems", supplier);
        assertTrue(registry.getRegisteredLabels().isEmpty());
    }
}
