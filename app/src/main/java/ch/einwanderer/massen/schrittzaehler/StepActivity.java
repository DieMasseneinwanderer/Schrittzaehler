package ch.einwanderer.massen.schrittzaehler;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import ch.appquest.stepcounter.StepCounter;
import ch.appquest.stepcounter.StepListener;

public class StepActivity extends AppCompatActivity {

    private Iterator<StepInstruction> instructions;
    private StepInstruction currentInst;
    private  StepCounter sc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step);

        final TextView txtInst = (TextView) findViewById(R.id.txtInst);

        Intent intent = getIntent();
        List<String> insts = intent.getStringArrayListExtra("instructions");
        List<StepInstruction> instList = new ArrayList<>();
        for(String inst: insts) {
            instList.add(new StepInstruction(inst));
        }
        instructions = instList.iterator();
        currentInst = instructions.next();
        txtInst.setText(currentInst.nextInstruction());

        final SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sc = new StepCounter(new StepListener() {

            @Override
            public void onStep() {
                if(!currentInst.hasNext()) {
                    if(instructions.hasNext()) {
                        currentInst = instructions.next();
                    } else {
                        sensorManager.unregisterListener(sc);
                        finish();
                        return;
                    }
                }
                txtInst.setText(currentInst.nextInstruction());
            }
        });

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(sc, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private class StepInstruction {

        private int steps;
        private String direction;
        boolean isSteps;

        public StepInstruction(String inst) {
            try {
                steps = Integer.parseInt(inst);
                isSteps = true;
            } catch(NumberFormatException e) {
                steps = 1;
                direction = inst;
                isSteps = false;
            }
        }

        public boolean hasNext() {
            return steps > 0;
        }

        public String nextInstruction() {
            steps--;
            return isSteps ? Integer.toString(steps + 1) : direction;
        }
    }
}
