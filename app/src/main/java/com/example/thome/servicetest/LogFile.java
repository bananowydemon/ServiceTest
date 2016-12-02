package com.example.thome.servicetest;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class LogFile {
    BufferedReader br = null; // do .read()
    public LogFile() {
    }



    public static void log(String msg, String fileName) {
        File logFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + fileName);
        if(!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException var6) {
                var6.printStackTrace();
            }
        }

        try {
            Date e = Calendar.getInstance().getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy,HH:mm:ss:SSS", Locale.US);
            BufferedWriter buffer = new BufferedWriter(new FileWriter(logFile, true));
            buffer.append(dateFormat.format(e) + " " + msg);
            buffer.newLine();
            buffer.close();
        } catch (IOException var5) {
            var5.printStackTrace();
        }

    }

    // odczytuje plik, zwraca cala jego zawartosc w formie listy z linijkami tekstu
    public static List<String> read(String fileName) {
        File logFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + fileName);
        List<String> output = new ArrayList<String>(); // tu przechowujemy zawartosc pliku
        if(!logFile.exists()) { //zwraca pustego stringa jesli nie ma pliku
            output.add("");
            return output;
        } else {

            BufferedReader br = null;
            //jak coś to nie null
            try {

                String sCurrentLine;

                br = new BufferedReader(new FileReader(logFile));

                while ((sCurrentLine = br.readLine()) != null) {
                    output.add(sCurrentLine);
                }




            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (br != null)br.close();
                } catch (IOException ex) {
                    ex.printStackTrace();

                }
            }

        }
        return output;

    }


    // zwraca liste 2 list - z datami i z warosciami; drugi argument kontroluje ile maksymalnie punktow zostanie zwroconych (dobre dla wykresow)
    // przyklad uzycia: List<List<String>> data = LogFile.timeSeriesData(LogFile.read("filename"), 50);
    public static List<List<String>>  timeSeriesData(List<String> content, int noOfLines) {

        int contentSize = content.size();
        List<List<String>> output = new ArrayList<List<String>>() {
        }; //to bedzie na wyjsciu
        List<String> dates = new ArrayList<String>();
        List<String> values = new ArrayList<String>();

        if(contentSize <= noOfLines) {
            for (int i = 0; i < contentSize; i++) {
                if (content.get(i) != "") {
                    String[] parts = content.get(i).split(" ");
                    try {
                        dates.add(parts[0]);
                        values.add(parts[1]);
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        ex.printStackTrace();
                    }

                }
            }
        } else {
            int fromLine = contentSize - noOfLines - 1; // od ktorej linijki odczytywac dane
            for (int i = 0; i < contentSize; i++) {
                if (i >= fromLine) {
                    if (content.get(i) != "") {
                        String[] parts = content.get(i).split(" ");
                        try {
                            dates.add(parts[0]);
                            values.add(parts[1]);
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
        output.add(dates);
        output.add(values);
        return output;
    }

    public static void fileSizeControl (int maxLineNumber, String fileName) {
        List<String> output = new ArrayList<String>();
        output = LogFile.read(fileName);
        int size = output.size();

        if (size > maxLineNumber){
            File logFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + fileName);
            if(logFile.exists()) {
                boolean deleted = logFile.delete();
                if (deleted) {
                    try {
                        logFile.createNewFile();
                    } catch (IOException var6) {
                        var6.printStackTrace();
                    }
                    int difference = size - maxLineNumber;
                    String newContent = new String();
                    for (int i = 0; i < size; i++) {
                        if (i >= difference) {newContent += output.get(i) + "\n";}
                    }
                    try {
                        BufferedWriter buffer = new BufferedWriter(new FileWriter(logFile, true));
                        buffer.append(newContent);
                        buffer.close();
                    } catch (IOException var5) {
                        var5.printStackTrace();
                    }
                }

            }
        }
    }

}

