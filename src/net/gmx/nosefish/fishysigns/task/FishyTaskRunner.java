package net.gmx.nosefish.fishysigns.task;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


import net.canarymod.tasks.ServerTask;
import net.canarymod.tasks.ServerTaskManager;
import net.canarymod.tasks.TaskOwner;
import net.gmx.nosefish.fishysigns.Log;

public class FishyTaskRunner {
	private volatile boolean shutdown = false;

	private final TaskOwner owner;
	private final ExecutorService workerPool;
	
	private final ExecutorService queuePool;
	private final FishyTaskSorter inputSorter, timeDelaySorter, tickDelaySorter;
	
	/**
	 * Constructor
	 * @param owner 
	 * 			the <code>TaskOwner</code> to use when running a task through the <code>ServerTaskManager</code>;
	 * 			usually the plugin.
	 */
	public FishyTaskRunner(TaskOwner owner){
		this.owner = owner;
		// prepare Executors
		//TODO: make configurable
		this.workerPool = Executors.newFixedThreadPool(8);
		if (workerPool instanceof ThreadPoolExecutor) {
			((ThreadPoolExecutor)workerPool).setMaximumPoolSize(16);
		}
		this.queuePool = Executors.newCachedThreadPool();
		// prepare sorters
		this.inputSorter = new FishyTaskInputSorter();
		this.timeDelaySorter = new TimeDelaySorter();
		this.tickDelaySorter = new TickDelaySorter();
	}
	
	public void start() {
		queuePool.execute(timeDelaySorter);
		queuePool.execute(inputSorter);
	}
	
	/**
	 * Enqueues a <code>FishyTask</code> for execution.
	 * @param task the task to execute
	 * @throws RejectedExecutionException if the task cannot be executed
	 */
	public void submit(FishyTask task) throws RejectedExecutionException {
		if (this.shutdown) {
			throw new RejectedExecutionException("FishyTaskRunner is shutting down.");
		}
		this.inputSorter.submit(task);
	}
	
	/**
	 * Wraps the <code>FishyTask</code> in a <code>FishyServerTask</code>
	 * and adds it to the server queue.
	 * 
	 * @param task the task to run in the server
	 */
	private void runInServer(FishyTask task) {
		ServerTask serverTask = new FishyServerTask(task);
		ServerTaskManager.addTask(serverTask);
	}
	
	/**
	 * Called when the server ticks. Do not call yourself.
	 */
	public void tick() {
		tickDelaySorter.run();
	}
	
	/**
	 * Shuts down the FishyTask runner, cancels all queued tasks.
	 * Must be called from the server thread.
	 */
	public void shutdown() {
		this.shutdown = true;
		// shut down sorters
		ShutdownTask poisonPill = new ShutdownTask();
		inputSorter.submit(poisonPill);
		timeDelaySorter.submit(poisonPill);
		tickDelaySorter.submit(poisonPill);
		// shut down thread pools
		queuePool.shutdown();
		workerPool.shutdown();
		try {
			queuePool.awaitTermination(1, TimeUnit.SECONDS);
			workerPool.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			Log.get().warn("FishyTaskRunner was interrupted while waiting for the thread pool to shut down!");
		} finally {
			queuePool.shutdownNow();
			workerPool.shutdownNow();
		}
		Log.get().info("The FishyTaskRunner has been shut down.");
	}
	

	/**
	 * Wraps a <code>FishyTask</code> in a <code>ServerTask</code> for execution
	 * by Canary's <code>ServerTaskManager</code>.
	 * 
	 * @author Stefan Steinheimer (nosefish)
	 *
	 */
	private class FishyServerTask extends ServerTask {
		private FishyTask task;
		
		public FishyServerTask(FishyTask task) {
			super(owner, 0, false);
			this.task = task;
		}
		
		public void run() {
			task.run();
		}
	}
	
	/**
	 * Poison pill that makes the task sorters shut down.
	 * 
	 * @author Stefan Steinheimer
	 *
	 */
	private static class ShutdownTask extends FishyTask {
		public void doStuff() {
			Log.get().warn("The ShutdownTask is being executed. This should not happen. Please file a bug report.");
		}
	} // end of internal class
	
	
	private abstract class FishyTaskSorter implements Runnable {
		protected final BlockingQueue<FishyTask> inQ;
		protected boolean shutdown;
		/**
		 * Constructor
		 */
		public FishyTaskSorter(BlockingQueue<FishyTask> inputQueue) {
			this.inQ = inputQueue;
		}
		
