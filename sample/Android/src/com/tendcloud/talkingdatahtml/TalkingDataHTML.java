package com.tendcloud.talkingdatahtml;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.webkit.WebView;

import com.tendcloud.tenddata.TCAgent;

public class TalkingDataHTML {
	private static volatile TalkingDataHTML talkingDataHTML = null;
	
	public static TalkingDataHTML GetInstance(){
		if(talkingDataHTML == null){
			synchronized (TalkingDataHTML.class) {
				if(talkingDataHTML == null){
					talkingDataHTML = new TalkingDataHTML();
				}
			}
		}
		return talkingDataHTML;
	}
	
	Activity activity = null;
	
	public void execute(final Activity activity, final String url, final WebView webView) throws Exception {
		if (url.startsWith("talkingdata")) {
			talkingDataHTML.activity = activity;
			String str = url.substring(12);
			JSONObject jsonObj = new JSONObject(str);
			String functionName = jsonObj.getString("functionName");
			JSONArray args = jsonObj.getJSONArray("arguments");
			if (functionName.equals("getDeviceId")) {
				talkingDataHTML.getDeviceId(args, webView);
			} else {
				Class<TalkingDataHTML> classType = TalkingDataHTML.class;
				Method method = classType.getDeclaredMethod(functionName, JSONArray.class);
				method.invoke(talkingDataHTML, args);
			}
		}
	}
	
	private void getDeviceId(final JSONArray args, final WebView webView) throws JSONException {
		String deviceId = TCAgent.getDeviceId(MainActivity.context);
		String callBack = args.getString(0);
		webView.loadUrl("javascript:" + callBack + "('" + deviceId + "')");
	}
	
	@SuppressWarnings("unused")
	private void trackEvent(final JSONArray args) throws JSONException {
		String eventId = args.getString(0);
		TCAgent.onEvent(MainActivity.context, eventId);
	}
	
	@SuppressWarnings("unused")
	private void trackEventWithLabel(final JSONArray args) throws JSONException {
		String eventId = args.getString(0);
		String eventLabel = args.getString(1);
		TCAgent.onEvent(MainActivity.context, eventId, eventLabel);
	}
	
	@SuppressWarnings("unused")
	private void trackEventWithParameters(final JSONArray args) throws JSONException {
		String eventId = args.getString(0);
		String eventLabel = args.getString(1);
		String eventDataJson = args.getString(2);
		Map<String, Object> eventData = this.toMap(eventDataJson);
		TCAgent.onEvent(MainActivity.context, eventId, eventLabel, eventData);
	}
	
	@SuppressWarnings("unused")
	private void trackPageBegin(final JSONArray args) throws JSONException {
		String pageName = args.getString(0);
		if(GetInstance().activity != null){
			TCAgent.onPageStart(talkingDataHTML.activity, pageName);
		}
	}
	
	@SuppressWarnings("unused")
	private void trackPageEnd(final JSONArray args) throws JSONException {
		String pageName = args.getString(0);
		TCAgent.onPageEnd(talkingDataHTML.activity, pageName);
	}
	
	@SuppressWarnings("unused")
	private void setLocation(final JSONArray args) {
		
	}
	
	private Map<String, Object> toMap(String jsonStr)
    {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
			Iterator<String> keys = jsonObj.keys();
            String key = null;
            Object value = null;
            while (keys.hasNext())
            {
                key = keys.next();
                value = jsonObj.get(key);
                result.put(key, value);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
