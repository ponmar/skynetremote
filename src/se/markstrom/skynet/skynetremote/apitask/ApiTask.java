package se.markstrom.skynet.skynetremote.apitask;

import se.markstrom.skynet.api.SkynetAPI;
import se.markstrom.skynet.api.SkynetAPIClientError;
import se.markstrom.skynet.api.SkynetAPI.SkynetAPIError;
import se.markstrom.skynet.skynetremote.GUI;

public interface ApiTask {
	/**
	 * Implement this method to execute SkynetAPI tasks in the API thread.
	 * 
	 * @param apiThread
	 * @param api                   Is null when API is not connected
	 * @param gui
	 * @throws SkynetAPIClientError
	 * @throws SkynetAPIError
	 */
	void run(ApiThread apiThread, SkynetAPI api, GUI gui) throws SkynetAPIClientError, SkynetAPIError;
}
