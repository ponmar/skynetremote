package se.markstrom.skynet.skynetremote.apitask;

import se.markstrom.skynet.api.SkynetAPI;
import se.markstrom.skynet.api.SkynetAPIClientError;
import se.markstrom.skynet.api.SkynetAPI.SkynetAPIError;
import se.markstrom.skynet.skynetremote.GUI;

public class AcceptEventsTask implements ApiTask {
	
	public enum EventGroup { ALL_MINOR, ALL_MAJOR, ALL };
	
	private EventGroup eventGroup;
	
	public AcceptEventsTask(EventGroup eventGroup) {
		this.eventGroup = eventGroup;
	}

	@Override
	public void run(ApiThread apiThread, SkynetAPI api, GUI gui) throws SkynetAPIClientError, SkynetAPIError {
		if (api != null) {
			switch (eventGroup) {
			case ALL:
				api.acceptEvent(-1);
				break;
			case ALL_MINOR:
				api.acceptEvent(-2);
				break;
			case ALL_MAJOR:
				api.acceptEvent(-3);
				break;
			}
		}
	}
}
