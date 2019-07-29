package ru.lizzzi.rowingstatistic;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import ru.lizzzi.rowingstatistic.view.MainActivity;

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
        private ImageButton imageDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            editCaption = itemView.findViewById(R.id.editNameRower);
            progressBar = itemView.findViewById(R.id.progressBar);
            textCaption = itemView.findViewById(R.id.textCaption);
            imageDelete = itemView.findViewById(R.id.imageDelete);
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
        final int positionInAdapter = position;
        String caption =
                mainActivity.getResources().getString(R.string.chartCaption) +
                " " +
                (positionInAdapter + 1);
        viewHolder.textCaption.setText(caption);
        if (rowersName.get(position).contains("Load")) {
            viewHolder.progressBar.setVisibility(View.VISIBLE);
            viewHolder.editCaption.setVisibility(View.INVISIBLE);
            viewHolder.imageDelete.setVisibility(View.GONE);
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
                                rowersName.get(positionInAdapter),
                                viewHolder.editCaption.getText().toString());
                        return true;
                    }
                    return false;
                }
            });
            viewHolder.imageDelete.setVisibility(View.VISIBLE);
            viewHolder.imageDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainActivity.removeRower(rowersName.get(positionInAdapter));
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
