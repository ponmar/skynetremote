package se.markstrom.skynet.skynetremote.window;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import se.markstrom.skynet.api.SkynetAPI;
import se.markstrom.skynet.api.SkynetAPI.Protocol;

public class ConnectWindow {
	
	private Display display;
	private Shell shell;
	
	private Text hostText;
	private Text portText;
	private Button sshButton;
	private Button telnetButton;
	private Text passwordText;
	private Button savePasswordButton;
	private Button hashKnownHostsButton;
	
	private String host;
	private int port;
	private Protocol protocol;
	private String password;
	private boolean savePassword;
	
	private boolean hashKnownHosts = false;
	
	static String savedPassword;
	
	public ConnectWindow(String defaultHost, int defaultPort, Protocol defaultProtocol, Shell parentShell) {
		createGui(defaultHost, defaultPort, defaultProtocol, parentShell);
	}
	
	private void createGui(String defaultHost, int defaultPort, Protocol defaultProtocol, Shell parentShell) {
		display = Display.getDefault();
		shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText("Connect");
		GridLayout layout = new GridLayout(2, false);
		layout.marginTop = 10;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		layout.marginBottom = 5;
		shell.setLayout(layout);

		Label hostLabel = new Label(shell, SWT.NONE);
		hostLabel.setText("Host:");
		
		hostText = new Text(shell, SWT.BORDER);
		hostText.setText(defaultHost);
		hostText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label portLabel = new Label(shell, SWT.NONE);
		portLabel.setText("Port:");
		
		portText = new Text(shell, SWT.BORDER);
		portText.setText(new Integer(defaultPort).toString());
		portText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	    Group protocolGroup = new Group(shell, SWT.SHADOW_IN);
	    protocolGroup.setText("API protocol");
	    protocolGroup.setLayout(new RowLayout(SWT.VERTICAL));
	    GridData protocolGroupLayout = new GridData(GridData.FILL_HORIZONTAL);
	    protocolGroupLayout.horizontalSpan = 2;
	    protocolGroup.setLayoutData(protocolGroupLayout);
	    
	    sshButton = new Button(protocolGroup, SWT.RADIO);
	    sshButton.setText("Secure Shell (SSH)");
	    sshButton.setSelection(defaultProtocol == Protocol.SSH);
	    telnetButton = new Button(protocolGroup, SWT.RADIO);
	    telnetButton.setText("Telnet");
	    telnetButton.setSelection(defaultProtocol == Protocol.TELNET);
	    
		Label passwordLabel = new Label(shell, SWT.NONE);
		passwordLabel.setText("Password:");
		
		passwordText = new Text(shell, SWT.BORDER);
		passwordText.setEchoChar('*');
		if (savedPassword != null) {
			passwordText.setText(savedPassword);
		}
		else {
			passwordText.setText("");			
		}
		passwordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (!defaultHost.equals("")) {
			passwordText.setFocus();
		}

		savePasswordButton = new Button(shell, SWT.CHECK);
		savePasswordButton.setText("Save password in memory until exit");
		savePasswordButton.setSelection(savedPassword != null);
		GridData savePasswordLayout = new GridData(GridData.FILL_HORIZONTAL);
	    savePasswordLayout.horizontalSpan = 2;
	    savePasswordButton.setLayoutData(savePasswordLayout);

		hashKnownHostsButton = new Button(shell, SWT.CHECK);
		hashKnownHostsButton.setText("Save SSH host key");
		hashKnownHostsButton.setSelection(false);
		GridData hashKnownHostsLayout = new GridData(GridData.FILL_HORIZONTAL);
		hashKnownHostsLayout.horizontalSpan = 2;
	    hashKnownHostsButton.setLayoutData(hashKnownHostsLayout);

		// Skip a column
		new Label(shell, SWT.NONE);

		Button connectButton = new Button(shell, SWT.PUSH);
		connectButton.setText("Connect");
		GridData connectButtonLayout = new GridData(GridData.FILL_HORIZONTAL);
	    connectButton.setLayoutData(connectButtonLayout);
		connectButton.addSelectionListener(new SelectionAdapter() {
		    @Override
		    public void widgetSelected(SelectionEvent e) {
		    	connect();
		    }
		});
		
		shell.addTraverseListener(new KeyListener());
		
		shell.pack();
		
		// Center window
		Point parentLocation = parentShell.getLocation();
		Point parentSize = parentShell.getSize();
		Point dialogSize = shell.getSize();
		shell.setLocation(
				parentLocation.x + (parentSize.x - dialogSize.x) / 2,
				parentLocation.y + (parentSize.y - dialogSize.y) / 2);
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
		
		if (sshButton.getSelection()) {
			protocol = Protocol.SSH;
		}
		else if (telnetButton.getSelection()) {
			protocol = Protocol.TELNET;
		}
		else {
			return false;
		}
		
		password = passwordText.getText();
		savePassword = savePasswordButton.getSelection();
		if (savePassword) {
			savedPassword = password;
		}
		else {
			savedPassword = null;
		}
		
		hashKnownHosts = hashKnownHostsButton.getSelection();
		
		return true;
	}
	
	public boolean hasValidInput() {
		// Note: password is only required when using SSH
		return host != null && !host.equals("") &&
				port > 0 && port <= 65535 &&
				((protocol == Protocol.SSH && (password != null && !password.equals(""))) ||
				protocol == Protocol.TELNET);
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public SkynetAPI.Protocol getProtocol() {
		return protocol;
	}
	
	public String getPassword() {
		return password;
	}
	
	public boolean savePassword() {
		return savePassword;
	}
	
	public boolean getHashKnownHosts() {
		return hashKnownHosts;
	}

	public boolean getStrictHostKeyChecking() {
		// Strict key checking can not be used when the SSH host key should be added
		return !hashKnownHosts;
	}
	
	private class KeyListener implements TraverseListener {
		@Override
		public void keyTraversed(TraverseEvent event) {
			switch (event.detail) {
			case SWT.TRAVERSE_RETURN:
				connect();
				break;
			case SWT.TRAVERSE_ESCAPE:
				shell.dispose();
				break;
			}
		}
	}
}
