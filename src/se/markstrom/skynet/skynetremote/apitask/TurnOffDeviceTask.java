package se.markstrom.skynet.skynetremote.apitask;

import se.markstrom.skynet.api.SkynetAPI;
import se.markstrom.skynet.api.SkynetAPI.SkynetAPIError;
import se.markstrom.skynet.api.SkynetAPIClientError;
import se.markstrom.skynet.skynetremote.GUI;

public class TurnOffDeviceTask implements ApiTask {

	private int deviceId;	
	
	public TurnOffDeviceTask(int deviceId) {
		this.deviceId = deviceId;
	}
	
	@Override
	public void run(ApiThread apiThread, SkynetAPI api, GUI gui) throws SkynetAPIClientError, SkynetAPIError {
		if (api != null) {
			api.turnOff(deviceId);
		}
	}
}
