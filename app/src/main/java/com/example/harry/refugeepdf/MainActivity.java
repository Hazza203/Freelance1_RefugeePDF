package com.example.harry.refugeepdf;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private String LOG_TAG = this.getClass().getName();
    private int count = 0;
    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL = 101;
    private PdfReader reader = null;
    private Set<String> fieldNames = null;
    private String[] fields = null;
    private HashMap<Integer, String> answerMap = new HashMap<Integer, String>();
    private HashMap<Integer, String> fieldMap = new HashMap<Integer, String>();
    private HashMap<Integer, String> allFields = new HashMap<Integer, String>();
    private int questionNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Requesting permissions to write to external storage. if not given, exit the application
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL);
        } else {
            init();
        }

    }

    private void init(){
        File file = new File(Environment.getExternalStorageDirectory() + "/866.pdf");
        //Checking if file has already been copied, if not copy over to storage.
        if(!file.exists()){
            copyPDFtoExternal();
        }

        try{
            Log.i(LOG_TAG, Environment.getExternalStorageDirectory().toString());
            reader = new PdfReader(Environment.getExternalStorageDirectory() + "/866.pdf");
            AcroFields fields = reader.getAcroFields();

            fieldNames = fields.getFields().keySet();
            Log.i(LOG_TAG, Integer.toString(fieldNames.size()));

            for(String field : fieldNames){
                allFields.put(count, field);
                Log.i(LOG_TAG, "Field #" + count + " / Name: " + field);
                count++;
            }
        } catch (IOException e){
            Log.e(LOG_TAG, "FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //Method for copying PDF to external storage
    private void copyPDFtoExternal(){
        AssetManager assetManager = getAssets();
        String[] files = null;
        int read;
        byte buffer[];

        try{
            files = assetManager.list("");
        } catch (IOException e){
            Log.e(LOG_TAG, "FAILED: failed to get assets");
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }

        //Find pdf in asset folder
        for(String fileStr : files){
            Log.i("MainClass", fileStr);

            if(fileStr.equalsIgnoreCase("866.pdf")){
                InputStream in = null;
                OutputStream out = null;

                if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
                    Toast.makeText(this, "Cannot connect to external storage for writing", Toast.LENGTH_LONG).show();
                }
                else {
                    try {
                        //Open pdf for reading
                        in = assetManager.open(fileStr);
                        //Open outputStream to external storage for writing pdf
                        out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/" + fileStr);
                        buffer = new byte[1024];

                        //Write PDF to storage byte by byte
                        while((read = in.read(buffer)) != -1){
                            out.write(buffer, 0, read);
                        }

                        //Close Streams
                        in.close();
                        in = null;
                        out.flush();
                        out.close();
                        out = null;
                        break;
                    } catch(FileNotFoundException e){
                        Log.e(LOG_TAG, e.getMessage());
                        e.printStackTrace();
                    } catch (IOException e){
                        Log.e(LOG_TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    //If permissions granted, write PDF to storage, if denied quit the app as we cannot continue
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch(requestCode){
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    init();
                }
                else {
                    this.finish();
                    System.exit(0);
                }
            }
        }
    }

    public void onClick(View v){

        TextView questionText = findViewById(R.id.questionText);
        TextView questionNumText = findViewById(R.id.questionNum);
        RadioGroup radioGroup = findViewById(R.id.radioYesNo);
        RadioButton radioYes = findViewById(R.id.radioYes);
        RadioButton radioNo = findViewById(R.id.radioNo);

        if(v.getId() == R.id.btnNext){

            if(radioYes.isChecked()){

                answerMap.put(questionNum, "1");
                fieldMap.put(questionNum, allFields.get(questionNum + 63));
                radioGroup.clearCheck();

            } else if(radioNo.isChecked()){
                answerMap.put(questionNum, "0");
                fieldMap.put(questionNum, allFields.get(questionNum + 63));
                radioGroup.clearCheck();
            } else{
                answerMap.put(questionNum, "skip");
                fieldMap.put(questionNum, allFields.get(questionNum + 63));
            }

            questionNum++;

            if(answerMap.containsKey(questionNum)){
                if(answerMap.get(questionNum).equals("1")){
                    radioYes.toggle();
                }
                else if(answerMap.get(questionNum).equals("0")){
                    radioNo.toggle();
                }
            }


        }
        else if(v.getId() == R.id.btnBack){
            if(questionNum == 0){
                return;
            }
            questionNum--;
            if(answerMap.get(questionNum).equals("1")){
                radioYes.toggle();

            }
            else if(answerMap.get(questionNum).equals("0")){
                radioNo.toggle();
            } else {
                radioGroup.clearCheck();
            }

        }

        if(questionNum == 17){
            WriteToPDF.writeFields(answerMap, fieldMap);
            Intent intent = new Intent(this, DisplayPDFActivity.class);
            startActivity(intent);
        }

        switch(questionNum){
            case 0: questionText.setText(R.string.Q1); questionNumText.setText("Q3.1");break;
            case 1: questionText.setText(R.string.Q2); questionNumText.setText("Q3.2");break;
            case 2: questionText.setText(R.string.Q3); questionNumText.setText("Q3.3");break;
            case 3: questionText.setText(R.string.Q4); questionNumText.setText("Q3.4");break;
            case 4: questionText.setText(R.string.Q5); questionNumText.setText("Q3.5");break;
            case 5: questionText.setText(R.string.Q6); questionNumText.setText("Q3.6");break;
            case 6: questionText.setText(R.string.Q7); questionNumText.setText("Q3.7");break;
            case 7: questionText.setText(R.string.Q8); questionNumText.setText("Q3.8");break;
            case 8: questionText.setText(R.string.Q9); questionNumText.setText("Q3.9");break;
            case 9: questionText.setText(R.string.Q10); questionNumText.setText("Q3.10");break;
            case 10: questionText.setText(R.string.Q11); questionNumText.setText("Q3.11");break;
            case 11: questionText.setText(R.string.Q12); questionNumText.setText("Q3.12"); break;
            case 12: questionText.setText(R.string.Q13); questionNumText.setText("Q3.13");break;
            case 13: questionText.setText(R.string.Q14); questionNumText.setText("Q3.14");break;
            case 14: questionText.setText(R.string.Q15); questionNumText.setText("Q3.15");break;
            case 15: questionText.setText(R.string.Q16); questionNumText.setText("Q3.16");break;
            case 16: questionText.setText(R.string.Q17); questionNumText.setText("Q3.17");break;
        }
    }
}
