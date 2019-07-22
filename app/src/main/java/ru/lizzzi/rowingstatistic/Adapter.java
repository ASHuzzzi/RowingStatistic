package ru.lizzzi.rowingstatistic;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    private List<String> rowersName;
    private MainActivity mainActivity;

    public Adapter(MainActivity mainActivity, @NonNull List<String> rowersName) {
        this.mainActivity = mainActivity;
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
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        final int newPosition = position + 1;
        String caption = mainActivity.getResources().getString(R.string.chartCaption) + newPosition;
        viewHolder.textCaption.setText(caption);
        if (rowersName.get(position).contains("Load")) {
            viewHolder.progressBar.setVisibility(View.VISIBLE);
            viewHolder.editCaption.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.progressBar.setVisibility(View.GONE);
            viewHolder.editCaption.setText(rowersName.get(position));
            viewHolder.editCaption.setVisibility(View.VISIBLE);
            viewHolder.editCaption.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN)
                            && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        mainActivity.renameChart(
                                rowersName.get(newPosition - 1),
                                viewHolder.editCaption.getText().toString());
                        return true;
                    }
                    return false;
                }
            });
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
