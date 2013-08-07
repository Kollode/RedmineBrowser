package de.kollode.redminebrowser;

import java.util.ArrayList;
import java.util.Arrays;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import de.kollode.redminebrowser.adapter.ActionBarSpinnerAdapter;
import de.kollode.redminebrowser.authentication.RedmineAuthenticator;
import de.kollode.redminebrowser.provider.RedmineTables;
import de.kollode.redminebrowser.provider.RedmineTables.Project;

/**
 * A list fragment representing a list of Projects. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link ProjectDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 * @param <D>
 */
public class ProjectListFragment extends ListFragment implements OnNavigationListener, LoaderCallbacks<Cursor> {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;
	
	private SimpleCursorAdapter mAdapter;
	private Account selectedAccount;
	private ArrayList<Account> redmineAccounts;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ProjectListFragment() {
	}

	@Override 
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        if (getActivity().findViewById(R.id.project_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			setActivateOnItemClick(true);
		}
        
        AccountManager acMgr = AccountManager.get(getActivity().getApplicationContext());
		redmineAccounts = new ArrayList<Account>(Arrays.asList(acMgr.getAccountsByType(RedmineAuthenticator.REDMINE_ACCOUNT_TYPE)));

		// No Accounts found, let the user create a new one
		if (redmineAccounts.size() == 0) {
			startActivityForResult(
					new Intent(Settings.ACTION_ADD_ACCOUNT).putExtra(Settings.EXTRA_AUTHORITIES, new String[] { RedmineTables.CONTENT_AUTHORITY }), 0);
		} else {
			this.createAccountSelect(redmineAccounts);
			this.fillListForSelectedAccount();
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		redmineAccounts = new ArrayList<Account>(Arrays.asList(AccountManager.get(getActivity().getApplicationContext()).getAccountsByType(
				RedmineAuthenticator.REDMINE_ACCOUNT_TYPE)));
		if (redmineAccounts.size() > 0) {
			// If the only account is an account that can't use Calendar we let
			// the user into
			// Calendar, but they can't create any events until they add an
			// account with a
			// Calendar.
			this.createAccountSelect(redmineAccounts);
			this.fillListForSelectedAccount();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		AccountManager acMgr = AccountManager.get(getActivity().getApplicationContext());
		redmineAccounts = new ArrayList<Account>(Arrays.asList(acMgr.getAccountsByType(RedmineAuthenticator.REDMINE_ACCOUNT_TYPE)));

		// No Accounts found, let the user create a new one
		if (redmineAccounts.size() == 0) {
			startActivityForResult(
					new Intent(Settings.ACTION_ADD_ACCOUNT).putExtra(Settings.EXTRA_AUTHORITIES, new String[] { RedmineTables.CONTENT_AUTHORITY }), 0);
		} else {
			this.createAccountSelect(redmineAccounts);
			this.fillListForSelectedAccount();
		}
	}

	public void fillListForSelectedAccount() {

		//Create dummy Cursor
		mAdapter = new SimpleCursorAdapter(getActivity().getApplicationContext(), R.layout.issue_list_row, null, new String[] { Project.NAME },
				new int[] { R.id.name_entry }, 0);

		setListAdapter(mAdapter);
		
		//Prepare to load the cursor with the Provider
		getLoaderManager().initLoader(1, null, this);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	private void createAccountSelect(ArrayList<Account> redmineAccounts) {

		if (redmineAccounts.size() > 1) {
			ActionBarSpinnerAdapter mSpinnerAdapter = new ActionBarSpinnerAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, redmineAccounts);

			ActionBar actionBar = ((ProjectListActivity)getActivity()).getActionBar();
			actionBar.setDisplayShowTitleEnabled(true);
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			actionBar.setListNavigationCallbacks(mSpinnerAdapter, this);
		}

		selectedAccount = redmineAccounts.get(0);
	}
	
	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);

		Cursor c = ((SimpleCursorAdapter) listView.getAdapter()).getCursor();
		c.moveToPosition(position);
		String projectId = Long.toString(c.getLong(0));
		
		Bundle arguments = new Bundle();
		arguments.putString(ProjectDetailFragment.PROJECT_ID, projectId);
		arguments.putString(ProjectDetailFragment.ACCOUNT_NAME, selectedAccount.name);
		
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.		
			ProjectDetailFragment fragment = new ProjectDetailFragment();
			fragment.setArguments(arguments);
			getFragmentManager().beginTransaction().replace(R.id.project_detail_container, fragment).commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(getActivity(), ProjectDetailActivity.class);
			detailIntent.putExtras(arguments);
			startActivity(detailIntent);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}
	
	@Override
	public boolean onNavigationItemSelected(int position, long itemId) {
		Log.d("Spinner", "Choose Account");
		selectedAccount = redmineAccounts.get(position);

		getLoaderManager().destroyLoader(1);
		this.fillListForSelectedAccount();
		return true;
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Log.d("Loader", "Create Cursor URI: " + Project.buildProjectsUri(selectedAccount.name));
		CursorLoader loader = new CursorLoader(getActivity(), Project.buildProjectsUri(selectedAccount.name), null, null, null, null);

		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.d("Loader", "New Cursor Size: " + cursor.getCount());
		mAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
}
