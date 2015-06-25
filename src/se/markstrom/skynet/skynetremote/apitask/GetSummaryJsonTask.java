package se.markstrom.skynet.skynetremote.apitask;

import se.markstrom.skynet.api.SkynetAPI;
import se.markstrom.skynet.api.SkynetAPIClientError;
import se.markstrom.skynet.api.SkynetAPI.SkynetAPIError;
import se.markstrom.skynet.skynetremote.GUI;

public class GetSummaryJsonTask implements ApiTask {
	
	@Override
	public void run(ApiThread apiThread, GUI gui) throws SkynetAPIClientError, SkynetAPIError {
		SkynetAPI api = apiThread.getApi();
		if (api != null) {
			String json = api.getSummaryJson();
			gui.updateSummaryJson(json);
		}
	}
}
