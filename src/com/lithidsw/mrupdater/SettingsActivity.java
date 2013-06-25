package com.lithidsw.mrupdater;

import java.io.File;

import com.lithidsw.mrupdaterheadless.R;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Toast;

public class SettingsActivity extends Activity {

	static SharedPreferences prefs;

	static EditTextPreference mEditUrl;
	static ListPreference mListTheme;
	static CheckBoxPreference mCheckInterval;
	
	static Preference prefTime1;
	static Preference prefTime2;

	static Context c;
	static DownloadedDataSource mDDSource;
	static Utils mUtils = new Utils();
	static Activity a;
	String curTheme = null;
	String newTheme = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		prefs = getSharedPreferences(C.PREF, MODE_PRIVATE);
		String themeSty = prefs.getString(C.PREF_THEME, null);
		if (themeSty != null) {
			setTheme(getResources().getIdentifier(themeSty, "style", C.THIS));
		} else {
			setTheme(R.style.Dark);
		}
		super.onCreate(savedInstanceState);
		a = this;
		c = getApplicationContext();
		prefs = getSharedPreferences(C.PREF, Context.MODE_PRIVATE);
		mDDSource = new DownloadedDataSource(c);
		curTheme = prefs.getString(C.PREF_CUR_THEME, null);
		setUpActionBar();

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new PrefsFragment()).commit();
	}

	private void setUpActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			newTheme = prefs.getString(C.PREF_THEME, null);
			if (newTheme != curTheme) {
				Intent intent = new Intent(this, MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			} else {
				finish();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private static void updateListTheme(String str) {
		String message;
		if ((str == "null") || (str.length() == 0)) {
			message = "Dark";
		} else {
			message = a.getString(R.string.pref_choose_theme_summary, str);
		}
		mListTheme.setSummary(message);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (newTheme != curTheme) {
				Intent intent = new Intent(this, MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			} else {
				finish();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public static class PrefsFragment extends PreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
			
			mCheckInterval = (CheckBoxPreference) findPreference(C.PREF_CHECK_INTERVAL);
			mCheckInterval.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference arg0, Object arg1) {
					if (arg1.equals(true)) {
						mUtils.setAlarms(c, "start");
					} else {
						mUtils.setAlarms(c, "stop");
					}
					return true;
				}
			});

			Preference prefDown = (Preference) findPreference(C.PREF_CLEAR_DOWNLOADED);
			prefDown.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					File dir = new File(Environment
							.getExternalStorageDirectory().getAbsolutePath()
							+ "/"
							+ a.getApplicationContext().getString(
									R.string.app_name));
					mDDSource.deleteAllTableDownloaded();
					mUtils.removeDir(dir);
					prefs.edit().putLong(C.PREF_CURRENT_DOWNLOAD, 0).commit();
					CharSequence cha = c.getString(R.string.downloaded_removed);
					Toast.makeText(c, cha, Toast.LENGTH_SHORT).show();
					return false;
				}
			});

			mListTheme = (ListPreference) findPreference(C.PREF_THEME);
			mListTheme
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
						@Override
						public boolean onPreferenceChange(Preference arg0,
								Object arg1) {
							String str = arg1.toString();
							prefs.edit().putString(C.PREF_THEME, str).commit();
							Intent intent = a.getIntent();
							intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
							a.finish();
							startActivity(intent);
							return false;
						}
					});
			updateListTheme(prefs.getString(C.PREF_THEME, "null"));
		}
	}
}
