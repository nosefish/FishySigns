package net.gmx.nosefish.fishysigns.exception;

/**
 * Thrown when an object is no longer available because the FishyLib plugin
 * has been disabled.
 * 
 * @author Stefan Steinheimer (nosefish)
 *
 */
public class DisabledException extends Exception {
	String message = "";
	private static final long serialVersionUID = -7720891600303727418L;
	
	public DisabledException() {}
	
	public DisabledException(String message) {
		this.message = message;
	}
}
