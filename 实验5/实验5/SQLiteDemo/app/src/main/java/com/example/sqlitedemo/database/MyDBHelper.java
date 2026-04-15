package com.example.sqlitedemo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import com.example.sqlitedemo.bean.UserInfo;

public class MyDBHelper extends SQLiteOpenHelper {
	private static final String TAG = "MyDBHelper";
	private static final String DB_NAME = "mydb.db";
	private static final int DB_VERSION = 1;
	private static MyDBHelper mHelper = null;
	private SQLiteDatabase mDB = null;
	private static final String TABLE_NAME = "user_info";

	private MyDBHelper(Context context) {
		super(context, DB_NAME, null,DB_VERSION);
	}

	//使用单例模式创建DBHelper
	public static MyDBHelper getInstance(Context context, int version) {
		if (mHelper == null) {
			synchronized (MyDBHelper.class) {
				mHelper = new MyDBHelper(context);
			}
		}
		return mHelper;
	}

	public SQLiteDatabase openReadLink() {
		if (mDB == null || mDB.isOpen() != true) {
			//创建或打开一个只读数据库
			mDB = mHelper.getReadableDatabase();
		}
		return mDB;
	}

	public SQLiteDatabase openWriteLink() {
		if (mDB == null || mDB.isOpen() != true) {
			//创建或打开一个读写数据库
			mDB = mHelper.getWritableDatabase();
		}
		return mDB;
	}

	public void closeLink() {
		if (mDB != null && mDB.isOpen() == true) {
			mDB.close();
			mDB = null;
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "onCreate");
		String drop_sql = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
		Log.d(TAG, "drop_sql:" + drop_sql);
		db.execSQL(drop_sql);
		String create_sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
				+ "u_id INTEGER PRIMARY KEY  AUTOINCREMENT NOT NULL,"
				+ "u_name VARCHAR NOT NULL,"
				+ "u_age INTEGER NOT NULL,"
				+ "u_sex INTEGER NOT NULL,"
				+ "buy_credits LONG NOT NULL,"
				+ "buy_totals FLOAT NOT NULL,"
				+ "update_time VARCHAR NOT NULL,"
				+ "u_phone VARCHAR,"
				+ "u_password VARCHAR"
				+ ");";
		Log.d(TAG, "create_sql:" + create_sql);
		db.execSQL(create_sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public int delete(String whereClause,String[] whereArgs) {
		int count = mDB.delete(TABLE_NAME, whereClause, whereArgs);
		return count;
	}

	public long insert(UserInfo info) {
		ArrayList<UserInfo> infoArray = new ArrayList<UserInfo>();
		infoArray.add(info);
		return insert(infoArray);
	}
	
	public long insert(ArrayList<UserInfo> infoArray) {
		long result = -1;
		for (int i = 0; i < infoArray.size(); i++) {
			UserInfo info = infoArray.get(i);
			ArrayList<UserInfo> tempArray = new ArrayList<UserInfo>();
			// 如果存在同名记录，则更新记录
			if (info.name!=null && info.name.length()>0) {
				String condition = String.format("u_name='%s'", info.name);
				tempArray = query(condition);
				if (tempArray.size() > 0) {
					update(info, condition);
					result = tempArray.get(0).userid;
					continue;
				}
			}
			// 不存在唯一性重复的记录，则插入新记录
			ContentValues cv = new ContentValues();
			cv.put("u_name", info.name);
			cv.put("u_age", info.age);
			cv.put("u_sex", info.sex);
			cv.put("buy_credits", info.credits);
			cv.put("buy_totals", info.totals);
			cv.put("update_time", info.update_time);
			cv.put("u_phone", info.phone);
			cv.put("u_password", info.password);
			result = mDB.insert(TABLE_NAME, "", cv);
			// 添加成功后返回行号，失败后返回-1
			if (result == -1) {
				return result;
			}
		}
		return result;
	}

	public int update(UserInfo info, String condition) {
		ContentValues cv = new ContentValues();
		cv.put("u_name", info.name);
		cv.put("u_age", info.age);
		cv.put("u_sex", info.sex);
		cv.put("buy_credits", info.credits);
		cv.put("buy_totals", info.totals);
		cv.put("update_time", info.update_time);
		cv.put("u_phone", info.phone);
		cv.put("u_password", info.password);
		int count = mDB.update(TABLE_NAME, cv, condition, null);
		return count;
	}

	public ArrayList<UserInfo> query(String condition) {
		String sql = String.format("select u_id,u_name,u_age,u_sex,buy_credits,buy_totals,update_time," +
				"u_phone,u_password from %s where %s;", TABLE_NAME, condition);
		Log.d(TAG, "query sql: "+sql);
		ArrayList<UserInfo> infoArray = new ArrayList<UserInfo>();
		Cursor cursor = mDB.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			for (;; cursor.moveToNext()) {
				UserInfo info = new UserInfo();
				info.userid = cursor.getInt(0);
				info.name = cursor.getString(1);
				info.age = cursor.getInt(2);
				//SQLite没有布尔型，用0表示false，用1表示true
				info.sex =  (cursor.getInt(3)==0)?false:true;
				info.credits=cursor.getLong(4);
				info.totals = cursor.getFloat(5);
				info.update_time = cursor.getString(6);
				info.phone = cursor.getString(7);
				info.password = cursor.getString(8);

				infoArray.add(info);
				if (cursor.isLast() == true) {
					break;
				}
			}
		}
		cursor.close();
		return infoArray;
	}
}
