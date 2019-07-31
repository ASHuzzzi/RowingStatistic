package ru.lizzzi.rowingstatistic.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import ru.lizzzi.rowingstatistic.R;

public class FileNameDialog extends DialogFragment {

    private EditText editFileName;
    private static final String APP_PREFERENCES = "lastdir";
    private static final String APP_PREFERENCES_DIR = "dir";
    private SharedPreferences sharedPreferencesForSettings;
    private String dataForSaveInFile;
    private ArrayList<String> chartName;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dataForSaveInFile = getArguments().getString("dataForSaveInFile");
        chartName = getArguments().getStringArrayList("chartName");
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setView(createDialogView())
                .setCancelable(false)
                .setPositiveButton("Записать", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String firstRow = "Период;";
                        if (chartName.size() > 0) {
                            firstRow = firstRow + chartName.get(0) + ";";
                        }
                        if (chartName.size() > 1) {
                            firstRow = firstRow + chartName.get(1) + ";";
                        }
                        if (chartName.size() > 2) {
                            firstRow = firstRow + chartName.get(2) + ";";
                        }
                        if (chartName.size() > 3) {
                            firstRow = firstRow + chartName.get(3) + ";";
                        }
                        if (chartName.size() > 4) {
                            firstRow = firstRow + chartName.get(4) + ";";
                        }
                        if (chartName.size() > 5) {
                            firstRow = firstRow + chartName.get(5) + ";";
                        }
                        if (chartName.size() > 6) {
                            firstRow = firstRow + chartName.get(6) + ";";
                        }
                        if (chartName.size() > 7) {
                            firstRow = firstRow + chartName.get(7) + ";";
                        }
                        firstRow = firstRow + "Комментарий" + "\n";

                        sharedPreferencesForSettings = getContext().getSharedPreferences(
                                APP_PREFERENCES,
                                Context.MODE_PRIVATE);
                        String nameSavedFile;
                        if (editFileName.length() > 0) {
                            nameSavedFile = editFileName.getText().toString() + ".csv";
                        } else {
                            Calendar currentTime = Calendar.getInstance();
                            nameSavedFile =
                                    "Result_" +
                                    currentTime.get(Calendar.HOUR_OF_DAY) + "-" +
                                    currentTime.get(Calendar.MINUTE) + "-" +
                                    currentTime.get(Calendar.SECOND) + ".csv";
                        }

                        String checkDirectory;
                        File pathForSave;
                        String textForToast;
                        //пытаемся сохраниться в папку с исходниками
                        try {
                            checkDirectory = Environment.getExternalStorageDirectory().getPath();
                            if (checkDirectory.equals(sharedPreferencesForSettings.getString(APP_PREFERENCES_DIR, ""))){
                                throw new Exception();
                            }
                            pathForSave = new File(
                                    sharedPreferencesForSettings.getString(APP_PREFERENCES_DIR, "") +
                                            "/" +
                                            nameSavedFile);
                            saveFile(pathForSave, firstRow, dataForSaveInFile);
                            //пытаемся сохраниться в папку с исходниками
                            pathForSave = new File(sharedPreferencesForSettings.getString(APP_PREFERENCES_DIR, "") + "/" + nameSavedFile);
                            saveFile(pathForSave, firstRow, dataForSaveInFile);
                            textForToast = "Файл сохранен в папку с иcходными файлами!";
                        } catch (Exception e) {
                            checkDirectory = Environment.getExternalStorageState();
                            if (Environment.MEDIA_MOUNTED.equals(checkDirectory)) //проверям доступность внешней памяти
                            {
                                try { //сохраняем во внешнюю память
                                    pathForSave = new File(
                                            Environment.getExternalStorageDirectory() +
                                                    "/" +
                                                    nameSavedFile);
                                    saveFile(pathForSave, firstRow, dataForSaveInFile);
                                    textForToast = "Файл сохранен на внешнюю память!";
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                    textForToast = "Файл не сохранен!";
                                }
                            } else {
                                try { //сохраняем во внутренню память
                                    pathForSave = new File(
                                            getContext().getFilesDir() +
                                                    "/" +
                                                    nameSavedFile);
                                    saveFile(pathForSave, firstRow, dataForSaveInFile);
                                    textForToast = "Файл сохранен в папку приложения!";
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                    textForToast = "Файл не сохранен!";
                                }
                            }
                        }
                        Toast.makeText(getContext(), textForToast, Toast.LENGTH_SHORT).show();
                        dataForSaveInFile = "";
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        return builder.create();
    }

    private View createDialogView() {
        LayoutInflater layoutInflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_filename, null);
        editFileName = view.findViewById(R.id.editFileName);
        return view;
    }

    private boolean saveFile(File myFile, String firts_row, String mass_temp) throws IOException {
        if (myFile.createNewFile()) {
            FileOutputStream outputStream = new FileOutputStream(myFile);   // После чего создаем поток для записи                 // и производим непосредственно запись
            outputStream.write(firts_row.getBytes("Cp1251"));
            outputStream.write(mass_temp.getBytes("Cp1251"));
            outputStream.close();
            return true;
        } else {
            return false;
        }
    }
}
