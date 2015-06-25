package se.markstrom.skynet.skynetremote;

/**
 * The methods in this interface should be called from the API thread.
 * The purpose of them is to trigger updates in the GUI when data has been
 * changed after some API call.
 */
public interface GUI {
	void updateConnectedState(boolean state);
	void updateArmState(boolean state);
	void showApiError(String message);
}
