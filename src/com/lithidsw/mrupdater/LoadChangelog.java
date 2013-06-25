package com.lithidsw.mrupdater;

import com.lithidsw.mrupdaterheadless.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class LoadChangelog {

	private loadChange mLoad;
	Utils mUtils = new Utils();
	Activity a;
	TextView message;
	Status statFin = loadChange.Status.FINISHED;

	public LoadChangelog(Activity act) {
		a = act;
	}

	public void showChangeAlert(String url) {
		TypedValue typedValue = new TypedValue();
		a.getTheme()
				.resolveAttribute(R.attr.action_changelog, typedValue, true);
		LayoutInflater li = LayoutInflater.from(a);
		View view = li.inflate(R.layout.changelog, null);
		message = (TextView) view.findViewById(R.id.changelog_body);
		mLoad = (loadChange) new loadChange().executeOnExecutor(
				AsyncTask.THREAD_POOL_EXECUTOR, url);
		new AlertDialog.Builder(a)
				.setTitle(a.getString(R.string.changelog))
				.setIcon(typedValue.resourceId)
				.setView(view)
				.setCancelable(false)
				.setNegativeButton("Close",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								stopIt();
							}
						}).show();
	}

	private void stopIt() {
		if (mLoad != null && mLoad.getStatus() != statFin) {
			mLoad.cancel(true);
			mLoad = null;
		}
	}

	class loadChange extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... arg0) {
			return mUtils.getNetFile(null, arg0[0], false);
		}

		@Override
		protected void onPostExecute(final String string) {
			message.setText(Html.fromHtml(string));
			message.setMovementMethod(LinkMovementMethod.getInstance());
		}

	}

}
