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
	private boolean workingState = false;
	
	public ApiThread(GUI gui) {
		this.gui = gui;
	}
	
	public void run() {
		try {
			while (run) {
				ApiTask task = queue.poll();
				if (task != null) {
					isWorking(true);
					System.out.println("Pre task run");
					task.run(this, api, gui);
					System.out.println("Post task run");
				}
				else {
					// Do not sleep when there are queued tasks
					try {
						isWorking(false);
						sleep(250);
					}
					catch (InterruptedException e) {
					}
				}
			}
		}
		catch (SkynetAPIClientError e) {
			gui.showApiError(e.getMessage());
		}
		catch (SkynetAPIError e) {
			gui.showApiError(e.getMessage());
		}
		
		isWorking(false);
		gui.updateConnectedState(GUI.ConnectedState.DISCONNECTED);
		
		disconnect();
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
		run = false;
		try {
			join();
		}
		catch (InterruptedException e) {
		}
	}
	
	public void runTask(ApiTask task) {
		queue.add(task);
	}

	void connect(String host, int port, Protocol protocol, String password, boolean debug) throws SkynetAPIError {
		if (api == null) {
			System.out.println("Connecting to host " + host + "...");
			api = new SkynetAPI(host, port, protocol, password, debug);
			System.out.println("Connected");
		}
	}
	
	void disconnect() {
		if (api != null) {
			api.close();
			api = null;
			System.out.println("Disconnected");
		}
	}
	
	/**
	 * A thread safe method for checking number of queued API tasks.
	 * @return
	 */
	public int getNumQueuedTasks() {
		return queue.size();
	}
}
