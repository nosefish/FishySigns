package net.gmx.nosefish.fishysigns.activator;

import net.gmx.nosefish.fishylib.worldmath.FishyLocation;



public interface Activatable {
	/**
	 *  
	 * Gets the unique ID of this Activatable that identifies it in the
	 * ActivationManager.
	 * The ID must not changed once assigned.
	 * 
	 * @return the unique ID issued by the ActivationManager upon registration, or -1L when unregistered
	 */
	public long getID();
	
	/**
	 * Sets the unique id of this Activatable. Called by the ActivationManager after registration.
	 *  Store the ID passed to this method in a thread-safe way.
	 * (e.g. as <code>AtomicLong</code>) .
	 * The ID must not changed once assigned.
	 * 
	 * @param id the unique ID issued by the ActivationManager upon registration
	 */
	public void setID(long id);
	
	
	/**
	 * Gets the location.
	 * 
	 * <code>Activatable</code>s are usually blocks.
	 * <code>Activatables</code> without a location
	 * are not supported at this time. File a bug
	 * if you have a use for them.
	 * 
	 * @return the location of the <code>Activatable</code>.
	 */
	public FishyLocation getLocation();
	
	/**
	 * Called when the <code>Activatable</code> has been registered. All initialization code goes here,
	 * e.g. registering blocks with the BlockChangeWatcher.
	 */
	public void initialize();
	
	/**
	 * Called when the <code>Activatable</code> is activated.
	 * Do not make any assumptions about the thread that calls this.
	 * Usually, it will <i>not</i> be the server thread.
	 * 
	 * @param activator holds the details of the activation.
	 */
	public void activate(Activator activator);
	
	/**
	 * Called when the <code>Activatable</code> has been removed from the
	 * <code>ActivationManager<code>. It will never be re-added, so it's
	 * time to make your will, say goodbye to your loved ones, and
	 * clean up after yourself. Unregister with watchers, cancel your
	 * <code>FishyTasks</code>, make sure that no strong references
	 * remain. 
	 */
	public void remove();
	

}
