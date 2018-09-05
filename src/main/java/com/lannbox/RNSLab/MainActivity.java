package com.lannbox.RNSLab;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.renderscript.Sampler;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

//import com.github.mikephil.charting.charts.LineChart;
//import com.github.mikephil.charting.components.YAxis;
//import com.github.mikephil.charting.data.Entry;
//import com.github.mikephil.charting.data.LineData;
//import com.github.mikephil.charting.data.LineDataSet;
//import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.w3c.dom.Text;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
//import java.util.Timer;
//import java.util.TimerTask;
import java.util.UUID;



public class MainActivity extends Activity implements BluetoothAdapter.LeScanCallback {
    // State machine
    final private static int STATE_BLUETOOTH_OFF = 1;
    final private static int STATE_DISCONNECTED = 2;
    final private static int STATE_CONNECTING = 3;
    final private static int STATE_CONNECTED = 4;
//    int j1=0;
//    int j2=0;
//    int j3=0;
//    int j4=0;
    private int state;
//    static short[] Room1_ch1 = new short[1024];
//    static short[] Room1_ch2 = new short[1024];
//    static short[] Room1_ch3 = new short[1024];
//    static short[] Room1_ch4 = new short[1024];
    boolean flag  =false;
    boolean C = true;
    boolean A = false;
    boolean B = false;
    boolean flag_c = false;
//    boolean flag1 = true;
//    boolean flag2 =false;
//    boolean flag3 =false;
//    boolean flag4 =false;
//    byte ch_valid;
//    boolean ch_valid1;
//    boolean ch_valid2;
//    boolean ch_valid3;
//    boolean ch_valid4;
//    byte currentChannel=3;
    boolean ch_phase=true;

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
    private Button disconnectButton;
    private Button sendZeroButton;
    private Button sendValueButton;
    private Button modeBtn1;
    private Button modeBtn2;
    private Button modeBtn3;
    private Button modeBtn4;
    private TextView modeText;
    private Button clearButton;
    private LinearLayout dataLayout;

