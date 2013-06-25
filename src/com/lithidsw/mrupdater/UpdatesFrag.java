package com.lithidsw.mrupdater;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.lithidsw.mrupdater.apply.ApplyActivity;
import com.lithidsw.mrupdaterheadless.R;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class UpdatesFrag extends Fragment {

	SharedPreferences prefs;

	private DownloadedDataSource datasource;

	private JSONArray mArray;
	Adapter mAdapter;

	private String mDevice;

	MenuItem mRefreshItem;

	TextView mTextRom;
	TextView mTextDevice;
	TextView mTextVersion;
	TextView mTextDate;
	TextView mTextDay;
	TextView mTextMonth;

	String mRom;
	String mId;
	String mFile;
	String mMd5sum;
	String mVersion;
	String mChangelog;
	String mDate;

	Button btnDownload;
	Button btnChangeLog;
	Button btnApply;
	Button btnStop;

	RelativeLayout rl;

	LinearLayout topLin;
	LinearLayout bottomLin;
	TextView isUpdates;

	private loadAllUpdates mTask;
	private downloadRunning mDlCheck;
	private DownloadManager mgr;

	private long idDl;

	private FragmentActivity fa;

	Utils mUtils = new Utils();
	LoadChangelog mAlert;

	boolean firstRun = true;
	private boolean ACTIVE_DOWNLOAD;
	int curVer;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		fa = super.getActivity();
		mAlert = new LoadChangelog(fa);

		mgr = (DownloadManager) fa.getSystemService(Context.DOWNLOAD_SERVICE);

		prefs = fa.getSharedPreferences(C.PREF, Context.MODE_PRIVATE);

		rl = (RelativeLayout) inflater.inflate(R.layout.frag_updates,
				container, false);

		btnChangeLog = (Button) rl.findViewById(R.id.btn_changelog);
		btnChangeLog.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mAlert.showChangeAlert(mChangelog);
			}
		});

		btnDownload = (Button) rl.findViewById(R.id.btn_download);
		btnDownload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				new DL().Download(fa, fa, mFile, mId, mMd5sum);
				mDlCheck = (downloadRunning) new downloadRunning().execute();
			}
		});

		btnApply = (Button) rl.findViewById(R.id.btn_apply);
		btnApply.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent in = new Intent(fa, ApplyActivity.class);
				in.putExtra(
						C.EXTRA_URI_PATH,
						mFile.substring(mFile.lastIndexOf('/') + 1,
								mFile.length()));
				in.putExtra(C.EXTRA_ID, mId);
				startActivity(in);
			}
		});

		btnStop = (Button) rl.findViewById(R.id.btn_stop);
		btnStop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ACTIVE_DOWNLOAD = false;
				mgr.remove(idDl);
				prefs.edit().putLong(C.PREF_CURRENT_DOWNLOAD, 0).commit();
				try {
					datasource = new DownloadedDataSource(fa
							.getApplicationContext());
					datasource.open();
					datasource.deleteItem(Integer.parseInt(mId));
				} finally {
					datasource.close();
				}
			}
		});

		toggleUpdateView(false);
		mDevice = mUtils.getPropString("ro.product.device");
		curVer = mUtils.getPropInt("ro.mrupdater.id");
		setHasOptionsMenu(true);
		return rl;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		mRefreshItem = menu.getItem(0);
		if (firstRun) {
			mRefreshItem.setVisible(false);
			mTask = (loadAllUpdates) new loadAllUpdates().execute();
			firstRun = false;
		}
		// menu.getItem(1).setVisible(false);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			mRefreshItem.setVisible(false);
			stopLoader();
			mTask = (loadAllUpdates) new loadAllUpdates().execute();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void updateTextViews(String rom, String device, String version,
			String date, String url) {

		String[] d = date.split("/");

		mTextRom = (TextView) rl.findViewById(R.id.yes_updates_rom);
		mTextRom.setText(rom);

		mTextDevice = (TextView) rl.findViewById(R.id.yes_updates_device);
		mTextDevice.setText(device);

		mTextVersion = (TextView) rl.findViewById(R.id.yes_updates_version);
		mTextVersion.setText(version);

		mTextDay = (TextView) rl.findViewById(R.id.yes_updates_day);
		mTextDay.setText(d[0]);

		mTextMonth = (TextView) rl.findViewById(R.id.yes_updates_month);
		mTextMonth.setText(mUtils.getMonthAbr(fa, d[1]));

		setCurrentBtn(url);
	}

	private void setCurrentBtn(String url) {
		boolean check = mUtils.fileDownloaded(fa, url);
		if (check) {
			btnDownload.setVisibility(View.GONE);
			btnApply.setVisibility(View.VISIBLE);
		} else {
			btnDownload.setVisibility(View.VISIBLE);
			btnApply.setVisibility(View.GONE);
		}
	}

	private void toggleUpdateView(boolean updates) {
		topLin = (LinearLayout) rl.findViewById(R.id.top_btn_updates);
		bottomLin = (LinearLayout) rl.findViewById(R.id.bottom_btn_updates);
		isUpdates = (TextView) rl.findViewById(R.id.is_updates);
		if (updates) {
			isUpdates.setVisibility(View.GONE);
			topLin.setVisibility(View.VISIBLE);
			bottomLin.setVisibility(View.VISIBLE);
		} else {
			isUpdates.setText("Update not available");
			isUpdates.setVisibility(View.VISIBLE);
			topLin.setVisibility(View.GONE);
			bottomLin.setVisibility(View.GONE);
		}
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

	private void stopLoader() {
		if (mTask != null
				&& mTask.getStatus() != loadAllUpdates.Status.FINISHED) {
			mTask.cancel(true);
			mTask = null;
		}
	}

	private void stopDownloadCheck() {
		ACTIVE_DOWNLOAD = false;
		if (mDlCheck != null
				&& mDlCheck.getStatus() != downloadRunning.Status.FINISHED) {
			mDlCheck.cancel(true);
			mDlCheck = null;
		}
	}

	class downloadRunning extends AsyncTask<String, Integer, String> {
		protected String doInBackground(String... arg0) {
			ACTIVE_DOWNLOAD = true;
			while (ACTIVE_DOWNLOAD) {
				idDl = prefs.getLong(C.PREF_CURRENT_DOWNLOAD, 0);
				if (idDl == 0) {
					return null;
				}
				DownloadManager.Query q = new DownloadManager.Query();
				q.setFilterById(idDl);
				Cursor cursor = mgr.query(q);
				if (cursor.moveToFirst()) {
					int status = cursor.getInt(cursor
							.getColumnIndex(DownloadManager.COLUMN_STATUS));
					switch (status) {
					case DownloadManager.STATUS_PAUSED:
						break;
					case DownloadManager.STATUS_PENDING:
						break;
					case DownloadManager.STATUS_RUNNING:
						break;
					case DownloadManager.STATUS_SUCCESSFUL:
						ACTIVE_DOWNLOAD = false;
						break;
					case DownloadManager.STATUS_FAILED:
						ACTIVE_DOWNLOAD = false;
						break;
					}
					if (ACTIVE_DOWNLOAD) {
						int tot = cursor
								.getInt(cursor
										.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
						int dl = cursor
								.getInt(cursor
										.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
						publishProgress(dl, tot);
					}
				} else {
					ACTIVE_DOWNLOAD = false;
				}
				cursor.close();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}

		protected void onProgressUpdate(Integer... progress) {
			btnDownload.setVisibility(View.GONE);
			btnApply.setVisibility(View.GONE);
			btnStop.setVisibility(View.VISIBLE);
			mRefreshItem.setVisible(false);
			int a = (progress[0] / 1048576);
			int b = (progress[1] / 1048576);
			btnStop.setText("" + a + "/" + b + " M: " + "Stop");
		}

		protected void onPostExecute(final String string) {
			btnStop.setVisibility(View.GONE);
			mRefreshItem.setVisible(true);
			setCurrentBtn(mFile);
		}
	}

	class loadAllUpdates extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			fa.setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected String doInBackground(String... arg0) {
			String json = mUtils.getJsonUrl(mDevice);
			try {
				JSONObject jsonObject = new JSONObject(json);
				mArray = jsonObject.getJSONArray("updates");
				int success = jsonObject.getInt("success");
				mRom = jsonObject.get("rom_name").toString();
				switch (success) {
				case 1:
					for (int i = 0; i < mArray.length();) {
						JSONObject c = mArray.getJSONObject(i);
						mId = c.getString(C.ID);
						mFile = c.getString(C.FILE);
						mMd5sum = c.getString(C.MD5SUM);
						mVersion = c.getString(C.VERSION);
						mChangelog = c.getString(C.CHANGELOG);
						mDate = c.getString(C.DATE);
						return mId;
					}
					return json;
				case 0:
					return null;
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
			fa.setProgressBarIndeterminateVisibility(false);
			mRefreshItem.setVisible(true);
			if (string == null) {
				toggleUpdateView(false);
				Intent in = new Intent(fa, SettingsActivity.class);
				Toast.makeText(fa, "Please setup a proper MrUpdater Url",
						Toast.LENGTH_LONG).show();
				startActivity(in);
				return;
			}
			if (curVer != 0) {
				if (Integer.parseInt(string) > curVer) {
					updateTextViews(mRom, mDevice, mVersion, mDate, mFile);
					toggleUpdateView(true);
					stopDownloadCheck();
					mDlCheck = (downloadRunning) new downloadRunning()
							.execute();
				} else {
					toggleUpdateView(false);
				}
			} else {
				Toast.makeText(
						fa,
						"No mrupdater id property found, contact supporting rom Dev",
						Toast.LENGTH_LONG).show();
				toggleUpdateView(false);
			}
		}

	}
}
