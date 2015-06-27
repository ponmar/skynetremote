package se.markstrom.skynet.skynetremote;

/**
 * The methods in this interface should be called from the API thread.
 * The purpose of them is to trigger updates in the GUI when data has been
 * changed after API calls.
 */
public interface GUI {
	void updateConnectedState(boolean state);

	void updateEventsXml(String xml);
	void updateLogXml(String xml);
	void updateSummaryXml(String xml);
	
	void updateEventImage(long eventId, int imageIndex, String base64Image);
	
	void showApiError(String message);
}
