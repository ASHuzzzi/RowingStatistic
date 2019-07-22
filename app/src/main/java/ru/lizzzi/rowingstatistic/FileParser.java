package ru.lizzzi.rowingstatistic;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.TimeZone;

public class FileParser {

    private String fileLocation;
    private String fileNumber;
    private Context context;

    public FileParser(Context context, String fileLocation, String fileNumber) {
        this.context = context;
        this.fileLocation = fileLocation;
        this.fileNumber = fileNumber;
    }

    public String parseFile() {
        String rowerName = "Гребец " + fileNumber;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileLocation));
            if (reader.ready()) {
                Log.d("LoadFile", "Старт загрузки " + fileNumber + " спортсмена");
                String rowForScanner;
                boolean isIncorrectRow = false;
                int lineInFile = 0;
                Date startTime = null;
                String badSymbols = "---";
                String distance = "0";
                String time = "0";
                String speed = "0";
                String strokeRate = "0";
                String power = "0";
                List<Map> listForDB = new ArrayList<>();
                while ((rowForScanner = reader.readLine()) != null) {
                    int scannerIndex = 0;
                    HashMap<String, String> row = new HashMap<>();
                    Scanner lineScanner = new Scanner(rowForScanner);
                    lineScanner.useDelimiter(",");
                    if (lineInFile == 2) {
                        while (lineScanner.hasNext()) {
                            String scannerElement = lineScanner.next();
                            if (scannerIndex == 5) {
                                rowerName = scannerElement;
                            }
                            scannerIndex++;
                        }
                    }
                    if (lineInFile == 3) {
                        while (lineScanner.hasNext()) {
                            String scannerElement = lineScanner.next();
                            if (scannerIndex == 1) {
                                             /*
                                            Берем время начала тренировки. Ниже берем именно время, отрезая дату
                                             */
                                SimpleDateFormat timeFormat =
                                        new SimpleDateFormat("HH:mm", Locale.getDefault());
                                timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                                startTime = timeFormat.parse(scannerElement.substring(9));
                                time = String.valueOf(startTime.getTime());
                            }
                            scannerIndex++;
                        }
                    }
                    if (lineInFile > 29) {
                        while (lineScanner.hasNext()) {
                            String scannerElement = lineScanner.next();
                            switch (scannerIndex) {
                                case 1:
                                    if (!badSymbols.equals(scannerElement)) {
                                        distance = scannerElement;
                                    } else {
                                        isIncorrectRow = true;
                                    }
                                    break;
                                case 3:
                                    if (!badSymbols.equals(scannerElement)) {
                                        SimpleDateFormat dateFormat = new SimpleDateFormat(
                                                "HH:mm:ss.S",
                                                Locale.getDefault());
                                        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                                        Date segment = dateFormat.parse(scannerElement);
                                        time = String.valueOf(
                                                startTime.getTime() + segment.getTime());
                                    } else {
                                        isIncorrectRow = true;
                                    }
                                    break;
                                case 5:
                                    if (!badSymbols.equals(scannerElement)){
                                        speed = scannerElement;
                                    } else {
                                        isIncorrectRow = true;
                                    }
                                    break;
                                case 8:
                                    if (!badSymbols.equals(scannerElement)) {
                                        strokeRate = scannerElement;
                                    } else {
                                        isIncorrectRow = true;
                                    }
                                    break;
                                case 13:
                                    if (!badSymbols.equals(scannerElement)) {
                                        power = scannerElement;
                                    } else {
                                        isIncorrectRow = true;
                                    }
                                    break;
                            }
                            scannerIndex++;
                        }
                    }
                    if (lineInFile == 3 || (lineInFile > 29 & !isIncorrectRow)) {
                        row.put("rowerName", rowerName);
                        row.put("distance", distance);
                        row.put("time", time);
                        row.put("speed", speed);
                        row.put("strokeRate", strokeRate);
                        row.put("power", power);
                        listForDB.add(row);
                    }
                    lineInFile++;
                    isIncorrectRow = false;
                }
                reader.close();
                SQLiteStorage sqlStorage = new SQLiteStorage(context);
                sqlStorage.saveData(listForDB);
                Log.d("LoadFile", "Окончание загрузки " + fileNumber + " спортсмена");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return rowerName;
    }
}
