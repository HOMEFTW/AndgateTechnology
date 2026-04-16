package com.andgatech.AHTech.common.machine;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlocksTiered;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofChain;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.withChannel;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ASSEMBLY_LINE;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ASSEMBLY_LINE_ACTIVE;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ASSEMBLY_LINE_ACTIVE_GLOW;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ASSEMBLY_LINE_GLOW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import com.andgatech.AHTech.common.currency.CurrencyType;
import com.andgatech.AHTech.common.modularizedMachine.FunctionType;
import com.andgatech.AHTech.common.modularizedMachine.ModularHatchType;
import com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineSupportAllModuleBase;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.FinancialHatch;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.IModularHatch;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.ModularHatchBase;
import com.andgatech.AHTech.common.supplier.SupplierHatch;
import com.andgatech.AHTech.common.supplier.SupplierId;
import com.andgatech.AHTech.config.Config;
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
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.metatileentity.IMetricsExporter;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.metatileentity.implementations.MTEHatchDataAccess;
import gregtech.api.metatileentity.implementations.MTEHatchEnergy;
import gregtech.api.metatileentity.implementations.MTEHatchInput;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.metatileentity.implementations.MTEHatchOutput;
import gregtech.api.metatileentity.implementations.MTEHatchOutputBus;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.recipe.check.SimpleCheckRecipeResult;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.ExoticEnergyInputHelper;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;
import gregtech.api.util.HatchElementBuilder;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.ParallelHelper;

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
    private final List<FinancialHatch> financialHatches = new ArrayList<>();
    private final int[] syncedFinancialCounts = new int[CurrencyType.values().length];
    private int syncedFinancialHatchCount;
    private CurrencyType currentRecipeCurrencyType;
    private int currentRecipeCurrencyCost;
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
        lines.add("Stage: " + getStructureTierLabel() + " | Contract: " + getContractTierLabel());
        lines.add("Suppliers: " + getActiveSupplierCount() + " | Parallel: " + getMaxParallelRecipes());
        lines.add("Speed Bonus: " + formatPercent(getSpeedBonus()));
        lines.add(
            "Recovery Rate: " + formatPercent(getRecoveryRate())
                + " | Perfect Overclock: "
                + (isEnablePerfectOverclock() ? "ON" : "OFF"));
        lines.add("Modules: " + getInstalledModulesLabel());
        lines.add("Finance: " + getFinancialStatusLine());
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

    protected String getFinancialStatusLine() {
        if (!Config.EnableFinancialSystem) {
            return "Disabled";
        }
        if (financialHatches.isEmpty()) {
            return "No Hatch";
        }

        List<String> balances = new ArrayList<>();
        for (CurrencyType type : CurrencyType.values()) {
            int total = getTotalCurrency(type);
            if (total > 0) {
                balances.add(type.getName() + "x" + total);
            }
        }
        return balances.isEmpty() ? "Empty" : String.join(" | ", balances);
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

    @Override
    public int getMaxAllowedModuleTier(ModularHatchType type) {
        if (getStructureTier() >= TIER_II) {
            return switch (type) {
                case PARALLEL_CONTROLLER, SPEED_CONTROLLER, OVERCLOCK_CONTROLLER, POWER_CONSUMPTION_CONTROLLER, EXECUTION_CORE -> Integer.MAX_VALUE;
                default -> getStructureTier();
            };
        }
        return getStructureTier();
    }

    /**
     * Stage I: skip module application, use hardcoded parameters only.
     * Stage II/III: apply module effects normally via super.
     */
    @Override
    public void checkModularStaticSettings() {
        if (getStructureTier() == TIER_I) {
            // Stage I does not use the modular system
            resetModularStaticSettings();
            setStaticParallelParameter(3); // hardcoded 4 parallel (3 + 1 base)
            return;
        }
        // Stage II/III: apply module effects normally
        super.checkModularStaticSettings();
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

            @NotNull
            @Override
            protected ParallelHelper createParallelHelper(@NotNull GTRecipe recipe) {
                int affordableParallel = ElectronicsMarket.this.getAffordableCurrencyParallel(recipe, maxParallel);
                return super.createParallelHelper(recipe).setMaxParallel(affordableParallel);
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
            // Power insufficiency check: if enabled and power is too low, swallow materials
            if (Config.EnablePowerInsufficientMaterialLoss && !isPowerSufficientForModules()) {
                mOutputItems = null;
                mOutputFluids = null;
                consumeCurrencyFromRecipe();
                resetRecipeCurrencyState();
                return SimpleCheckRecipeResult.ofFailure("power_insufficient");
            }
            applyRecoveryRate();
            consumeCurrencyFromRecipe();
        }
        resetRecipeCurrencyState();
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
            int recovered = calculateRecoveredStackSize(original, rate, nextRecoveryRoll());
            if (recovered <= 0) {
                mOutputItems[i] = null;
            } else {
                mOutputItems[i].stackSize = recovered;
            }
        }
    }

    private void consumeCurrencyFromRecipe() {
        if (!Config.EnableFinancialSystem || currentRecipeCurrencyType == null || currentRecipeCurrencyCost <= 0) {
            return;
        }

        int remaining = scaleCurrencyCostForParallels(currentRecipeCurrencyCost, getCurrentRecipeParallelCount());
        for (FinancialHatch hatch : financialHatches) {
            if (remaining <= 0) {
                break;
            }
            remaining -= hatch.consumeCurrency(currentRecipeCurrencyType, remaining);
        }
    }

    protected int getCurrentRecipeParallelCount() {
        if (processingLogic == null) {
            return 1;
        }
        return Math.max(1, processingLogic.getCurrentParallels());
    }

    protected int getAffordableCurrencyParallel(@NotNull GTRecipe recipe, int requestedParallel) {
        CurrencyType currencyType = recipe.getMetadata(AHTechRecipeMetadata.CURRENCY_TYPE);
        Integer currencyCost = recipe.getMetadata(AHTechRecipeMetadata.CURRENCY_COST);
        if (!Config.EnableFinancialSystem || currencyType == null || currencyCost == null || currencyCost <= 0) {
            return requestedParallel;
        }
        return limitParallelByAvailableCurrency(requestedParallel, getTotalCurrency(currencyType), currencyCost);
    }

    static int scaleCurrencyCostForParallels(int baseCost, int parallels) {
        if (baseCost <= 0) {
            return 0;
        }
        long scaled = (long) baseCost * Math.max(1, parallels);
        return scaled > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) scaled;
    }

    static int limitParallelByAvailableCurrency(int requestedParallel, int availableCurrency, int currencyCost) {
        if (requestedParallel <= 0 || availableCurrency <= 0 || currencyCost <= 0) {
            return 0;
        }
        return Math.min(requestedParallel, availableCurrency / currencyCost);
    }

    static int calculateRecoveredStackSize(int original, float rate, int recoveryRoll) {
        if (original <= 0 || rate <= 0.0f) {
            return 0;
        }
        if (rate >= 1.0f) {
            return original;
        }

        float expected = original * rate;
        int guaranteed = (int) Math.floor(expected);
        float fractional = expected - guaranteed;
        int fractionalChance = Math.max(0, Math.min(10000, Math.round(fractional * 10000.0f)));
        return guaranteed + (recoveryRoll < fractionalChance ? 1 : 0);
    }

    protected int nextRecoveryRoll() {
        if (getBaseMetaTileEntity() == null) {
            return 0;
        }
        return getBaseMetaTileEntity().getRandomNumber(10000);
    }

    // region Power Sufficiency Check

    /**
     * Calculates the total maintenance EU/t cost of all installed modules.
     * Each module's cost scales with its tier.
     */
    private long calculateTotalMaintenanceEUt() {
        long total = 0;
        for (IModularHatch hatch : getModulesSubjectToMaintenance()) {
            if (hatch instanceof ModularHatchBase base) {
                total += base.getMaintenanceEUt();
            }
        }
        return total;
    }

    /**
     * Only AHTech-native modular hatches participate in the Electronics Market maintenance-EU mechanic.
     * TST interop hatches still contribute their normal behavior, but TST's original implementation does
     * not have this extra maintenance cost layer, so we keep them outside this pool for parity.
     */
    protected List<IModularHatch> getModulesSubjectToMaintenance() {
        return new ArrayList<>(getAllModularHatches());
    }

    /**
     * Sums stored EU across all energy hatches.
     */
    private long getTotalStoredEU() {
        long stored = 0;
        for (MTEHatch hatch : getExoticAndNormalEnergyHatchList()) {
            stored += hatch.getBaseMetaTileEntity()
                .getStoredEU();
        }
        return stored;
    }

    /**
     * Checks if the stored EU is sufficient to cover module maintenance for at least one tick.
     */
    private boolean isPowerSufficientForModules() {
        long maintenance = calculateTotalMaintenanceEUt();
        if (maintenance <= 0) return true; // no modules, no cost
        return hasEnoughStoredPower(getTotalStoredEU(), maintenance);
    }

    static boolean hasEnoughStoredPower(long storedEU, long maintenanceEUt) {
        return storedEU >= maintenanceEUt;
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

    private static final String STRUCTURE_PIECE_STAGE1 = "stage1";
    private static final String STRUCTURE_PIECE_STAGE2 = "stage2";
    private static final String STRUCTURE_PIECE_STAGE3 = "stage3";
    private static final Collection<Class<?>> HATCH_SLOT_CLASSES = createAllowedHatchClassesForStructureSlots();
    private static IStructureDefinition<ElectronicsMarket> STRUCTURE_DEFINITION = null;

    // Structure offsets: controller at Y=1, Z=0, X=9 in the 18-wide coordinate system
    private static final int horizontalOffSet = 9;
    private static final int verticalOffSet = 1;
    private static final int depthOffSet = 0;

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

    static Collection<Class<?>> getAllowedHatchClassesForStructureSlots() {
        return HATCH_SLOT_CLASSES;
    }

    private static Collection<Class<?>> createAllowedHatchClassesForStructureSlots() {
        List<Class<?>> classes = new ArrayList<>(
            Arrays.asList(
                MTEHatchInputBus.class,
                MTEHatchOutputBus.class,
                MTEHatchInput.class,
                MTEHatchOutput.class,
                MTEHatchEnergy.class,
                MTEHatchDataAccess.class,
                ModularHatchBase.class));
        classes.addAll(ExoticEnergyInputHelper.getAllClasses());
        return Collections.unmodifiableList(classes);
    }

    @SuppressWarnings("unchecked")
    private static List<Class<? extends IMetaTileEntity>> getAllowedHatchClassesForStructureSlotsTyped() {
        return (List<Class<? extends IMetaTileEntity>>) (List<?>) new ArrayList<>(HATCH_SLOT_CLASSES);
    }

    @Override
    public IStructureDefinition<ElectronicsMarket> getStructureDefinition() {
        if (STRUCTURE_DEFINITION == null) {
            STRUCTURE_DEFINITION = StructureDefinition.<ElectronicsMarket>builder()
                .addShape(STRUCTURE_PIECE_STAGE1, transpose(ElectronicsMarketShapes.STAGE1_SHAPE))
                .addShape(STRUCTURE_PIECE_STAGE2, transpose(ElectronicsMarketShapes.STAGE2_SHAPE))
                .addShape(STRUCTURE_PIECE_STAGE3, transpose(ElectronicsMarketShapes.STAGE3_SHAPE))
                // Podium casing - tier-determining
                .addElement(
                    'P',
                    withChannel(
                        "casingtier",
                        ofBlocksTiered(
                            ElectronicsMarket::getCasingTier,
                            ImmutableList.of(
                                Pair.of(GregTechAPI.sBlockCasings2, 0),
                                Pair.of(GregTechAPI.sBlockCasings4, 1),
                                Pair.of(GregTechAPI.sBlockCasings8, 10)),
                            TIER_NONE,
                            (m, t) -> m.structureTier = t,
                            m -> m.structureTier)))
                // Door - same tier logic as podium
                .addElement(
                    'D',
                    withChannel(
                        "casingtier",
                        ofBlocksTiered(
                            ElectronicsMarket::getCasingTier,
                            ImmutableList.of(
                                Pair.of(GregTechAPI.sBlockCasings2, 0),
                                Pair.of(GregTechAPI.sBlockCasings4, 1),
                                Pair.of(GregTechAPI.sBlockCasings8, 10)),
                            TIER_NONE,
                            (m, t) -> m.structureTier = t,
                            m -> m.structureTier)))
                // Tower wall
                .addElement('T', ofBlock(GregTechAPI.sBlockCasings2, 15))
                // Vertical decorative lines (meta 2 avoids conflict with Tier II casing at meta 1)
                .addElement('V', ofBlock(GregTechAPI.sBlockCasings4, 2))
                // Setback transition
                .addElement('S', ofBlock(GregTechAPI.sBlockCasings3, 2))
                // Crown
                .addElement('K', ofBlock(GregTechAPI.sBlockCasings4, 8))
                // Antenna
                .addElement('A', ofBlock(GregTechAPI.sBlockCasings5, 4))
                // Hatch slots - fallback uses tiered casing so H positions accept matching tier
                .addElement(
                    'H',
                    ofChain(
                        HatchElementBuilder.<ElectronicsMarket>builder()
                            .adder(ElectronicsMarket::addAllowedHatchToMachineList)
                            .hatchClasses(getAllowedHatchClassesForStructureSlotsTyped())
                            .dot(1)
                            .casingIndex(GTUtility.getCasingTextureIndex(GregTechAPI.sBlockCasings2, 0))
                            .build(),
                        withChannel(
                            "casingtier",
                            ofBlocksTiered(
                                ElectronicsMarket::getCasingTier,
                                ImmutableList.of(
                                    Pair.of(GregTechAPI.sBlockCasings2, 0),
                                    Pair.of(GregTechAPI.sBlockCasings4, 1),
                                    Pair.of(GregTechAPI.sBlockCasings8, 10)),
                                TIER_NONE,
                                (ElectronicsMarket m, Integer t) -> m.structureTier = t,
                                (ElectronicsMarket m) -> m.structureTier))))
                .build();
        }
        return STRUCTURE_DEFINITION;
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        int tier = (stackSize != null && stackSize.stackSize > 0) ? Math.min(3, Math.max(1, stackSize.stackSize)) : 3;
        String piece = switch (tier) {
            case 1 -> STRUCTURE_PIECE_STAGE1;
            case 2 -> STRUCTURE_PIECE_STAGE2;
            default -> STRUCTURE_PIECE_STAGE3;
        };
        buildPiece(piece, stackSize, hintsOnly, horizontalOffSet, verticalOffSet, depthOffSet);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (this.mMachine) return -1;
        int tier = (stackSize != null && stackSize.stackSize > 0) ? Math.min(3, Math.max(1, stackSize.stackSize)) : 3;
        String piece = switch (tier) {
            case 1 -> STRUCTURE_PIECE_STAGE1;
            case 2 -> STRUCTURE_PIECE_STAGE2;
            default -> STRUCTURE_PIECE_STAGE3;
        };
        return this.survivalBuildPiece(
            piece,
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
        resetStructureCheckStateForAttempt();

        // Try Stage III (full 220-layer building) first
        if (checkPiece(STRUCTURE_PIECE_STAGE3, horizontalOffSet, verticalOffSet, depthOffSet)
            && structureTier >= TIER_I) {
            return finalizeStructureCheck();
        }

        // Reset and try Stage II (podium + setback + 4-layer tower + cap, 26 layers)
        resetStructureCheckStateForAttempt();
        if (checkPiece(STRUCTURE_PIECE_STAGE2, horizontalOffSet, verticalOffSet, depthOffSet)
            && structureTier >= TIER_I) {
            return finalizeStructureCheck();
        }

        // Reset and try Stage I (podium only, 20 layers)
        resetStructureCheckStateForAttempt();
        if (!checkPiece(STRUCTURE_PIECE_STAGE1, horizontalOffSet, verticalOffSet, depthOffSet)
            || structureTier < TIER_I) {
            return false;
        }

        return finalizeStructureCheck();
    }

    void resetStructureCheckStateForAttempt() {
        mInputHatches.clear();
        mOutputHatches.clear();
        mInputBusses.clear();
        mOutputBusses.clear();
        mDualInputHatches.clear();
        mSmartInputHatches.clear();
        mEnergyHatches.clear();
        getExoticEnergyHatches().clear();
        mDynamoHatches.clear();
        mMufflerHatches.clear();
        mMaintenanceHatches.clear();
        mDataAccessHatches.clear();
        activeSuppliers.clear();
        financialHatches.clear();
        contractTier = ContractTier.NONE;
        structureTier = TIER_NONE;
        resetRecipeCurrencyState();
        resetModularHatchCollections();
    }

    private boolean finalizeStructureCheck() {
        readContractFromDataHatches();
        rebuildActiveSuppliers();
        rebuildFinancialHatches();
        autoRefillFinancialHatches();
        return true;
    }

    private boolean addAllowedHatchToMachineList(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        return addInputBusToMachineList(aTileEntity, aBaseCasingIndex)
            || addOutputBusToMachineList(aTileEntity, aBaseCasingIndex)
            || addInputHatchToMachineList(aTileEntity, aBaseCasingIndex)
            || addOutputHatchToMachineList(aTileEntity, aBaseCasingIndex)
            || addEnergyInputToMachineList(aTileEntity, aBaseCasingIndex)
            || addExoticEnergyInputToMachineList(aTileEntity, aBaseCasingIndex)
            || addDataAccessToMachineList(aTileEntity, aBaseCasingIndex)
            || addAnyModularHatchToMachineList(aTileEntity, aBaseCasingIndex);
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

    private void rebuildFinancialHatches() {
        financialHatches.clear();
        List<IModularHatch> hatches = new ArrayList<>(
            modularHatches.getOrDefault(ModularHatchType.FINANCIAL, Collections.emptyList()));
        for (IModularHatch hatch : hatches) {
            if (hatch instanceof FinancialHatch financialHatch) {
                financialHatches.add(financialHatch);
            }
        }
    }

    private void autoRefillFinancialHatches() {
        if (!Config.EnableFinancialSystem || !Config.EnableAutoRefillFromInputBus || financialHatches.isEmpty()) {
            return;
        }

        List<MTEHatchInputBus> inputBuses = getInputBuses();
        for (FinancialHatch financialHatch : financialHatches) {
            financialHatch.autoRefillFromInputBus(inputBuses);
        }
    }

    void prepareFinancialStateForRecipeCheck() {
        autoRefillFinancialHatches();
    }

    private List<MTEHatchInputBus> getInputBuses() {
        List<MTEHatchInputBus> inputBuses = new ArrayList<>();
        for (Object hatch : GTUtility.validMTEList(mInputBusses)) {
            if (hatch instanceof MTEHatchInputBus inputBus) {
                inputBuses.add(inputBus);
            }
        }
        return inputBuses;
    }

    private int getTotalCurrency(CurrencyType type) {
        int total = 0;
        for (FinancialHatch hatch : financialHatches) {
            total += hatch.countCurrency(type);
        }
        return total;
    }

    private void resetRecipeCurrencyState() {
        currentRecipeCurrencyType = null;
        currentRecipeCurrencyCost = 0;
    }

    protected CheckRecipeResult validateRecipeAccess(@NotNull GTRecipe recipe) {
        resetRecipeCurrencyState();
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
        if ((getStructureTier() == TIER_II) && (recipe.mEUt > Config.getStage2_MaxVoltageEUt())) {
            return SimpleCheckRecipeResult.ofFailure("voltage_exceeded");
        }

        String supplierIdString = recipe.getMetadata(AHTechRecipeMetadata.SUPPLIER_ID);
        if (!isSupplierRecipeAccessible(supplierIdString)) {
            return CheckRecipeResultRegistry.NO_RECIPE;
        }

        if (!Config.EnableFinancialSystem) {
            return CheckRecipeResultRegistry.SUCCESSFUL;
        }

        prepareFinancialStateForRecipeCheck();

        CurrencyType currencyType = recipe.getMetadata(AHTechRecipeMetadata.CURRENCY_TYPE);
        Integer currencyCost = recipe.getMetadata(AHTechRecipeMetadata.CURRENCY_COST);
        if (currencyType == null || currencyCost == null || currencyCost <= 0) {
            return CheckRecipeResultRegistry.SUCCESSFUL;
        }
        if (financialHatches.isEmpty()) {
            return CheckRecipeResultRegistry.NO_RECIPE;
        }
        if (getTotalCurrency(currencyType) < currencyCost) {
            return CheckRecipeResultRegistry.NO_RECIPE;
        }

        currentRecipeCurrencyType = currencyType;
        currentRecipeCurrencyCost = currencyCost;
        return CheckRecipeResultRegistry.SUCCESSFUL;
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
            .addInfo("SEG Plaza Electronics Recycling Center")
            .addInfo("Three-tier building structure determines available recipes.")
            .addSeparator()
            .addInfo("Stage I (Podium): 18x18 rounded base, 20 layers")
            .addInfo("  - Basic recipes only, 30% recovery, hardcoded parallel")
            .addInfo("Stage II (Podium + Tower): 26 layers")
            .addInfo("  - Advanced recipes + modular hatches unlocked")
            .addInfo("Stage III (Full Building): 220 layers")
            .addInfo("  - All recipes + full modular hatch support")
            .addSeparator()
            .addInfo("Casing tier (P/D blocks) determines structure tier:")
            .addInfo("  Tier I: Robust Tungstensteel | Tier II: Stable Titanium | Tier III: Prediction")
            .addInfo("Use programmable builder with stackSize 1/2/3 for each stage.")
            .addSeparator()
            .beginStructureBlock(18, 220, 18, false)
            .addController("Podium front face, Y=1 center")
            .addInputBus("Any H slot (podium front)", 1)
            .addOutputBus("Any H slot (podium front)", 1)
            .addInputHatch("Any H slot (podium front)", 1)
            .addOutputHatch("Any H slot (podium front)", 1)
            .addEnergyHatch("Any H slot (podium front)", 1)
            .addOtherStructurePart(
                StatCollector.translateToLocal("GT5U.tooltip.structure.data_access_hatch"),
                "Any H slot (for contracts)",
                1)
            .toolTipFinisher("AHTech");
        return tt;
    }

    // endregion

    // region UI

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        super.addUIWidgets(builder, buildContext);

        // Stage display (synced from server)
        builder.widget(new TextWidget().setStringSupplier(() -> {
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
            .widget(
                new FakeSyncWidget.DoubleSyncer(
                    () -> (double) getSpeedBonus(),
                    val -> syncedSpeedBonus = (float) (double) val));

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
            .widget(
                new FakeSyncWidget.DoubleSyncer(
                    () -> (double) getRecoveryRate(),
                    val -> syncedRecoveryRate = (float) (double) val));

        builder
            .widget(
                new TextWidget()
                    .setStringSupplier(
                        () -> StatCollector.translateToLocalFormatted(
                            "AHTech.UI.Contract",
                            StatCollector.translateToLocal(
                                ContractTier.fromTier(syncedContractTier)
                                    .getTranslationKey())))
                    .setDefaultColor(0xAAAAFF)
                    .setPos(6, 113)
                    .setSize(110, 10))
            .widget(
                new FakeSyncWidget.IntegerSyncer(
                    () -> getContractTierForUi().getTier(),
                    val -> syncedContractTier = val));

        builder
            .widget(
                new TextWidget()
                    .setStringSupplier(
                        () -> StatCollector.translateToLocalFormatted("AHTech.UI.Suppliers", syncedActiveSuppliers))
                    .setDefaultColor(0xFFAA55)
                    .setPos(6, 123)
                    .setSize(110, 10))
            .widget(new FakeSyncWidget.IntegerSyncer(this::getActiveSupplierCount, val -> syncedActiveSuppliers = val));

        // Perfect overclock indicator (synced via getter, dynamic color via DynamicTextWidget)
        builder.widget(TextWidget.dynamicText(() -> {
            boolean on = isEnablePerfectOverclock();
            return new Text(
                on ? StatCollector.translateToLocal("AHTech.UI.PerfectOverclock.On")
                    : StatCollector.translateToLocal("AHTech.UI.PerfectOverclock.Off")).color(on ? 0x55FF55 : 0xFF5555);
        })
            .setPos(6, 133)
            .setSize(80, 10));

        if (Config.EnableFinancialSystem) {
            builder.widget(
                new TextWidget().setStringSupplier(
                    () -> StatCollector.translateToLocalFormatted("AHTech.UI.Finance", getFinancialStatusTextForUi()))
                    .setDefaultColor(0xFFD700)
                    .setPos(6, 143)
                    .setSize(160, 10));

            builder.widget(
                new FakeSyncWidget.IntegerSyncer(this::getFinancialHatchCount, val -> syncedFinancialHatchCount = val));
            for (int i = 0; i < CurrencyType.values().length; i++) {
                CurrencyType type = CurrencyType.values()[i];
                int index = i;
                builder.widget(
                    new FakeSyncWidget.IntegerSyncer(
                        () -> getTotalCurrency(type),
                        val -> syncedFinancialCounts[index] = val));
            }
        }
    }

    private int getFinancialHatchCount() {
        return financialHatches.size();
    }

    private String getFinancialStatusTextForUi() {
        if (syncedFinancialHatchCount <= 0) {
            return StatCollector.translateToLocal("AHTech.UI.Finance.NoHatch");
        }

        List<String> balances = new ArrayList<>();
        CurrencyType[] types = CurrencyType.values();
        for (int i = 0; i < types.length; i++) {
            if (syncedFinancialCounts[i] > 0) {
                balances.add(types[i].getName() + "x" + syncedFinancialCounts[i]);
            }
        }
        return balances.isEmpty() ? StatCollector.translateToLocal("AHTech.UI.Finance.Empty")
            : String.join(" | ", balances);
    }

    // endregion
}
