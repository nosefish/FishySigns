package net.gmx.nosefish.fishysigns.plugin.engine;

import java.util.List;

import net.gmx.nosefish.fishysigns.signs.FishySign;
import net.gmx.nosefish.fishysigns.task.FishyTask;


/**
 * Finding the correct class for a sign and loading it involves
 * a lot of reflection and pattern matching,
 * so we do that outside the server thread.
 * 
 * @author Stefan Steinheimer
 *
 */
public class FishySignLoaderTask extends FishyTask {
	private List<UnloadedSign> signsToLoad;
	
	public FishySignLoaderTask(List<UnloadedSign> signsToLoad) {
		super();
		this.signsToLoad = signsToLoad;
		this.setThreadsafe_IPromiseThatThisDoesNotTouchTheWorld();
	}

	@Override
	public void doStuff() {
		for (UnloadedSign sign : signsToLoad) {
			FishySign.loadAndRegister(sign);
		}
	}

}
