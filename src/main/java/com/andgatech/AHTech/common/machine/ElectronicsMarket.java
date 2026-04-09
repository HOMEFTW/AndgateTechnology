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

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import com.andgatech.AHTech.config.Config;
import com.andgatech.AHTech.recipe.recipeMap.AHTechRecipeMaps;
import com.google.common.collect.ImmutableList;
import com.gtnewhorizon.structurelib.alignment.constructable.IConstructable;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import gregtech.api.GregTechAPI;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.implementations.MTEExtendedPowerMultiBlockBase;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;
import gregtech.api.util.HatchElementBuilder;
import gregtech.api.util.MultiblockTooltipBuilder;

/**
 * Electronics Market - a multiblock machine that processes electronics recycling recipes.
 * Structure tier (I/II/III) is determined by the casing blocks used in the structure.
 * Higher tiers and higher voltage provide better speed and parallel.
 */
public class ElectronicsMarket extends MTEExtendedPowerMultiBlockBase<ElectronicsMarket>
    implements IConstructable, ISurvivalConstructable {

    // region Structure Tier Constants
    public static final int TIER_NONE = 0;
    public static final int TIER_I = 1;
    public static final int TIER_II = 2;
    public static final int TIER_III = 3;
    // endregion

    // region Instance Fields
    private int structureTier = TIER_NONE;
    protected boolean enablePerfectOverclock = false;
    protected int maxParallel = 1;
    protected float euModifier = 1;
    protected float speedBonus = 1;
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
                // Structure tier gates which recipes are available
                if (structureTier < TIER_I) {
                    return CheckRecipeResultRegistry.insufficientMachineTier(structureTier);
                }
                return CheckRecipeResultRegistry.SUCCESSFUL;
            }

            @NotNull
            @Override
            public CheckRecipeResult process() {
                setEuModifier(getEuModifier());
                setSpeedBonus(getSpeedBonus());
                // Perfect overclock: time/4, power*4; Normal overclock: time/2, power*4
                setOverclock(isEnablePerfectOverclock() ? 4 : 2, 4);
                return super.process();
            }

        }.setMaxParallelSupplier(this::getMaxParallelRecipes);
    }

    protected boolean isEnablePerfectOverclock() {
        return enablePerfectOverclock;
    }

    protected float getEuModifier() {
        return euModifier;
    }

    protected float getSpeedBonus() {
        return speedBonus;
    }

    public int getMaxParallelRecipes() {
        return maxParallel;
    }

    // endregion

    // region Maintenance / Repair

    /**
     * Repairs the machine by enabling all tools (no maintenance required).
     */
    public void repairMachine() {
        mHardHammer = true;
        mSoftMallet = true;
        mScrewdriver = true;
        mCrowbar = true;
        mSolderingTool = true;
        mWrench = true;
    }

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
        aNBT.setBoolean("enablePerfectOverclock", enablePerfectOverclock);
        aNBT.setInteger("maxParallel", maxParallel);
        aNBT.setFloat("euModifier", euModifier);
        aNBT.setFloat("speedBonus", speedBonus);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        structureTier = aNBT.getInteger("structureTier");
        enablePerfectOverclock = aNBT.getBoolean("enablePerfectOverclock");
        maxParallel = Math.max(aNBT.getInteger("maxParallel"), 1);
        euModifier = aNBT.getFloat("euModifier");
        if (euModifier <= 0) euModifier = 1;
        speedBonus = aNBT.getFloat("speedBonus");
        if (speedBonus <= 0) speedBonus = 1;
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

    /**
     * Calculates the voltage tier from the machine's total available EU.
     * 0 = ULV, 1 = LV, 2 = MV, 3 = HV, etc.
     */
    private int getTotalPowerTier() {
        long totalEu = getMaxInputEu();
        if (totalEu <= 0) return 0;
        return (int) Math.round(1 + Math.max(0, (Math.log(totalEu) / Math.log(2) - 5) / 2));
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        repairMachine();
        structureTier = TIER_NONE;
        boolean sign = checkPiece(STRUCTURE_PIECE_MAIN, horizontalOffSet, verticalOffSet, depthOffSet);
        if (!sign || structureTier < TIER_I) {
            return false;
        }

        // Compute speed bonus and parallel based on structure tier and voltage
        int voltageTier = getTotalPowerTier();

        // Speed bonus: higher tiers are faster
        // Tier I: 1x, Tier II: 2x, Tier III: 4x
        speedBonus = switch (structureTier) {
            case TIER_III -> 1.0F / 4.0F;
            case TIER_II -> 1.0F / 2.0F;
            default -> 1.0F;
        };

        // Parallel: tier base * voltage factor
        // Tier I: 4, Tier II: 16, Tier III: 64, multiplied by (1 + voltageTier / 8)
        int tierBaseParallel = switch (structureTier) {
            case TIER_III -> 64;
            case TIER_II -> 16;
            default -> 4;
        };
        maxParallel = (int) Math.min(Config.MAX_PARALLEL_LIMIT, (long) tierBaseParallel * (1 + voltageTier / 8));

        // Perfect overclock at tier III
        enablePerfectOverclock = structureTier >= TIER_III;

        // EU modifier: no discount by default
        euModifier = 1.0F;

        return true;
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
            .addInfo("Structure tier determines available recipes and performance.")
            .addInfo("Tier I: Robust Tungstensteel Casing - Basic recipes")
            .addInfo("Tier II: Stable Titanium Casing - Advanced recipes")
            .addInfo("Tier III: Prediction Casing - All recipes + Perfect Overclock")
            .addInfo("Higher voltage tiers increase parallel.")
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
}
