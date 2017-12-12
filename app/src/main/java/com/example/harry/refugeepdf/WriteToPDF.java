package com.example.harry.refugeepdf;

import android.os.Environment;
import android.util.Log;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Harry on 12-Dec-17.
 */

public class WriteToPDF {

    public static void writeFields(HashMap<Integer, String> answerMap, HashMap<Integer, String> fieldMap) {
        Log.i("WRITE TO PDF", "IN WRITE TO PDF");

        try {
            PdfReader reader = new PdfReader(Environment.getExternalStorageDirectory() + "/866.pdf");
            PdfStamper finalForm = new PdfStamper(reader, new FileOutputStream(Environment.getExternalStorageDirectory() + "/866_complete.pdf"));
            AcroFields acroFields = finalForm.getAcroFields();

            //Loop over each of the answers and input them into their fields
            for(int i = 0; i < answerMap.size(); i++){

                String value = answerMap.get(i);
                Log.i("WRITETOPDF", Integer.toString(answerMap.size()));

                //If value is binary, then it is a checkbox and we need different methods for filling in
                if(value.equals("1") || value.equals("0")){
                    Log.i("WRITETOPDF", "is checkbox");
                    Log.i("WRITETOPDF", fieldMap.get(i));
                    String fieldName = fieldMap.get(i);
                    String[] states = acroFields.getAppearanceStates(fieldName);
                    if(value.equals("1")){
                        acroFields.setField(fieldName, states[1]);
                    }
                    else if(value.equals("0")){
                        acroFields.setField(fieldName, states[0]);
                    }
                    Log.i("WriteToPDF.class", Integer.toString(states.length));
                }
            }

            finalForm.close();


        } catch (IOException e){
            Log.e("WRITETOPDF", e.getMessage());

        } catch (DocumentException e){
            Log.e("WRITETOPDF", e.getMessage());
        }
    }

}