    //public Timer timer1;
    //public Timer timer2;
    //public Timer timer3;
    boolean connected;
    //static TimerTask tt1;
    //static TimerTask tt2;
    //static TimerTask tt3;

    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
            if (state == BluetoothAdapter.STATE_ON) {
                upgradeState(STATE_DISCONNECTED);
            } else if (state == BluetoothAdapter.STATE_OFF) {
                downgradeState(STATE_BLUETOOTH_OFF);
            }
        }
    };

    private final BroadcastReceiver scanModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanning = (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_NONE);
            scanStarted &= scanning;
            updateUi();
        }
    };


    private final ServiceConnection rfduinoServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            rfduinoService = ((RFduinoService.LocalBinder) service).getService();
            if (rfduinoService.initialize()) {
                if (rfduinoService.connect(bluetoothDevice.getAddress())) {
                    upgradeState(STATE_CONNECTING);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            rfduinoService = null;
            downgradeState(STATE_DISCONNECTED);
        }
    };

    private final BroadcastReceiver rfduinoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (RFduinoService.ACTION_CONNECTED.equals(action)) {
                upgradeState(STATE_CONNECTED);
            } else if (RFduinoService.ACTION_DISCONNECTED.equals(action)) {
                downgradeState(STATE_DISCONNECTED);
            } else if (RFduinoService.ACTION_DATA_AVAILABLE.equals(action)) {
               addData(intent.getByteArrayExtra(RFduinoService.EXTRA_DATA));
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("");

        ActionBar actionBar = getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
        setContentView(R.layout.activity_main);



        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Bluetooth
        enableBluetoothButton = (Button) findViewById(R.id.enableBluetooth);
        enableBluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableBluetoothButton.setEnabled(false);
                enableBluetoothButton.setText(
                        bluetoothAdapter.enable() ? "Enabling bluetooth..." : "Enable failed!");
            }
        });

        // Find Device
        scanStatusText = (TextView) findViewById(R.id.scanStatus);

        scanButton = (Button) findViewById(R.id.scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanStarted = true;
                bluetoothAdapter.startLeScan(
                        new UUID[]{ RFduinoService.UUID_SERVICE },
                        MainActivity.this);
            }
        });

        // Device Info
        deviceInfoText = (TextView) findViewById(R.id.deviceInfo);

        // Connect Device
        connectionStatusText = (TextView) findViewById(R.id.connectionStatus);

        //final Timer timer1 = new Timer(true);
        //final Timer timer2 = new Timer(true);
        //final Timer timer3 = new Timer(true);


        connectButton = (Button) findViewById(R.id.connect);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                connectionStatusText.setText("Connecting...");
                Intent rfduinoIntent = new Intent(MainActivity.this, RFduinoService.class);
                bindService(rfduinoIntent, rfduinoServiceConnection, BIND_AUTO_CREATE);

                EditText editText = (EditText)findViewById(R.id.period);
                String period = editText.getText().toString();
                int val = Integer.parseInt(period);
                int data = 1000*val;

                //tt1 = timerTaskMaker1();
                //tt2 = timerTaskMaker2();
                //tt3 = timerTaskMaker3();
                //timer1.schedule(tt1,3000);
                //timer2.schedule(tt2,5000);
                //timer3.schedule(tt3,15000,data);

            }
        });
        // disconnect device

       /* TimerTask  timerTask = new TimerTask() {
            @Override
            public void run() {
                if(ch_phase){
                    rfduinoService.send(new byte[]{'c'});
                    flag_c=true;
                }
                else if(flag_c){
                    rfduinoService.send(new byte[]{'a'});
                    flag_c =false;
                }
                else{
                    rfduinoService.send(new byte[]{'b'});
                }
            }
        }*/

        disconnectButton = (Button)findViewById(R.id.disconnect);
        disconnectButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                v.setEnabled(false);



                connectionStatusText.setText("Disconnected");
                rfduinoService.send(new byte[]{'d'});
                //ch_phase = true;
                //tt1.cancel();
                //tt2.cancel();
                //tt3.cancel();
                /*timer1.cancel();
                timer2.cancel();
                timer3.cancel();*/
                connected = false;
                rfduinoService.disconnect();
                unbindService(rfduinoServiceConnection);
            }
        });












        /*
        final ToggleButton tb2=(ToggleButton)findViewById(R.id.toggleButton7);
        tb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tb2.isChecked()){
                    flag = true;
                    //draw1();
                    //draw2();
                    //draw3();
                    //draw4();

                }
                else{
                    flag = false;
                    //LineChart chart1 =(LineChart)findViewById(R.id.chart1);
                    //LineChart chart2 =(LineChart)findViewById(R.id.chart2);
                    //LineChart chart3 =(LineChart)findViewById(R.id.chart3);
                    //LineChart chart4 =(LineChart)findViewById(R.id.chart4);
                    //LineData data = new LineData();

                    //chart1.setData(data);
                    //chart1.invalidate();
                    //chart2.setData(data);
                    //chart2.invalidate();
                    //chart3.setData(data);
                    //chart3.invalidate();
                    //chart4.setData(data);
                    //chart4.invalidate();

                }
            }});
            */



    }
    /*
    public TimerTask timerTaskMaker3(){
        TimerTask tempTask = new TimerTask() {
            @Override
            public void run() {

                    rfduinoService.send(new byte[]{'b'});

            }
        };
        return tempTask;

    }
    public TimerTask timerTaskMaker2(){
        TimerTask tempTask = new TimerTask() {
            @Override
            public void run() {

                    rfduinoService.send(new byte[]{'a'});

            }
        };
        return tempTask;

    }
    public TimerTask timerTaskMaker1(){
        TimerTask tempTask = new TimerTask() {
            @Override
            public void run() {

                    rfduinoService.send(new byte[]{'c'});

            }
        };
        return tempTask;

    }
    */

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(scanModeReceiver, new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
        registerReceiver(bluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(rfduinoReceiver, RFduinoService.getIntentFilter());

        updateState(bluetoothAdapter.isEnabled() ? STATE_DISCONNECTED : STATE_BLUETOOTH_OFF);

    }

    @Override
    protected void onStop() {
        super.onStop();

        bluetoothAdapter.stopLeScan(this);

        unregisterReceiver(scanModeReceiver);
        unregisterReceiver(bluetoothStateReceiver);
        unregisterReceiver(rfduinoReceiver);
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
        connected = false;
        String connectionText = "Disconnected";
        if (state == STATE_CONNECTING) {
            connectionText = "Connecting...";
        } else if (state == STATE_CONNECTED) {
            connected = true;
            connectionText = "Connected";
        }
        connectionStatusText.setText(connectionText);
        connectButton.setEnabled(bluetoothDevice != null && state == STATE_DISCONNECTED);
        disconnectButton.setEnabled(bluetoothDevice != null && state == STATE_CONNECTED);
        // Send
        sendZeroButton.setEnabled(connected);
        sendValueButton.setEnabled(connected);
        modeBtn1.setEnabled(connected);
        modeBtn2.setEnabled(connected);
        modeBtn3.setEnabled(connected);
        modeBtn4.setEnabled(connected);
    }

    public void addData(byte[] data) {
        View view = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, dataLayout, false);

        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
        TextView text2 = (TextView) view.findViewById(android.R.id.text2);

        float f = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        String fdata = String.format("%.2f", f);

        Calendar calender = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if(fdata != null)
        {
            text1.setText(fdata);
            text1.setTextColor(Color.BLACK);
            text2.setText(dateFormat.format(calender.getTime()));
        }

        dataLayout.addView(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        /*String ascii = HexAsciiHelper.bytesToAsciiMaybe(data);
        if (ascii != null) {
            TextView text2 = (TextView) view.findViewById(android.R.id.text2);
            //text2.setText(ascii);
        }*/


        //text1.setText(HexAsciiHelper.bytesToHex(data));
        //dataLayout.addView(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //return  HexAsciiHelper.bytesToHex(data);


        //if (ch_phase) {
            //ch_valid1 = (1 ==
        // (data[0] & 1));
            //ch_valid2 = (1 == (data[0] & 2));
            //ch_valid3 = (1 == (data[0] & 4));
            //ch_valid4 = (1 == (data[0] & 8));
            //ch_valid = data[0];
            //ch_phase = false;
        //}
        //else {
            //currentChannel = nextChannel(currentChannel,ch_valid);
            //if(currentChannel == 0)
            //        drawnow1(data);
            //else if(currentChannel == 1)
            //    drawnow2(data);
            //else if(currentChannel == 2)
            //    drawnow3(data);
            //else if(currentChannel == 3)
            //    drawnow4(data);
            //}

    }
    /*
    public void Time(){

    }
    */
/*
    public byte nextChannel(byte currentChannel,byte ch_valid)
    {
        byte i;
        int nC;
        boolean[] valid= new boolean[4];

        nC = currentChannel + 1;
        if(nC == 4)   nC = 0;

        for(i=0;i<4;i++)
        {
            valid[i] = (1 == ((ch_valid >> i) & 1));
        }

        while(valid[nC] != true)
        {
            nC = nC+1;
            if(nC == 4)   nC = 0;
        }

        return (byte)nC;
    }
*/
/*
    public void  drawnow1(byte[] val){

        LineChart chart =(LineChart)findViewById(R.id.chart1);

        ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
        ArrayList<String> xVals = new ArrayList<String>();

        LineDataSet setComp1 = new LineDataSet(valsComp1, "ROOM 1");
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setComp1);
        setComp1.setLineWidth(4);
        short[] data1= new short[1024];
            //if(val[1] <0)
            //    data1[1] = (int)val[1] + 256;
            //else
            //    data1[1] = (int)val[1];


            if(val[0] < 0)
                data1[0] = (short) ((short)val[0] + (short)256);
            else
                data1[0] = (short)val[0];


                Room1_ch1[j1] = (short) (((val[1]) & 0xff) << 8 | data1[0]);


            for(int i=0; i<j1+1; i++)
            {
                xVals.add(String.valueOf(i+1));
                valsComp1.add(new Entry(Room1_ch1[i],i));

            }

            j1++;

        LineData data = new LineData(xVals, dataSets);
        if(flag){

            chart.setData(data);
            chart.invalidate();

        }
    }
    public void  drawnow2(byte[] val){

        LineChart chart =(LineChart)findViewById(R.id.chart2);

        ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
        ArrayList<String> xVals = new ArrayList<String>();

        LineDataSet setComp1 = new LineDataSet(valsComp1, "ROOM 1");
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setComp1);
        setComp1.setLineWidth(4);
        short[] data1= new short[1024];
        //if(val[1] <0)
        //    data1[1] = (int)val[1] + 256;
        //else
        //    data1[1] = (int)val[1];


        if(val[0] < 0)
            data1[0] = (short) (val[0] + 256);
        else
            data1[0] = val[0];


        Room1_ch2[j2] = (short) (((val[1]) & 0xff) << 8 | data1[0]);
        for(int i=0; i<j2+1; i++)
        {
            xVals.add(String.valueOf(i+1));
            valsComp1.add(new Entry(Room1_ch2[i],i));

        }

        j2++;

        LineData data = new LineData(xVals, dataSets);
        if(flag){

            chart.setData(data);
            chart.invalidate();

        }
    }
    public void  drawnow3(byte[] val){

        LineChart chart =(LineChart)findViewById(R.id.chart3);

        ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
        ArrayList<String> xVals = new ArrayList<String>();

        LineDataSet setComp1 = new LineDataSet(valsComp1, "ROOM 1");
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp1.setLineWidth(4);
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setComp1);

        short[] data1= new short[1024];
        //if(val[1] <0)
        //    data1[1] = (int)val[1] + 256;
        //else
        //    data1[1] = (int)val[1];


        if(val[0] < 0)
            data1[0] = (short) (val[0] + 256);
        else
            data1[0] = val[0];


        Room1_ch3[j3] = (short) (((val[1]) & 0xff) << 8 | data1[0]);
        for(int i=0; i<j3+1; i++)
        {
            xVals.add(String.valueOf(i+1));
            valsComp1.add(new Entry(Room1_ch3[i],i));

        }

        j3++;

        LineData data = new LineData(xVals, dataSets);
        if(flag){

            chart.setData(data);
            chart.invalidate();

        }
    }
    public void  drawnow4(byte[] val){

        LineChart chart =(LineChart)findViewById(R.id.chart4);

        ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
        ArrayList<String> xVals = new ArrayList<String>();

        LineDataSet setComp1 = new LineDataSet(valsComp1, "ROOM 1");
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp1.setLineWidth(4);
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setComp1);

        short[] data1= new short[1024];
        //if(val[1] <0)
        //    data1[1] = (int)val[1] + 256;
        //else
        //    data1[1] = (int)val[1];


        if(val[0] < 0)
            data1[0] = (short) (val[0] + 256);
        else
            data1[0] = val[0];


        Room1_ch4[j4] = (short) (((val[1]) & 0xff) << 8 | data1[0]);
        for(int i=0; i<j4+1; i++)
        {
            xVals.add(String.valueOf(i+1));
            valsComp1.add(new Entry(Room1_ch4[i],i));

        }
        j4++;
        LineData data = new LineData(xVals, dataSets);
        if(flag){

            chart.setData(data);
            chart.invalidate();

        }
    }

    public void  draw1(){

        LineChart chart =(LineChart)findViewById(R.id.chart1);

        ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
        ArrayList<String> xVals = new ArrayList<String>();

        LineDataSet setComp1 = new LineDataSet(valsComp1, "ROOM 1");
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp1.setLineWidth(4);
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setComp1);

        for(int i=0; i<j1; i++)
        {
            xVals.add(String.valueOf(i+1));
            valsComp1.add(new Entry(Room1_ch1[i],i));

        }

        LineData data = new LineData(xVals, dataSets);

        chart.setData(data);
        chart.invalidate();

    }
    public void  draw2(){

        LineChart chart =(LineChart)findViewById(R.id.chart2);

        ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
        ArrayList<String> xVals = new ArrayList<String>();

        LineDataSet setComp1 = new LineDataSet(valsComp1, "ROOM 1");
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp1.setLineWidth(4);
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setComp1);

        for(int i=0; i<j2; i++)
        {
            xVals.add(String.valueOf(i+1));
            valsComp1.add(new Entry(Room1_ch2[i],i));

        }

        LineData data = new LineData(xVals, dataSets);

        chart.setData(data);
        chart.invalidate();
    }
    public void  draw3(){

        LineChart chart =(LineChart)findViewById(R.id.chart3);

        ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
        ArrayList<String> xVals = new ArrayList<String>();

        LineDataSet setComp1 = new LineDataSet(valsComp1, "ROOM 1");
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp1.setLineWidth(4);
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setComp1);

        for(int i=0; i<j3; i++)
        {
            xVals.add(String.valueOf(i+1));
            valsComp1.add(new Entry(Room1_ch3[i],i));

        }

        LineData data = new LineData(xVals, dataSets);

        chart.setData(data);
        chart.invalidate();
    }
    public void  draw4(){

        LineChart chart =(LineChart)findViewById(R.id.chart4);

        ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
        ArrayList<String> xVals = new ArrayList<String>();

        LineDataSet setComp1 = new LineDataSet(valsComp1, "ROOM 1");
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp1.setLineWidth(4);
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setComp1);

        for(int i=0; i<j4; i++)
        {
            xVals.add(String.valueOf(i+1));
            valsComp1.add(new Entry(Room1_ch4[i],i));

        }

        LineData data = new LineData(xVals, dataSets);
        chart.setData(data);
        chart.invalidate();
    }
*/
    @Override
    public void onLeScan(BluetoothDevice device, final int rssi, final byte[] scanRecord) {
        bluetoothAdapter.stopLeScan(this);
        bluetoothDevice = device;

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceInfoText.setText(
                        BluetoothHelper.getDeviceInfoText(bluetoothDevice, rssi, scanRecord));
                updateUi();

            }
        });
    }

}

