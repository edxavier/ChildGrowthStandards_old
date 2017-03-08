package com.edxavier.childgrowthstandards.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eder Xavier Rojas on 06/09/2016.
 */
public class CSVreader {
    InputStream inputStream;

    public CSVreader(InputStream inputStream) {
        this.inputStream = inputStream;
    }
    public  ArrayList<float[]> read(){
        //List resultList = new ArrayList();
        ArrayList<float[]> list = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String csvLine;
            while ((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split(",");
                float[] floats = new float[6];
                floats[0] = Float.parseFloat(row[0]);
                floats[1] = Float.parseFloat(row[1]);
                floats[2] = Float.parseFloat(row[2]);
                floats[3] = Float.parseFloat(row[3]);
                floats[4] = Float.parseFloat(row[4]);
                floats[5] = Float.parseFloat(row[5]);
                list.add(floats);
            }
        }
        catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: "+ex);
        }
        finally {
            try {
                inputStream.close();
            }
            catch (IOException e) {
                throw new RuntimeException("Error while closing input stream: "+e);
            }
        }
        return list;
    }
}
