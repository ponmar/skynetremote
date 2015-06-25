package se.markstrom.skynet.skynetremote.apitask;

import java.util.concurrent.ConcurrentLinkedQueue;

import se.markstrom.skynet.api.SkynetAPI;
import se.markstrom.skynet.api.SkynetAPI.Protocol;
import se.markstrom.skynet.api.SkynetAPI.SkynetAPIError;
import se.markstrom.skynet.api.SkynetAPIClientError;
import se.markstrom.skynet.skynetremote.GUI;

public class ApiThread extends Thread {
	
	private boolean run = true;
	private SkynetAPI api = null;
	private ConcurrentLinkedQueue<ApiTask> queue = new ConcurrentLinkedQueue<ApiTask>();
	private GUI gui;
	
	public ApiThread(GUI gui) {
		this.gui = gui;
	}
	
	public void run() {
		System.out.println("Starting Skynet API thread");
		
		try {
			while (run) {
				ApiTask task = queue.poll();
				if (task != null) {
					task.run(this, gui);
				}
				else {
					try {
						sleep(100);
					}
					catch (InterruptedException e) {
					}
				}
			}
		}
		catch (SkynetAPIClientError e) {
			e.printStackTrace();
		}
		catch (SkynetAPIError e) {
			e.printStackTrace();
		}
        api = null;
        gui.updateConnectedState(false);
    }
	
	/**
	 * Call this method from the GUI thread to close the API connection
	 * and wait for the API thread to stop.
	 */
	public void close() {
		run = false;
		try {
			join();
		}
		catch (InterruptedException e) {
		}
	}
	
	/**
	 * Call this method from the GUI thread to check if the API is connected. 
	 */
	public boolean isConnected() {
		return api != null;
	}
	
	public void runTask(ApiTask task) {
		queue.add(task);
	}

	void connect(String host, int port, Protocol protocol, String password, boolean debug) throws SkynetAPIError {
		if (api == null) {
			System.out.println("Connecting to host: " + host);
			api = new SkynetAPI(host, port, protocol, password, debug);
		}
	}
}
