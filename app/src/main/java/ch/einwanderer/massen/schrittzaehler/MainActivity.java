package ch.einwanderer.massen.schrittzaehler;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final int START_STATION_CODE = 1729, END_STATION_CODE = 1337;
    private int startStation = -1;
    Button btnEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStart = (Button) findViewById(R.id.btnStart);
        btnEnd = (Button) findViewById(R.id.btnEnd);

        btnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                scanQRForResult(START_STATION_CODE);
            }
        });

        btnEnd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                scanQRForResult(END_STATION_CODE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnEnd.setVisibility( (startStation == -1) ? View.INVISIBLE : View.VISIBLE );
    }

    private void scanQRForResult(int requestCode) {
        Intent i = new Intent("com.google.zxing.client.android.SCAN");

        if (getPackageManager().queryIntentActivities(i, PackageManager.MATCH_ALL).isEmpty()) {
            Toast.makeText(getApplicationContext(), "QR scan App not Installed", Toast.LENGTH_LONG).show();
            return;
        }

        i.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(i, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        if(resultCode == RESULT_OK) {
            try {
                if(requestCode == START_STATION_CODE) {

                    JSONObject scanResult = new JSONObject(resultIntent.getStringExtra("SCAN_RESULT"));

                    startStation = scanResult.getInt("startStation");

                    JSONArray insts = scanResult.getJSONArray("input");
                    ArrayList<String> instList = new ArrayList<>(insts.length());
                    for(int i = 0; i < insts.length(); i++) {
                        instList.add(insts.getString(i));
                    }

                    Intent stepIntent = new Intent(this, StepActivity.class);
                    stepIntent.putExtra("instructions", instList);
                    startActivity(stepIntent);
                } else if(requestCode == END_STATION_CODE) {
                    JSONObject scanResult = new JSONObject(resultIntent.getStringExtra("SCAN_RESULT"));
                    Intent logIntent = new Intent("ch.appquest.intent.LOG");

                    if (getPackageManager().queryIntentActivities(logIntent, PackageManager.MATCH_ALL).isEmpty()) {
                        Toast.makeText(this, "Logbook App not Installed", Toast.LENGTH_LONG).show();
                        return;
                    }

                    JSONObject jsonLog = new JSONObject();
                    try {
                        jsonLog.put("task", "Schrittzaehler");
                        jsonLog.put("startStation", startStation);
                        jsonLog.put("endStation", scanResult.getInt("endStation"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    logIntent.putExtra("ch.appquest.logmessage", jsonLog.toString());
                    startActivity(logIntent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
