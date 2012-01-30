package org.stratum0.statuswidget;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class StratumsphereStatusProvider extends AppWidgetProvider {
	
	private String url = "http://rohieb.name/stratum0/status.json";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		for (int i=0; i<appWidgetIds.length; i++) {
			
			int appWidgetId = appWidgetIds[i];
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main);
			int currentImage = R.drawable.stratum0_unknown;
			
			Calendar now = Calendar.getInstance();
			String text = "Updated: ";
			if (now.getTime().getHours() < 10) text += "0";
			text += now.getTime().getHours() + ":";
			if (now.getTime().getMinutes() < 10) text += "0";
			text += now.getTime().getMinutes();
			
			String jsonText = getStatusFromJSON();
			if (jsonText.startsWith("{") && jsonText.endsWith("}")) {
				try {
					JSONObject jsonObject = new JSONObject(jsonText);
					if (jsonObject.getBoolean("isOpen")) {
						currentImage = R.drawable.stratum0_open;
					}
					else {
						currentImage = R.drawable.stratum0_closed;
					}
				} catch (Exception e) {
					//in case of any error, just leave the state as unknown for now
				}
			}
		
			views.setImageViewResource(R.id.statusImageView, currentImage);
			views.setTextViewText(R.id.timestampTextView, text);
			
			// Register an onClickListener
			Intent intent = new Intent(context, StratumsphereStatusProvider.class);
			intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
			PendingIntent updateOnClickIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			views.setOnClickPendingIntent(R.id.statusImageView, updateOnClickIntent);
			
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
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
			//just return an empty string when something goes wrong
		}
		return result;
	}
 	
	

}
