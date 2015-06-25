package se.markstrom.skynet.skynetremote.apitask;

import se.markstrom.skynet.api.SkynetAPI.SkynetAPIError;
import se.markstrom.skynet.api.SkynetAPIClientError;
import se.markstrom.skynet.skynetremote.GUI;

public class DisconnectTask implements ApiTask {
	
	@Override
	public void run(ApiThread apiThread, GUI gui) throws SkynetAPIClientError, SkynetAPIError {
		apiThread.disconnect();
		gui.updateConnectedState(false);
	}
}
