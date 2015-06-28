package se.markstrom.skynet.skynetremote.apitask;

import java.util.Base64;

import se.markstrom.skynet.api.SkynetAPI;
import se.markstrom.skynet.api.SkynetAPIClientError;
import se.markstrom.skynet.api.SkynetAPI.SkynetAPIError;
import se.markstrom.skynet.skynetremote.GUI;

public class GetEventImageTask implements ApiTask {
	
	private long eventId;
	private int imageIndex;
	
	public GetEventImageTask(long eventId, int imageIndex) {
		this.eventId = eventId;
		this.imageIndex = imageIndex;
	}
	
	@Override
	public void run(ApiThread apiThread, SkynetAPI api, GUI gui) throws SkynetAPIClientError, SkynetAPIError {
		if (api != null) {
			String base64Image = api.getImageFromEvent(eventId, imageIndex, false);

			// TODO: Some timing issue. Sometimes the prompt is included in the XML.
			String PROMPT = ">> ";
			if (base64Image.endsWith(PROMPT)) {
				base64Image = base64Image.substring(0, base64Image.length() - PROMPT.length());
			}
			
			try {
				byte[] jpegData = Base64.getDecoder().decode(base64Image);
				gui.updateEventImage(eventId, imageIndex, jpegData);
				// TODO: write image to file if cache is enabled?
			}
			catch (IllegalArgumentException e) {
				gui.showApiError("Received invalid Base64 image for event " + eventId + " image " + (imageIndex+1));
			}
		}
	}
}
