package com.example.multion_offkeyingprototype;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.security.Permissions;
import java.util.Collections;
import java.util.Vector;

import Constant.Constant;
import FFT.SoundAnalyzer;

public class MainActivity extends AppCompatActivity {

    TextView dopplerView, idView;
    boolean wait = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        // get View Component
        this.dopplerView = findViewById(R.id.dopplerTextView);
        this.idView = findViewById(R.id.idTextView);

        this.getPermission();

    }

    private void getPermission() {
        // get Record Permission
//        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
//        }
        // @@@

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            new Handler().postDelayed(new permissionCheckThread(), 200);
        }else{
            new Service().start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new Service().start();
            }else{
                new Handler().postDelayed(new permissionCheckThread(), 200);
            }
        }
    }

    private class permissionCheckThread implements Runnable {
        @Override
        public void run() {
            AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
            ad.setTitle("초음파 신호를 듣기 위해 권한이 필요합니다.");
            ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                }
            });
            ad.setNegativeButton("종료", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                    System.exit(0);
                }
            });
            ad.show();
        }
    }

    private class Service extends Thread {

        // Attribute
        private int granule = 100, start = 20000, end = 22000, count =0;

        // Component
        private Vector<String> logs;
        private Vector<Integer> ids;
        private SoundAnalyzer soundAnalyzer;

        // Working Variable
        private int beforeSignal=-2;

        // Constructor
        public Service(){
            this.logs = new Vector<>();
            this.ids = new Vector<>();
            this.soundAnalyzer = new SoundAnalyzer();
        }

        public void run() {
            while (true) {
                this.soundAnalyzer.startRecord();
                long startTime = System.currentTimeMillis();
                while(System.currentTimeMillis()-startTime < 5000){
                    this.soundAnalyzer.readData();
                    double[] nowSoundInfo = this.soundAnalyzer.getMaxAmpBetween(this.start, this.end);

                    boolean signalOn = this.isSignalOn(nowSoundInfo[1]);
                    boolean noDopplerEffect = this.isNoDopplerEffect(nowSoundInfo[0]);
                    String log;
                    int nowReadID;
                    if (signalOn && noDopplerEffect) {
                        nowReadID = (((int) nowSoundInfo[0]) - this.start) / this.granule;
                        log = "ID : [" + nowReadID + "], AMP : [" + (int) nowSoundInfo[1] + "]";
                    }else{
                        nowReadID = -1;
                        log = "No Signal";
                    }
                    this.count++;
                    this.ids.add(nowReadID);
                    this.logs.add(log);

                    this.printID();
                    this.printDoppler(signalOn, noDopplerEffect);
                }

                int nowSignal = this.getMaxShowID(this.ids);
                if(beforeSignal!=nowSignal){
                    Log.d("TEEESSTTT", ""+nowSignal);
                    this.sendResultToServer(nowSignal);
                    beforeSignal=nowSignal;
                }

                this.count = (this.count>1000)? 0:this.count;
                this.ids.clear();
                this.logs.clear();
                this.soundAnalyzer.stopRecord();

//                try { Thread.sleep(1000*60); } catch (InterruptedException e) { e.printStackTrace(); }
                try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
            }
        }

        private int getMaxShowID(Vector<Integer> ids) {
            int maxId = -1, maxCount = 0;
            for(int i=0; i<(this.end-this.start)/this.granule; i++){ // i = ID
                int nowCount = Collections.frequency(ids, i);
                if(nowCount > maxCount){
                    maxId = i;
                    maxCount = nowCount;
                }
            }
            return maxId;
        }

        private void sendResultToServer(final int id) {
            new Thread(){
                @Override
                public void run(){
                    new Connect().sendData2Server("2", id+"");
                }
            }.start();
        }

        public boolean isSignalOn(double amp) {return amp > Constant.CheckWaveMinAmp;}
        public boolean isNoDopplerEffect(double freq) {return Math.abs(freq % this.granule - this.granule / 2) < this.granule / 4;}

        private void printDoppler(boolean signalOn, boolean noDopplerEffect) {
            String log = "Doppler Effect : ";
            if(signalOn){ log+= !noDopplerEffect; } else{ log+="No Signal"; }
            final String finalLog = log;
            runOnUiThread(new Runnable() {@Override public void run() { dopplerView.setText(finalLog); }});
        }
        private void printID() {
            if (this.logs.size() > 20) {
                String log = "";
                for(int i=10; i>0; i--){ log += ((count - i) + " : " + this.logs.get(this.logs.size() - i) +"\n"); }
                final String finalLog = log;
                runOnUiThread(new Runnable() {@Override public void run() { idView.setText(finalLog); }});
            }
        }
    }

}
