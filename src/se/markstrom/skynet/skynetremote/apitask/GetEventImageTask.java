package se.markstrom.skynet.skynetremote.apitask;

import java.util.Base64;

import se.markstrom.skynet.api.SkynetAPI;
import se.markstrom.skynet.api.SkynetAPIClientError;
import se.markstrom.skynet.api.SkynetAPI.SkynetAPIError;
import se.markstrom.skynet.skynetremote.GUI;

public class GetEventImageTask implements ApiTask {
	
	private long eventId;
	private int imageIndex;
	private boolean show;
	private boolean save;
	
	public GetEventImageTask(long eventId, int imageIndex, boolean show, boolean save) {
		this.eventId = eventId;
		this.imageIndex = imageIndex;
		this.show = show;
		this.save = save;
	}
	
	@Override
	public void run(ApiThread apiThread, SkynetAPI api, GUI gui) throws SkynetAPIClientError, SkynetAPIError {
		if (api != null) {
			String base64Image = api.getImageFromEvent(eventId, imageIndex, false);
			try {
				byte[] jpegData = Base64.getDecoder().decode(base64Image);
				gui.updateEventImage(eventId, imageIndex, jpegData, show, save);
			}
			catch (IllegalArgumentException e) {
				gui.showApiError("Received invalid Base64 image for event " + eventId + " image " + (imageIndex+1));
			}
		}
	}
}
