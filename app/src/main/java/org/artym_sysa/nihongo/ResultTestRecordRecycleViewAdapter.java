package org.artym_sysa.nihongo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.artym_sysa.nihongo.room.AppDatabase;

import java.util.List;

public class ResultTestRecordRecycleViewAdapter extends RecyclerView.Adapter<ResultTestRecordRecycleViewAdapter.ViewHolder> {
    Context context;
    List<TestRecord> data = null;
    AppDatabase DB;

    public ResultTestRecordRecycleViewAdapter(Context context, List<TestRecord> data) {
        this.context = context;
        this.data = data;
        DB = AppDatabase.Companion.getInstance(context);
    }

    @Override
    public ResultTestRecordRecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.result_test_record_recycle_view_layout, parent, false);
        ResultTestRecordRecycleViewAdapter.ViewHolder holder = new ResultTestRecordRecycleViewAdapter.ViewHolder(layout);

        return holder;
    }

    @Override
    public void onBindViewHolder(ResultTestRecordRecycleViewAdapter.ViewHolder holder, int position) {
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
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Button status;
        TextView text;
        TextView type;
        TextView mode;

        public ViewHolder(View itemView) {
            super(itemView);

            status = itemView.findViewById(R.id.answerStatus);
            text = itemView.findViewById(R.id.textTextView);
            type = itemView.findViewById(R.id.typeTextView);
            mode = itemView.findViewById(R.id.modeTextView);
        }
    }
}