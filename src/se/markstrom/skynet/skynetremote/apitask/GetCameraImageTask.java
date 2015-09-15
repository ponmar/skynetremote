package se.markstrom.skynet.skynetremote.apitask;

import java.util.Base64;

import se.markstrom.skynet.api.SkynetAPI;
import se.markstrom.skynet.api.SkynetAPIClientError;
import se.markstrom.skynet.api.SkynetAPI.SkynetAPIError;
import se.markstrom.skynet.skynetremote.GUI;

public class GetCameraImageTask implements ApiTask {

	public enum ImageType { SNAPSHOT, STREAM };
	
	private int cameraIndex;
	private ImageType imageType;
	
	public GetCameraImageTask(int cameraIndex, ImageType imageType) {
		this.cameraIndex = cameraIndex;
		this.imageType = imageType;
	}
	
	@Override
	public void run(ApiThread apiThread, SkynetAPI api, GUI gui) throws SkynetAPIClientError, SkynetAPIError {
		if (api != null) {
			String base64Image = api.getImageFromCamera(cameraIndex, false);
			try {
				byte[] jpegData = Base64.getDecoder().decode(base64Image);
				switch (imageType) {
				case SNAPSHOT:
					gui.updateCameraSnapshot(cameraIndex, jpegData);
					break;
				case STREAM:
					gui.updateCameraStream(cameraIndex, jpegData);
					break;
				}
			}
			catch (IllegalArgumentException e) {
				gui.showApiError("Received invalid Base64 image for camera " + (cameraIndex + 1));
			}
		}
	}
}
