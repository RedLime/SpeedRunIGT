package com.redlimerl.speedrunigt.timer.category;

import java.util.function.Function;

@SuppressWarnings("unused")
public class RunCategoryBuilder {

    private final String id;
    private final String categoryUrl;
    private final String translateKey;
    private boolean autoStart = true;
    private boolean canSegment = false;
    private boolean customUrl = false;
    private Function<Long, Boolean> retimeFunction = (value) -> false;

    public static RunCategoryBuilder create(String id, String categoryUrl, String translateKey) {
        return new RunCategoryBuilder(id, categoryUrl, translateKey);
    }



    RunCategoryBuilder(String id, String categoryUrl, String translateKey) {
        this.id = id;
        this.categoryUrl = categoryUrl;
        this.translateKey = translateKey;
    }

    public RunCategory build() {
        return new RunCategory(
                id,
                categoryUrl,
                translateKey,
                null,
                null,
                autoStart,
                canSegment,
                customUrl,
                retimeFunction
        );
    }



    public RunCategoryBuilder setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
        return this;
    }

    public RunCategoryBuilder setCanSegment(boolean canSegment) {
        this.canSegment = canSegment;
        return this;
    }

    public RunCategoryBuilder setUseCustomUrl(boolean useCustomUrl) {
        this.customUrl = useCustomUrl;
        return this;
    }

    public RunCategoryBuilder setRetimeFunction(Function<Long, Boolean> retimeFunction) {
        this.retimeFunction = retimeFunction;
        return this;
    }
}
