package com.andgatech.AHTech.common.machine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.andgatech.AHTech.common.contract.ContractTier;
import com.andgatech.AHTech.common.modularizedMachine.ModularHatchType;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.ModularHatchBase;
import com.andgatech.AHTech.common.supplier.SupplierId;

import gregtech.api.metatileentity.implementations.MTEHatchDataAccess;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;

class ElectronicsMarketStructureBehaviorTest {

    @Test
    void stageShapesLockHSlotsToApprovedFloorsAndProgressivelyExpand() {
        assertShapeDistribution(ElectronicsMarketShapes.STAGE1_SHAPE, 12, 24, expectedStage1Floors());
        assertShapeDistribution(ElectronicsMarketShapes.STAGE2_SHAPE, 24, 48, expectedStage2Floors());
        assertShapeDistribution(ElectronicsMarketShapes.STAGE3_SHAPE, 32, 64, expectedStage3Floors());

        Set<String> stage1Positions = collectHPositions(ElectronicsMarketShapes.STAGE1_SHAPE);
        Set<String> stage2Positions = collectHPositions(ElectronicsMarketShapes.STAGE2_SHAPE);
        Set<String> stage3Positions = collectHPositions(ElectronicsMarketShapes.STAGE3_SHAPE);

        assertTrue(stage2Positions.containsAll(stage1Positions));
        assertTrue(stage3Positions.containsAll(stage2Positions));
    }

    @Test
    void resetStructureCheckStateForAttemptClearsResidualHatchState() throws Exception {
        TestElectronicsMarket market = new TestElectronicsMarket();
        market.seedResidualState();

        market.resetStructureCheckStateForAttempt();

        assertEquals(0, market.mInputBusses.size());
        assertEquals(0, market.mOutputBusses.size());
        assertEquals(0, market.mInputHatches.size());
        assertEquals(0, market.mOutputHatches.size());
        assertEquals(0, market.mEnergyHatches.size());
        assertEquals(
            0,
            market.getExoticEnergyHatches()
                .size());
        assertEquals(0, market.mMaintenanceHatches.size());
        assertEquals(0, market.mMufflerHatches.size());
        assertEquals(0, market.mDynamoHatches.size());
        assertEquals(0, market.mDualInputHatches.size());
        assertEquals(0, market.mSmartInputHatches.size());
        assertEquals(
            0,
            market.getAllModularHatches()
                .size());
        assertEquals(0, getDataAccessHatchCount(market));
        assertEquals(ElectronicsMarket.TIER_NONE, getStructureTier(market));
        assertEquals(ContractTier.NONE, getContractTier(market));
        assertEquals(0, getActiveSupplierCount(market));
    }

    @Test
    void hSlotsAdvertiseDataAccessAndModularHatchesAsValidPlacements() {
        Collection<Class<?>> allowed = ElectronicsMarket.getAllowedHatchClassesForStructureSlots();

        assertTrue(allowed.contains(MTEHatchInputBus.class));
        assertTrue(allowed.contains(MTEHatchDataAccess.class));
        assertTrue(allowed.contains(ModularHatchBase.class));
    }

    @Test
    void stage3SilhouetteKeepsUpperTowerBroadAndUsesTwinAntennas() {
        assertTrue(maxSpan(ElectronicsMarketShapes.STAGE3_SHAPE, 157) >= 9, "upper tower should stay broad");
        assertTrue(maxSpan(ElectronicsMarketShapes.STAGE3_SHAPE, 182) >= 9, "crown base should keep a broad platform");
        assertEquals(2, countDistinctColumns(ElectronicsMarketShapes.STAGE3_SHAPE, 205, 'A'), "antenna shaft should use twin masts");
        assertEquals(2, countDistinctColumns(ElectronicsMarketShapes.STAGE3_SHAPE, 219, 'A'), "antenna tip should keep twin masts");
    }

