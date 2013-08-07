package de.kollode.redminebrowser.sync;

import java.io.IOException;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

	protected static final String sTag = "RedmineSyncAdapter";
	private final Context mContext;
	private SyncHelper mSyncHelper;

	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		mContext = context;
	}

	public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
		super(context, autoInitialize, allowParallelSyncs);
		mContext = context;
	}
	
	@Override
	public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
		Log.i(sTag, "Beginning Redmine sync: " + account.name);

		// Perform a sync using SyncHelper
		if (mSyncHelper == null) {
			mSyncHelper = new SyncHelper(mContext);
		}

		try {
			mSyncHelper.performSync(syncResult, account);
		} catch (IOException e) {
			++syncResult.stats.numIoExceptions;
			Log.e(SyncAdapter.sTag, "Error syncing data for Redmine.", e);
		}
	}

	@Override
	public void onSyncCanceled() {
		super.onSyncCanceled();
		
		Log.d(sTag, "Cancel Sync");
		
		if(mSyncHelper != null) {
			mSyncHelper.setCanceled(true);
		}
	}
}
