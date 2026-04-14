package com.andgatech.AHTech.common.supplier;

import java.util.Arrays;

import net.minecraft.util.EnumChatFormatting;

import com.andgatech.AHTech.common.contract.ContractTier;

public enum SupplierId {
    SHANDONG_DEZHOU(
        "shandong_dezhou",
        ContractTier.LV1,
        "AHTech.Supplier.ShandongDezhou.Name",
        "AHTech.Supplier.ShandongDezhou.Tagline",
        EnumChatFormatting.GOLD),
    YADEPIAN(
        "yadepian",
        ContractTier.LV1,
        "AHTech.Supplier.Yadepian.Name",
        "AHTech.Supplier.Yadepian.Tagline",
        EnumChatFormatting.BLUE),
    ERFA(
        "erfa",
        ContractTier.LV1,
        "AHTech.Supplier.Erfa.Name",
        "AHTech.Supplier.Erfa.Tagline",
        EnumChatFormatting.WHITE),
    CHAOLA(
        "chaola",
        ContractTier.LV2,
        "AHTech.Supplier.Chaola.Name",
        "AHTech.Supplier.Chaola.Tagline",
        EnumChatFormatting.DARK_PURPLE),
    HUANGWEIDA(
        "huangweida",
        ContractTier.LV2,
        "AHTech.Supplier.Huangweida.Name",
        "AHTech.Supplier.Huangweida.Tagline",
        EnumChatFormatting.GREEN),
    GAIGUANG(
        "gaiguang",
        ContractTier.LV2,
        "AHTech.Supplier.Gaiguang.Name",
        "AHTech.Supplier.Gaiguang.Tagline",
        EnumChatFormatting.YELLOW),
    DITONG(
        "ditong",
        ContractTier.LV3,
        "AHTech.Supplier.Ditong.Name",
        "AHTech.Supplier.Ditong.Tagline",
        EnumChatFormatting.RED);

    private final String id;
    private final ContractTier minimumContractTier;
    private final String nameKey;
    private final String taglineKey;
    private final EnumChatFormatting color;

    SupplierId(String id, ContractTier minimumContractTier, String nameKey, String taglineKey,
        EnumChatFormatting color) {
        this.id = id;
        this.minimumContractTier = minimumContractTier;
        this.nameKey = nameKey;
        this.taglineKey = taglineKey;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public ContractTier getMinimumContractTier() {
        return minimumContractTier;
    }

    public String getNameKey() {
        return nameKey;
    }

    public String getTaglineKey() {
        return taglineKey;
    }

    public EnumChatFormatting getColor() {
        return color;
    }

    public boolean isUnlockedBy(ContractTier contractTier) {
        return contractTier != null && contractTier.meets(minimumContractTier);
    }

    public static SupplierId fromId(String id) {
        return Arrays.stream(values())
            .filter(value -> value.id.equals(id))
            .findFirst()
            .orElse(null);
    }
}
