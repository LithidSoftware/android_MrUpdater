package com.lithidsw.mrupdater;

import com.lithidsw.mrupdaterheadless.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
	SharedPreferences prefs;
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	Utils mUtils = new Utils();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		prefs = getSharedPreferences(C.PREF, MODE_PRIVATE);
		String themeSty = prefs.getString(C.PREF_THEME, null);
		int page = prefs.getInt(C.PREF_GET_PAGE, 0);
		if (themeSty != null) {
			setTheme(getResources().getIdentifier(themeSty, "style", C.THIS));
		} else {
			setTheme(R.style.Dark);
		}
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		boolean owner = mUtils.isCurrentUserOwner(this);
		if (!owner && !prefs.getBoolean(C.PREF_ALLOW_NONOWNER, false)) {
			Toast.makeText(this,
					"You are not an owner!\nYou shouldn't be here. Closing.",
					Toast.LENGTH_LONG).show();
			finish();
		}
		setContentView(R.layout.activity_main);
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setCurrentItem(page);
		prefs.edit().putInt(C.PREF_GET_PAGE, 0).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_all_updates:
			startActivity(new Intent(this, AllUpdatesActivity.class));
			return true;
		case R.id.action_settings:
			prefs.edit().putInt(C.PREF_GET_PAGE, mViewPager.getCurrentItem())
					.commit();
			prefs.edit()
					.putString(C.PREF_CUR_THEME,
							prefs.getString(C.PREF_THEME, null)).commit();
			startActivity(new Intent(this, SettingsActivity.class));
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mViewPager.getCurrentItem() == 1) {
				mViewPager.setCurrentItem(0, true);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			switch (position) {
			case 0:
				fragment = new UpdatesFrag();
				break;
			case 1:
				fragment = new AboutFrag();
				break;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getApplicationContext().getString(R.string.updates);
			case 1:
				return getApplicationContext().getString(R.string.about);
			}
			return null;
		}
	}

}
