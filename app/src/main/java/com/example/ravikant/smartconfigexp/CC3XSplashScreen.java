/**

* File:CC3XSplashScreen.java

* Copyright © 2013, Texas Instruments Incorporated - http://www.ti.com/

* All rights reserved.

*/
package com.example.ravikant.smartconfigexp;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;

import com.example.ravikant.smartconfigexp.utils.CC3XConstants;
import com.example.ravikant.smartconfigexp.utils.CCX300Utils;

/**
 * CC3XSplashScreen Activity shows the Splash image for a given time and later navigates the screen to the DeviceConfiguration Activity {@link CC3XConfigActivity}
 * The Activity is finished after the loading time is done and starts next screen.
 * @author raviteja
 *
 */
public class CC3XSplashScreen extends Activity 
{
	/**
	 * Called initially and loading of views are done here
	 * Orientation is restricted for mobile phones to protrait only and both orientations for tablet  
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		//Called initially to set the orientation to protrait only for mobiles and both for tablets
		CCX300Utils.setProtraitOrientationEnabled(CC3XSplashScreen.this);
		
		//Setting the Splashscren view to Activity
		setContentView(R.layout.cc3_xsplash_screen);
		
		//Timer to start CC3XConfigActivity 
		startTimer();

	}

	/**
	 * Starts the next activity with a little delay
	 */
	private void startTimer()
	{
		Handler splashTimer=new Handler();
		splashTimer.postDelayed(new Runnable()
		{
			public void run()
			{
				Intent configIntent=new Intent(CC3XSplashScreen.this,CC3XConfigActivity.class);
				startActivity(configIntent);
				finish();
			}
		}, CC3XConstants.CC3X_SPLASH_DELAY);
	}

	
	/**
	 *gets called in activity when a device is rotated  in any side..
	 *so we set the orientation based on screen size.
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) 
	{
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);


		if(!(CCX300Utils.isScreenXLarge(getApplicationContext())))
		{
			return;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
	return true;
	}
	
	/**
	 * Ovveriding back press function to ignore the back key event when user press back while processing the Splashscreen wait thread
	 */
	@Override
	public void onBackPressed()
	{
		return;
		
	}
}