		public void submit(FishyTask task) {
			if (this.shutdown) {
				throw new RejectedExecutionException(this.getClass().getName() +" has been shut down");
			}
			this.inQ.offer(task);
		}
		
		
		@Override
		public void run() {
			try {
				while (true) {
					FishyTask task = null;
					try {
						task = inQ.take();
						if (task instanceof ShutdownTask) {
							break;
						}
						sort(task);
					} catch(InterruptedException e) {
						break;
					} catch(RejectedExecutionException e) {
						// shutting down
						break;
					}
				}
			} finally {
				cleanup();
				Log.get().info("FishyTaskSorter has been shut down.");
			}
		}
		
		/**
		 * Empties input queue and cancels all pending tasks
		 */
		protected void cleanup() {
			this.shutdown = true;
			while (! inQ.isEmpty()) {
				Iterator<FishyTask> iter = inQ.iterator();
				// cancel and remove all pending tasks
				while (iter.hasNext()) {
					FishyTask task = iter.next();
					if (task != null) {
						task.cancel();
					}
					iter.remove();
				}
			}
		}
		
		protected abstract void sort(FishyTask task);

	} // end of internal class

	/**
	 * Sorts incoming <code>FishyTask</code>s for execution
	 * according to their properties.
	 * 
	 * @author Stefan Steinheimer (nosefish)
	 *
	 */
	private class FishyTaskInputSorter extends FishyTaskSorter {
		public FishyTaskInputSorter() {
			super(new LinkedBlockingQueue<FishyTask>());
		}
		
		/**
		 * Sort task into appropriate execution queue
		 * @param task
		 */
		@Override
		protected void sort(FishyTask task) {
			final Set<FishyTaskProperties> taskProperties = task.getTaskProperties();
			if (taskProperties == null) {
				task.cancel();
				Log.get().warn("FishyTaskRunner canceled a task with null properties: " + task);
				return;
			}
			// get all delay types
			EnumSet<FishyTaskProperties> delays = EnumSet.copyOf(FishyTaskProperties.DELAYS);
			// in-place intersection with task properties
			delays.retainAll(taskProperties);
			if (delays.isEmpty()) {
				// no delay, run now
				if (taskProperties.contains(FishyTaskProperties.THREADSAFE)) {
					workerPool.execute(task);
				} else {
					runInServer(task);
				}
			} else {
				if (taskProperties.contains(FishyTaskProperties.TICKDELAYED)) {
					tickDelaySorter.submit(task);
				}
				if (taskProperties.contains(FishyTaskProperties.TIMEDELAYED)) {
					timeDelaySorter.submit(task);
				}
			}
		}
	} // end of internal class
	
	
	private class TimeDelaySorter extends FishyTaskSorter{

		public TimeDelaySorter() {
			super(new DelayQueue<FishyTask>());
		}

		@Override
		protected void sort(FishyTask task) {
			if (task.getTaskProperties().contains(FishyTaskProperties.THREADSAFE)) {
				workerPool.execute(task);
			} else {
				runInServer(task);
			}
		}
	} // end of internal class
	
	
	/**
	 * Executed by tick() in the server thread.
	 * @author StefanSteinheimer
	 *
	 */
	private class TickDelaySorter extends FishyTaskSorter{
		public TickDelaySorter() {
			super(new TickDelayQueue<FishyTask>());
		}

		@Override
		public void submit(FishyTask task) {
			super.submit(task);
			if (task instanceof ShutdownTask) {
				// the ShutdownTask is always submitted from the server thread
				cleanup();
			}
		}
		
		@Override
		public void run(){
			while (true) {
				try {
					FishyTask task = inQ.poll();
					if (task == null) {
						// no task is ready
						// nothing left to do on this tick
						break;
					}
					sort(task);
				} catch(RejectedExecutionException e) {
					// shutting down
					this.shutdown = true;
					break;
				}
			}
		}

		@Override
		protected void sort(FishyTask task) {
			if (task.getTaskProperties().contains(FishyTaskProperties.THREADSAFE)) {
				workerPool.execute(task);
			} else {
				task.run();
			}
		}
	} // end of internal class


	


}// end of class
