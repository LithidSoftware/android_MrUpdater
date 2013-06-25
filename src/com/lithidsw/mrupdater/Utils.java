package com.lithidsw.mrupdater;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.lithidsw.mrupdater.apply.ApplyActivity;
import com.lithidsw.mrupdaterheadless.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.UserManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

public class Utils {

	public void rebootRecovery(Activity a) {
		try {
			Runtime.getRuntime().exec(
					new String[] { "su", "-c", "reboot recovery" });
		} catch (IOException e) {
			Toast.makeText(a, "Couldn't reboot into recovery",
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	public void writeCWM(Activity a, String path) {
		final String update = getSdcardMnt(a, path);
		try {
			Process p = Runtime.getRuntime().exec("su");
			OutputStream os = p.getOutputStream();
			os.write("mkdir -p /cache/recovery/\n".getBytes());
			os.write("echo 'boot-recovery' >/cache/recovery/command\n"
					.getBytes());
			String cmd = "echo '--update_package=" + update
					+ "' >> /cache/recovery/command\n";
			os.write(cmd.getBytes());
			String x = "cp "+ path +" "+ update + "\n";
			os.write(x.getBytes());
			os.flush();

		} catch (IOException e) {
		}
	}

	public void writeTWRP(Activity a, String path) {
		final String update = getSdcardMnt(a, path);
		try {
			Process p = Runtime.getRuntime().exec("su");
			OutputStream os = p.getOutputStream();
			os.write("mkdir -p /cache/recovery/\n".getBytes());		
			String cmd = "echo 'install " + update
					+ "' > /cache/recovery/openrecoveryscript\n";
			os.write(cmd.getBytes());
			os.write("echo 'wipe dalvik' >/cache/recovery/openrecoveryscript\n".getBytes());
			String x = "cp "+ path +" "+ update + "\n";
			os.write(x.getBytes());
			os.flush();

		} catch (IOException e) {
		}
	}

	public boolean checkMD5(String md5, File updateFile) {
		if (md5 == null || md5.equals("") || updateFile == null) {
			return false;
		}

		String calculatedDigest = calculateMD5(updateFile);
		if (calculatedDigest == null) {
			return false;
		}

		return calculatedDigest.equalsIgnoreCase(md5);
	}

	public static String calculateMD5(File updateFile) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			return null;
		}

		InputStream is;
		try {
			is = new FileInputStream(updateFile);
		} catch (FileNotFoundException e) {
			return null;
		}

		byte[] buffer = new byte[8192];
		int read;
		try {
			while ((read = is.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}
			byte[] md5sum = digest.digest();
			BigInteger bigInt = new BigInteger(1, md5sum);
			String output = bigInt.toString(16);
			// Fill to 32 chars
			output = String.format("%32s", output).replace(' ', '0');
			return output;
		} catch (IOException e) {
			throw new RuntimeException("Unable to process file for MD5", e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
	}

	public String getMonthAbr(Activity a, String month) {
		String[] m = a.getResources().getStringArray(R.array.abr_months);
		int i = Integer.parseInt(month);
		return m[i - 1];
	}

	public String getPropString(String prop) {
		String line = "";
		Process ifc = null;
		try {
			ifc = Runtime.getRuntime().exec("getprop " + prop);
			BufferedReader bis = new BufferedReader(new InputStreamReader(
					ifc.getInputStream()));
			line = bis.readLine();
		} catch (java.io.IOException e) {
		}
		ifc.destroy();
		return line;
	}

	public int getPropInt(String prop) {
		String line = "";
		Process ifc = null;
		try {
			ifc = Runtime.getRuntime().exec("getprop " + prop);
			BufferedReader bis = new BufferedReader(new InputStreamReader(
					ifc.getInputStream()));
			line = bis.readLine();
		} catch (java.io.IOException e) {
		}
		ifc.destroy();
		int num;
		try {
			num = Integer.parseInt(line);
		} catch (NumberFormatException e) {
			num = 0;
		}
		return num;
	}

	public CharSequence upperFirst(CharSequence s) {
		if (s.length() == 0) {
			return s;
		} else {
			return Character.toUpperCase(s.charAt(0))
					+ s.subSequence(1, s.length()).toString();
		}
	}

	public void removeDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				new File(dir, children[i]).delete();
			}
		}
	}

	public boolean fileDownloaded(Activity a, String file) {
		try {
			String name = file.substring(file.lastIndexOf('/') + 1,
					file.length());
			File f = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ "/"
					+ a.getApplicationContext().getString(R.string.app_name)
					+ "/" + name);
			return f.exists();
		} catch (NullPointerException e) {
			return false;
		}
	}

