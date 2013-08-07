package de.kollode.redminebrowser.authentication;

import java.net.MalformedURLException;
import java.net.URL;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import de.kollode.redminebrowser.R;

public class RedmineAuthenticatorActivity extends AccountAuthenticatorActivity {

	protected static final String sTag = "RedmineAuthenticatorActivity";

	protected EditText mAccountName;
	protected EditText mApiKey;
	protected EditText mServerAddress;
	protected Button mCreate;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_authenticator);

		this.mCreate = (Button) findViewById(R.id.authenticator_account_add);

		this.mAccountName = (EditText) findViewById(R.id.authenticator_account_name);
		this.mAccountName.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (mAccountName.getText().length() == 0) {
					mAccountName.setError("You need a account name");
				}

				activateCreatButton();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		this.mApiKey = (EditText) findViewById(R.id.authenticator_account_apikey);
		this.mApiKey.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (mApiKey.getText().length() < 30) {
					mApiKey.setError("You need a valid API Key");
				}

				activateCreatButton();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		this.mServerAddress = (EditText) findViewById(R.id.authenticator_account_server);
		this.mServerAddress.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (mServerAddress.getText().length() == 0) {
					mServerAddress.setError("You need a account name");
				}

				activateCreatButton();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	protected void activateCreatButton() {
		if (this.mAccountName.getText().length() > 0 && this.mAccountName.getError() == null && this.mApiKey.getText().length() > 0
				&& this.mApiKey.getError() == null && this.mServerAddress.getText().length() > 0 && this.mServerAddress.getError() == null) {

			this.mCreate.setEnabled(true);
		} else {
			this.mCreate.setEnabled(false);
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		if (!this.connectedToInternet()) {

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.authenticator_account_error_internet).setCancelable(false)
					.setPositiveButton(R.string.authenticator_account_btn_yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					}).setNegativeButton(R.string.authenticator_account_btn_no, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							RedmineAuthenticatorActivity.this.finish();
						}
					});
			builder.create().show();
		}
	}

	public void onSafe(View v) {

	}

	/**
	 * Method that will be called if the create button is clicked
	 * 
	 * @param v
	 */
	public void createAccount(View v) {

		String accountName = this.mAccountName.getText().toString();
		String apiKey = this.mApiKey.getText().toString();
		String serverAddress = this.mServerAddress.getText().toString();
		String serverProtocol = ((CheckBox) findViewById(R.id.authenticator_account_usehttps)).isChecked() ? "https" : "http";
		URL serverUrl = null;

		try {
			serverUrl = new URL(serverProtocol, serverAddress, "");
			Log.d(RedmineAuthenticatorActivity.sTag, "Created server URL: " + serverUrl.toString());
		} catch (MalformedURLException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}

		if (this.connectedToInternet()) {
			Log.d(RedmineAuthenticatorActivity.sTag, "Validate server URL");
		}

		// Create the actual account

		AccountManager accMgr = AccountManager.get(this);

		// This is the magic that adds the account to the Android Account
		// Manager
		Account account = new Account(accountName, RedmineAuthenticator.REDMINE_ACCOUNT_TYPE);
		accMgr.addAccountExplicitly(account, apiKey, null);
		accMgr.setUserData(account, "serverUrl", serverUrl.toString());
		// Set SyncAdapter
		ContentResolver.setIsSyncable(account, "de.kollode.redminebrowser.provider", 1);
		ContentResolver.setSyncAutomatically(account, "de.kollode.redminebrowser.provider", false);

		Bundle params = new Bundle();
		params.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, false);
		params.putBoolean(ContentResolver.SYNC_EXTRAS_DO_NOT_RETRY, false);
		params.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false);
		ContentResolver.addPeriodicSync(account, "de.kollode.redminebrowser.provider", params, 60*60);
	
		Intent intent = new Intent();
		intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, accountName);
		intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, RedmineAuthenticator.REDMINE_ACCOUNT_TYPE);
		this.setAccountAuthenticatorResult(intent.getExtras());
		this.setResult(RESULT_OK, intent);
		this.finish();
	}

	protected Boolean connectedToInternet() {
		// Test if we have a internet connection
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		if (cm.getActiveNetworkInfo() == null || !cm.getActiveNetworkInfo().isConnected()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * If the user clicks on the cancel button
	 * 
	 * @param v
	 */
	public void onCancel(View v) {

		this.finish();
	}
}
