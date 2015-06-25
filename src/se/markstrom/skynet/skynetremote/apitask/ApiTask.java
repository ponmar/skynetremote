package se.markstrom.skynet.skynetremote.apitask;

import se.markstrom.skynet.api.SkynetAPIClientError;
import se.markstrom.skynet.api.SkynetAPI.SkynetAPIError;
import se.markstrom.skynet.skynetremote.GUI;

public interface ApiTask {
	/**
	 * This method is executed in the API thread.
	 * 
	 * @param apiThread
	 * @param gui
	 * @throws SkynetAPIClientError
	 * @throws SkynetAPIError
	 */
	void run(ApiThread apiThread, GUI gui) throws SkynetAPIClientError, SkynetAPIError;
}
