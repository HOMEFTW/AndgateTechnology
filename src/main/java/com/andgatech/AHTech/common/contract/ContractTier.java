package com.andgatech.AHTech.common.contract;

public enum ContractTier {

    NONE(0, "AHTech.Contract.None"),
    LV1(1, "AHTech.Contract.Lv1"),
    LV2(2, "AHTech.Contract.Lv2"),
    LV3(3, "AHTech.Contract.Lv3"),
    LV4(4, "AHTech.Contract.Lv4");

    private final int tier;
    private final String translationKey;

    ContractTier(int tier, String translationKey) {
        this.tier = tier;
        this.translationKey = translationKey;
    }

    public int getTier() {
        return tier;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public boolean meets(ContractTier requiredTier) {
        return tier >= requiredTier.tier;
    }

    public static ContractTier fromTier(int tier) {
        for (ContractTier value : values()) {
            if (value.tier == tier) {
                return value;
            }
        }
        return NONE;
    }
}
