package se.markstrom.skynet.skynetremote;

/**
 * The methods in this interface should be called from the API thread.
 * The purpose of them is to trigger updates in the GUI when data has been
 * changed after API calls.
 */
public interface GUI {
	
	public enum CONNECTED_STATE {
		DISCONNECTING,
		DISCONNECTED,
		CONNECTING,
		CONNECTED
	}
	
	void updateConnectedState(CONNECTED_STATE state);

	void updateCamerasXml(String xml);
	void updateControlXml(String xml);
	void updateEventsXml(String xml);
	void updateLogXml(String xml);
	void updateSummaryXml(String xml);
	void updateSensorsXml(String xml);
	
	void updateEventImage(long eventId, int imageIndex, byte[] jpegData, boolean show, boolean save);
	void updateCameraImage(int cameraIndex, byte[] jpegData);
	
	void updateApiWorkingState(boolean isWorking);
	
	void showApiError(String message);
}
