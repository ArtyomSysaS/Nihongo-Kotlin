package org.artym_sysa.nihongo;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.artym_sysa.nihongo.room.AppDatabase;
import org.artym_sysa.nihongo.room.entity.Word;

import java.util.List;

public class WordListAdapter extends BaseAdapter {

    Context context;
    int layoutResourceId;
    int meaningLayoutId;

    public WordListAdapter(Context context, int layoutResourceId, int meaningLaoutId, List<Word> data) {
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.meaningLayoutId = meaningLaoutId;
        this.data = data;
    }

    List<Word> data = null;

    public List<Word> getData() {
        return data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Nullable
    @Override
    public Word getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        WordListAdapter.WordHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new WordHolder();

            holder.text = row.findViewById(R.id.wText);
            holder.statusBtn = row.findViewById(R.id.statusBtn);
            holder.meaning = row.findViewById(R.id.wMeaning);
            row.setTag(holder);

        } else {
            holder = (WordHolder) row.getTag();
        }


        Word word = data.get(position);
        holder.text.setText(word.getText());
        holder.meaning.setText(word.getMeaning());

        if (word.getStatus() == 1) {
            holder.statusBtn.setBackgroundResource(R.drawable.ic_star_black_24px);
        } else {
            holder.statusBtn.setBackgroundResource(R.drawable.ic_star_border_black_24px);
        }

        holder.statusBtn.setOnClickListener(v -> {
            if (v.getId() == R.id.statusBtn) {
                word.setStatus(word.getStatus() == 0 ? 1 : 0);

                if (word.getStatus() == 1) {
                    v.setBackgroundResource(R.drawable.ic_star_black_24px);
                } else {
                    v.setBackgroundResource(R.drawable.ic_star_border_black_24px);
                }

                AppDatabase.Companion.getInstance(context).wordDao().update(word);
            }
        });

        return row;
    }

    static class WordHolder {
        TextView text;
        TextView meaning;
        Button statusBtn;
    }
}