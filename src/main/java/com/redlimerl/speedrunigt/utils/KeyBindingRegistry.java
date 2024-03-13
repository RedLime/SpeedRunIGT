/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Original Source : https://github.com/FabricMC/fabric/blob/1.16.1/fabric-key-binding-api-v1/src/main/java/net/fabricmc/fabric/impl/client/keybinding/KeyBindingRegistryImpl.java
 */

package com.redlimerl.speedrunigt.utils;

import com.google.common.collect.Lists;
import com.redlimerl.speedrunigt.mixins.keybinding.KeyBindingAccessor;
import java.util.List;
import java.util.Set;
import net.minecraft.client.option.KeyBinding;

public final class KeyBindingRegistry {
    private static final List<KeyBinding> moddedKeyBindings = Lists.newArrayList();

    private static Set<String> getCategoryMap() {
        return KeyBindingAccessor.invokeGetCategoryMap();
    }

    private static boolean hasCategory(String categoryTranslationKey) {
        return getCategoryMap().contains(categoryTranslationKey);
    }

    public static void addCategory(String categoryTranslationKey) {
        Set<String> map = getCategoryMap();

        if (map.contains(categoryTranslationKey)) {
            return;
        }

        map.add(categoryTranslationKey);
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