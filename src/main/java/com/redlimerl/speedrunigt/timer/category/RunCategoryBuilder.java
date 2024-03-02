package com.redlimerl.speedrunigt.timer.category;

import com.redlimerl.speedrunigt.timer.InGameTimer;

import java.util.function.Function;

@SuppressWarnings("unused")
public class RunCategoryBuilder {

    private final String id;
    private final String categoryUrl;
    private final String translateKey;
    private boolean autoStart = true;
    private boolean canSegment = false;
    private boolean customUrl = false;
    private boolean hideCategory = false;
    private Function<InGameTimer, Boolean> retimeFunction = (value) -> false;

    public static RunCategoryBuilder create(String id, String categoryUrl, String translateKey) {
        return new RunCategoryBuilder(id, categoryUrl, translateKey);
    }

    RunCategoryBuilder(String id, String categoryUrl, String translateKey) {
        this.id = id;
        this.categoryUrl = categoryUrl;
        this.translateKey = translateKey;
    }

    public RunCategory build() {
        return new RunCategory(this.id, this.categoryUrl, this.translateKey,
            null,
            null, this.autoStart, this.canSegment, this.customUrl, this.hideCategory, this.retimeFunction
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

    public RunCategoryBuilder setHideCategory(boolean hideCategory) {
        this.hideCategory = hideCategory;
        return this;
    }

    public RunCategoryBuilder setRetimeFunction(Function<InGameTimer, Boolean> retimeFunction) {
        this.retimeFunction = retimeFunction;
        return this;
    }
}
