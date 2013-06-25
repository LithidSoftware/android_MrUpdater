package com.lithidsw.mrupdater;

import com.lithidsw.mrupdaterheadless.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

public class AboutFrag extends Fragment {

	Button mBtnTwitter;
	Button mBtnGooglePlus;
	Button mBtnGmail;
	Button mBtnInfo;
	Button mBtnDonate;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		RelativeLayout rl = (RelativeLayout) inflater.inflate(
				R.layout.frag_about, container, false);

		mBtnTwitter = (Button) rl.findViewById(R.id.btn_twitter);
		mBtnTwitter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				openUrl("http://twitter.com/lithid");
			}
		});

		mBtnGooglePlus = (Button) rl.findViewById(R.id.btn_googleplus);
		mBtnGooglePlus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				openUrl("https://plus.google.com/u/0/103024643047948973176");
			}
		});

		mBtnGmail = (Button) rl.findViewById(R.id.btn_gmail);
		mBtnGmail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				openUrl("mailto:mrlithid@gmail.com");
			}
		});

		mBtnInfo = (Button) rl.findViewById(R.id.btn_info);
		mBtnInfo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				openUrl("http://mrlithid.com");
			}
		});

		mBtnDonate = (Button) rl.findViewById(R.id.btn_donate);
		mBtnDonate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				openUrl("http://goo.gl/511ca");
			}
		});

		setHasOptionsMenu(true);
		return rl;
	}

	private void openUrl(String url) {
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(i);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.getItem(0).setVisible(false);
		super.onCreateOptionsMenu(menu, inflater);
	}

}
