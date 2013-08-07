package de.kollode.redminebrowser.authentication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class RedmineAuthenticatorService extends Service {

	private static RedmineAuthenticator redmineAuthenticator = null;

	@Override
	public IBinder onBind(Intent intent) {
		IBinder ret = null;
		if (intent.getAction().equals(android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT)) {
			ret = getAuthenticator().getIBinder();
		}
		return ret;
	}

	/**
	 * Get the RedmineAuthenticator implementation. Will create the object if
	 * not available.
	 * 
	 * @return RedmineAutheticator
	 * 
	 */
	private RedmineAuthenticator getAuthenticator() {
		if (redmineAuthenticator == null)
			redmineAuthenticator = new RedmineAuthenticator(this);
		return redmineAuthenticator;
	}

}
