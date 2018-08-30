package com.lannbox.RNSLab;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;


public class subActivity extends Activity {

    static float[] entry = new float[1024];
    int j=0;
    private int state;
    final private static int STATE_BLUETOOTH_OFF = 1;
    final private static int STATE_DISCONNECTED = 2;
    final private static int STATE_CONNECTING = 3;
    final private static int STATE_CONNECTED = 4;

    private boolean scanStarted;
    private boolean scanning;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;

    private RFduinoService rfduinoService;

    private Button enableBluetoothButton;
    private TextView scanStatusText;
    private Button scanButton;
    private TextView deviceInfoText;
    private TextView connectionStatusText;
    private Button connectButton;
    private EditData valueEdit1;
    private Button sendZeroButton;
    private Button sendValueButton;
    private Button clearButton;
    private LinearLayout dataLayout;



    /*private final BroadcastReceiver rfduinoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context,Intent intent ) {
            final String action = intent.getAction();
            if (RFduinoService.ACTION_DATA_AVAILABLE.equals(action)) {
               drawnow(intent.getByteArrayExtra(RFduinoService.EXTRA_DATA));
            }
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        setTitle("RNSlab");


        Intent intent = getIntent();

        Data data =(Data)intent.getSerializableExtra("data");
        if(entry != null)
        {
            drawnow(data.entry, data.length);

        }

        valueEdit1 = (EditData) findViewById(R.id.value1);
        valueEdit1.setImeOptions(EditorInfo.IME_ACTION_SEND);
        valueEdit1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendValueButton.callOnClick();
                    return true;
                }
                return false;
            }
        });


        sendValueButton = (Button) findViewById(R.id.button);
        sendValueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rfduinoService.send(valueEdit1.getData());
            }
        });


        }
    public void  drawnow(float[] val,int length){

        LineChart chart =(LineChart)findViewById(R.id.chart);

        ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
        ArrayList<String> xVals = new ArrayList<String>();

        LineDataSet setComp1 = new LineDataSet(valsComp1, "ROOM 1");
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setComp1);



        for(int i=0; i<length; i++)
        {
            xVals.add(String.valueOf(i+1));
            valsComp1.add(new Entry(val[i],i));

        }



        LineData data = new LineData(xVals, dataSets);



        chart.setData(data);
        chart.invalidate();





    }






    private void upgradeState(int newState) {
        if (newState > state) {
            updateState(newState);
        }
    }

    private void downgradeState(int newState) {
        if (newState < state) {
            updateState(newState);
        }
    }

    private void updateState(int newState) {
        state = newState;
        updateUi();
    }

    private void updateUi() {
        // Enable Bluetooth
        boolean on = state > STATE_BLUETOOTH_OFF;
        enableBluetoothButton.setEnabled(!on);
        enableBluetoothButton.setText(on ? "Bluetooth enabled" : "Enable Bluetooth");
        scanButton.setEnabled(on);

        // Scan
        if (scanStarted && scanning) {
            scanStatusText.setText("Scanning...");
            scanButton.setText("Stop Scan");
            scanButton.setEnabled(true);
        } else if (scanStarted) {
            scanStatusText.setText("Scan started...");
            scanButton.setEnabled(false);
        } else {
            scanStatusText.setText("");
            scanButton.setText("Scan");
            scanButton.setEnabled(true);
        }

        // Connect
        boolean connected = false;
        String connectionText = "Disconnected";
        if (state == STATE_CONNECTING) {
            connectionText = "Connecting...";
        } else if (state == STATE_CONNECTED) {
            connected = true;
            connectionText = "Connected";
        }
        connectionStatusText.setText(connectionText);
        connectButton.setEnabled(bluetoothDevice != null && state == STATE_DISCONNECTED);

        // Send
        sendZeroButton.setEnabled(connected);
        sendValueButton.setEnabled(connected);
    }


    public  int byteArrayToInt(byte bytes[]) {
        return ((((int)bytes[0] & 0xff) << 24) |
                (((int)bytes[1] & 0xff) << 16) |
                (((int)bytes[2] & 0xff) << 8) |
                (((int)bytes[3] & 0xff)));
    }
    public float byteArrayToFloat(byte bytes[]) {
        int value =  byteArrayToInt(bytes);
        return Float.intBitsToFloat(value);
    }

    public byte[] floatToByteArray(float value) {
        int floatValue =  Float.floatToIntBits(value);
        return intToByteArray(floatValue);
    }
    public  byte[] intToByteArray(int value) {
        byte[] byteArray = new byte[4];
        byteArray[0] = (byte)(value >> 24);
        byteArray[1] = (byte)(value >> 16);
        byteArray[2] = (byte)(value >> 8);
        byteArray[3] = (byte)(value);
        return byteArray;
    }
    public int byteToint(byte[] arr){
        return (arr[0] & 0xff)<<24 | (arr[1] & 0xff)<<16 |
                (arr[2] & 0xff)<<8 | (arr[3] & 0xff);
    }

    }

