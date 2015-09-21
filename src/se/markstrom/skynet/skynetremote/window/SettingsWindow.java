package se.markstrom.skynet.skynetremote.window;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import se.markstrom.skynet.skynetremote.model.Settings;

public class SettingsWindow {
	
	private Display display;
	private Shell shell;
	private Settings settings = new Settings();
	
	private Button updateEventsButton;
	private Button updateControlButton;
	private Button updateSensorsButton;
	private Button updateLogButton;
	private Button notifyOnNewInfoEventButton;
	private Button notifyOnNewMinorEventButton;
	private Button notifyOnNewMajorEventButton;
	private Button logDetailsButton;
	private Text summaryPollInterval;
	
	private boolean savePressed = false;
	
	public SettingsWindow(Settings settings, Shell parentShell) {
		this.settings.host = settings.host;
		this.settings.port = settings.port;
		
		display = Display.getDefault();
		shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText("Settings");
		
		GridLayout shellLayout = new GridLayout(1, false);
		shellLayout.marginTop = 5;
		shellLayout.marginLeft = 5;
		shellLayout.marginRight = 5;
		shellLayout.marginBottom = 5;
		shell.setLayout(shellLayout);

		Group graphicsGroup = new Group(shell, SWT.SHADOW_ETCHED_IN);
	    graphicsGroup.setText("Graphics");
		
	    GridLayout graphicsGroupLayout = new GridLayout(1, false);
		graphicsGroupLayout.marginTop = 5;
		graphicsGroupLayout.marginLeft = 5;
		graphicsGroupLayout.marginRight = 5;
		graphicsGroupLayout.marginBottom = 5;
		graphicsGroup.setLayout(graphicsGroupLayout);
	    
		notifyOnNewInfoEventButton = new Button(graphicsGroup, SWT.CHECK);
		notifyOnNewInfoEventButton.setText("Show notification at new event with info severity");

		notifyOnNewMinorEventButton = new Button(graphicsGroup, SWT.CHECK);
		notifyOnNewMinorEventButton.setText("Show notification at new event with minor severity");

		notifyOnNewMajorEventButton = new Button(graphicsGroup, SWT.CHECK);
		notifyOnNewMajorEventButton.setText("Show notification at new event with major severity");

		logDetailsButton = new Button(graphicsGroup, SWT.CHECK);
		logDetailsButton.setText("Enable detailed logging");

		Group pollGroup = new Group(shell, SWT.SHADOW_ETCHED_IN);
	    pollGroup.setText("Skynet API data fetching");

	    GridLayout pollGroupLayout = new GridLayout(2, false);
	    pollGroupLayout.marginTop = 5;
	    pollGroupLayout.marginLeft = 5;
	    pollGroupLayout.marginRight = 5;
	    pollGroupLayout.marginBottom = 5;
		pollGroup.setLayout(pollGroupLayout);
	    
		Label label = new Label(pollGroup, SWT.NONE);
		label.setText("Poll interval (seconds):");
		label.pack();
		
		summaryPollInterval = new Text(pollGroup, SWT.BORDER);
		
		updateEventsButton = new Button(pollGroup, SWT.CHECK);
		updateEventsButton.setText("Download events when a new event is detected");
		GridData gd2 = new GridData();
		gd2.horizontalSpan = 2;
		updateEventsButton.setLayoutData(gd2);

		updateControlButton = new Button(pollGroup, SWT.CHECK);
		updateControlButton.setText("Download devices when a new checksum is detected");
		GridData controlGd = new GridData();
		controlGd.horizontalSpan = 2;
		updateControlButton.setLayoutData(controlGd);

		updateSensorsButton = new Button(pollGroup, SWT.CHECK);
		updateSensorsButton.setText("Download sensors when a new trigger sum is detected");
		GridData sensorsGd = new GridData();
		sensorsGd.horizontalSpan = 2;
		updateSensorsButton.setLayoutData(sensorsGd);

		updateLogButton = new Button(pollGroup, SWT.CHECK);
		updateLogButton.setText("Download log when a new timestamp is detected");
		GridData logGd = new GridData();
		logGd.horizontalSpan = 2;
		updateLogButton.setLayoutData(logGd);
		
		Button resetButton = new Button(shell, SWT.PUSH);
		resetButton.setText("Reset to default");
		GridData resetButtonLayout = new GridData(GridData.FILL_HORIZONTAL);
	    resetButton.setLayoutData(resetButtonLayout);
		resetButton.addSelectionListener(new SelectionAdapter() {
		    @Override
		    public void widgetSelected(SelectionEvent e) {
		    	reset();
		    }
		});
		
		Button saveButton = new Button(shell, SWT.PUSH);
		saveButton.setText("Save");
		GridData saveButtonLayout = new GridData(GridData.FILL_HORIZONTAL);
	    saveButton.setLayoutData(saveButtonLayout);
		saveButton.addSelectionListener(new SelectionAdapter() {
		    @Override
		    public void widgetSelected(SelectionEvent e) {
		    	save();
		    }
		});
		
		updateFromSettings(settings);
		
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
	
	private void updateFromSettings(Settings settings) {
		updateEventsButton.setSelection(settings.getNewEvents);
		updateControlButton.setSelection(settings.getNewControl);
		updateSensorsButton.setSelection(settings.getNewSensors);
		updateLogButton.setSelection(settings.getNewLog);
		notifyOnNewInfoEventButton.setSelection(settings.notifyOnNewInfoEvent);
		notifyOnNewMinorEventButton.setSelection(settings.notifyOnNewMinorEvent);
		notifyOnNewMajorEventButton.setSelection(settings.notifyOnNewMajorEvent);
		logDetailsButton.setSelection(settings.logDetails);
		summaryPollInterval.setText(new Integer(settings.summaryPollInterval).toString());
	}
	
	public void run() {
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	public Settings getSettings() {
		if (savePressed) {
			return settings;
		}
		else {
			return null;
		}
	}
	
	private void save() {
		if (saveTextInput() && settings.validate()) {
			savePressed = true;
			shell.dispose();
		}
	}

	private void reset() {
		updateFromSettings(new Settings());
	}

	private boolean saveTextInput() {
		settings.getNewEvents = updateEventsButton.getSelection();
		settings.getNewControl = updateControlButton.getSelection();
		settings.getNewSensors = updateSensorsButton.getSelection();
		settings.getNewLog = updateLogButton.getSelection();
		settings.notifyOnNewInfoEvent = notifyOnNewInfoEventButton.getSelection();
		settings.notifyOnNewMinorEvent = notifyOnNewMinorEventButton.getSelection();
		settings.notifyOnNewMajorEvent = notifyOnNewMajorEventButton.getSelection();
		settings.logDetails = logDetailsButton.getSelection();
		try {
			settings.summaryPollInterval = Integer.parseInt(summaryPollInterval.getText());
		}
		catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	private class KeyListener implements TraverseListener {
		@Override
		public void keyTraversed(TraverseEvent event) {
			switch (event.detail) {
			case SWT.TRAVERSE_ESCAPE:
				shell.dispose();
				break;
			}
		}
	}
}
