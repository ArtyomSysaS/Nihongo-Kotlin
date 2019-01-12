package org.artym_sysa.nihongo;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.artym_sysa.nihongo.room.entity.GroupPojo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupListAdapter extends BaseAdapter implements Filterable {

    Context context;
    int layoutResourceId;
    List<GroupPojo> data = null;

    public List<GroupPojo> getData() {
        return data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Nullable
    @Override
    public GroupPojo getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public GroupListAdapter(@NonNull Context context, int resource, @NonNull List<GroupPojo> objects) {
        this.context = context;
        this.layoutResourceId = resource;
        this.data = objects;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        GroupHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new GroupHolder();

            holder.name = row.findViewById(R.id.gTitle);
            holder.wordsCount = row.findViewById(R.id.gWordCount);
            holder.date = row.findViewById(R.id.gDate);

            row.setTag(holder);

        } else {
            holder = (GroupHolder) row.getTag();
        }

        GroupPojo group = data.get(position);

        holder.name.setText(group.getName());
        holder.wordsCount.setText(String.valueOf(group.getWordsCount()));
        holder.date.setText(group.getDate());

        return row;
    }


    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                List<GroupPojo> filteredList = new ArrayList<>();

                constraint = constraint.toString().toLowerCase();

                for (GroupPojo groupPojo : data) {
                    if (groupPojo.getName().toLowerCase().contains(constraint) || constraint.toString().isEmpty()) {
                        filteredList.add(groupPojo);
                    }
                }

                if(!filteredList.isEmpty()) {
                    Collections.sort(filteredList, (o1, o2) -> o1.getId() < o2.getId() ? 1 : -1);
                }
                results.count = filteredList.size();
                results.values = filteredList;

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                data = (List<GroupPojo>) results.values;
                notifyDataSetChanged();
            }
        };

        return filter;
    }

    static class GroupHolder {
        TextView name;
        TextView wordsCount;
        TextView date;
    }
}