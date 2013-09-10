package net.gmx.nosefish.fishysigns.task;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.Delayed;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import net.gmx.nosefish.fishysigns.exception.DisabledException;
import net.gmx.nosefish.fishysigns.plugin.engine.ServerTicker;


/**
 * A task to be run by the FishyTaskManager.
 * 
 * Overriding any of the non-abstract methods is probably a bad idea,
 * unless you want to use a different task manager. 
 * 
 * @author Stefan Steinheimer (nosefish)
 *
 */
public abstract class FishyTask implements Runnable, Delayed, TickDelayed {
	private final EnumSet<FishyTaskProperties> taskProperties;
	private volatile FishyTask nextTask = null;
	private volatile boolean cancelled = false;
	private volatile long timeDelayNanos = 0L;
	private volatile long repeatDelayNanos = 0L;
	private volatile long submitTimeNanos = 0L;
	private volatile long tickDelay = 0L;
	private volatile long repeatDelayTicks = 0L;
	private volatile long submitTick = 0L;
	

	/**
	 * Constructs a FishyTask that will immediately be executed in the
	 * server thread. Use the <code>set*</code> methods to adjust the
	 * behavior before calling <code>submit</code>.
	 * 
	 * @param runnable 
	 * 		the task to run as a FishyTask
	 */
	public FishyTask() {
		this.taskProperties = EnumSet.noneOf(FishyTaskProperties.class);
	}
	
	/**
	 * Called when the task runs.
	 * Put your code in here. 
	 */
	public abstract void doStuff();
	
	/**
	 * Sets a task that will be submitted for execution when this task has completed.
	 * 
	 * You can use this to build a chain of tasks that have to be run sequentially, but not
	 * in the same thread. If this task repeats, the next task will be submitted after each run.
	 * 
	 * @param task
	 */
	public synchronized void setNextTask(FishyTask task) {
		this.nextTask = task;
	}
	
	/**
	 * Sets the task to run in a worker thread outside the main server thread.
	 */
	public synchronized void setThreadsafe_IPromiseThatThisDoesNotTouchTheWorld() {
		this.taskProperties.add(FishyTaskProperties.THREADSAFE);
	}
	
	/**
	 * Set the task to run after the specified delay.
	 * 
	 * Set to a value <= 0 to remove the delay.
	 * 
	 * <code>setTimeDelay</code> and <code>setTickDelay</code> are independent.
	 * If you set both, the task will run twice and repeating tasks may behave erratically!
	 * 
	 * @param time
	 * @param unit
	 */
	public synchronized void setTimeDelay(long time, TimeUnit unit) {
		if (time > 0L) {
			this.timeDelayNanos = TimeUnit.NANOSECONDS.convert(time, unit);
			this.taskProperties.add(FishyTaskProperties.TIMEDELAYED);
		} else {
			this.timeDelayNanos = 0L;
			this.taskProperties.remove(FishyTaskProperties.TIMEDELAYED);
		}
	}

	/**
	 * Set the task to run after the specified delay.
	 * 
	 * If this is not set, or set to a negative value,
	 * the task will be executed asynchronously. 
	 * Set to 0 to run the task on the next tick.
	 * 
	 * <code>setTimeDelay</code> and <code>setTickDelay</code> are independent.
	 * If you set both, the task will run twice, and repeating tasks may behave erratically!
	 * 
	 * @param ticks 
	 * 		number of ticks to wait before executing the task.
	 */
	public synchronized void setTickDelay(long ticks) {
		if (ticks >= 0) {
			this.tickDelay = ticks;
			this.taskProperties.add(FishyTaskProperties.TICKDELAYED);
		} else {
			this.tickDelay = 0L;
			this.taskProperties.remove(FishyTaskProperties.TICKDELAYED);
		}
	}

	public synchronized void setTimeRepeatDelay(long time, TimeUnit unit) {
		if (time >= 0) {
			this.repeatDelayNanos = TimeUnit.NANOSECONDS.convert(time, unit);
			this.taskProperties.add(FishyTaskProperties.TIMEREPEAT);
		} else {
			this.repeatDelayNanos = 0L;
			this.taskProperties.remove(FishyTaskProperties.TIMEREPEAT);
		}
	}

