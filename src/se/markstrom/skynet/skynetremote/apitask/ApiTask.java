package se.markstrom.skynet.skynetremote.apitask;

import se.markstrom.skynet.api.SkynetAPIClientError;
import se.markstrom.skynet.api.SkynetAPI.SkynetAPIError;
import se.markstrom.skynet.skynetremote.GUI;

public interface ApiTask {
	void run(ApiThread api, GUI gui) throws SkynetAPIClientError, SkynetAPIError;
}
