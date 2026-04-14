package com.andgatech.AHTech.common.machine;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlocksTiered;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.withChannel;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.ExoticEnergy;
import static gregtech.api.enums.HatchElement.InputBus;
import static gregtech.api.enums.HatchElement.InputHatch;
import static gregtech.api.enums.HatchElement.OutputBus;
import static gregtech.api.enums.HatchElement.OutputHatch;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ASSEMBLY_LINE;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ASSEMBLY_LINE_ACTIVE;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ASSEMBLY_LINE_ACTIVE_GLOW;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ASSEMBLY_LINE_GLOW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import com.andgatech.AHTech.common.contract.ContractItem;
import com.andgatech.AHTech.common.contract.ContractTier;
import com.andgatech.AHTech.common.modularizedMachine.FunctionType;
import com.andgatech.AHTech.common.modularizedMachine.ModularHatchType;
import com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineSupportAllModuleBase;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.IModularHatch;
import com.andgatech.AHTech.common.supplier.SupplierHatch;
import com.andgatech.AHTech.common.supplier.SupplierId;
import com.andgatech.AHTech.recipe.machineRecipe.RecyclingRecipeGenerator;
import com.andgatech.AHTech.recipe.metadata.AHTechRecipeMetadata;
import com.andgatech.AHTech.recipe.recipeMap.AHTechRecipeMaps;
import com.google.common.collect.ImmutableList;
import com.gtnewhorizon.structurelib.alignment.constructable.IConstructable;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import gregtech.api.GregTechAPI;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetricsExporter;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.metatileentity.implementations.MTEHatchDataAccess;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;
import gregtech.api.util.HatchElementBuilder;
import gregtech.api.util.MultiblockTooltipBuilder;

/**
 * Electronics Market - a multiblock machine that processes electronics recycling recipes.
 * Structure tier (I/II/III) is determined by the casing blocks used in the structure.
 * Tier I uses hardcoded parameters; Tier II/III performance is driven by installed modular hatches.
 */
