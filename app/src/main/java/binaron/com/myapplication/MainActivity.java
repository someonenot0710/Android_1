package binaron.com.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    Button btnRead , btnSave;
    EditText txtInput;
    EditText name;
    Button btnviewAll;
    Button seefile;
    TextView txtContent;
    Context context;
    File file;
    Handler Handler1;
    Handler Handler2;
    Handler Handler3;
    Handler Handler4;
    DatabaseHelper myDb;
    private static final String TAG = "MyActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtContent = (TextView) findViewById(R.id.txtContent);
        txtInput = (EditText) findViewById(R.id.txtInput);
        name = (EditText) findViewById(R.id.name);
        btnRead = (Button) findViewById(R.id.btnRead);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnviewAll = (Button) findViewById(R.id.viewall);
        context = getApplicationContext();
        seefile = (Button) findViewById(R.id.seefile);
        Handler1 = new Handler();
        Handler2 = new Handler();
        Handler3 = new Handler();
        Handler4 = new Handler();
        myDb = new DatabaseHelper(this);
    }

    public  void AddData(String file_name) {
        String path = getFilesDir().getAbsolutePath();
        path += "/";
        path += file_name;

        Log.d(TAG,"-------");
        Log.d(TAG,path);
        boolean isInserted = myDb.insertData(file_name,
                path);
//        if(isInserted == true)
//            Toast.makeText(MainActivity.this,"Data Inserted",Toast.LENGTH_LONG).show();
//        else
//            Toast.makeText(MainActivity.this,"Data not Inserted",Toast.LENGTH_LONG).show();
    }

    String data;
    public void lookfile(View view){
        Thread SaveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final String filename = name.getText().toString();
                int ch;
                StringBuffer fileContent = new StringBuffer("");
                FileInputStream fis;
                try {
                    fis = context.openFileInput(filename);
                    try {
                        while( (ch = fis.read()) != -1)
                            fileContent.append((char)ch);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                data = new String(fileContent);

                Handler3.post(new Runnable() {
                    @Override
                    public void run() {
                        txtContent.setText(data);
                        name.setText("");
                    }
                });
            }
        });SaveThread.start();
    }
    StringBuffer buffer;
    public void viewAll(View view){

        Thread SaveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor res = myDb.getAllData();
                if(res.getCount()==0){
                    showMessage("Error","None");
                    return;
                }

                buffer = new StringBuffer();
                while(res.moveToNext()){
                    buffer.append("ID: "+res.getString(0)+"\n");
                    buffer.append("Name: "+res.getString(1)+"\n");
                    buffer.append("Path: "+res.getString(2)+"\n\n");
                }

                Handler3.post(new Runnable() {
                    @Override
                    public void run() {
                        showMessage("Data",buffer.toString());
                    }
                });
            }

        });SaveThread.start();
//        Cursor res = myDb.getAllData();
//        if(res.getCount()==0){
//            showMessage("Error","None");
//            return;
//        }
//
//        StringBuffer buffer = new StringBuffer();
//        while(res.moveToNext()){
//            buffer.append("ID: "+res.getString(0)+"\n");
//            buffer.append("Name: "+res.getString(1)+"\n");
//            buffer.append("Path: "+res.getString(2)+"\n\n");
//        }
//        showMessage("Data",buffer.toString());
    }

    public void showMessage(String title , String Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }


    public void buttonSave(View view) {
        Thread SaveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final String [] savetext = String.valueOf(txtInput.getText()).split(System.getProperty("line.separator"));
                final String filename = name.getText().toString();
                file = new File(context.getFilesDir(),filename);
                AddData(filename);
                Handler1.post(new Runnable() {
                    @Override
                    public void run() {
                        Save (file , savetext);
                    }
                });
            }

        });
        SaveThread.start();
        txtInput.setText("");
        name.setText("");
    }


    String finalString = "";
    public void buttonRead(View view){
        Thread ReadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String[] loadText = Load(file);
                finalString = "";
                for(int i=0 ; i<loadText.length; i++){
                    finalString += loadText[i]+System.getProperty("line.separator");
            }
                Handler2.post(new Runnable() {
                    @Override
                    public void run() {
                        txtContent.setText(finalString);
                    }
                });
            }

        });
        ReadThread.start();
    }


    public static void Save(File file, String[] data)
    {
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(file,true);
        }
        catch (FileNotFoundException e) {e.printStackTrace();}
        try
        {
            try
            {
                for (int i = 0; i<data.length; i++)
                {
                    fos.write(data[i].getBytes());
                        fos.write("\n".getBytes());
                }

            }
            catch (IOException e) {e.printStackTrace();}
        }
        finally
        {
            try
            {
                fos.close();
            }
            catch (IOException e) {e.printStackTrace();}
        }
    }


    public static String[] Load(File file)
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(file);
        }
        catch (FileNotFoundException e) {e.printStackTrace();}
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        String test;
        int anzahl=0;
        try
        {
            while ((test=br.readLine()) != null)
            {
                anzahl++;
            }
        }
        catch (IOException e) {e.printStackTrace();}

        try
        {
            fis.getChannel().position(0);
        }
        catch (IOException e) {e.printStackTrace();}

        String[] array = new String[anzahl];

        String line;
        int i = 0;
        try
        {
            while((line=br.readLine())!=null)
            {
                array[i] = line;
                i++;
            }
        }
        catch (IOException e) {e.printStackTrace();}
        return array;
    }

}



