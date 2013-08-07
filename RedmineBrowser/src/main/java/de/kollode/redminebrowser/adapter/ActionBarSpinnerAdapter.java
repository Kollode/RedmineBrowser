package de.kollode.redminebrowser.adapter;

import java.util.ArrayList;

import android.accounts.Account;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ActionBarSpinnerAdapter extends BaseAdapter {

	// Your sent context
	private Context context;
	// Your custom values for the spinner (User)
	private ArrayList<Account> values;
	
	private int mResource;

	public ActionBarSpinnerAdapter(Context context, int textViewResourceId, ArrayList<Account> values) {
		this.context = context;
		this.values = values;
		this.mResource = textViewResourceId;
	}

	@Override
	public int getCount() {
		return values.size();
	}

	@Override
	public Account getItem(int position) {
		return values.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mResource);
    }
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mResource);
    }
	
	private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
		LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		TextView view = (TextView) mInflater.inflate(resource, parent, false);

		Account item = getItem(position);
		view.setText(item.name);

		return view;
	}
}
