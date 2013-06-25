package com.lithidsw.mrupdater;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocalDBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "local_updates.db";
	private static final int DATABASE_VERSION = 1;

	/** Downloaded databases for downloads **/
	public static final String TABLE_DOWNLOADED = "downloaded";
	public static final String C_ID = C.ID;
	public static final String C_DL_ID = C.DL_ID;
	public static final String C_PATH = C.PATH;
	public static final String C_MD5SUM = C.MD5SUM;

	private static final String DATABASE_CREATE_DOWNLOADED = "create table "
			+ TABLE_DOWNLOADED + "(" + C_ID + " integer not null, " + C_DL_ID
			+ " long not null, " + C_PATH + " text not null, "
			+ C_MD5SUM + " text not null);";

	public LocalDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE_DOWNLOADED);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOWNLOADED);
		onCreate(db);
	}

}
