package com.lithidsw.mrupdater;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class UpdateChecker {
	
	private JSONArray mArray;
	
	private String mRom;
	private String mId;
	private String mVersion;
	
	private String mDevice;
	private int curId;
	
	Context context;
	private Utils mUtils = new Utils();

	public UpdateChecker(Context c) {
		context = c;
	}
	
	public void runUpdateCheck() {
		new checkUpdates().execute();
	}
	
	private void showToast(String string) {
		Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
	}
	
	class checkUpdates extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... arg0) {
			mDevice = mUtils.getPropString("ro.product.device");
			curId = mUtils.getPropInt("ro.mrupdater.id");
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
						mVersion = c.getString(C.VERSION);
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
			if (string != null) {
				if (curId != 0) {
					if (Integer.parseInt(mId) > curId) {
						mUtils.showUpdateNoti(context, mVersion, mRom);
					}
				} else {
					showToast("No mrupdater id property found, contact supporting rom Dev");
				}	
			}
		}
		
	}
}
