package com.tendcloud.talkingdatahtml;

import android.app.Activity;
import android.content.Context;
import android.webkit.WebView;

import com.tendcloud.tenddata.TCAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TalkingDataHTML {
	private static volatile TalkingDataHTML talkingDataHTML = null;
	private static String lastStartedPage = null;
	private static String lastStartedActivity = "";
	
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
	private void trackEvent(final JSONArray event) throws JSONException {
		String eventId = event.getString(0);
		TCAgent.onEvent(MainActivity.context, eventId);
	}


	@SuppressWarnings("unused")
	private void trackEventWithLabel(final JSONArray event) throws JSONException {
		String eventId = event.getString(0);
		String eventLabel = event.getString(1);
		TCAgent.onEvent(MainActivity.context, eventId, eventLabel);
	}
	
	@SuppressWarnings("unused")
	private void trackEventWithParameters(final JSONArray event) throws JSONException {
		String eventId = event.getString(0);
		String eventLabel = event.getString(1);
		String eventDataJson = event.getString(2);
		Map<String, Object> eventData = this.toMap(eventDataJson);
		TCAgent.onEvent(MainActivity.context, eventId, eventLabel, eventData);
	}
	
	@SuppressWarnings("unused")
	private void trackPageBegin(final JSONArray pages) throws JSONException {
		String pageName = pages.getString(0);
		if(GetInstance().activity != null){
			TCAgent.onPageStart(talkingDataHTML.activity, pageName);
		}
	}

	@SuppressWarnings("unused")
	private void trackPageEnd(final JSONArray pages) throws JSONException {
		String pageName = pages.getString(0);
		TCAgent.onPageEnd(talkingDataHTML.activity, pageName);
	}

	@SuppressWarnings("unused")
	private void trackPage(final JSONArray args) throws JSONException {
		String pageName = args.getString(0);
		String isAutoCountPage = args.getString(1);
		if(lastStartedPage != null && !lastStartedPage.isEmpty()){
			TCAgent.onPageEnd(talkingDataHTML.activity, lastStartedPage);
		}
		TCAgent.onPageStart(talkingDataHTML.activity, pageName);
		if("1".equals(isAutoCountPage)){
			TCAgent.onEvent(talkingDataHTML.activity, pageName);
		}
		lastStartedPage = pageName;
	}

	@SuppressWarnings("unused")
	public static void onPage(Context ctx, String pageName){
		onPage(ctx,pageName,false);
	}
	@SuppressWarnings("unused")
	public static void onPage(Context ctx, String startPageName ,boolean autoCountPageName){
		if(startPageName.isEmpty() || startPageName == null){
			if(lastStartedPage != null && !lastStartedPage.isEmpty()){
				TCAgent.onPageEnd(talkingDataHTML.activity, lastStartedPage);
			}
			if(lastStartedActivity != null && !lastStartedActivity.isEmpty()){
				TCAgent.onPageEnd(talkingDataHTML.activity, lastStartedActivity);
			}
		}else {
			if(lastStartedPage != null && !lastStartedPage.isEmpty()){
				TCAgent.onPageEnd(talkingDataHTML.activity, lastStartedPage);
			}
			TCAgent.onPageStart(talkingDataHTML.activity, startPageName);
			if(autoCountPageName){
				TCAgent.onEvent(ctx, startPageName);
			}
			lastStartedPage = startPageName;
		}
	}

//	@SuppressWarnings("unused")
//	public static void onPageStart(Context ctx, String activityName){
//		TCAgent.onPageStart(ctx, activityName);
//		lastStartedActivity = activityName;
//		if(lastStartedPage != null && !lastStartedPage.isEmpty()){
//			TCAgent.onPageStart(talkingDataHTML.activity, lastStartedPage);
//		}
//	}
//
//	@SuppressWarnings("unused")
//	public static void onPageEnd(Context ctx, String activityName){
//		if(lastStartedPage != null && !lastStartedPage.isEmpty()){
//			TCAgent.onPageEnd(talkingDataHTML.activity, lastStartedPage);
//		}
//		TCAgent.onPageEnd(talkingDataHTML.activity, activityName);
//	}

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
