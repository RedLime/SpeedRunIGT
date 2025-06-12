package com.redlimerl.speedrunigt.timer.category;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.CommandSource;

import java.util.concurrent.CompletableFuture;

public class RunCategoryArgumentType implements ArgumentType<RunCategory> {

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        CommandSource.suggestMatching(RunCategory.getCategories().keySet(), builder);
        return builder.buildFuture();
    }

    @Override
    public RunCategory parse(StringReader reader) throws CommandSyntaxException {
        String key = reader.readString();
        if (!RunCategory.getCategories().containsKey(key)) return null;
        return RunCategory.getCategory(key);
    }
}