	public synchronized void setTickRepeatDelay(long ticks) {
		if (ticks >= 0) {
			this.repeatDelayTicks = ticks;
			this.taskProperties.add(FishyTaskProperties.TICKREPEAT);
		} else {
			this.repeatDelayTicks = 0L;
			this.taskProperties.remove(FishyTaskProperties.TICKREPEAT);
		}
	}

	public synchronized void stopRepeating() {
		this.repeatDelayTicks = 0L;
		this.taskProperties.remove(FishyTaskProperties.TICKREPEAT);
		this.repeatDelayNanos = 0L;
		this.taskProperties.remove(FishyTaskProperties.TIMEREPEAT);
	}

	/**
	 * Submit this task to the <code>TaskManager</code>.
	 * @return 
	 * 		<code>true</code> if the task was successfully enqueued,
	 * 		<code>false</code> otherwise.
	 * 		A return value of <code>false</code> usually indicates that the plugin has been disabled."
	 */
	public synchronized boolean submit() {
		try {
			if (this.isCancelled()) {
				return false;
			}
			submitTimeNanos = System.nanoTime();
			submitTick = ServerTicker.getInstance().getTickCount();
			FishyTaskManager.submit(this);
		} catch(RejectedExecutionException e) {
			this.cancel();
			return false;
		} catch (DisabledException e){
			this.cancel();
			return false;
		}
		return true;
	}
	
	@Override
	public synchronized void run() {
		try {
			long startTime = System.nanoTime();
			long startTick = ServerTicker.getInstance().getTickCount();
			// run it
			if (! cancelled) {
				this.doStuff();
			}
			if (! cancelled && nextTask != null) {
				nextTask.submit();
			}
			// resubmit repeating tasks
			if (taskProperties.contains(FishyTaskProperties.TIMEREPEAT)
					|| taskProperties.contains(FishyTaskProperties.TICKREPEAT)) {
				// time
				if (taskProperties.contains(FishyTaskProperties.TIMEREPEAT)) {
					long runTime = startTime - System.nanoTime();
					this.setTimeDelay(repeatDelayNanos - runTime, TimeUnit.NANOSECONDS);
				}
				// tick
				if (taskProperties.contains(FishyTaskProperties.TICKREPEAT)) {
					long runTicks = startTick - ServerTicker.getInstance().getTickCount();
					this.setTickDelay(repeatDelayTicks - runTicks);
				}
				if (! cancelled) this.submit();
			}
		} catch(RejectedExecutionException e) {
			this.cancel();
		} catch (DisabledException e){
			this.cancel();
		}
	}

	/**
	 * Cancels this task and all subsequent tasks in the chain.
	 * If a task is already running, the execution will finish
	 * before this takes effect.
	 */
	public void cancel() {
		this.cancelled = true;
		if (nextTask != null) {
			nextTask.cancel();
		}
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	/**
	 * Gets the task's properties; used internally.
	 *
	 * @return the task's properties
	 */
	public synchronized Set<FishyTaskProperties> getTaskProperties() {
		return Collections.unmodifiableSet(taskProperties);
	}

	/**
	 * Gets the remaining delay time.
	 */
	@Override
	public synchronized long getDelay(TimeUnit timeUnit) {
		long remaining = (submitTimeNanos + timeDelayNanos) - System.nanoTime();
		return timeUnit.convert(remaining, TimeUnit.NANOSECONDS);
	}

	/**
	 * Gets the remaining number of delay ticks.
	 */
	@Override
	public synchronized long getTickDelay() {
		long remaining;
		try {
			remaining = (submitTick + tickDelay) - ServerTicker.getInstance().getTickCount();
		} catch(DisabledException e) {
			this.cancel();
			remaining = -1;
		}
		return remaining;
	}

	/**
	 * Compares results of <code>getDelay</code>, as required by the <code>Delayed</code> interface
	 */
	@Override
	public int compareTo(Delayed other) {
		long myDelay = this.getDelay(TimeUnit.NANOSECONDS);
		long otherDelay = other.getDelay(TimeUnit.NANOSECONDS);
		if (myDelay < otherDelay) return -1;
		if (myDelay > otherDelay) return 1;
		return 0;
	}
}
