package com.example.sasasushiq.writeandread;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import android.util.Log;
import android.view.View;
import android.widget.*;

public class MainActivity extends AppCompatActivity {

    private Button writeText, readText;
    private EditText enterText;
    private EditText enterId;
    private TextView showText;
    private TextView showText2;
    private String file = "IP_Direccion.txt";
    private String file2 = "ID_Save.txt";
    private String fileContents;
    private String fileContents2;
    private int REQUEST_PERMISSION =1;
    private int REQUEST_PERMISSION2 =2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        writeText = findViewById(R.id.writeText);
        readText = findViewById(R.id.readText);
        enterText = findViewById(R.id.enterText);
        enterId = findViewById(R.id.enterId);
        showText = findViewById(R.id.showText);
        showText2 = findViewById(R.id.showText2);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);

            //return;
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION2);

            return;
        }


        writeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileContents = enterText.getText().toString();
                fileContents2 = enterId.getText().toString();

                fileContents = fileContents.replace(" ", "");
                fileContents = fileContents.replace("\r\n", "");

                fileContents2 = fileContents2.replace(" ", "");
                fileContents2 = fileContents2.replace("\r\n", "");

                File myFile2 = new File(Environment.getExternalStorageDirectory() + "/Download/"); // file path
                myFile2.delete();

                try {
//                    FileOutputStream fOut = openFileOutput(file,MODE_PRIVATE);
//                    fOut.write(fileContents.getBytes());
//                    fOut.close();
//                    File filePath = new File(getFilesDir(),file);
                    /*if (filePath.exists()){
                        filePath.delete();
                    }
                    filePath.createNewFile();*/

                    String strFileName = file; // file name

                    File myFile = new File(Environment.getExternalStorageDirectory() + "/Download/"); // file path
                    if (!myFile.exists()) { // directory is exist or not
                        myFile.mkdirs();    // if not create new
                        Log.e("DataStoreSD 0 ", myFile.toString());
                    } else {
                        myFile = new File(Environment.getExternalStorageDirectory() + "/Download/");
                        Log.e("DataStoreSD 1 ", myFile.toString());
                    }

                    try {
                        File Notefile = new File(myFile, strFileName);
                        FileWriter writer = new FileWriter(Notefile); // set file path & name to write
                        writer.append(fileContents); // write string
                        writer.flush();
                        writer.close();
                        Log.e("DataStoreSD 2 ", myFile.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }



                    Toast.makeText(getBaseContext(), "Guardado IP Correctamente... " +fileContents, Toast.LENGTH_LONG).show();

                    GuardaID();
                }
                catch (Exception e){
                        e.printStackTrace();
                }
                enterText.setText("");
            }
        });

        readText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                try {
//                    FileInputStream fIn = openFileInput(file);
//                    //File fIn = new File(getFilesDir(), file);
//                    int c;
//                    String temp = "";
//
//                    while ((c = fIn.read())!= -1)
//                    {
//                        temp = temp + Character.toString((char)c);
//                    }
//                    showText.setText(temp);
//                    Toast.makeText(getBaseContext(), "File Read Successfully", Toast.LENGTH_LONG).show();
//                }
//                catch (Exception e)
//                {
//                    e.printStackTrace();
//                }


                String line = null;

                try {
                    FileInputStream fileInputStream = new FileInputStream (new File(Environment.getExternalStorageDirectory() + "/Download/" + file));
                    Log.i("ReadIP",  "" + fileInputStream);
                    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    StringBuilder stringBuilder = new StringBuilder();

                    while ( (line = bufferedReader.readLine()) != null )
                    {
                        stringBuilder.append(line + System.getProperty("line.separator"));
                    }
                    fileInputStream.close();
                    line = stringBuilder.toString();

                    showText.setText(line);



                    bufferedReader.close();
                }
                catch(FileNotFoundException ex) {
                    Log.d("SAVE", ex.getMessage());
                }
                catch(IOException ex) {
                    Log.d("SAVE", ex.getMessage());
                }

                LeoId();

            }
        });
    }


    public void GuardaID(){
        fileContents2 = enterId.getText().toString();

        fileContents2 = fileContents2.replace(" ", "");
        fileContents2 = fileContents2.replace("\r\n", "");

        File myFile2 = new File(Environment.getExternalStorageDirectory() + "/Download/"); // file path
        myFile2.delete();

        try {
//                    FileOutputStream fOut = openFileOutput(file,MODE_PRIVATE);
//                    fOut.write(fileContents.getBytes());
//                    fOut.close();
//                    File filePath = new File(getFilesDir(),file);
                    /*if (filePath.exists()){
                        filePath.delete();
                    }
                    filePath.createNewFile();*/

            String strFileName = file2; // file name

            File myFile = new File(Environment.getExternalStorageDirectory() + "/Download/"); // file path
            if (!myFile.exists()) { // directory is exist or not
                myFile.mkdirs();    // if not create new
                Log.e("DataStoreSD 0 ", myFile.toString());
            } else {
                myFile = new File(Environment.getExternalStorageDirectory() + "/Download/");
                Log.e("DataStoreSD 1 ", myFile.toString());
            }

            try {
                File Notefile = new File(myFile, strFileName);
                FileWriter writer = new FileWriter(Notefile); // set file path & name to write
                writer.append(fileContents2); // write string
                writer.flush();
                writer.close();
                Log.e("DataStoreSD 2 ", myFile.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }



            Toast.makeText(getBaseContext(), "Guardado ID Correctamente... " +fileContents2, Toast.LENGTH_LONG).show();

           ;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        enterId.setText("");
    }


    public void LeoId(){

        String line = null;

        try {
            FileInputStream fileInputStream = new FileInputStream (new File(Environment.getExternalStorageDirectory() + "/Download/" + file2));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            while ( (line = bufferedReader.readLine()) != null )
            {
                stringBuilder.append(line + System.getProperty("line.separator"));
            }
            fileInputStream.close();
            line = stringBuilder.toString();

            showText2.setText(line);



            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            Log.d("SAVE", ex.getMessage());
        }
        catch(IOException ex) {
            Log.d("SAVE", ex.getMessage());
        }

    }



    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
            } else {
                // User refused to grant permission.
            }
        }else  if (requestCode == REQUEST_PERMISSION2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
            } else {
                // User refused to grant permission.
            }
        }
    }
}
