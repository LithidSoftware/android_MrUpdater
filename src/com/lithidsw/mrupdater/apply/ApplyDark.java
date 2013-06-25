package com.lithidsw.mrupdater.apply;

import com.lithidsw.mrupdater.C;
import com.lithidsw.mrupdater.DownloadedDataSource;
import com.lithidsw.mrupdater.Utils;
import com.lithidsw.mrupdaterheadless.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ApplyDark extends Activity {

	private DownloadedDataSource datasource;
	private TextView message;

	private String mUpdate;
	private String mId;

	Activity activity;

	private Utils mUtils = new Utils();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.apply_activity);
		activity = this;
		Intent in = getIntent();
		mUpdate = in.getStringExtra(C.EXTRA_URI_PATH);
		mId = in.getStringExtra(C.EXTRA_ID);
		LayoutInflater li = LayoutInflater.from(this);
		View view = li.inflate(R.layout.apply_update, null);
		message = (TextView) view.findViewById(R.id.update_file);
		message.setText(mUpdate);
		new AlertDialog.Builder(this)
				.setTitle(this.getString(R.string.apply))
				.setIcon(R.drawable.ic_launcher)
				.setView(view)
				.setCancelable(false)
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								finish();
							}
						})
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String run;
								try {
									datasource = new DownloadedDataSource(
											activity);
									datasource.open();
									run = datasource.getPath(mId);
								} finally {
									datasource.close();
								}
								if (run != null) {
									mUtils.writeCWM(activity, run);
									mUtils.writeTWRP(activity, run);
									mUtils.rebootRecovery(activity);
								} else {
									Toast.makeText(activity,
											"Can't apply update, try again",
											Toast.LENGTH_LONG).show();
								}
								finish();
							}
						}).show();
	}

}
