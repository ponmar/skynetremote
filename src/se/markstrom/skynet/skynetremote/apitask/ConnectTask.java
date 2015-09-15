package se.markstrom.skynet.skynetremote.apitask;

import se.markstrom.skynet.api.SkynetAPI.SkynetAPIError;
import se.markstrom.skynet.api.SkynetAPI;
import se.markstrom.skynet.api.SkynetAPIClientError;
import se.markstrom.skynet.api.SkynetAPI.Protocol;
import se.markstrom.skynet.skynetremote.GUI;

public class ConnectTask implements ApiTask {
	
	private String host;
	private int port;
	private Protocol protocol;
	private String password;
	private boolean debug;
	
	public ConnectTask(String host, int port, Protocol protocol, String password, boolean debug) {
		this.host = host;
		this.port = port;
		this.protocol = protocol;
		this.password = password;
		this.debug = debug;
	}

	@Override
	public void run(ApiThread apiThread, SkynetAPI api, GUI gui) throws SkynetAPIClientError, SkynetAPIError {
		gui.updateConnectedState(GUI.ConnectedState.CONNECTING);
		apiThread.connect(host, port, protocol, password, debug);
		gui.updateConnectedState(GUI.ConnectedState.CONNECTED);
	}
}
