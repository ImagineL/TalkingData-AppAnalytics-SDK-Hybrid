package com.tendcloud.talkingdatahtml;

import com.tendcloud.tenddata.TCAgent;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.webkit.WebView;
import android.webkit.WebViewClient;

//@SuppressLint({ "JavascriptInterface", "SetJavaScriptEnabled" })
public class MainActivity extends Activity {

	static Context context = null;
	WebView webView = null;
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();
		webView = new WebView(this);
		setContentView(webView);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl("file:///android_asset/index.html");
		webView.setWebViewClient(new MyWebviewClient());
		TCAgent.LOG_ON = true;
		TCAgent.init(context, "DDEB735ADB53819FBC39F70C7A9D5CC3", "HaiYan");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	protected void onResume() {
		super.onResume();
		TCAgent.onResume(this);
	}
	
	protected void onPause() {
		super.onPause();
		TCAgent.onPause(this);
	}
	
	class MyWebviewClient extends WebViewClient{
		
	    @Override 
	    public void onPageFinished(WebView view, String url)
	    { 
	    	view.loadUrl("javascript:setWebViewFlag()"); 
	    	if(url != null && url.endsWith("/index.html")){
	    	    TCAgent.onPageStart(MainActivity.this, "index.html");
	    	}
	    } 
	    
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			try {
			    String decodedURL = java.net.URLDecoder.decode(url, "UTF-8");
				TalkingDataHTML.GetInstance().execute(MainActivity.this, decodedURL, view);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
	}
}
