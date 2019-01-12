package org.artym_sysa.nihongo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.artym_sysa.nihongo.room.entity.Word;

import java.util.List;

public class CardsRecycleViewAdapter extends RecyclerView.Adapter<CardsRecycleViewAdapter.ViewHolder> {
    Context context;
    List<Word> data = null;

    public CardsRecycleViewAdapter(Context context, List<Word> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public CardsRecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        ViewHolder holder = new ViewHolder(layout);

        return holder;
    }

    @Override
    public void onBindViewHolder(CardsRecycleViewAdapter.ViewHolder holder, int position) {
        final int[] action = {0};

        LinearLayout content = holder.layout.findViewById(R.id.cardContent);

        content.removeAllViews();

        TextView item = (TextView) LayoutInflater.from(context).inflate(R.layout.card_text_item, content, false);
        item.setText(data.get(position).getText());
        content.addView(item);


        content.setOnClickListener(l -> {
            cardContentChange(position, action, content);
        });

        holder.layout.setOnClickListener((View l) -> {
            cardContentChange(position, action, content);
        });
    }

    private void cardContentChange(int position, int[] action, LinearLayout content) {
        content.removeAllViews();

        if (action[0] == 0) {
            LinearLayout meaning = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.card_info, content, false);
            ((TextView) meaning.findViewById(R.id.wKey)).setText("Значение");
            ((TextView) meaning.findViewById(R.id.wValue)).setText(data.get(position).getText());

            LinearLayout reading = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.card_info, content, false);
            ((TextView) reading.findViewById(R.id.wKey)).setText("Чтение");
            ((TextView) reading.findViewById(R.id.wValue)).setText(data.get(position).getReading());

            List<WordField> fields = new Gson().fromJson(data.get(position).getFields(),
                    new TypeToken<List<WordField>>() {
                    }.getType());

            content.addView(meaning);
            content.addView(reading);

            if (fields != null) {
                for (WordField field : fields) {
                    LinearLayout fieldLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.card_info, content, false);
                    ((TextView) fieldLayout.findViewById(R.id.wKey)).setText(field.getKey());
                    ((TextView) fieldLayout.findViewById(R.id.wValue)).setText(field.getValue());

                    content.addView(fieldLayout);
                }
            }

            action[0] = 1;
        } else {
            TextView text = (TextView) LayoutInflater.from(context).inflate(R.layout.card_text_item, content, false);
            text.setText(data.get(position).getText());

            content.addView(text);

            action[0] = 0;
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);

            layout = itemView.findViewById(R.id.cardLayout);
        }
    }
}
