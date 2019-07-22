package ru.lizzzi.rowingstatistic;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

public class FileLoader extends AsyncTaskLoader<String> {

    private String fileLocation;
    private String fileNumber;

    public FileLoader(@NonNull Context context, @NonNull Bundle bundle) {
        super(context);
        fileLocation = bundle.getString("fileLocation");
        fileNumber = bundle.getString("fileNumber");
    }

    @Nullable
    @Override
    public String loadInBackground() {
        Parser parser = new Parser(
                getContext(),
                fileLocation,
                fileNumber);
        return parser.parseFile();
    }
}
