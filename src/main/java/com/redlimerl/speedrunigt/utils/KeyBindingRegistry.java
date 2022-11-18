/**
 * Removed FabricMC copyright as it is outdated. - Wurgo
 */

package com.redlimerl.speedrunigt.utils;

import com.google.common.collect.Lists;
import com.redlimerl.speedrunigt.mixins.access.KeyBindingAccessor;
import net.minecraft.client.options.KeyBinding;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class KeyBindingRegistry {
    private static final List<KeyBinding> moddedKeyBindings = Lists.newArrayList();

    private static Map<String, Integer> getCategoryMap() {
        return KeyBindingAccessor.invokeGetCategoryMap();
    }

    private static boolean hasCategory(String categoryTranslationKey) {
        return getCategoryMap().containsKey(categoryTranslationKey);
    }

    public static void addCategory(String categoryTranslationKey) {
        Map<String, Integer> map = getCategoryMap();

        if (map.containsKey(categoryTranslationKey)) {
            return;
        }

        Optional<Integer> largest = map.values().stream().max(Integer::compareTo);
        int largestInt = largest.orElse(0);
        map.put(categoryTranslationKey, largestInt + 1);
    }

    public static KeyBinding registerKeyBinding(KeyBinding binding) {
        if (!hasCategory(binding.getCategory())) {
            addCategory(binding.getCategory());
        }

        moddedKeyBindings.add(binding);
        return binding;
    }

    /**
     * Processes the keybindings array for our modded ones by first removing existing modded keybindings and readding them,
     * we can make sure that there are no duplicates this way.
     */
    public static KeyBinding[] process(KeyBinding[] keysAll) {
        List<KeyBinding> newKeysAll = Lists.newArrayList(keysAll);
        newKeysAll.removeAll(moddedKeyBindings);
        newKeysAll.addAll(moddedKeyBindings);
        return newKeysAll.toArray(new KeyBinding[0]);
    }
}