package net.gmx.nosefish.fishysigns.task;

import java.util.concurrent.RejectedExecutionException;

import net.canarymod.tasks.TaskOwner;

public class FishyTaskManager {
	private static volatile FishyTaskRunner FishyTaskRunnerInstance = null;
	
	/**
	 * Initializes the FishyTaskRunner instance. Called by the plugin.
	 * @param owner
	 */
	public static void initialize(TaskOwner owner) {
		FishyTaskRunnerInstance = new FishyTaskRunner(owner);
		FishyTaskRunnerInstance.start();
	}
	
	/**
	 * Submits a <code>FishyTask</code> to be executed.
	 * <p>
	 * Do not call this yourself unless you have a very good reason to.
	 * Under normal circumstances, you should use the <code>submit</code>
	 * method of the <code>FishyTask</code> instance.
	 * 
	 * @param task
	 * @throws RejectedExecutionException
	 */
	public static void submit(FishyTask task) throws RejectedExecutionException {
		if (FishyTaskRunnerInstance == null) {
			throw new AssertionError("FishyTaskManager: submit() called before initialize().");
		}
		FishyTaskRunnerInstance.submit(task);
	}
	
	/**
	 * Gets the FishyTaskRunner instance for FishySigns.
	 * @return the instance
	 */
	public static FishyTaskRunner getInstance() {
		if (FishyTaskRunnerInstance == null) {
			throw new AssertionError("FishyTaskManager: getInstance() called before initialize().");
		}
		return FishyTaskRunnerInstance;
	}
	
	

}
