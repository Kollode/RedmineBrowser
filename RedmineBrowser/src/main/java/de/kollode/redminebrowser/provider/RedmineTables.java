package de.kollode.redminebrowser.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class RedmineTables {

	public static final String CONTENT_AUTHORITY = "de.kollode.redminebrowser.provider";
	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	interface RedmineBaseColumns extends BaseColumns {
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item";
		public static final String UPDATED = "updated";
		public static final String CREATED = "created";
	}

	interface User extends RedmineBaseColumns {
		public static final String FIRST_NAME = "first_name";
		public static final String LAST_NAME = "last_name";
		public static final String MAIL = "mail";
	}

	interface Membership extends RedmineBaseColumns {
		public static final String PROJECT_ID = "project_id";
	}

	interface ProjectColumns extends RedmineBaseColumns {

		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath("projects").build();

		public static final String NAME = "name";
		public static final String PARENT = "parent";
		public static final String IDENTIFIER = "identifier";
		public static final String CREATED_ON = "created_on";
		public static final String UPDATED_ON = "updated_on";
	}

	interface IssueColumns extends RedmineBaseColumns {

		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath("issues").build();

		public static final String PROJECT_ID = "project_id";
		public static final String TRACKER = "tracker";
		public static final String STATUS = "status";
		public static final String PRIORITY = "priority";
		public static final String AUTHOR = "author";
		public static final String ASSIGNED_TO = "assigned_to";
		public static final String SUBJECT = "subject";
		public static final String DESCRIPTION = "description";
		public static final String START_DATE = "start_date";
		public static final String DUE_DATE = "due_date";
		public static final String DONE_RATIO = "done_ratio";
	}

	public static class Project implements ProjectColumns {
		public static Uri buildProjectUri(String projectId) {
			return CONTENT_URI.buildUpon().appendPath(projectId).build();
		}

		public static Uri buildProjectsUri(String accountName) {
			return CONTENT_URI.buildUpon().appendQueryParameter("accountName", accountName).build();
		}
	}

	public static class Issue implements IssueColumns {
		public static Uri buildBlockUri(String issueId) {
			return CONTENT_URI.buildUpon().appendPath(issueId).build();
		}

		public static Uri buildIssuesUri(String accountName) {
			return CONTENT_URI.buildUpon().appendQueryParameter("accountName", accountName).build();
		}
	}
}