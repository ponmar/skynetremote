package se.markstrom.skynet.skynetremote;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import se.markstrom.skynet.api.SkynetAPI;

public class ConnectWindow {
	
	private Display display;
	private Shell shell;
	
	private Text hostText;
	private Text portText;
	private Text passwordText;
	
	private String host;
	private int port;
	private String password;
	
	public ConnectWindow(String defaultHost, int defaultPort) {
		createGui(defaultHost, defaultPort);
	}
	
	private void createGui(String defaultHost, int defaultPort) {
		display = Display.getDefault();
		shell = new Shell(display);
		shell.setText("Connect");
		shell.setLayout(new GridLayout(2, true));

		Label hostLabel = new Label(shell, SWT.NONE);
		hostLabel.setText("Host:");
		
		hostText = new Text(shell, SWT.BORDER);
		hostText.setText(defaultHost);
		
		Label portLabel = new Label(shell, SWT.NONE);
		portLabel.setText("Port:");
		
		portText = new Text(shell, SWT.BORDER);
		portText.setText(new Integer(defaultPort).toString());

		Label passwordLabel = new Label(shell, SWT.NONE);
		passwordLabel.setText("Password:");
		
		passwordText = new Text(shell, SWT.BORDER);
		passwordText.setEchoChar('*');
		passwordText.setText("");
		if (!defaultHost.equals("")) {
			passwordText.setFocus();
		}
		
		// TODO: protocol radio buttons (or a checkbox)

		Button cancelButton = new Button(shell, SWT.PUSH);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
		    @Override
		    public void widgetSelected(SelectionEvent e) {
		    	shell.dispose();
		    }
		}); 

		Button connectButton = new Button(shell, SWT.PUSH);
		connectButton.setText("Connect");
		connectButton.addSelectionListener(new SelectionAdapter() {
		    @Override
		    public void widgetSelected(SelectionEvent e) {
		    	connect();
		    }
		});
		
		shell.addTraverseListener(new EnterListener());
		
		shell.pack();
		shell.open();
	}
	
	public void run() {
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	private void connect() {
    	if (saveTextInput() && hasValidInput()) {
    		// Only close window if the input is valid
    		shell.dispose();
    	}
	}
	
	private boolean saveTextInput() {
		host = hostText.getText();
		try {
			port = Integer.parseInt(portText.getText());
		}
		catch (NumberFormatException e) {
			return false;
		}
		password = passwordText.getText();
		return true;
	}
	
	public boolean hasValidInput() {
		return host != null && !host.equals("") &&
				port > 0 && port <= 65535 &&
				password != null && !password.equals("");
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public SkynetAPI.Protocol getProtocol() {
		// TODO:
		return SkynetAPI.Protocol.SSH;
	}
	
	public String getPassword() {
		return password;
	}
	
	private class EnterListener implements TraverseListener {
		@Override
		public void keyTraversed(TraverseEvent event) {
			if (event.detail == SWT.TRAVERSE_RETURN) {
				connect();
			}
		}
	}
}
