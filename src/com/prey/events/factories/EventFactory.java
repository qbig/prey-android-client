package com.prey.events.factories;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.widget.Toast;

import com.prey.PreyConfig;
import com.prey.events.Event;
import com.prey.managers.PreyConnectivityManager;
import com.prey.managers.PreyTelephonyManager;

public class EventFactory {

	private static final String BOOT_COMPLETED="android.intent.action.BOOT_COMPLETED";
	private static final String CONNECTIVITY_CHANGE="android.net.conn.CONNECTIVITY_CHANGE";
	private static final String WIFI_STATE_CHANGED="android.net.wifi.WIFI_STATE_CHANGED";
	private static final String ACTION_POWER_CONNECTED="android.intent.action.ACTION_POWER_CONNECTED";
	private static final String ACTION_POWER_DISCONNECTED="android.intent.action.ACTION_POWER_DISCONNECTED";
	private static final String ACTION_BATTERY_CHANGE="android.intent.action.ACTION_BATTERY_CHANGE";
	private static final String ACTION_BATTERY_LOW="android.intent.action.ACTION_BATTERY_LOW";
	private static final String ACTION_SHUTDOWN="android.intent.action.ACTION_SHUTDOWN";
	
	
	public static Event getEvent(Context ctx,Intent intent){
		//Toast.makeText(ctx, "getEvent["+intent.getAction()+"]", Toast.LENGTH_LONG).show();
		if (BOOT_COMPLETED.equals(intent.getAction()) ){
			if (PreyConfig.getPreyConfig(ctx).isSimChanged()){
				JSONObject info=new JSONObject();
				try{
					info.put("new_phone_number",PreyTelephonyManager.getInstance(ctx).getLine1Number());
				}catch (Exception e) {
				}
				return new Event(Event.SIM_CHANGED,info);
			}else{
				return new Event(Event.TURNED_ON);
			}
		}
		
		if (ACTION_SHUTDOWN.equals(intent.getAction()) ){
			return new Event(Event.TURNED_OFF);
		}
		if (CONNECTIVITY_CHANGE.equals(intent.getAction())|| WIFI_STATE_CHANGED.equals(intent.getAction()) ){
			JSONObject info=new JSONObject();
			int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
			try{
				if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
					info.put("connected", "wifi");
				}
				if (PreyConnectivityManager.getInstance(ctx).isMobileConnected()) {
					info.put("connected", "mobile");
				}
			}catch (Exception e) {
			}
			return new Event(Event.WIFI_CHANGED,info);
		}
        
		if (ACTION_POWER_CONNECTED.equals(intent.getAction()) ){
			return new Event(Event.PLUGGED);
		}
		if (ACTION_POWER_DISCONNECTED.equals(intent.getAction()) ){
			return new Event(Event.UN_PLUGGED);
		}
		
		if (ACTION_BATTERY_LOW.equals(intent.getAction()) ){
			return new Event(Event.UN_PLUGGED);
		}
		
		if (ACTION_BATTERY_CHANGE.equals(intent.getAction())){
			int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
			JSONObject info=new JSONObject();
			try{
				if(plugged==BatteryManager.BATTERY_PLUGGED_AC ){
					info.put("source ","ac");
				}else{
					if(plugged==BatteryManager.BATTERY_PLUGGED_USB){
						info.put("source ","usb");
					}
				}
			}catch (Exception e) {
			}
			return new Event(Event.PLUGGED,info);
		}
		return null;
	}
}
