package com.lithidsw.mrupdater;

import java.io.File;

import com.lithidsw.mrupdaterheadless.R;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class DownloadReceiver extends BroadcastReceiver {

	private static final String EXTRA_DOWNLOAD_ID = DownloadManager.EXTRA_DOWNLOAD_ID;
	private static final String COLUMN_STATUS = DownloadManager.COLUMN_STATUS;
	private static final int STATUS_SUCCESSFUL = DownloadManager.STATUS_SUCCESSFUL;
	
	SharedPreferences prefs;

	private DownloadManager dm = null;
	private DownloadedDataSource datasource;
	private Utils mUtils = new Utils();
	long downloadId;

	int notiID = 1;

	private DownloadManager.Query query = null;
	private Cursor cursor = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		prefs = context.getSharedPreferences(C.PREF, Context.MODE_PRIVATE);
		String action = intent.getAction();
		dm = (DownloadManager) context
				.getSystemService(Context.DOWNLOAD_SERVICE);
		if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
			downloadId = intent.getLongExtra(EXTRA_DOWNLOAD_ID, 0);
			try {
				datasource = new DownloadedDataSource(context);
				datasource.open();
				if (datasource.isQueueItem(downloadId)) {
					query = new DownloadManager.Query();
					query.setFilterById(downloadId);
					cursor = dm.query(query);
					if (cursor.moveToFirst()) {
						int columnIndex = cursor.getColumnIndex(COLUMN_STATUS);
						if (STATUS_SUCCESSFUL == cursor.getInt(columnIndex)) {
							String[] items = datasource
									.getItemsFromQueueId(downloadId);
							if (items[0] != null && items[1] != null) {
								File filename = new File(items[1]);
								if (mUtils.checkMD5(items[2], filename)) {
									showNoti(context, Integer.parseInt(items[0]), items[1]);
								} else {
									mUtils.showMd5ErrorNoti(context, items[1]);
									try {
										datasource = new DownloadedDataSource(context);
										datasource.open();
										datasource.deleteItem(Integer.parseInt(items[0]));
									} finally {
										datasource.close();
									}
									filename.delete();
								}
								prefs.edit().putLong(C.PREF_CURRENT_DOWNLOAD, 0).commit();
							} else {
								errorToast(context,
										R.string.toast_notification_error);
							}
						}
					}
				} else {
					errorToast(context, R.string.toast_download_unsuccessful);
				}
			} finally {
				cursor.close();
				datasource.close();
			}
		}
	}

	private void showNoti(Context context, int id, String uri) {
		String name = uri.substring(uri.lastIndexOf('/') + 1, uri.length());
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(context.getString(R.string.app_name))
				.setContentText(name)
				.addAction(R.drawable.ic_action_apply_dark,
						context.getString(R.string.apply),
						getPendingIntent(context, uri, id));

		mBuilder.setAutoCancel(true);

		Intent resultIntent = new Intent();
		resultIntent.setAction("com.lithidsw.mrupdater.HOME");
		PendingIntent resultPendingIntent = PendingIntent.getBroadcast(context,
				0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);

		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(notiID, mBuilder.build());
	}

	private void errorToast(Context context, int i) {
		Toast.makeText(context, context.getString(i), Toast.LENGTH_LONG).show();
	}

	public PendingIntent getPendingIntent(Context context, String uri, int id) {
		Intent intent = mUtils.getApplyIntent(context, uri, id);
		return PendingIntent.getActivity(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}
}
