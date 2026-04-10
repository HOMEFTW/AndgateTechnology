package com.andgatech.AHTech.common.modularizedMachine.modularHatches.executionCore;

/**
 * Interface for execution core modules. Execution cores perform actual processing work within a modularized machine.
 */
public interface IExecutionCore {

    /**
     * @return If true this execution core module is idle, new recipe detection can be performed.
     */
    boolean isIdle();

    /**
     * @return If true this execution core module is working.
     */
    boolean isWorking();

    /**
     * @return If true this execution core uses main machine power system.
     */
    boolean useMainMachinePower();

    /**
     * Finish parameter setting.
     *
     * @return Success.
     */
    boolean done();
}
