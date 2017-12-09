package com.example.harry.refugeepdf;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private String LOG_TAG = this.getClass().getName();
    private int count = 0;
    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL = 101;
    private PdfReader reader = null;

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

            Set<String> fieldNames = fields.getFields().keySet();
            Log.i(LOG_TAG, Integer.toString(fieldNames.size()));

            for(String field : fieldNames){
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


    //If permissions granted, writed PDF to storage, if denied quit the app as we cannot continue
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

        TextView textView = findViewById(R.id.questionText);

        if(v.getId() == R.id.btnYes){

        }
        else if(v.getId() == R.id.btnNo){
            textView.setText(R.string.Q1);
        }
    }
}
