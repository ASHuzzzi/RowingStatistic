package ru.lizzzi.rowingstatistic.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Objects;

import ru.lizzzi.rowingstatistic.R;
import ru.lizzzi.rowingstatistic.view.ChartActivity;

public class CommentDialog extends DialogFragment {

    private EditText editComment;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setView(createDialogView())
                .setCancelable(false)
                .setPositiveButton("Обработать",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String result = editComment.getText().toString();
                                ChartActivity chartActivity = (ChartActivity) getActivity();
                                if (chartActivity != null) {
                                    chartActivity.gerAverageValue(result);
                                }
                                dismiss();
                            }
                        })
                .setNegativeButton("Отмена",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dismiss();
                                Toast.makeText(
                                        getContext(),
                                        "Выборка не добавлена!",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
        return builder.create();
    }

    private View createDialogView() {
        LayoutInflater layoutInflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_comment, null);
        editComment = view.findViewById(R.id.editComment);
        return view;
    }
}