    private static int countChar(String[][] shape, char target) {
        int count = 0;
        for (String[] layer : shape) {
            for (String row : layer) {
                for (int i = 0; i < row.length(); i++) {
                    if (row.charAt(i) == target) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static void assertShapeDistribution(String[][] shape, int minH, int maxH, Set<Integer> allowedFloors) {
        int total = countChar(shape, 'H');
        assertTrue(total >= minH, "expected at least " + minH + " H slots, got " + total);
        assertTrue(total <= maxH, "expected at most " + maxH + " H slots, got " + total);

        Set<Integer> floors = collectHFloors(shape);
        assertEquals(allowedFloors, floors, "unexpected H floors");
    }

    private static Set<Integer> collectHFloors(String[][] shape) {
        Set<Integer> floors = new LinkedHashSet<>();
        for (int y = 0; y < shape.length; y++) {
            for (String row : shape[y]) {
                if (row.indexOf('H') >= 0) {
                    floors.add(y);
                    break;
                }
            }
        }
        return floors;
    }

    private static Set<String> collectHPositions(String[][] shape) {
        Set<String> positions = new LinkedHashSet<>();
        for (int y = 0; y < shape.length; y++) {
            for (int z = 0; z < shape[y].length; z++) {
                String row = shape[y][z];
                for (int x = 0; x < row.length(); x++) {
                    if (row.charAt(x) == 'H') {
                        positions.add(y + ":" + z + ":" + x);
                    }
                }
            }
        }
        return positions;
    }

    private static int maxSpan(String[][] shape, int y) {
        int maxSpan = 0;
        for (String row : shape[y]) {
            int min = row.length();
            int max = -1;
            for (int x = 0; x < row.length(); x++) {
                if (row.charAt(x) != ' ') {
                    min = Math.min(min, x);
                    max = Math.max(max, x);
                }
            }
            if (max >= min) {
                maxSpan = Math.max(maxSpan, max - min + 1);
            }
        }
        return maxSpan;
    }

    private static int countDistinctColumns(String[][] shape, int y, char target) {
        Set<Integer> columns = new LinkedHashSet<>();
        for (String row : shape[y]) {
            for (int x = 0; x < row.length(); x++) {
                if (row.charAt(x) == target) {
                    columns.add(x);
                }
            }
        }
        return columns.size();
    }

    private static Set<Integer> expectedStage1Floors() {
        Set<Integer> floors = new LinkedHashSet<>();
        floors.add(1);
        floors.add(2);
        floors.add(3);
        floors.add(6);
        floors.add(7);
        return floors;
    }

    private static Set<Integer> expectedStage2Floors() {
        Set<Integer> floors = expectedStage1Floors();
        floors.add(10);
        floors.add(11);
        return floors;
    }

    private static Set<Integer> expectedStage3Floors() {
        Set<Integer> floors = expectedStage2Floors();
        floors.add(30);
        floors.add(31);
        floors.add(90);
        floors.add(91);
        return floors;
    }

    private static Set<Integer> allowedHFloors() {
        Set<Integer> floors = new LinkedHashSet<>();
        floors.add(1);
        floors.add(2);
        return floors;
    }

    private static int getDataAccessHatchCount(ElectronicsMarket market) throws Exception {
        Field field = ElectronicsMarket.class.getDeclaredField("mDataAccessHatches");
        field.setAccessible(true);
        return ((Collection<?>) field.get(market)).size();
    }

    private static int getStructureTier(ElectronicsMarket market) throws Exception {
        Field field = ElectronicsMarket.class.getDeclaredField("structureTier");
        field.setAccessible(true);
        return field.getInt(market);
    }

    private static ContractTier getContractTier(ElectronicsMarket market) throws Exception {
        Field field = ElectronicsMarket.class.getDeclaredField("contractTier");
        field.setAccessible(true);
        return (ContractTier) field.get(market);
    }

    private static int getActiveSupplierCount(ElectronicsMarket market) throws Exception {
        Field field = ElectronicsMarket.class.getDeclaredField("activeSuppliers");
        field.setAccessible(true);
        return ((Collection<?>) field.get(market)).size();
    }

    private static final class TestElectronicsMarket extends ElectronicsMarket {

        private TestElectronicsMarket() {
            super("test.electronics.market.structure");
        }

        @SuppressWarnings("unchecked")
        private void seedResidualState() throws Exception {
            mInputBusses.add(null);
            mOutputBusses.add(null);
            mInputHatches.add(null);
            mOutputHatches.add(null);
            mEnergyHatches.add(null);
            mMaintenanceHatches.add(null);
            mMufflerHatches.add(null);
            mDynamoHatches.add(null);
            mDualInputHatches.add(null);
            mSmartInputHatches.add(null);
            getExoticEnergyHatches().add(null);
            modularHatches.put(ModularHatchType.FINANCIAL, java.util.Collections.emptyList());
            allModularHatches.add(null);
            tstModularHatches.add(new Object());

            Field dataAccessField = ElectronicsMarket.class.getDeclaredField("mDataAccessHatches");
            dataAccessField.setAccessible(true);
            ((Collection<Object>) dataAccessField.get(this)).add(null);

            Field structureTierField = ElectronicsMarket.class.getDeclaredField("structureTier");
            structureTierField.setAccessible(true);
            structureTierField.setInt(this, TIER_III);

            Field contractTierField = ElectronicsMarket.class.getDeclaredField("contractTier");
            contractTierField.setAccessible(true);
            contractTierField.set(this, ContractTier.LV4);

            Field activeSuppliersField = ElectronicsMarket.class.getDeclaredField("activeSuppliers");
            activeSuppliersField.setAccessible(true);
            ((EnumSet<SupplierId>) activeSuppliersField.get(this)).add(SupplierId.SHANDONG_DEZHOU);
        }
    }
}
