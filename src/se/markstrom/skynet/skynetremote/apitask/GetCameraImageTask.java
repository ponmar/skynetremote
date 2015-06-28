package se.markstrom.skynet.skynetremote.apitask;

import java.util.Base64;

import se.markstrom.skynet.api.SkynetAPI;
import se.markstrom.skynet.api.SkynetAPIClientError;
import se.markstrom.skynet.api.SkynetAPI.SkynetAPIError;
import se.markstrom.skynet.skynetremote.GUI;

public class GetCameraImageTask implements ApiTask {
	
	private int cameraIndex;
	
	public GetCameraImageTask(int cameraIndex) {
		this.cameraIndex = cameraIndex;
	}
	
	@Override
	public void run(ApiThread apiThread, SkynetAPI api, GUI gui) throws SkynetAPIClientError, SkynetAPIError {
		if (api != null) {
			String base64Image = api.getImageFromCamera(cameraIndex, false);

			// TODO: Some timing issue. Sometimes the prompt is included in the XML.
			String PROMPT = ">> ";
			if (base64Image.endsWith(PROMPT)) {
				base64Image = base64Image.substring(0, base64Image.length() - PROMPT.length());
			}
			
			try {
				byte[] jpegData = Base64.getDecoder().decode(base64Image);
				gui.updateCameraImage(cameraIndex, jpegData);
			}
			catch (IllegalArgumentException e) {
				gui.showApiError("Received invalid Base64 image for camera " + (cameraIndex + 1));
			}
		}
	}
}
