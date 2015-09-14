package org.hopegames.mobile.utils;

import org.hopegames.mobile.activity.PrefsActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

public class ConnectionUtils {

	public final static String TAG = ConnectionUtils.class.getSimpleName();
	
	

	public static boolean isOnWifi(Context ctx) {
		ConnectivityManager conMan = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = conMan.getActiveNetworkInfo();
		if (netInfo == null || netInfo.getType() != ConnectivityManager.TYPE_WIFI) {
			return false;
		} else {
			return true;
		}
	}

	public static boolean isNetworkConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm
				.getActiveNetworkInfo().isConnected());
	}
	
	public static boolean isOffLineMode(Context context){
		SharedPreferences prefs;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if(prefs.getBoolean(PrefsActivity.PREF_OFFLINE_MODE, false)){
			return true;
		}
		
		return false;
		
	}
	

}
