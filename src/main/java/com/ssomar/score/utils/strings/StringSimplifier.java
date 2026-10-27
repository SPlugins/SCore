package com.ssomar.score.utils.strings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.Collection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringSimplifier {

	public static String simplify(final Object object) {
		if (object instanceof Entity)
			return ((Entity) object).getName();

		if (object instanceof CommandSender)
			return ((CommandSender) object).getName();

		if (object instanceof World)
			return ((World) object).getName();

		if (object instanceof Collection)
			return StringJoiner.join((Collection<?>) object, ", ", StringSimplifier::simplify);

		if (object instanceof ChatColor)
			return ((Enum<?>) object).name().toLowerCase();

		if (object instanceof Enum)
			return object.toString().toLowerCase();

		try {
			if (object instanceof net.md_5.bungee.api.ChatColor)
				return ((net.md_5.bungee.api.ChatColor) object).getName();
		} catch (final Exception ignored) {
		}

		return object.toString();
	}
}