public class ElectronicsMarket extends ModularizedMachineSupportAllModuleBase<ElectronicsMarket>
    implements IConstructable, ISurvivalConstructable, IMetricsExporter {

    // region Structure Tier Constants
    public static final int TIER_NONE = 0;
    public static final int TIER_I = 1;
    public static final int TIER_II = 2;
    public static final int TIER_III = 3;
    // endregion

    // region Instance Fields
    private int structureTier = TIER_NONE;
    private ContractTier contractTier = ContractTier.NONE;
    private final EnumSet<SupplierId> activeSuppliers = EnumSet.noneOf(SupplierId.class);
    private final ArrayList<MTEHatchDataAccess> mDataAccessHatches = new ArrayList<>();
    // Client-side synced cache fields (written by FakeSyncWidget setters on client)
    private int syncedParallel;
    private float syncedSpeedBonus;
    private float syncedRecoveryRate;
    private int syncedContractTier;
    private int syncedActiveSuppliers;
    // endregion

    // region Class Constructor

    public ElectronicsMarket(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public ElectronicsMarket(String aName) {
        super(aName);
    }

    // endregion

    // region Meta Entity

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new ElectronicsMarket(this.mName);
    }

    // endregion

    // region Information Panel Integration

    @Override
    public String[] getInfoData() {
        return buildInformationLines().toArray(new String[0]);
    }

    @Override
    public @NotNull List<String> reportMetrics() {
        return ImmutableList.copyOf(buildInformationLines());
    }

    private List<String> buildInformationLines() {
        List<String> lines = new ArrayList<>(8);
        lines.add("Electronics Market");
        lines.add("Status: " + getMachineStatusLine());
        lines.add("Stage: " + getStructureTierLabel());
        lines.add("Contract: " + getContractTierLabel());
        lines.add("Suppliers: " + getActiveSupplierCount());
        lines.add("Parallel: " + getMaxParallelRecipes());
        lines.add("Speed Bonus: " + formatPercent(getSpeedBonus()));
        lines.add("Recovery Rate: " + formatPercent(getRecoveryRate()));
        lines.add("Perfect Overclock: " + (isEnablePerfectOverclock() ? "ON" : "OFF"));
        lines.add("Modules: " + getInstalledModulesLabel());
        return lines;
    }

    private String getMachineStatusLine() {
        if (getStructureTier() < TIER_I) {
            return "Incomplete";
        }

        IGregTechTileEntity base = getBaseMetaTileEntity();
        if (base != null && base.isActive()) {
            return "Running";
        }

        return "Idle";
    }

    private String getStructureTierLabel() {
        return switch (getStructureTier()) {
            case TIER_I -> "I";
            case TIER_II -> "II";
            case TIER_III -> "III";
            default -> "N/A";
        };
    }

    private String getContractTierLabel() {
        return switch (getContractTierForUi()) {
            case LV1 -> "Lv1";
            case LV2 -> "Lv2";
            case LV3 -> "Lv3";
            case LV4 -> "Lv4";
            case NONE -> "None";
            default -> "Unknown";
        };
    }

    private String formatPercent(float value) {
        return Math.round(value * 100.0F) + "%";
    }

    private String getInstalledModulesLabel() {
        if (getInstalledFunctionTypes().isEmpty()) {
            return "None";
        }

        List<FunctionType> modules = new ArrayList<>(getInstalledFunctionTypes());
        Collections.sort(modules);

        List<String> labels = new ArrayList<>(modules.size());
        for (FunctionType module : modules) {
            labels.add(getFunctionTypeLabel(module));
        }
        return String.join(", ", labels);
    }

    private String getFunctionTypeLabel(FunctionType type) {
        return switch (type) {
            case GENERAL_DISASSEMBLY -> "General Disassembly";
            case CIRCUIT_DISASSEMBLY -> "Circuit Disassembly";
            case CABLE_DISASSEMBLY -> "Cable Disassembly";
            case COMPONENT_FACTORY -> "Component Factory";
            case CIRCUIT_BOARD_FACTORY -> "Circuit Board Factory";
        };
    }

    protected ContractTier getContractTierForUi() {
        return contractTier;
    }

    protected EnumSet<SupplierId> getActiveSuppliersForUi() {
        return activeSuppliers.clone();
    }

    protected int getActiveSupplierCount() {
        return getActiveSuppliersForUi().size();
    }

    // endregion

    // region Structure Tier

    @Override
    public int getStructureTier() {
        return structureTier;
    }

    // endregion

    // region Modular Hatch Configuration

    @Override
    protected boolean canMultiplyModularHatchType() {
        return true; // Allow multiple hatches of the same type to stack
    }

    // endregion

    // region Processing Logic

    @Override
    public RecipeMap<?> getRecipeMap() {
        return AHTechRecipeMaps.ElectronicsMarketRecipes;
    }

    @Override
    protected ProcessingLogic createProcessingLogic() {
        return new ProcessingLogic() {

            @NotNull
            @Override
            protected CheckRecipeResult validateRecipe(@NotNull GTRecipe recipe) {
                return validateRecipeAccess(recipe);
            }

            @NotNull
            @Override
            public CheckRecipeResult process() {
                setEuModifier(ElectronicsMarket.this.getEuModifier());
                setSpeedBonus(ElectronicsMarket.this.getSpeedBonus());
                setOverclock(ElectronicsMarket.this.isEnablePerfectOverclock() ? 4 : 2, 4);
                return super.process();
            }
        }.setMaxParallelSupplier(ElectronicsMarket.this::getMaxParallelRecipes);
    }

    // endregion

    // region Check Processing with Recovery Rate

    @NotNull
    @Override
    protected CheckRecipeResult checkProcessingMM() {
        CheckRecipeResult result = super.checkProcessingMM();
        if (result.wasSuccessful()) {
            applyRecoveryRate();
        }
        return result;
    }

    /**
     * Applies the recovery rate to output items. Circuit boards are always recovered at 100%.
     */
    private void applyRecoveryRate() {
        float rate = getRecoveryRate();
        if (rate >= 1.0f || mOutputItems == null) return;

        for (int i = 0; i < mOutputItems.length; i++) {
            if (mOutputItems[i] == null) continue;
            if (RecyclingRecipeGenerator.isCircuitBoard(mOutputItems[i])) continue;
            int original = mOutputItems[i].stackSize;
            if (original <= 0) continue;
            int recovered = Math.max(1, Math.round(original * rate));
            mOutputItems[i].stackSize = recovered;
        }
    }

    // endregion

    // region Maintenance

    @Override
    public boolean getDefaultHasMaintenanceChecks() {
        return false;
    }

    // endregion

    // region NBT

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger("structureTier", structureTier);
        aNBT.setInteger("contractTier", contractTier.getTier());
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        structureTier = aNBT.getInteger("structureTier");
        contractTier = ContractTier.fromTier(aNBT.getInteger("contractTier"));
    }

    // endregion

    // region Structure

    private static final String STRUCTURE_PIECE_MAIN = "mainElectronicsMarket";
    private static IStructureDefinition<ElectronicsMarket> STRUCTURE_DEFINITION = null;

    // Structure offsets: controller is at front center of the 5x5x5 box
    private final int horizontalOffSet = 2;
    private final int verticalOffSet = 0;
    private final int depthOffSet = 0;

    /**
     * Determines the tier of a casing block.
     *
     * @param block The block to check.
     * @param meta  The metadata of the block.
     * @return Integer tier value (1, 2, or 3), or null if the block is not a valid tiered casing.
     */
    public static Integer getCasingTier(Block block, int meta) {
        if (block == GregTechAPI.sBlockCasings2 && meta == 0) {
            return TIER_I; // Robust Tungstensteel Casing
        }
        if (block == GregTechAPI.sBlockCasings4 && meta == 1) {
            return TIER_II; // Stable Titanium Casing
        }
        if (block == GregTechAPI.sBlockCasings8 && meta == 10) {
            return TIER_III; // Prediction Casing
        }
        return null;
    }

    @Override
    public IStructureDefinition<ElectronicsMarket> getStructureDefinition() {
        if (STRUCTURE_DEFINITION == null) {
            STRUCTURE_DEFINITION = StructureDefinition.<ElectronicsMarket>builder()
                .addShape(STRUCTURE_PIECE_MAIN, transpose(shape))
                .addElement(
                    'C',
                    withChannel(
                        "casingtier",
                        ofBlocksTiered(
                            ElectronicsMarket::getCasingTier,
                            ImmutableList.of(
                                Pair.of(GregTechAPI.sBlockCasings2, 0),
                                Pair.of(GregTechAPI.sBlockCasings4, 1),
                                Pair.of(GregTechAPI.sBlockCasings8, 10)),
                            TIER_NONE,
                            (m, t) -> m.structureTier = Math.max(m.structureTier, t),
                            m -> m.structureTier)))
                .addElement(
                    'H',
                    HatchElementBuilder.<ElectronicsMarket>builder()
                        .atLeast(InputBus, OutputBus, InputHatch, OutputHatch, Energy.or(ExoticEnergy))
                        .adder(ElectronicsMarket::addToMachineList)
                        .dot(1)
                        .casingIndex(GTUtility.getCasingTextureIndex(GregTechAPI.sBlockCasings2, 0))
                        .buildAndChain(GregTechAPI.sBlockCasings2, 0))
                .addElement('A', ofBlock(GregTechAPI.sBlockCasings2, 0))
                .build();
        }
        return STRUCTURE_DEFINITION;
    }

    /**
     * Simple placeholder 5x5x5 structure.
     * Controller ('~') at front center, surrounded by tiered casings ('C'),
     * hatch slots ('H') on the front face sides, and solid casing ('A') elsewhere.
     *
     * Layout (each slice is one Z-layer, front to back):
     * Layer 0 (front): HHHHH, HCCCH, HC~CH, HCCCH, HHHHH
     * Layer 1: ACCCA, ACCCA, ACCCA, ACCCA, ACCCA
     * Layer 2: ACCCA, ACCCA, ACCCA, ACCCA, ACCCA
     * Layer 3: ACCCA, ACCCA, ACCCA, ACCCA, ACCCA
     * Layer 4 (back): ACCCA, ACCCA, ACCCA, ACCCA, ACCCA
     */
    // spotless:off
    public static final String[][] shape = new String[][] {
        { // Layer 0 - Front face with controller
            "HHHHH",
            "HCCCH",
            "HC~CH",
            "HCCCH",
            "HHHHH"
        },
        { // Layer 1
            "ACCCA",
            "ACCCA",
            "ACCCA",
            "ACCCA",
            "ACCCA"
        },
        { // Layer 2
            "ACCCA",
            "ACCCA",
            "ACCCA",
            "ACCCA",
            "ACCCA"
        },
        { // Layer 3
            "ACCCA",
            "ACCCA",
            "ACCCA",
            "ACCCA",
            "ACCCA"
        },
        { // Layer 4 - Back
            "ACCCA",
            "ACCCA",
            "ACCCA",
            "ACCCA",
            "ACCCA"
        }
    };
    // spotless:on

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece(STRUCTURE_PIECE_MAIN, stackSize, hintsOnly, horizontalOffSet, verticalOffSet, depthOffSet);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (this.mMachine) return -1;
        return this.survivalBuildPiece(
            STRUCTURE_PIECE_MAIN,
            stackSize,
            horizontalOffSet,
            verticalOffSet,
            depthOffSet,
            elementBudget,
            env,
            false,
            true);
    }

    @Override
    public boolean checkMachineMM(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        mDataAccessHatches.clear();
        activeSuppliers.clear();
        contractTier = ContractTier.NONE;
        structureTier = TIER_NONE;
        boolean sign = checkPiece(STRUCTURE_PIECE_MAIN, horizontalOffSet, verticalOffSet, depthOffSet);
        if (!sign || structureTier < TIER_I) {
            return false;
        }
        readContractFromDataHatches();
        rebuildActiveSuppliers();
        // Tier I base parallel: 4 (staticParallel=3, plus base +1 from getMaxParallelRecipes = 4)
        if (structureTier == TIER_I) {
            setStaticParallelParameter(3);
        }
        // Tier II/III: parallel, speed, overclock, recovery rate are driven by installed modules
        return true;
    }

    @Override
    public boolean addToMachineList(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        return super.addToMachineList(aTileEntity, aBaseCasingIndex)
            || addDataAccessToMachineList(aTileEntity, aBaseCasingIndex);
    }

    private boolean addDataAccessToMachineList(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        if (aTileEntity == null) {
            return false;
        }
        IMetaTileEntity metaTileEntity = aTileEntity.getMetaTileEntity();
        if (!(metaTileEntity instanceof MTEHatchDataAccess dataAccessHatch)) {
            return false;
        }
        ((MTEHatch) metaTileEntity).updateTexture(aBaseCasingIndex);
        return mDataAccessHatches.add(dataAccessHatch);
    }

    private void readContractFromDataHatches() {
        List<ItemStack> stacks = new ArrayList<>();
        for (MTEHatchDataAccess hatch : GTUtility.validMTEList(mDataAccessHatches)) {
            for (int slot = 0; slot < hatch.getSizeInventory(); slot++) {
                stacks.add(hatch.getStackInSlot(slot));
            }
        }
        contractTier = ContractItem.findHighestTier(stacks);
    }

    private void rebuildActiveSuppliers() {
        activeSuppliers.clear();
        List<IModularHatch> supplierHatches = new ArrayList<>(
            modularHatches.getOrDefault(ModularHatchType.SUPPLIER, Collections.emptyList()));
        for (IModularHatch hatch : supplierHatches) {
            if (hatch instanceof SupplierHatch supplierHatch) {
                SupplierId supplierId = supplierHatch.getSupplierId();
                if (supplierId.isUnlockedBy(contractTier)) {
                    activeSuppliers.add(supplierId);
                }
            }
        }
    }

    protected CheckRecipeResult validateRecipeAccess(@NotNull GTRecipe recipe) {
        if (getStructureTier() < TIER_I) {
            return CheckRecipeResultRegistry.insufficientMachineTier(getStructureTier());
        }
        if (getStructureTier() == TIER_I && recipe.mSpecialValue > 0) {
            return CheckRecipeResultRegistry.insufficientMachineTier(getStructureTier());
        }
        if (recipe.mSpecialValue == 1 && !hasFunction(FunctionType.GENERAL_DISASSEMBLY)) {
            return CheckRecipeResultRegistry.NO_RECIPE;
        }
        if (recipe.mSpecialValue == 2 && getStructureTier() < TIER_II) {
            return CheckRecipeResultRegistry.insufficientMachineTier(getStructureTier());
        }

        String supplierIdString = recipe.getMetadata(AHTechRecipeMetadata.SUPPLIER_ID);
        return isSupplierRecipeAccessible(supplierIdString) ? CheckRecipeResultRegistry.SUCCESSFUL
            : CheckRecipeResultRegistry.NO_RECIPE;
    }

    protected boolean isSupplierRecipeAccessible(String supplierIdString) {
        if (supplierIdString == null) {
            return true;
        }
        SupplierId supplierId = SupplierId.fromId(supplierIdString);
        return supplierId != null && getActiveSuppliersForUi().contains(supplierId);
    }

    // endregion

    // region Texture

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
        int aColorIndex, boolean aActive, boolean aRedstone) {
        int casingIndex = GTUtility.getCasingTextureIndex(GregTechAPI.sBlockCasings2, 0);
        if (side == facing) {
            if (aActive) return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(casingIndex),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_ASSEMBLY_LINE_ACTIVE)
                    .extFacing()
                    .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_ASSEMBLY_LINE_ACTIVE_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
            return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(casingIndex), TextureFactory.builder()
                .addIcon(OVERLAY_FRONT_ASSEMBLY_LINE)
                .extFacing()
                .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_ASSEMBLY_LINE_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
        }
        return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(casingIndex) };
    }

    // endregion

    // region Tooltip

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        final MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Electronics Market")
            .addInfo("Recycles electronics into raw materials.")
            .addInfo("Structure tier determines available recipes.")
            .addInfo("Tier I: Robust Tungstensteel Casing - Basic recipes, 30% recovery rate")
            .addInfo("Tier II: Stable Titanium Casing - Advanced recipes + modular hatches")
            .addInfo("Tier III: Prediction Casing - All recipes + modular hatches")
            .addInfo("Install modular hatches in 'H' slots to boost performance.")
            .addSeparator()
            .beginStructureBlock(5, 5, 5, false)
            .addController("Front center")
            .addCasingInfoExactly("Tiered Casing", 4, false)
            .addInputBus("Any casing", 1)
            .addOutputBus("Any casing", 1)
            .addInputHatch("Any casing", 1)
            .addOutputHatch("Any casing", 1)
            .addEnergyHatch("Any casing", 1)
            .toolTipFinisher("AHTech");
        return tt;
    }

    // endregion

    // region UI

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        super.addUIWidgets(builder, buildContext);

        // Stage display (synced from server)
        builder
            .widget(new TextWidget().setStringSupplier(() -> {
                String tierName = switch (structureTier) {
                    case TIER_I -> "I";
                    case TIER_II -> "II";
                    case TIER_III -> "III";
                    default -> "?";
                };
                return StatCollector.translateToLocalFormatted("AHTech.UI.Stage", tierName);
            })
                .setDefaultColor(0x55FF55)
                .setPos(6, 73)
                .setSize(80, 10))
            .widget(new FakeSyncWidget.IntegerSyncer(() -> structureTier, val -> structureTier = val));

        // Parallel display (synced via cached field)
        builder
            .widget(
                new TextWidget()
                    .setStringSupplier(
                        () -> StatCollector.translateToLocalFormatted("AHTech.UI.Parallel", syncedParallel))
                    .setDefaultColor(0x55FFFF)
                    .setPos(6, 83)
                    .setSize(80, 10))
            .widget(new FakeSyncWidget.IntegerSyncer(this::getMaxParallelRecipes, val -> syncedParallel = val));

        // Speed bonus display (synced via cached field)
        builder
            .widget(
                new TextWidget().setStringSupplier(
                    () -> StatCollector
                        .translateToLocalFormatted("AHTech.UI.Speed", String.format("%.0f%%", syncedSpeedBonus * 100)))
                    .setDefaultColor(0xFFFF55)
                    .setPos(6, 93)
                    .setSize(80, 10))
            .widget(new FakeSyncWidget.DoubleSyncer(() -> (double) getSpeedBonus(), val -> syncedSpeedBonus = (float) (double) val));

        // Recovery rate display (synced via cached field)
        builder
            .widget(
                new TextWidget()
                    .setStringSupplier(
                        () -> StatCollector.translateToLocalFormatted(
                            "AHTech.UI.Recovery",
                            String.format("%.0f%%", syncedRecoveryRate * 100)))
                    .setDefaultColor(0x55FFFF)
                    .setPos(6, 103)
                    .setSize(80, 10))
            .widget(new FakeSyncWidget.DoubleSyncer(() -> (double) getRecoveryRate(), val -> syncedRecoveryRate = (float) (double) val));

        builder
            .widget(
                new TextWidget().setStringSupplier(
                    () -> StatCollector.translateToLocalFormatted(
                        "AHTech.UI.Contract",
                        StatCollector.translateToLocal(ContractTier.fromTier(syncedContractTier).getTranslationKey())))
                    .setDefaultColor(0xAAAAFF)
                    .setPos(6, 113)
                    .setSize(110, 10))
            .widget(new FakeSyncWidget.IntegerSyncer(() -> getContractTierForUi().getTier(), val -> syncedContractTier = val));

        builder
            .widget(
                new TextWidget().setStringSupplier(
                    () -> StatCollector.translateToLocalFormatted("AHTech.UI.Suppliers", syncedActiveSuppliers))
                    .setDefaultColor(0xFFAA55)
                    .setPos(6, 123)
                    .setSize(110, 10))
            .widget(new FakeSyncWidget.IntegerSyncer(this::getActiveSupplierCount, val -> syncedActiveSuppliers = val));

        // Perfect overclock indicator (synced via getter, dynamic color via DynamicTextWidget)
        builder
            .widget(
                TextWidget.dynamicText(() -> {
                    boolean on = isEnablePerfectOverclock();
                    return new Text(on
                        ? StatCollector.translateToLocal("AHTech.UI.PerfectOverclock.On")
                        : StatCollector.translateToLocal("AHTech.UI.PerfectOverclock.Off"))
                            .color(on ? 0x55FF55 : 0xFF5555);
                })
                    .setPos(6, 133)
                    .setSize(80, 10));
    }

    // endregion
}
