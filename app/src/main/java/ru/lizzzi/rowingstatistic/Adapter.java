package ru.lizzzi.rowingstatistic;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    private List<String> rowersName;
    private Context context;

    public Adapter(Context context, @NonNull List<String> rowersName) {
        this.context = context;
        this.rowersName = rowersName;
    }

    public static class ViewHolder  extends RecyclerView.ViewHolder{
        private EditText editCaption;
        private ProgressBar progressBar;
        private TextView textCaption;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            editCaption = itemView.findViewById(R.id.editNameRower);
            progressBar = itemView.findViewById(R.id.progressBar);
            textCaption = itemView.findViewById(R.id.textCaption);
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_rows, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        int newPosition = position + 1;
        String caption = context.getResources().getString(R.string.chartCaption) + newPosition;
        viewHolder.textCaption.setText(caption);
        if (rowersName.get(position).contains("Load")) {
            viewHolder.progressBar.setVisibility(View.VISIBLE);
            viewHolder.editCaption.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.progressBar.setVisibility(View.GONE);
            viewHolder.editCaption.setText(rowersName.get(position));
            viewHolder.editCaption.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return rowersName.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