	@SuppressLint("NewApi")
	public boolean isCurrentUserOwner(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			try {
				Method getUserHandle = UserManager.class
						.getMethod("getUserHandle");
				int userHandle = (Integer) getUserHandle.invoke(context
						.getSystemService(Context.USER_SERVICE));
				return userHandle == 0;
			} catch (Exception e) {
				return false;
			}
		}
		return true;
	}

	@SuppressLint("SdCardPath")
	public String getSdcardMnt(Activity a, String path) {
		String name = path.substring(path.lastIndexOf('/') + 1, path.length());
		return "/cache/recovery/" + name;
	}

	public void showChangelogAlert(Activity a, String url) {
		// String change = getNetFile(null, url, false);
		TypedValue typedValue = new TypedValue();
		a.getTheme()
				.resolveAttribute(R.attr.action_changelog, typedValue, true);
		LayoutInflater li = LayoutInflater.from(a);
		View view = li.inflate(R.layout.changelog, null);
		new AlertDialog.Builder(a)
				.setTitle(a.getString(R.string.changelog))
				.setIcon(typedValue.resourceId)
				.setView(view)
				.setNegativeButton("Close",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}

	public String getNetFile(String device, String url, boolean isJ) {
		DefaultHttpClient defaultClient = new DefaultHttpClient();
		String urlStr = null;
		if (isJ) {
			urlStr = url+"/mrupdater-" + device + ".json";
		} else {
			urlStr = url;
		}
		HttpGet httpGetRequest = new HttpGet(urlStr);
		HttpResponse httpResponse = null;
		try {
			httpResponse = defaultClient.execute(httpGetRequest);
		} catch (IllegalStateException e) {
			return null;
		} catch (IllegalArgumentException e) {
			return null;
		} catch (ClientProtocolException e) {
			return null;
		} catch (IOException e) {
			return null;
		}

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(httpResponse
					.getEntity().getContent(), "UTF-8"));
		} catch (NullPointerException e) {
			return null;
		} catch (UnsupportedEncodingException e) {
			return null;
		} catch (IllegalStateException e) {
			return null;
		} catch (IOException e) {
			return null;
		}

		String json = null;
		StringBuilder total = new StringBuilder();
		try {
			while ((json = reader.readLine()) != null) {
				if (json.equals("<!doctype html>")) {
					return null;
				}
				total.append(json + "\n");
			}
		} catch (NullPointerException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
		return total.toString();
	}
	
	public String getJsonUrl(String device) {
		String json;
		String url = getPropString("ro.mrupdater.url");
		json = getNetFile(device, url, true);
		if (json == null) {
			String backup = getPropString("ro.mrupdater.url.backup");
			json = getNetFile(device, backup, true);
			if (json == null) {
				String backup2 = getPropString("ro.mrupdater.url.backup2");
				json = getNetFile(device, backup2, true);
			}
		}
		return json;
	}
	
	public void showMd5ErrorNoti(Context context, String uri) {
		String name = uri.substring(uri.lastIndexOf('/') + 1, uri.length());
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(context.getString(R.string.app_name)+": "+"Error")
				.setContentText("Md5 didn't match: "+name);
		mBuilder.setAutoCancel(true);

		Intent resultIntent = new Intent();
		resultIntent.setAction("com.lithidsw.mrupdater.HOME");
		PendingIntent resultPendingIntent = PendingIntent.getBroadcast(context,
				0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);

		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(1, mBuilder.build());
	}
	
	public void showUpdateNoti(Context context, String uri, String rom) {
		String name = uri.substring(uri.lastIndexOf('/') + 1, uri.length());
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(context.getString(R.string.app_name)+": "+rom)
				.setContentText("Update is now available, version: "+name);
		mBuilder.setAutoCancel(true);

		Intent resultIntent = new Intent();
		resultIntent.setAction("com.lithidsw.mrupdater.HOME");
		PendingIntent resultPendingIntent = PendingIntent.getBroadcast(context,
				0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);

		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(1, mBuilder.build());
	}

	public Intent getApplyIntent(Context context, String string, int id) {
		String path = string.substring(string.lastIndexOf('/') + 1,
				string.length());
		Log.e("MR PENDINGINTENT", "Name: "+path+" Id: "+id);
		Intent intent = new Intent(context, ApplyActivity.class);
		intent.putExtra(C.EXTRA_URI_PATH, path);
		intent.putExtra(C.EXTRA_ID, ""+id);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		return intent;
	}
	
	public void setAlarms(Context c, String string) {
	    Calendar cal1 = Calendar.getInstance();
	    cal1.set(Calendar.HOUR_OF_DAY, 2);
	    cal1.set(Calendar.MINUTE, 0);
	    cal1.set(Calendar.SECOND, 0);
		Intent in1 = new Intent("com.lithidsw.mrupdater.CHECK_UPDATES");
		PendingIntent pi1 = PendingIntent.getBroadcast(c,C.CHECK_INTERVAL_ID_AM, in1, 0);
		AlarmManager am1 = (AlarmManager) c.getSystemService(Activity.ALARM_SERVICE);
		am1.setRepeating(AlarmManager.RTC_WAKEUP, cal1.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi1);
		if ("stop".equals(string)) {
			am1.cancel(pi1);
		}
		
	    Calendar cal2 = Calendar.getInstance();
	    cal2.set(Calendar.HOUR_OF_DAY, 14);
	    cal2.set(Calendar.MINUTE, 0);
	    cal2.set(Calendar.SECOND, 0);
		Intent in2 = new Intent("com.lithidsw.mrupdater.CHECK_UPDATES");
		PendingIntent pi2 = PendingIntent.getBroadcast(c,C.CHECK_INTERVAL_ID_PM, in2, 0);
		AlarmManager am2 = (AlarmManager) c.getSystemService(Activity.ALARM_SERVICE);
		am2.setRepeating(AlarmManager.RTC_WAKEUP, cal2.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi2);
		if ("stop".equals(string)) {
			am1.cancel(pi2);
		}
	}

}
