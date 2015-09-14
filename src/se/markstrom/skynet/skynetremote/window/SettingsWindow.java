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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import se.markstrom.skynet.skynetremote.Settings;

public class SettingsWindow {
	
	private Display display;
	private Shell shell;
	private Settings settings = new Settings();
	
	private Button updateEventsButton;
	private Button updateControlButton;
	private Button updateSensorsButton;
	private Button updateLogButton;
	private Button notifyOnNewEventButton;
	private Text summaryPollInterval;
	
	private boolean savePressed = false;
	
	public SettingsWindow(Settings settings, Shell parentShell) {
		this.settings.host = settings.host;
		this.settings.port = settings.port;
		
		display = Display.getDefault();
		shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText("Settings");
		
		GridLayout layout = new GridLayout(2, false);
		layout.marginTop = 10;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		layout.marginBottom = 10;
		shell.setLayout(layout);

		updateEventsButton = new Button(shell, SWT.CHECK);
		updateEventsButton.setText("Download events when a new event is detected");
		GridData gd2 = new GridData();
		gd2.horizontalSpan = 2;
		updateEventsButton.setLayoutData(gd2);

		updateControlButton = new Button(shell, SWT.CHECK);
		updateControlButton.setText("Download devices when a new checksum is detected");
		GridData controlGd = new GridData();
		controlGd.horizontalSpan = 2;
		updateControlButton.setLayoutData(controlGd);

		updateSensorsButton = new Button(shell, SWT.CHECK);
		updateSensorsButton.setText("Download sensors when a new trigger sum is detected");
		GridData sensorsGd = new GridData();
		sensorsGd.horizontalSpan = 2;
		updateSensorsButton.setLayoutData(sensorsGd);

		updateLogButton = new Button(shell, SWT.CHECK);
		updateLogButton.setText("Download log when a new timestamp is detected");
		GridData logGd = new GridData();
		logGd.horizontalSpan = 2;
		updateLogButton.setLayoutData(logGd);

		notifyOnNewEventButton = new Button(shell, SWT.CHECK);
		notifyOnNewEventButton.setText("Show notification at new event");
		GridData gd3 = new GridData();
		gd3.horizontalSpan = 2;
		notifyOnNewEventButton.setLayoutData(gd3);

		Label label = new Label(shell, SWT.NONE);
		label.setText("Poll interval (seconds):");
		label.pack();
		
		summaryPollInterval = new Text(shell, SWT.BORDER);
		
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
		notifyOnNewEventButton.setSelection(settings.notifyOnNewEvent);
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
		settings.notifyOnNewEvent = notifyOnNewEventButton.getSelection();
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
