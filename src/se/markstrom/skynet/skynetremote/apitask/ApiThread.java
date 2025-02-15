package se.markstrom.skynet.skynetremote.apitask;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.markstrom.skynet.api.SkynetAPI;
import se.markstrom.skynet.api.SkynetAPI.Protocol;
import se.markstrom.skynet.api.SkynetAPI.SkynetAPIError;
import se.markstrom.skynet.api.SkynetAPIClientError;
import se.markstrom.skynet.skynetremote.GUI;

public class ApiThread extends Thread {

	private static final Logger log = Logger.getLogger(ApiThread.class.getName());
	static {
		log.setLevel(Level.ALL);
	}

	private boolean run = true;
	private SkynetAPI api = null;
	private ConcurrentLinkedQueue<ApiTask> queue = new ConcurrentLinkedQueue<ApiTask>();
	private GUI gui;
	private boolean workingState = false;

	public ApiThread(GUI gui) {
		this.gui = gui;
	}

	public void run() {
		log.fine("API thread started");
		
		while (run) {
			ApiTask task = queue.poll();
			if (task != null) {
				isWorking(true);

				try {
					log.fine("Pre task run");
					task.run(this, api, gui);
					log.fine("Post task run");
				}
				catch (SkynetAPIClientError | SkynetAPIError e) {
					handleApiException(e);
				}

				isWorking(false);

				synchronized (this) {
					if (run && queue.isEmpty()) {
						try {
							wait();
						}
						catch (InterruptedException e) {
						}
					}
				}
			}
		}
		
		disconnect();
		log.fine("Stopping API thread");
	}
	
	private void handleApiException(Exception e) {
		disconnect();
		gui.updateConnectedState(GUI.ConnectedState.DISCONNECTED);
		String message = e.getMessage();
		Throwable cause = e.getCause();
		if (cause != null) {
			message += ": " + cause.getMessage();
		}
		gui.showApiError(message);
		log.fine(message);
	}

	private void isWorking(boolean state) {
		if (state != workingState) {
			workingState = state;
			gui.updateApiWorkingState(workingState);
		}
	}

	/**
	 * Call this method from the GUI thread to close the API connection
	 * and wait for the API thread to stop.
	 */
	public void close() {
		synchronized (this) {
			run = false;
			notify();
		}

		try {
			join();
		}
		catch (InterruptedException e) {
		}
	}

	public void runTask(ApiTask task) {
		synchronized (this) {
			queue.add(task);
			notify();
		}
	}

	void connect(String host, int port, Protocol protocol, String password, boolean hashKnownHosts, boolean strictHostKeyChecking, boolean debug) throws SkynetAPIError {
		if (api == null) {
			log.info("Connecting to host " + host + "...");
			api = new SkynetAPI(host, port, protocol, password, hashKnownHosts, strictHostKeyChecking, debug);
			log.info("Connected");
		}
	}

	void disconnect() {
		if (api != null) {
			api.close();
			api = null;
			log.info("Disconnected");
		}
	}
}
