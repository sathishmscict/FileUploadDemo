package com.example.satishgadde.uploaddemo;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetConnectivity {

	public static boolean isOnline(Context context) {

		try
		{
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netinfo = cm.getActiveNetworkInfo();
			if (netinfo != null && netinfo.isConnectedOrConnecting()) {
				return true;
			}
			
		}
		catch(Exception e)
		{
			System.out.println("Error :"+e.getMessage());
		}
	
		return false;
	}
}
