/**

 * File:CC3XAboutUs.java

 * 	Copyright © 2013, Texas Instruments Incorporated - http://www.ti.com/
 * 	All rights reserved.
 *	@author Ravi Teja
 *	 <Description> About screen showing the Details of Xcube and its contact details</Description>
 */
package com.example.ravikant.smartconfigexp;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.view.MotionEvent;

import com.example.ravikant.smartconfigexp.utils.CCX300Utils;


public class CC3XAboutUs  extends Activity implements OnClickListener
{
	/**
	 * Back button in actionbar navigates user to previous screen
	 */
	private Button mBackNavigator=null;
	/**
	 * A HTML mime type for loading the webview containing text content
	 */
	private final String mHtmlMimeType="text/html";
	/**
	 * Default encoding format for text 
	 */
	private final String mEncodingType="utf-8";

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		CCX300Utils.setProtraitOrientationEnabled(CC3XAboutUs.this);

		setContentView(R.layout.cc3x_about_us);

		mBackNavigator=(Button)findViewById(R.id.cc3_about_back_btn);
		mBackNavigator.setVisibility(View.VISIBLE);
		mBackNavigator.setOnClickListener(this);

		((TextView)findViewById(R.id.header_text)).setText("");
		((TextView)findViewById(R.id.header_text)).setBackgroundResource(R.drawable.xcube_logo);

		WebView webViewAboutScreen = (WebView)findViewById(R.id.about_us_desc_webview);
		webViewAboutScreen.setVerticalScrollBarEnabled(false);
		webViewAboutScreen.setBackgroundColor(Color.TRANSPARENT);

		/**
		 *  avoid touch longpress copying of the text in webview ie. clipboard
		 */
		webViewAboutScreen.setOnTouchListener(new View.OnTouchListener() 
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				return true;
			}

		});

		int fontSize=14;

		WebSettings webSettings=webViewAboutScreen.getSettings();
		webSettings.setDefaultFontSize(fontSize);

		String aboutContentStr = String.valueOf(Html
				.fromHtml("<![CDATA[<body style=\"text-align:left;color:#AAAAAA; \">"
						+"<p>"+ getResources().getString(R.string.about_us_desc_para_one)+
						"</p>"+"<p>"+ getResources().getString(R.string.about_us_desc_para_two)+
						"</p>"+"<p>"+ getResources().getString(R.string.about_us_desc_para_three)+
						"</p>"
						+ "</body>]]>"));

		webViewAboutScreen.loadData(aboutContentStr, mHtmlMimeType, mEncodingType);
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) 
	{
		super.onConfigurationChanged(newConfig);

		if(!(CCX300Utils.isScreenXLarge(getApplicationContext())))
		{
			return;
		}
	}


	@Override
	public void onClick(View v) 
	{
		if(v.getId()==R.id.cc3_about_back_btn)
		{
			CC3XAboutUs.this.finish();
		}
	}
}
