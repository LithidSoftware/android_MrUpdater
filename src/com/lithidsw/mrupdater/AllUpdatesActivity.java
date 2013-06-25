package com.lithidsw.mrupdater;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.lithidsw.mrupdaterheadless.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;
import android.widget.ListView;

public class AllUpdatesActivity extends Activity {

	SharedPreferences prefs;

	MenuItem mRefreshItem;

	Adapter mAdapter;
	private List<String> id = new ArrayList<String>();
	private List<String> file = new ArrayList<String>();
	private List<String> version = new ArrayList<String>();
	private List<String> md5sum = new ArrayList<String>();
	private List<String> type = new ArrayList<String>();
	private List<String> changelog = new ArrayList<String>();
	private List<String> date = new ArrayList<String>();
	private List<String> progress = new ArrayList<String>();

	private String mDevice;

	int curVer;

	private boolean ACTIVE_DOWNLOAD;
	boolean firstRun = true;
	private long idDl;
	private downloadRunning mDlCheck;
	private DownloadManager mgr;

	private loadAllUpdates mTask;
	private JSONArray mArray;

	Utils mUtils = new Utils();

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		prefs = getSharedPreferences(C.PREF, MODE_PRIVATE);
		String themeSty = prefs.getString(C.PREF_THEME, null);
		if (themeSty != null) {
			setTheme(getResources().getIdentifier(themeSty, "style", C.THIS));
		} else {
			setTheme(R.style.Dark);
		}
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.all_updates_list);
		ListView listView = (ListView) findViewById(R.id.list);

		prefs = getSharedPreferences(C.PREF, Context.MODE_PRIVATE);

		mgr = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

		mAdapter = new Adapter(this, id, file, md5sum, version, type, changelog, date,
				progress);
		listView.setAdapter(mAdapter);

		mDevice = mUtils.getPropString("ro.product.device");
		curVer = mUtils.getPropInt("ro.mrupdater.id");
		setUpActionBar();
		mTask = (loadAllUpdates) new loadAllUpdates().execute();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.action_all_refresh:
			stopDownloadCheck();
			mRefreshItem.setVisible(false);
			clearPages();
			mTask = (loadAllUpdates) new loadAllUpdates().execute();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.all_main, menu);
		mRefreshItem = menu.getItem(0);
		mRefreshItem.setVisible(false);
		return true;
	}

	private void setUpActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(mUtils.upperFirst(mDevice) + ": All updates");
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onStop() {
		super.onStop();
		stopLoader();
		stopDownloadCheck();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!firstRun) {
			stopDownloadCheck();
			mDlCheck = (downloadRunning) new downloadRunning().execute();
		}
	}

	void clearPages() {
		id.clear();
		file.clear();
		version.clear();
		md5sum.clear();
		type.clear();
		changelog.clear();
		date.clear();
		progress.clear();
		mAdapter.notifyDataSetChanged();
	}

	private void stopDownloadCheck() {
		ACTIVE_DOWNLOAD = false;
		if (mDlCheck != null
				&& mDlCheck.getStatus() != downloadRunning.Status.FINISHED) {
			mDlCheck.cancel(true);
			mDlCheck = null;
		}
	}

	private void stopLoader() {
		if (mTask != null
				&& mTask.getStatus() != loadAllUpdates.Status.FINISHED) {
			mTask.cancel(true);
			mTask = null;
		}
	}

	class downloadRunning extends AsyncTask<String, Integer, String> {
		protected String doInBackground(String... arg0) {

			ACTIVE_DOWNLOAD = true;
			while (ACTIVE_DOWNLOAD) {
				idDl = prefs.getLong(C.PREF_CURRENT_DOWNLOAD, 0);
				if (idDl != 0) {
					DownloadManager.Query q = new DownloadManager.Query();
					q.setFilterById(idDl);
					Cursor cursor = null;
					try {
						cursor = mgr.query(q);
						if (cursor.moveToFirst()) {
							if (ACTIVE_DOWNLOAD) {
								int tot = cursor
										.getInt(cursor
												.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
								int dl = cursor
										.getInt(cursor
												.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
								publishProgress(dl, tot);
							}
						}
					} finally {
						cursor.close();
					}
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}

		protected void onProgressUpdate(Integer... pro) {
			int a = (pro[0] / 1048576);
			int b = (pro[1] / 1048576);
			String proStr = "" + a + "/" + b + " M";
			int s = progress.size();
			progress.clear();
			for (int i = 0; i < s; i++) {
				progress.add(proStr);
			}
			mAdapter.notifyDataSetChanged();
		}

		protected void onPostExecute(final String string) {
			// TODO
		}
	}

	class loadAllUpdates extends AsyncTask<List<String>, String, String> {

		@Override
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected String doInBackground(List<String>... arg0) {
			String json = mUtils.getJsonUrl(mDevice);
			try {
				JSONObject jsonObject = new JSONObject(json);
				mArray = jsonObject.getJSONArray("updates");
				int success = jsonObject.getInt("success");
				switch (success) {
				case 1:
					for (int i = 0; i < mArray.length(); i++) {
						JSONObject c = mArray.getJSONObject(i);
						id.add(c.getString(C.ID));
						file.add(c.getString(C.FILE));
						md5sum.add(c.getString(C.MD5SUM));
						version.add("Version: " + c.getString(C.VERSION));
						type.add(c.getString(C.TYPE));
						changelog.add(c.getString(C.CHANGELOG));
						date.add(c.getString(C.DATE));
						progress.add("Progress: " + i);
					}
					break;
				case 0:
					return json;
				}
			} catch (NullPointerException e) {
				return null;
			} catch (JSONException e) {
				return null;
			}
			return json;
		}

		@Override
		protected void onPostExecute(final String string) {
			setProgressBarIndeterminateVisibility(false);
			if (string == null) {
				Intent in = new Intent(AllUpdatesActivity.this,
						SettingsActivity.class);
				Toast.makeText(AllUpdatesActivity.this,
						"Please setup a proper MrUpdater Url",
						Toast.LENGTH_LONG).show();
				startActivity(in);
				finish();
				return;
			}

			if (curVer != 0) {
				mRefreshItem.setVisible(true);
				mAdapter.notifyDataSetChanged();
				stopDownloadCheck();
				mDlCheck = (downloadRunning) new downloadRunning().execute();
				firstRun = false;
			} else {
				Toast.makeText(
						AllUpdatesActivity.this,
						"No mrupdater id property found, contact supporting rom Dev",
						Toast.LENGTH_LONG).show();
			}
		}

	}
}
