/**

 * File:CC3XConfigActivity.java

 * Copyright © 2013, Texas Instruments Incorporated - http://www.ti.com/

 * All rights reserved.
	
 */
package com.example.ravikant.smartconfigexp;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ravikant.smartconfigexp.utils.CC3XConstants;
import com.example.ravikant.smartconfigexp.utils.CC3XDialogManager;
import com.example.ravikant.smartconfigexp.utils.CC3XWifiManager;
import com.example.ravikant.smartconfigexp.utils.CCX300Utils;
import com.integrity_project.smartconfiglib.FirstTimeConfig;
import com.integrity_project.smartconfiglib.FirstTimeConfigListener;

/**
 * Device Configuration activity which holds the main operations related to UI
 * elements. The call to SmartConfig.jar library is done in this activity
 * 
 * @author raviteja
 * 
 */
public class CC3XConfigActivity extends Activity implements OnClickListener,
		FirstTimeConfigListener, OnCheckedChangeListener, TextWatcher {
	/**
	 * Wifi Manager instance which gives the network related information like
	 * Wifi ,SSID etc.
	 */
	private CC3XWifiManager mCC3XWifiManager = null;

	/**
	 * Footer view which navigates us to About Screen describing the CC3X &
	 * Xcube details
	 */
	private RelativeLayout footerView = null;

	/**
	 * Sending a request to server is done onClick of this button,it interacts
	 * with the smartConfig.jar library
	 */
	private Button mSendDataPackets = null;

	/**
	 * The ssid field details are set which are retrieved from CC3XWifiManager
	 */
	private EditText mSSIDInputField = null;

	/**
	 * The Password input field details are entered by user also called Key
	 * field
	 */
	private EditText mPasswordInputField = null;

	/**
	 * The GateWayIP input field details which are retrieved from
	 * CC3XWifiManager
	 */
	private TextView mGateWayIPInputField = null;

	/**
	 * The Encryption key field input field is entered by user
	 */
	private EditText mKeyInputField = null;

	/**
	 * Device name which user might enter by default its CC3000
	 */
	private EditText mDeviceNameInputField = null;

	/**
	 * A Dialog instance which is responsible to generate all dialogs in the app
	 */
	private CC3XDialogManager mCC3xDialogManager = null;

	/**
	 * A check box which when checked sends the encryption key to server as a
	 * input and checks if key is exactly 16chars
	 */
	private CheckBox mconfig_key_checkbox = null;

	/**
	 * Progressbar
	 */
	private ProgressBar mconfigProgress = null;

	/**
	 * Called initially and loading of views are done here Orientation is
	 * restricted for mobile phones to protrait only and both orientations for
	 * tablet
	 */

	/**
	 * Timer instance to activate network wifi check for every few minutes
	 * Popps out an alert if no network or if connected to any network
	 */
	private	Timer timer = new Timer();
	
	/**
	 * A boolean to check if network alert is visible or invisible. If visible we are dismissing the existing alert 
	 */
	public static boolean sIsNetworkAlertVisible = false;
	
	/**
	 * Boolean to check if network is enabled or not
	 */
	public boolean isNetworkConnecting=false;

	/**
	 * Dialog ID to tigger no network dialog
	 */
	private static final int NO_NETWORK_DIALOG_ID = 002;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/**
		 * Disable orientation if launched in mobile 
		 */
		CCX300Utils.setProtraitOrientationEnabled(CC3XConfigActivity.this);

		setContentView(R.layout.configration);

		/**
		 * Check for WIFI intially
		 */
		if(isNetworkConnecting)
		{
			
		}else
		{
			if(!sIsNetworkAlertVisible)
			{
				checkNetwork("ONCREATE");
			}
		}
		

		/**
		 * Initialize all view componets in screen
		 */
		initViews();

		/**
		 * Initailize all click listeners to views
		 */
		setViewClickListeners();

		/**
		 * Set initial data to all non editable components
		 */
		initData();
		
		timerDelayForAPUpdate();

		/**
		 * Initializing the intent Filter for network check cases and registering the events to broadcast reciever
		 */
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		registerReceiver(broadcastReceiver, intentFilter);
	}

	/**
	 * returns the Wifi Manager instance which gives the network related
	 * information like Wifi ,SSID etc.
	 * 
	 * @return Wifi Manager instance
	 */
	public CC3XWifiManager getWiFiManagerInstance() {
		if (mCC3XWifiManager == null)
		{
			mCC3XWifiManager = new CC3XWifiManager(CC3XConfigActivity.this);
		}
		return mCC3XWifiManager;
	}

	/**
	 * Initialize all view componets in screen with input data
	 */
	private void initData() {

		if (getWiFiManagerInstance().getCurrentSSID() != null && getWiFiManagerInstance().getCurrentSSID().length() > 0)
		{
			mSSIDInputField.setText(getWiFiManagerInstance().getCurrentSSID());
	
			/**
			 * removing the foucs of ssid when field is already configured from Network
			 */
			mSSIDInputField.setEnabled(false);
			mSSIDInputField.setFocusable(false);
			mSSIDInputField.setFocusableInTouchMode(false);
		}

		mGateWayIPInputField.setText(getWiFiManagerInstance().getGatewayIpAddress());
	}

	/**
	 * Check for wifi network if not throw a alert to user
	 */
	private boolean checkNetwork(String str) {
		
		if (!(getWiFiManagerInstance().isWifiConnected()))
		{
			sIsNetworkAlertVisible = true;
			mCC3xDialogManager = new CC3XDialogManager(CC3XConfigActivity.this);
			showDialog(NO_NETWORK_DIALOG_ID);
			return false;
			// Do stuff when wifi not there.. disable start button.
		} else
		{
			return true;
		}

	}

	/**
	 * Initialise all view components from xml
	 */
	private void initViews() 
	{
		mSendDataPackets = (Button) findViewById(R.id.config_start_button);
		footerView = (RelativeLayout) findViewById(R.id.config_footerview);
		mSSIDInputField = (EditText) findViewById(R.id.config_ssid_input);
		mPasswordInputField = (EditText) findViewById(R.id.config_passwd_input);
		mGateWayIPInputField = (TextView) findViewById(R.id.config_gateway_input);
		mKeyInputField = (EditText) findViewById(R.id.config_key_input);
		mDeviceNameInputField = (EditText) findViewById(R.id.config_device_name_input);
		mconfigProgress = (ProgressBar) findViewById(R.id.config_progress);
		mconfig_key_checkbox = (CheckBox) findViewById(R.id.config_key_checkbox);
		mconfig_key_checkbox.setChecked(false);
		mconfig_key_checkbox.setOnCheckedChangeListener(this);
		mKeyInputField.addTextChangedListener(this);

		if (!mconfig_key_checkbox.isChecked()) 
		{
			mKeyInputField.setEnabled(false);
			mKeyInputField.setFocusable(false);
		}
	}

	/**
	 * Init the click listeners of all required views
	 */
	private void setViewClickListeners() 
	{
		mSendDataPackets.setOnClickListener(this);
		footerView.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) 
	{
		switch (v.getId()) 
		{
		case R.id.config_footerview:
			// Navigate to About screen
			showAboutUsScreen();
			break;
		case R.id.config_start_button:
			// Check network
			if (checkNetwork("bUTTON")) 
			{
				try
				{
					sendPacketData();
					
				} 
				catch (Exception e) 
				{
					
				}
			}
			break;
		}
	}

	private FirstTimeConfig firstConfig = null;
	
	public boolean isCalled = false;

	/**
	 * Send the packet data to server
	 * 
	 * @throws Exception
	 */
	private void sendPacketData() throws Exception
	{
		if (!isCalled)
		{
			isCalled = true;
			mSendDataPackets.setText(getResources().getString(R.string.stop_label));
			try 
			{
				firstConfig = getFirstTimeConfigInstance(CC3XConfigActivity.this);
			} catch (Exception e) 
			{
				
			}

			firstConfig.transmitSettings();
			mSendDataPackets.setBackgroundResource(R.drawable.selection_focus_btn);
			mconfigProgress.setVisibility(ProgressBar.VISIBLE);
		} 
		else 
		{
			if (firstConfig != null) 
			{
				stopPacketData();
			}
		}
	}

	/**
	 * Stops the transmission of live packets to server. callback of FTC_SUCCESS
	 * or failure also will trigger this method.
	 */
	private void stopPacketData() 
	{
		if(isCalled)
		{		
			try 
		{
			isCalled = false;

			mSendDataPackets.setText(getResources().getString(R.string.start_label));

			mSendDataPackets.setBackgroundResource(R.drawable.selection);
			if (mconfigProgress != null) 
			{
				mconfigProgress.setVisibility(ProgressBar.INVISIBLE);
			}
			firstConfig.stopTransmitting();
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	}
	/**
	 * Returns the instance of FirstTimeConfig class which is further used to
	 * call startTransmission() to send the message to server.
	 * 
	 * @param apiListener
	 * @return instance of FirstTimeConfig with variying parameters to the
	 *         unique Constructor ie. Overloading of Constructor.
	 * @throws Exception
	 */
	private FirstTimeConfig getFirstTimeConfigInstance(FirstTimeConfigListener apiListener) throws Exception 
	{
		String ssidFieldTxt = mSSIDInputField.getText().toString().trim();
		String passwdText = mPasswordInputField.getText().toString().trim();
		String deviceInput = mDeviceNameInputField.getText().toString().trim();
		if (deviceInput.length() == 0) 
		{
			deviceInput = "CC300";
		}

		byte[] totalBytes = null;
		String keyInput = null;
		if (isKeyChecked) 
		{
			keyInput = mKeyInputField.getText().toString().trim();

			if (keyInput.length() == 16) 
			{
				totalBytes = (keyInput.getBytes());
			} else 
			{
				totalBytes = null;
			}
		}
		return new FirstTimeConfig(apiListener, passwdText, totalBytes,	getWiFiManagerInstance().getGatewayIpAddress(),
				ssidFieldTxt,deviceInput);
	}

	/**
	 * Navigate to About screen
	 */
	private void showAboutUsScreen() 
	{
		Intent aboutUsintent = new Intent(CC3XConfigActivity.this,CC3XAboutUs.class);
		startActivity(aboutUsintent);
	}

	/**
	 * Logic to restrict the orientation in mobiles phones only becasue of size
	 * constraint
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) 
	{
		super.onConfigurationChanged(newConfig);
		if (!(CCX300Utils.isScreenXLarge(getApplicationContext()))) 
		{
			return;
		}
	}

	/**
	 * Callback for Failure or success of the SmartConfig.jar library api
	 */

	@Override
	public void onFirstTimeConfigEvent(FtcEvent arg0, Exception arg1)
	{
		try 
		{
			/**
			 * Adding the Try catch just to ensure the event doesnt retrun null.Some times observed null from Lib file.Just a safety measure
			 */
			arg1.printStackTrace();

		} catch (Exception e) 
		{

		}

		switch (arg0) 
		{
		case FTC_ERROR:
			/**
			 * Stop transmission
			 */
			handler.sendEmptyMessage(CC3XConstants.DLG_CONNECTION_FAILURE);
			break;
		case FTC_SUCCESS:

			/**
			 * Show user alert on success
			 */
			handler.sendEmptyMessage(CC3XConstants.DLG_CONNECTION_SUCCESS);
			break;
		case FTC_TIMEOUT:
			/**
			 * Show user alert when timed out
			 */
			handler.sendEmptyMessage(CC3XConstants.DLG_CONNECTION_TIMEOUT);
			break;
		default:
			break;
		}
	}

	/**
	 * Show timeout alert
	 */
	private void showConnectionTimedOut(int dialogType) 
	{
		if (mCC3xDialogManager == null) 
		{
			mCC3xDialogManager = new CC3XDialogManager(CC3XConfigActivity.this);
		}
		mCC3xDialogManager.showCustomAlertDialog(dialogType);
	}

	/**
	 * Show Failure alert
	 */
	private void showFailureAlert(int dialogType)
	{
		if (mCC3xDialogManager == null) 
		{
			mCC3xDialogManager = new CC3XDialogManager(CC3XConfigActivity.this);
		}
		mCC3xDialogManager.showCustomAlertDialog(dialogType);
	}

	/**
	 * Throws an alert to user stating the success message recieved after
	 * configuration
	 */
	private void showConnectionSuccess(int dialogType) 
	{
		if (mCC3xDialogManager == null) 
		{
			mCC3xDialogManager = new CC3XDialogManager(CC3XConfigActivity.this);
		}
		mCC3xDialogManager.showCustomAlertDialog(dialogType);
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		/**
		 * Check for network cases when app is minimized again
		 */
		if (!sIsNetworkAlertVisible) 
		{
			if(isNetworkConnecting)
			{
				
			}else
			{
				if (!(getWiFiManagerInstance().isWifiConnected())) 
				{
					showDialog(NO_NETWORK_DIALOG_ID);
				}	
			}
			sIsNetworkAlertVisible = false;
		}
	}

	/**
	 * Stop data trasnfer on app exist if is running
	 */
	@Override
	protected void onStop() 
	{
		super.onStop();
		/**
		 * Calling Stop once again
		 */
		stopPacketData();
	}

	@Override
	protected void onPause() 
	{
		super.onPause();
	}

	private boolean isKeyChecked = false;

	
	/**
	 * Default listener for checkbox the encrypted key is enabled or disabled based on check
	 * 
	 * if it is checked we need to ensure the length of key is exactly 16 else start is disabled.
	 * 
	 * The start button is made semi transperent and click is disabled  if above case fails
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
	{
		isKeyChecked = isChecked;

		/**
		 * The start button is made semi transperent and click is disabled  if length is not 16chars case fails
		 */
		if (isChecked) 
		{
			if (mKeyInputField.length() != 16) 
			{
				mSendDataPackets.setEnabled(false);
				mSendDataPackets.getBackground().setAlpha(150);

			} else 
			{
				mSendDataPackets.setEnabled(false);
				mSendDataPackets.getBackground().setAlpha(255);
			}
			mKeyInputField.setEnabled(true);
			mKeyInputField.setFocusable(true);
			mKeyInputField.setFocusableInTouchMode(true);
			mKeyInputField.setTextColor(getResources().getColor(R.color.black));
		} 
		else 
		{
			mKeyInputField.setEnabled(false);
			mKeyInputField.setFocusable(false);
			mKeyInputField.setFocusableInTouchMode(false);
			mKeyInputField.setTextColor(getResources().getColor(R.color.disabled_text_color));
		}
	}

	@Override
	public void afterTextChanged(Editable s) 
	{
		// overriden method for text changed listener
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,	int after) 
	{
		// overriden method for text changed listener
	}

	int textCount = 0;

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
		/**
		 * if is checked and length matches 16 the start is enabled else disabled
		 */
		if (mconfig_key_checkbox.isChecked()) 
		{
			textCount = mKeyInputField.getText().length();
			if (textCount == 16)
			{
				mSendDataPackets.setEnabled(true);
				mSendDataPackets.getBackground().setAlpha(255);
			} else 
			{
				mSendDataPackets.setEnabled(false);
				mSendDataPackets.getBackground().setAlpha(150);
			}
		}
	}

	/**
	 * Handler class for invoking dialog when a thread notifies of FTC_SUCCESS or FTC_FAILURE
	 */
	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg) 
		{
			switch (msg.what) 
			{
			case CC3XConstants.DLG_CONNECTION_FAILURE:
				showFailureAlert(CC3XConstants.DLG_CONNECTION_FAILURE);
				break;
			case CC3XConstants.DLG_CONNECTION_SUCCESS:
				showConnectionSuccess(CC3XConstants.DLG_CONNECTION_SUCCESS);
				break;

			case CC3XConstants.DLG_CONNECTION_TIMEOUT:
				showConnectionTimedOut(CC3XConstants.DLG_CONNECTION_TIMEOUT);
				break;
			}
			/**
			 * Stop transmission
			 */
			stopPacketData();
		}
	};

	/**
	 * Timer to check network periodically at some time intervals.
	 * If network is available then the current access point details are set to the SSID field in next case else a alert dialog is shown
	 */
	void timerDelayForAPUpdate() 
	{
		int periodicDelay = 1000; // delay for 1 sec.
		int timeInterval = 180000; // repeat every 3minutes.

		timer.scheduleAtFixedRate(new TimerTask() 
		{
			public void run() 
			{
				runOnUiThread(new Runnable() 
				{
					public void run()
					{
						// stuff that updates ui
						if (!sIsNetworkAlertVisible) 
						{
							if (!(getWiFiManagerInstance().isWifiConnected())) 
							{
								showDialog(NO_NETWORK_DIALOG_ID);
							}
						}
					}
				});
			}
		}, periodicDelay, timeInterval);
	}

	/**
	 * Called when activity is no more visible to user and the activity is removed from stack
	 */
	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
		/**
		 * back end time for network check is un registered
		 */
		timer.cancel();
		
		/**
		 * the Network change or wifi change broadcast registered in onCreate is unregistered at the finish of activity to ensure there is no IntentFilter leak in the onDestroy()
		 */
		unregisterReceiver(broadcastReceiver);
	}
	
	/**
	 * Common Alert dialog instance to show no network or AP change message
	 */
	private AlertDialog alert = null;
	
	/**
	 * Dialog creation is done here
	 */
	@Override
	protected Dialog onCreateDialog(int id)
	{
		AlertDialog.Builder builder1;
		try
		{
			
		if(alert!=null)
		{
			if(alert.isShowing())
			{
				alert.dismiss();
			}
		}
		}catch (Exception e)
		{
			
		}
		
		switch (id) 
		{
		case NO_NETWORK_DIALOG_ID:
			sIsNetworkAlertVisible = true;
			builder1 = new AlertDialog.Builder(this);
			builder1.setCancelable(true).setTitle(getResources().getString(R.string.alert_cc3x_title))
										.setMessage(getResources().getString(R.string.alert_no_network_title))
										.setCancelable(false).setPositiveButton("OK",new DialogInterface.OnClickListener(){			
			public void onClick(DialogInterface dialog,int id) 
			{
				sIsNetworkAlertVisible = false;

			}});

			alert = builder1.create();
			alert.show();
			break;
		}
		return super.onCreateDialog(id);
	}

	
	/**
	 * A broadcast reciever which is registered to notify the app about the changes in network or Access point is switched by the Device WIfimanager
	 */
	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			final String action = intent.getAction();
			if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) 
			{
				if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) 
				{
					
				} else 
				{
					// wifi connection was lost
					if (!sIsNetworkAlertVisible)
					{
						if (!(getWiFiManagerInstance().isWifiConnected())) 
						{

						}
					}
					/**
					 * Clearing the previous SSID Gateway ip if accesspoint is disconnected in between connection or usage
					 */
					mSSIDInputField.setText("");
					mGateWayIPInputField.setText("");
				}
			}

			if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) 
			{

				NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				if (info.getType() == ConnectivityManager.TYPE_WIFI) 
				{
					
					
				}

				if (info.getDetailedState() == DetailedState.CONNECTED) 
				{
					isNetworkConnecting=true;
					WifiManager myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
					WifiInfo wifiInfo = myWifiManager.getConnectionInfo();
					mSSIDInputField.setText(CC3XWifiManager.removeSSIDQuotes(wifiInfo.getSSID()));
					mSSIDInputField.setEnabled(false);
					mSSIDInputField.setFocusable(false);
					mSSIDInputField.setFocusableInTouchMode(false);
					int gatwayVal=	myWifiManager.getDhcpInfo().gateway;
					String gateWayIP=	(String.format("%d.%d.%d.%d", (gatwayVal & 0xff),(gatwayVal >> 8 & 0xff),(gatwayVal >> 16 & 0xff),	(gatwayVal >> 24 & 0xff))).toString();
					
					mGateWayIPInputField.setText(gateWayIP);
				}
			}
		}
	};
}