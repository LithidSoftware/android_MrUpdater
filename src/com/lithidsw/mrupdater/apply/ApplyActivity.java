package com.lithidsw.mrupdater.apply;

import com.lithidsw.mrupdater.C;
import com.lithidsw.mrupdaterheadless.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class ApplyActivity extends Activity {

	private SharedPreferences prefs;
	private String mUpdate;
	private String mId;

	Activity activity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.apply_activity);
		Intent in = getIntent();
		mUpdate = in.getStringExtra(C.EXTRA_URI_PATH);
		mId = in.getStringExtra(C.EXTRA_ID);
		prefs = getSharedPreferences(C.PREF, MODE_PRIVATE);
		String themeSty = prefs.getString(C.PREF_THEME, null);
		Intent intent;
		if ((themeSty == null) || "Dark".equals(themeSty)) {
			intent = new Intent(this, ApplyDark.class);
		} else {
			intent = new Intent(this, ApplyLight.class);
		}
		intent.putExtra(C.EXTRA_URI_PATH, mUpdate);
		intent.putExtra(C.EXTRA_ID, mId);
		startActivity(intent);
		finish();
	}

}
