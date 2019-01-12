package org.artym_sysa.nihongo;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.artym_sysa.nihongo.room.AppDatabase;

import java.util.List;


public class TestRecordListAdapter extends BaseAdapter {

    private static AppDatabase DB;
    Context context;
    int layoutResourceId;
    List<TestRecord> data = null;

    public List<TestRecord> getData() {
        return data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Nullable
    @Override
    public TestRecord getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public TestRecordListAdapter(@NonNull Context context, int resource, @NonNull List<TestRecord> objects) {

        this.context = context;
        this.layoutResourceId = resource;
        this.data = objects;

        DB = AppDatabase.Companion.getInstance(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        TestRecordListAdapter.TestRecordHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new TestRecordHolder();

            holder.status = row.findViewById(R.id.answerStatus);
            holder.text = row.findViewById(R.id.textTextView);
            holder.type = row.findViewById(R.id.typeTextView);
            holder.mode = row.findViewById(R.id.modeTextView);

            row.setTag(holder);

        } else {
            holder = (TestRecordHolder) row.getTag();
        }

        TestRecord record = data.get(position);

        holder.status.setBackgroundResource(record.getResult() ? R.drawable.ic_checked : R.drawable.ic_cancel);
        holder.text.setText(DB.wordDao().getById(record.getWordId()).getText());

        switch (record.getType()) {
            case M2R_TYPE:
                holder.type.setText("Значение > Чтение");
                break;
            case M2W_TYPE:
                holder.type.setText("Значение > Слово");
                break;
            case R2M_TYPE:
                holder.type.setText("Чтение > Значение");
                break;
            case R2W_TYPE:
                holder.type.setText("Чтение > Слово");
                break;
            case W2M_TYPE:
                holder.type.setText("Слово > Значение");
                break;
            case W2R_TYPE:
                holder.type.setText("Слово > Чтение");
                break;
        }

        switch (record.getMode()) {
            case CORRECT_INCORRECT_MODE:
                holder.mode.setText("Верно-неверно");
                break;
            case WRITE_MODE:
                holder.mode.setText("Письменный");
                break;
            case SELECTION_MODE:
                holder.mode.setText("С выбором ответа");
                break;
        }

        return row;
    }


    static class TestRecordHolder {
        Button status;
        TextView text;
        TextView type;
        TextView mode;
    }
}