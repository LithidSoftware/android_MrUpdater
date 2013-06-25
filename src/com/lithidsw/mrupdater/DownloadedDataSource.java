package com.lithidsw.mrupdater;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DownloadedDataSource {

	// Database fields
	private SQLiteDatabase database;
	private LocalDBHelper dbHelper;
	private String TABLE = LocalDBHelper.TABLE_DOWNLOADED;

	public DownloadedDataSource(Context context) {
		dbHelper = new LocalDBHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public void createItem(int id, long dl, String path, String md5sum) {
		ContentValues values = new ContentValues();
		values.put(LocalDBHelper.C_ID, id);
		values.put(LocalDBHelper.C_DL_ID, dl);
		values.put(LocalDBHelper.C_PATH, path);
		values.put(LocalDBHelper.C_MD5SUM, md5sum);
		database.insert(TABLE, null, values);
	}

	public void deleteItem(int id) {
		database.delete(TABLE, LocalDBHelper.C_ID + " = " + id, null);
	}

	public boolean isSingleItem(int id) {
		Cursor all;
		all = database.rawQuery("select * from " + TABLE + " where "
				+ LocalDBHelper.C_ID + " = '" + id + "'", null);
		return all.moveToFirst();
	}

	public boolean isQueueItem(long queue) {
		Cursor all;
		all = database.rawQuery("select * from " + TABLE + " where "
				+ LocalDBHelper.C_DL_ID + " = '" + queue + "'", null);
		return all.moveToFirst();
	}

	public String getPath(String id) {
		String item = null;
		open();
		Cursor cur = database.rawQuery("select * from " + TABLE + " where "
				+ LocalDBHelper.C_ID + " = '" + id + "'", null);
		if (cur.moveToFirst()) {
			item = cur.getString(cur.getColumnIndex(LocalDBHelper.C_PATH));
		}
		close();
		return item;
	}

	public String[] getItemsFromQueueId(long queue) {
		String[] item = new String[3];
		String itemName = "";
		String itemUri = "";
		String itemMd5sum = "";
		open();
		Cursor cur = database.rawQuery("select * from " + TABLE + " where "
				+ LocalDBHelper.C_DL_ID + " = '" + queue + "'", null);
		if (cur.moveToFirst()) {
			itemName = cur.getString(cur.getColumnIndex(LocalDBHelper.C_ID));
			itemUri = cur.getString(cur.getColumnIndex(LocalDBHelper.C_PATH));
			itemMd5sum = cur.getString(cur.getColumnIndex(LocalDBHelper.C_MD5SUM));
		}
		close();
		item[0] = itemName;
		item[1] = itemUri;
		item[2] = itemMd5sum;
		return item;
	}

	public void cleanTable() {
		open();
		Cursor cur;
		cur = database.rawQuery("select * from " + TABLE, null);
		while (cur.moveToNext()) {
			File filePath = new File(cur.getString(cur
					.getColumnIndex(LocalDBHelper.C_PATH)));
			if (!filePath.exists()) {
				deleteItem(Integer.parseInt(cur.getString(cur
						.getColumnIndex(LocalDBHelper.C_ID))));
			}
		}
		cur.close();
		close();
	}

	public void deleteAllTableDownloaded() {
		open();
		database.delete(LocalDBHelper.TABLE_DOWNLOADED, null, null);
		close();
	}

}
