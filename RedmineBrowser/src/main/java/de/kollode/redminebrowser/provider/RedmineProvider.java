package de.kollode.redminebrowser.provider;

import java.util.Arrays;
import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import de.kollode.redminebrowser.provider.RedmineDatabase.Tables;
import de.kollode.redminebrowser.provider.RedmineTables.Issue;
import de.kollode.redminebrowser.provider.RedmineTables.Project;
import de.kollode.redminebrowser.provider.RedmineTables.ProjectColumns;
import de.kollode.redminebrowser.provider.RedmineTables.RedmineBaseColumns;

public class RedmineProvider extends ContentProvider {

	private static final String TAG = "RedmineProvider";
	private static final UriMatcher sUriMatcher = buildUriMatcher();

	private static final int ISSUES = 100;
	private static final int ISSUES_ID = 101;
	private static final int PROJECTS = 200;
	private static final int PROJECT_ID = 201;

	private static HashMap<String, RedmineDatabase> mDbHelper = new HashMap<String, RedmineDatabase>();

	protected RedmineDatabase getDatabaseForAccount(String accountName) {

		if (!mDbHelper.containsKey(accountName)) {
			mDbHelper.put(accountName, new RedmineDatabase(getContext(), accountName));
		}

		return mDbHelper.get(accountName);
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.v(TAG, "insert(uri=" + uri + " - " + uri.getQueryParameter("accountName") + ")");

		final SQLiteDatabase db = this.getDatabaseForAccount(uri.getQueryParameter("accountName")).getWritableDatabase();
		
		final int match = sUriMatcher.match(uri);
		values.remove("accountName");
		Log.v(TAG, "Matched: " + match);
		switch (match) {
		case ISSUES: {

			db.replaceOrThrow(Tables.ISSUE, null, values);
			getContext().getContentResolver().notifyChange(uri, null);
			// syncToNetwork);
			return Issue.buildBlockUri(values.getAsString(BaseColumns._ID));
		}

		case PROJECTS: {

			db.replaceOrThrow(Tables.PROJECT, null, values);
			getContext().getContentResolver().notifyChange(uri, null);
			// syncToNetwork);
			return Project.buildProjectUri(values.getAsString(BaseColumns._ID));
		}

		default: {
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		Log.v(TAG, "query(uri=" + uri + ", proj=" + Arrays.toString(projection) + ", selection=" + selection + ")");

		if (selectionArgs != null) {
			Log.v(TAG, selectionArgs.toString());
		}

		final SQLiteDatabase db = this.getDatabaseForAccount(uri.getQueryParameter("accountName")).getWritableDatabase();
		final int match = sUriMatcher.match(uri);
		Cursor cursor = null;

		switch (match) {
			case ISSUES: {
				cursor = db.query(Tables.ISSUE, null, selection, selectionArgs, null, null, sortOrder, null);
				cursor.setNotificationUri(getContext().getContentResolver(), uri);
				break;
			}
	
			case PROJECTS: {
				cursor = db.query(Tables.PROJECT, null, selection, selectionArgs, null, null, ProjectColumns.NAME, null);
				cursor.setNotificationUri(getContext().getContentResolver(), uri);
				break;
			}
	
			default: {
				throw new UnsupportedOperationException("Unknown uri: " + uri);
			}
		}

		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public String getType(Uri uri) {
		final int match = sUriMatcher.match(uri);
		switch (match) {
		case ISSUES:
			return RedmineBaseColumns.CONTENT_TYPE;
		case ISSUES_ID:
			return RedmineBaseColumns.CONTENT_ITEM_TYPE;
		case PROJECTS:
			return RedmineBaseColumns.CONTENT_TYPE;
		case PROJECT_ID:
			return RedmineBaseColumns.CONTENT_ITEM_TYPE;
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	private static UriMatcher buildUriMatcher() {
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = RedmineTables.CONTENT_AUTHORITY;

		matcher.addURI(authority, "issues", ISSUES);
		matcher.addURI(authority, "issues/*", ISSUES_ID);
		matcher.addURI(authority, "projects", PROJECTS);
		matcher.addURI(authority, "projects/*", PROJECT_ID);

		return matcher;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return true;
	}
}
