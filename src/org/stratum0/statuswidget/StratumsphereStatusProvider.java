package org.stratum0.statuswidget;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.RemoteViews;
import android.util.Log;
import android.app.NotificationManager;
import android.app.Notification;


public class StratumsphereStatusProvider extends AppWidgetProvider {
	
	
	private static final String TAG = "Stratum0";
	private static final String url = "http://rohieb.name/stratum0/status.json";
	private static final int nID = 1;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification nNotOpen = new Notification();
		Intent notificationIntent = new Intent(context, StratumsphereStatusProvider.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);		nNotOpen.defaults = Notification.DEFAULT_ALL;
		nNotOpen.icon = R.drawable.stratum0_unknown;
		nNotOpen.tickerText = context.getText(R.string.nNotOpen);
		nNotOpen.when = System.currentTimeMillis();
		nNotOpen.defaults = Notification.DEFAULT_ALL;
		nNotOpen.setLatestEventInfo(context, "Warnung!", "Schnell den Space im IRC als offen makieren.", contentIntent);
		
		for (int i=0; i<appWidgetIds.length; i++) {
			int appWidgetId = appWidgetIds[i];
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main);
			int currentImage = R.drawable.stratum0_unknown;
			Date now = new GregorianCalendar().getTime();

			//TODO proper number formatting
			String text = "Updated:\n";
			String upTimeText = "";
			if (now.getHours() < 10) text += "0";
			text += now.getHours() + ":";
			if (now.getMinutes() < 10) text += "0";
			text += now.getMinutes();
			
			String jsonText = getStatusFromJSON();
			if (jsonText.startsWith("{") && jsonText.endsWith("}")) {
				try {
					JSONObject jsonObject = new JSONObject(jsonText);
					String upTime = jsonObject.getString("since");
					SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
					Date d = f.parse(upTime);
					//TODO Date class probably offers a better way to do this 
					long upTimeMins = (now.getTime()-d.getTime())/(1000*60) % 60;
					long upTimeHours = (now.getTime()-d.getTime())/(1000*60) / 60;
					//TODO proper number formatting
					if (upTimeHours < 10) upTimeText += "0"; 
					upTimeText += upTimeHours + "     ";
					if (upTimeMins < 10) upTimeText += "0";
					upTimeText += upTimeMins;

					if (jsonObject.getBoolean("isOpen")) {
						currentImage = R.drawable.stratum0_open;
						notificationManager.cancel(nID);

					}
					else {	
						if (wifiInfo.getSSID() != null && wifiInfo.getSSID().equals("Stratum0")) {
								openSpace();
								currentImage = R.drawable.stratum0_closed;
								upTimeText = "";
								text = text + " WIFI";
								notificationManager.notify(nID, nNotOpen);
								
						}
						else {
							currentImage = R.drawable.stratum0_closed;
							notificationManager.cancel(nID);
						}
					}
				} catch (Exception e) {
					Log.w(TAG, "Exception " + e);
				}
			}



			views.setImageViewResource(R.id.statusImageView, currentImage);
			views.setTextViewText(R.id.lastUpdateTextView, text);
			views.setTextViewText(R.id.spaceUptimeTextView, upTimeText);
			
			// Register an onClickListener
			Intent intent = new Intent(context, StratumsphereStatusProvider.class);
			intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
			PendingIntent updateOnClickIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			views.setOnClickPendingIntent(R.id.statusImageView, updateOnClickIntent);
			
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}
	
	
	private void openSpace() {
		
		// call some API to open the Space (change status to open)
	}

	public String getStatusFromJSON() {
		String result = "";
		DefaultHttpClient client = new DefaultHttpClient();
		try {
			HttpResponse response = client.execute(new HttpGet(url));
			if (response.getStatusLine().getStatusCode() == 200) {
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String line;
				while ((line = br.readLine()) != null) {
					result += line;
				}
			}
		} catch (Exception e) {
			Log.w(TAG, "Exception " + e);
		}
		return result;
	}
 	


}
