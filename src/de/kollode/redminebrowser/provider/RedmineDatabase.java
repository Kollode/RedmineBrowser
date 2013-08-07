/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.kollode.redminebrowser.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;
import de.kollode.redminebrowser.provider.RedmineTables.IssueColumns;
import de.kollode.redminebrowser.provider.RedmineTables.Membership;
import de.kollode.redminebrowser.provider.RedmineTables.ProjectColumns;
import de.kollode.redminebrowser.provider.RedmineTables.RedmineBaseColumns;
import de.kollode.redminebrowser.provider.RedmineTables.User;

/**
 * Helper for managing {@link SQLiteDatabase} that stores data for
 * {@link ScheduleProvider}.
 */
public class RedmineDatabase extends SQLiteOpenHelper {
	private static final String TAG = "RedmineDatabase";

	private static final String DATABASE_NAME = "redmine.db";

	private static final int VER_LAUNCH = 2;

	private static final int DATABASE_VERSION = 16;

	interface Tables {
		String USER = "user";
		String MEMBERSHIP = "membership";
		String ISSUE = "issue";
		String PROJECT = "project";
		String JOURNAL = "journal";
	}

	public RedmineDatabase(Context context, String databaseName) {
		super(context, databaseName + '_' + DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("DROP TABLE IF EXISTS " + Tables.USER);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.MEMBERSHIP);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.ISSUE);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.PROJECT);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.JOURNAL);

		db.execSQL("CREATE TABLE " + Tables.USER + " (" + BaseColumns._ID + " INTEGER," + User.FIRST_NAME + " TEXT," + User.LAST_NAME + " TEXT," + User.MAIL
				+ " TEXT," + "PRIMARY KEY(" + BaseColumns._ID + ")," + "UNIQUE (" + BaseColumns._ID + ") ON CONFLICT REPLACE)");

		db.execSQL("CREATE TABLE " + Tables.MEMBERSHIP + " (" + BaseColumns._ID + " INTEGER," + Membership.PROJECT_ID + " INTEGER," + "PRIMARY KEY("
				+ BaseColumns._ID + ")," + "UNIQUE (" + BaseColumns._ID + ") ON CONFLICT REPLACE)");

		db.execSQL("CREATE TABLE " + Tables.ISSUE + " (" + BaseColumns._ID + " INTEGER," + IssueColumns.PROJECT_ID + " INTEGER," + IssueColumns.TRACKER
				+ " TEXT," + IssueColumns.STATUS + " TEXT," + IssueColumns.PRIORITY + " INTEGER," + IssueColumns.AUTHOR + " TEXT," + IssueColumns.ASSIGNED_TO
				+ " TEXT," + IssueColumns.SUBJECT + " TEXT," + IssueColumns.DESCRIPTION + " TEXT," + IssueColumns.START_DATE + " TEXT," + IssueColumns.DUE_DATE
				+ " TEXT," + IssueColumns.DONE_RATIO + " INTEGER," + RedmineBaseColumns.UPDATED + " TEXT," + RedmineBaseColumns.CREATED + " TEXT,"
				+ "PRIMARY KEY(" + BaseColumns._ID + ")," + "UNIQUE (" + BaseColumns._ID + ") ON CONFLICT REPLACE)");

		db.execSQL("CREATE TABLE " + Tables.PROJECT + " (" + BaseColumns._ID + " INTEGER," + ProjectColumns.PARENT + " INTEGER," + ProjectColumns.NAME
				+ " TEXT," + ProjectColumns.IDENTIFIER + " TEXT," + RedmineBaseColumns.UPDATED + " TEXT," + RedmineBaseColumns.CREATED + " TEXT,"
				+ ProjectColumns.CREATED_ON + " TEXT," + ProjectColumns.UPDATED_ON + " TEXT," + "PRIMARY KEY(" + BaseColumns._ID + ")," + "UNIQUE ("
				+ BaseColumns._ID + ") ON CONFLICT REPLACE)");

		/*
		 * db.execSQL("CREATE TABLE " + Tables.SEARCH_SUGGEST + " (" +
		 * BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		 * SearchManager.SUGGEST_COLUMN_TEXT_1 + " TEXT NOT NULL)");
		 */
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);

		int version = oldVersion;

		switch (version) {
		case VER_LAUNCH:
			break;

		default:
			Log.w(TAG, "Destroying old data during upgrade");
			db.execSQL("DROP TABLE IF EXISTS " + Tables.USER);
			onCreate(db);
		}
	}

	public static void deleteDatabase(Context context) {
		context.deleteDatabase(DATABASE_NAME);
	}
}
