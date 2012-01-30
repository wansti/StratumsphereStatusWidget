package org.stratum0.statuswidget;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class StratumsphereStatusProvider extends AppWidgetProvider {

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

			try {
				URL url = new URL("http://rohieb.name/stuff/stratum0/status/status.png");
				HttpURLConnection ucon = (HttpURLConnection)url.openConnection();
				ucon.setInstanceFollowRedirects(false);
				ucon.connect();
				String redirectedURL = ucon.getHeaderField("Location");
				if (redirectedURL.endsWith("open.png")) {
					currentImage = R.drawable.stratum0_open;
				}
				else if (redirectedURL.endsWith("closed.png")) {
					currentImage = R.drawable.stratum0_closed;
				}
				ucon.disconnect();
			} catch (Exception e) {
				//in case of any error, just leave the state as unknown for now
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
	
	

}
