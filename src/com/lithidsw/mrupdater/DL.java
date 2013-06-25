package com.lithidsw.mrupdater;

import java.io.File;

import com.lithidsw.mrupdaterheadless.R;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

public class DL {
	private DownloadedDataSource datasource;
	Utils mUtils = new Utils();
	SharedPreferences prefs;
	Context mContext;
	Activity mActivity;
	String mNameExt;
	String mNameCache;
	String mWallPath;
	String mIntCache;
	String mName;
	File mWallCache;
	File mCache;
	int ID;
	private long en;

	public void Download(Context c, Activity a, String url, String id, String md5sum) {
		mContext = c;
		mActivity = a;
		prefs = a.getSharedPreferences(C.PREF, Context.MODE_PRIVATE);
		ID = Integer.parseInt(id);
		mNameExt = url.substring(url.lastIndexOf('/') + 1, url.length());
		mWallCache = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ "/"
				+ mActivity.getString(R.string.app_name));
		mCache = mContext.getCacheDir();
		if (!mWallCache.exists()) {
			mWallCache.mkdirs();
		}
		mIntCache = mCache + "/" + mNameCache;
		mWallPath = mWallCache + "/" + mNameExt;

		boolean check = mUtils.fileDownloaded(a, url);

		if (!check) {
			DownloadManager.Request request;
			request = new DownloadManager.Request(Uri.parse(url));
			request.setTitle(mName);
			request.setDescription("Downloading...");
			request.setDestinationInExternalPublicDir(mActivity
					.getApplicationContext().getString(R.string.app_name),
					mNameExt);
			request.setVisibleInDownloadsUi(false);
			DownloadManager manager = (DownloadManager) mActivity
					.getSystemService(Context.DOWNLOAD_SERVICE);
			en = manager.enqueue(request);

			try {
				datasource = new DownloadedDataSource(mContext);
				datasource.open();
				datasource.createItem(ID, en, mWallPath, md5sum);
			} finally {
				datasource.close();
			}

			prefs.edit().putLong(C.PREF_CURRENT_DOWNLOAD, en).commit();

			Toast.makeText(mContext, "Download started", Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(mContext,
					"File: " + mNameExt + " Exists, not downloading",
					Toast.LENGTH_SHORT).show();
		}
	}
}
