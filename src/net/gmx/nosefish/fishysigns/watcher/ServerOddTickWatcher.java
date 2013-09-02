package net.gmx.nosefish.fishysigns.watcher;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import net.canarymod.Canary;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.system.ServerTickHook;
import net.canarymod.plugin.Priority;
import net.gmx.nosefish.fishysigns.activator.Activator;
import net.gmx.nosefish.fishysigns.activator.ActivatorServerTick;
import net.gmx.nosefish.fishysigns.plugin.FishySigns;
import net.gmx.nosefish.fishysigns.plugin.engine.ActivationManager;
import net.gmx.nosefish.fishysigns.task.FishyTask;

/**
 * Activates registered Activatables on every odd-numbered tick.
 * 
 * @author Stefan Steinheimer
 *
 */
public class ServerOddTickWatcher implements IFishyWatcher{
	private static ServerOddTickWatcher instance = new ServerOddTickWatcher();
	static {
		FishySigns.addWatcher(instance);
	}
	
	private boolean enabled = false;
	
	private Set<Long> registeredIds = new TreeSet<Long>();
	
	public static ServerOddTickWatcher getInstance() {
		return instance;
	}
	
	public void register(Long id) {
		synchronized(registeredIds) {
			registeredIds.add(id);
		}
	}
	
	public void remove(Long id) {
		synchronized (registeredIds) {
			registeredIds.remove(id);	
		}
	}
	
	@HookHandler(priority=Priority.PASSIVE)
	public void onTick(ServerTickHook hook) {
		if (! enabled) {
			return;
		}
		int tick = Canary.getServer().getCurrentTick();
		if (tick % 2 == 0) {
			return;
		}
		ActivationTask activate = new ActivationTask(tick);
		activate.submit();

	}
	
	/**
	 * Activates the registered IDs outside the server thread
	 * @author Stefan Steinheimer (nosefish)
	 *
	 */
	private class ActivationTask extends FishyTask {
		int tick;
		
		public ActivationTask(int tick) {
			this.tick = tick;
			this.setThreadsafe_IPromiseThatThisDoesNotTouchTheWorld();
		}
		
		@Override
		public void doStuff() {
			Activator activator = new ActivatorServerTick(tick);
			Map<Long, Activator> activationMap = new TreeMap<Long, Activator>();
			// let's get this over with quickly in order to not block registration
			// (which happens in the main thread) longer than necessary
			synchronized(registeredIds) {
				for (Long id : registeredIds) {
					activationMap.put(id, activator);
				}
			}
			ActivationManager.getInstance().activateAll(activationMap);
		}	
	}

	@Override
	public void enable() {
		enabled = true;
	}

	@Override
	public void disable() {
		enabled = false;
		registeredIds.clear();
	}
}
