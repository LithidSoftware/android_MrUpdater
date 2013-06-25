package com.lithidsw.mrupdater;

import java.util.ArrayList;
import java.util.List;

import com.lithidsw.mrupdater.apply.ApplyActivity;
import com.lithidsw.mrupdaterheadless.R;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Adapter extends BaseAdapter {

	SharedPreferences prefs;
	private DownloadedDataSource datasource;
	private DownloadManager mgr;

	private Activity activity;
	private List<String> id = new ArrayList<String>();
	private List<String> file = new ArrayList<String>();
	private List<String> md5sum = new ArrayList<String>();
	private List<String> version = new ArrayList<String>();
	private List<String> type = new ArrayList<String>();
	private List<String> changelog = new ArrayList<String>();
	private List<String> date = new ArrayList<String>();
	private List<String> progress = new ArrayList<String>();

	ImageButton btnDownload;
	ImageButton btnApply;
	ImageButton btnStop;

	Utils mUtils = new Utils();
	LoadChangelog mAlert;

	private static LayoutInflater inflater = null;

	View vi;

	Context context;

	public Adapter(Activity a, List<String> b, List<String> c, List<String> d, List<String> e,
			List<String> f, List<String> g, List<String> h, List<String> i) {
		activity = a;
		id = b;
		file = c;
		md5sum = d;
		version = e;
		type = f;
		changelog = g;
		date = h;
		progress = i;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		context = activity.getApplicationContext();
		mAlert = new LoadChangelog(activity);
	}

	@Override
	public int getCount() {
		try {
			return id.size();
		} catch (NullPointerException e) {
			return 0;
		}
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		vi = convertView;
		if (convertView == null)
			vi = inflater.inflate(R.layout.all_item, null);

		final String mId = id.get(position);
		final String mFile = file.get(position);
		final String mMd5sum = md5sum.get(position);
		final String mVersion = version.get(position);
		final String mType = type.get(position);
		final String mChangelog = changelog.get(position);
		final String mDate = date.get(position);
		final String mProgress = progress.get(position);

		prefs = activity.getSharedPreferences(C.PREF, Context.MODE_PRIVATE);
		String[] date = mDate.split("/");

		if (!"stable".equals(mType)) {
			LinearLayout mainLin = (LinearLayout) vi.findViewById(R.id.main_layout_item);
			mainLin.setBackgroundColor(getTypeColor(mType));
		}

		TextView txtday = (TextView) vi.findViewById(R.id.day);
		txtday.setText(date[0]);

		TextView txtmonth = (TextView) vi.findViewById(R.id.month);
		txtmonth.setText(mUtils.getMonthAbr(activity, date[1]));

		TextView txtid = (TextView) vi.findViewById(R.id.id);
		txtid.setText(mId);

		TextView txtfile = (TextView) vi.findViewById(R.id.file);
		txtfile.setText(mFile);

		TextView txtversion = (TextView) vi.findViewById(R.id.version);
		txtversion.setText(mVersion);

		TextView txtchangelog = (TextView) vi.findViewById(R.id.changelog);
		txtchangelog.setText(mChangelog);

		TextView txtdate = (TextView) vi.findViewById(R.id.date);
		txtdate.setText(mDate);

		ImageButton btnChangelog = (ImageButton) vi
				.findViewById(R.id.btn_changelog);
		btnChangelog.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mAlert.showChangeAlert(mChangelog);
			}
		});

		btnDownload = (ImageButton) vi.findViewById(R.id.btn_download);
		btnDownload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				long que = prefs.getLong(C.PREF_CURRENT_DOWNLOAD, 0);
				Log.e("MF", "" + que);
				if (que == 0) {
					new DL().Download(activity, activity, mFile, mId, mMd5sum);
				} else {
					Toast.makeText(context, "Download running, please wait...",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		btnApply = (ImageButton) vi.findViewById(R.id.btn_apply);
		btnApply.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent in = new Intent(activity, ApplyActivity.class);
				in.putExtra(
						C.EXTRA_URI_PATH,
						mFile.substring(mFile.lastIndexOf('/') + 1,
								mFile.length()));
				in.putExtra(C.EXTRA_ID, mId);
				activity.startActivity(in);
			}
		});

		btnStop = (ImageButton) vi.findViewById(R.id.btn_stop);
		btnStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				long que = prefs.getLong(C.PREF_CURRENT_DOWNLOAD, 0);
				mgr = (DownloadManager) activity
						.getSystemService(Context.DOWNLOAD_SERVICE);
				mgr.remove(que);
				prefs.edit().putLong(C.PREF_CURRENT_DOWNLOAD, 0).commit();
				try {
					datasource = new DownloadedDataSource(context);
					datasource.open();
					datasource.deleteItem(Integer.parseInt(mId));
				} finally {
					datasource.close();
				}
				notifyDataSetChanged();
			}
		});

		setCurrentBtn(mFile, mId, mVersion, mProgress, txtversion);

		return vi;
	}
	
	private int getTypeColor(String type) {
		TypedValue tv = new TypedValue();	
		if ("beta".equals(type)) {
			activity.getTheme().resolveAttribute(
					R.attr.color_beta, tv, true);	
		}
		if ("alpha".equals(type)) {
			activity.getTheme().resolveAttribute(
					R.attr.color_alpha, tv, true);	
		}
		return context.getResources().getColor(tv.resourceId);
	}

	private void setCurrentBtn(String url, String id, String version,
			String progress, TextView tv) {
		int thisId = Integer.parseInt(id);
		long que = prefs.getLong(C.PREF_CURRENT_DOWNLOAD, 0);

		boolean isSing = false;
		try {
			datasource = new DownloadedDataSource(
					activity.getApplicationContext());
			datasource.open();
			isSing = datasource.isSingleItem(thisId);
		} finally {
			datasource.close();
		}

		if (isSing) {
			boolean DL = false;

			String[] str;
			try {
				datasource = new DownloadedDataSource(activity);
				datasource.open();
				str = datasource.getItemsFromQueueId(que);
			} finally {
				datasource.close();
			}

			try {
				if (Integer.parseInt(str[0]) == Integer.parseInt(id)) {
					DL = true;
				}
			} catch (NumberFormatException e) {
				DL = false;
			}

			boolean check = mUtils.fileDownloaded(activity, url);
			if (check) {
				btnDownload.setVisibility(View.GONE);
				btnApply.setVisibility(View.VISIBLE);
				btnStop.setVisibility(View.GONE);
			} else {
				btnDownload.setVisibility(View.VISIBLE);
				btnApply.setVisibility(View.GONE);
				btnStop.setVisibility(View.GONE);
			}

			if (DL) {
				btnDownload.setVisibility(View.GONE);
				btnApply.setVisibility(View.GONE);
				btnStop.setVisibility(View.VISIBLE);
				tv.setText(version + " | " + progress);
			}
		} else {
			btnDownload.setVisibility(View.VISIBLE);
			btnApply.setVisibility(View.GONE);
			btnStop.setVisibility(View.GONE);
		}
		notifyDataSetChanged();
	}
}
