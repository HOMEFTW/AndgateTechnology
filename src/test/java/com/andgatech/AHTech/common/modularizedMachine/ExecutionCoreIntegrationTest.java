package com.andgatech.AHTech.common.modularizedMachine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import org.junit.jupiter.api.Test;

import com.andgatech.AHTech.common.machine.ElectronicsMarket;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.executionCore.ExecutionCoreBase;

import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

class ExecutionCoreIntegrationTest {

    @Test
    void offloadingRecipeClearsMainMachineStateAndArmsExecutionCore() {
        TestElectronicsMarket market = new TestElectronicsMarket();
        TestExecutionCore core = new TestExecutionCore();
        ItemStack[] outputs = { new ItemStack(new Item().setUnlocalizedName("execution.core.output"), 2) };

        assertTrue(core.setup(market));
        assertTrue(market.offloadCurrentRecipeToExecutionCoreForTest(core, outputs, null, 40, 128));
        assertTrue(core.isWorking());
        assertNull(market.mOutputItems);
        assertNull(market.mOutputFluids);
        assertEquals(0, market.mMaxProgresstime);
        assertEquals(0, market.lEUt);
    }

    @Test
    void executionCoreCompletionPushesOutputsBackToHost() {
        TestElectronicsMarket market = new TestElectronicsMarket();
        TestExecutionCore core = new TestExecutionCore();
        ItemStack[] outputs = { new ItemStack(new Item().setUnlocalizedName("execution.core.output"), 1) };

        assertTrue(core.setup(market));
        assertTrue(market.offloadCurrentRecipeToExecutionCoreForTest(core, outputs, null, 1, 32));

        core.runExecutionCoreTick(serverTileEntity(), 1L);

        assertTrue(core.isIdle());
        assertSame(outputs, market.mergedItems);
        assertNull(market.mergedFluids);
    }

    @Test
    void executionCoreReloadRestoresInFlightWorkFromNbt() {
        TestElectronicsMarket originalHost = new TestElectronicsMarket();
        TestExecutionCore originalCore = new TestExecutionCore();
        NBTTagCompound nbt = new NBTTagCompound();

        assertTrue(originalCore.setup(originalHost));
        assertTrue(
            originalHost.offloadCurrentRecipeToExecutionCoreForTest(
                originalCore,
                new ItemStack[] { new ItemStack(Items.apple, 3) },
                null,
                3,
                64));
        originalCore.boostTick(1);
        originalCore.saveExecutionStateForTest(nbt);

        assertEquals(1, nbt.getInteger("outputItemsLength"));
        assertTrue(nbt.hasKey("outputItems0"));
        assertEquals(3, nbt.getInteger("maxProgressingTime"));
        assertEquals(1, nbt.getInteger("progressedTime"));
        assertEquals(64L, nbt.getLong("eut"));

        TestElectronicsMarket restoredHost = new TestElectronicsMarket();
        TestExecutionCore restoredCore = new TestExecutionCore();
        restoredCore.loadExecutionStateForTest(nbt);

        assertFalse(restoredCore.isHasBeenSetup());
        assertTrue(restoredCore.setup(restoredHost));
        assertEquals(3L, restoredCore.getMaxProgressingTime());
        assertEquals(1L, restoredCore.getProgressedTime());
        assertEquals(64L, restoredCore.getEut());
        assertEquals(1, restoredCore.getOutputItems().length);
        assertNull(restoredHost.mergedItems);
        assertNull(restoredHost.mergedFluids);
    }

    private static IGregTechTileEntity serverTileEntity() {
        return (IGregTechTileEntity) Proxy.newProxyInstance(
            ExecutionCoreIntegrationTest.class.getClassLoader(),
            new Class<?>[] { IGregTechTileEntity.class },
            (proxy, method, args) -> {
                if ("isServerSide".equals(method.getName())) {
                    return true;
                }
                if (method.getReturnType().equals(boolean.class)) {
                    return false;
                }
                if (method.getReturnType().equals(int.class)) {
                    return 0;
                }
                if (method.getReturnType().equals(long.class)) {
                    return 0L;
                }
                return null;
            });
    }

    private static final class TestElectronicsMarket extends ElectronicsMarket {

        private ItemStack[] mergedItems;
        private FluidStack[] mergedFluids;

        private TestElectronicsMarket() {
            super("test.execution.core.market");
        }

        private boolean offloadCurrentRecipeToExecutionCoreForTest(ExecutionCoreBase executionCore, ItemStack[] outputs,
            FluidStack[] outputFluids, int duration, long eut) {
            mOutputItems = outputs;
            mOutputFluids = outputFluids;
            mMaxProgresstime = duration;
            lEUt = -eut;
            return offloadCurrentRecipeToExecutionCore(executionCore);
        }

        @Override
        public void mergeOutputItems(ItemStack[] outputs) {
            mergedItems = outputs;
        }

        @Override
        public void mergeOutputFluids(FluidStack[] outputs) {
            mergedFluids = outputs;
        }
    }

    private static final class TestExecutionCore extends ExecutionCoreBase {

        private TestExecutionCore() {
            super("test.execution.core", 1, new String[0], new ITexture[0][][]);
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
            return new TestExecutionCore();
        }

        private void saveExecutionStateForTest(NBTTagCompound aNBT) {
            saveExecutionState(aNBT);
        }

        private void loadExecutionStateForTest(NBTTagCompound aNBT) {
            loadExecutionState(aNBT);
        }

        @Override
        public boolean done() {
            trySetActive();
            return true;
        }

        @Override
        public boolean useMainMachinePower() {
            return true;
        }
    }
}
