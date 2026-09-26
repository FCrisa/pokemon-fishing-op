package com.fishingop.drop;

import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.regex.Pattern;

/** Padrão de id com curinga '*', por exemplo {@code cobblemon:ancient_*_ball}. */
public final class IdPattern {

    private final Pattern regex;

    private IdPattern(Pattern regex) {
        this.regex = regex;
    }

    public static IdPattern of(String raw) {
        String trimmed = raw.trim().toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder("^");
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c == '*') {
                sb.append(".*");
            } else {
                sb.append(Pattern.quote(String.valueOf(c)));
            }
        }
        sb.append('$');
        return new IdPattern(Pattern.compile(sb.toString()));
    }

    public boolean matches(ResourceLocation id) {
        return regex.matcher(id.toString()).matches();
    }
}
