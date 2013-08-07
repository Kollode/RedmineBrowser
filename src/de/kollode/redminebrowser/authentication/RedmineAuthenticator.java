package de.kollode.redminebrowser.authentication;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class RedmineAuthenticator extends AbstractAccountAuthenticator {
	// Constants
	public static final String REDMINE_ACCOUNT_TYPE = "de.kollode.redminebrowser";

	private Context authenticatorService = null;

	public RedmineAuthenticator(Context context) {
		super(context);
		this.authenticatorService = context;
	}

	/*
	 * The user has requested to add a new account to the system. We return an
	 * intent that will launch our login screen if the user has not logged in
	 * yet, otherwise our activity will just pass the user's credentials on to
	 * the account manager.
	 */
	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options)
			throws NetworkErrorException {

		if (authTokenType == null) {
			authTokenType = "api";
		}

		Bundle reply = new Bundle();

		Intent intent = new Intent(this.authenticatorService, RedmineAuthenticatorActivity.class);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

		reply.putParcelable(AccountManager.KEY_INTENT, intent);
		return reply;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAuthTokenLabel(String authTokenType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}

}
