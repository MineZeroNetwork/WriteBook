package net.minezero.writebook;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ComponentUtil {
    public static Component makeC(String text) {
        return Component.text(text);
    }

    public static Component makeC(String text, NamedTextColor color) {
        return Component.text(text).color(TextColor.color(color));
    }

    public static Component makeC(String text, int r, int g, int b) {
        return Component.text(text).color(TextColor.color(r, g, b));
    }

    public static String componentToString(Component text) {
        return PlainTextComponentSerializer.plainText().serialize(text);
    }

    public static Component minimsg(String text) {
        return MiniMessage.miniMessage().deserialize(text);
    }
}
