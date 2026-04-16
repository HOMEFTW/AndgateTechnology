package com.andgatech.AHTech.common.machine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;

import org.junit.jupiter.api.Test;

import com.andgatech.AHTech.common.modularizedMachine.modularHatches.IModularHatch;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.parallelController.StaticParallelController;

import gregtech.api.interfaces.ITexture;

class ElectronicsMarketModuleMaintenanceBehaviorTest {

    @Test
    void maintenanceTrackingKeepsTstInteropModulesOutsideAHTechMaintenancePool() {
        TestElectronicsMarket market = new TestElectronicsMarket();
        StaticParallelController aHTechModule =
            new StaticParallelController("test.ahtech.parallel", 7, 8, new ITexture[0][][]);
        Object tstModule = new Object();

        market.addAHTechModularHatch(aHTechModule);
        market.addTstInteropModule(tstModule);

        Collection<IModularHatch> maintained = market.getModulesSubjectToMaintenanceForTest();

        assertEquals(1, maintained.size());
        assertEquals(aHTechModule, maintained.iterator().next());
    }

    private static final class TestElectronicsMarket extends ElectronicsMarket {

        private TestElectronicsMarket() {
            super("test.electronics.market.maintenance");
        }

        private void addAHTechModularHatch(IModularHatch hatch) {
            allModularHatches.add(hatch);
        }

        private void addTstInteropModule(Object hatch) {
            tstModularHatches.add(hatch);
        }

        private Collection<IModularHatch> getModulesSubjectToMaintenanceForTest() {
            return getModulesSubjectToMaintenance();
        }
    }

}
