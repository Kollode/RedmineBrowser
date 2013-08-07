package de.kollode.redminebrowser;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import de.kollode.redminebrowser.provider.RedmineTables.Issue;
import de.kollode.redminebrowser.provider.RedmineTables.Project;

/**
 * A fragment representing a single Project detail screen. This fragment is
 * either contained in a {@link ProjectListActivity} in two-pane mode (on
 * tablets) or a {@link ProjectDetailActivity} on handsets.
 */
public class ProjectDetailFragment extends Fragment {

	public static final String PROJECT_ID = "project_id";
	public static final String ACCOUNT_NAME = "account_name";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private String selectedProjectId;

	private String selectedAccountName;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ProjectDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(PROJECT_ID) && getArguments().containsKey(ACCOUNT_NAME)) {
			selectedProjectId = getArguments().getString(PROJECT_ID);
			selectedAccountName = getArguments().getString(ACCOUNT_NAME);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_project_detail, container, false);

		// Show the dummy content as text in a TextView.
		if (selectedProjectId != null && selectedAccountName != null) {
			Log.d("ProjectDetailFragment", selectedProjectId + "@" + selectedAccountName);
			final ContentResolver resolver = this.getActivity().getContentResolver();
			Cursor issues = resolver.query(Issue.buildIssuesUri(selectedAccountName), null, Issue.PROJECT_ID + "=?", new String[] { selectedProjectId }, Issue.UPDATED);
			issues.setNotificationUri(resolver, Issue.CONTENT_URI);
			
			SimpleCursorAdapter mCursorAdapter = new SimpleCursorAdapter(this.getActivity().getApplicationContext(), R.layout.issue_list_row, issues,
					new String[] { Issue.SUBJECT }, new int[] { R.id.name_entry }, 0);

			((ListView) rootView.findViewById(R.id.project_detail)).setAdapter(mCursorAdapter);
		}

		return rootView;
	}
}
