package com.wy.bluetoothprinter;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;



import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class PrintActivity extends AppCompatActivity {

    @BindView(R.id.tv_printdevice)
    TextView tvPrintdevice;
    @BindView(R.id.tv_connect_state)
    TextView tvConnectState;
    @BindView(R.id.et_print_data)
    EditText etPrintData;
    @BindView(R.id.btn_print)
    Button btnPrint;
    @BindView(R.id.btn_command)
    Button btnCommand;
    @BindView(R.id.btn_connect)
    Button btnConnect;


    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


    BluetoothDevice device;
    private boolean isConnection;



    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);
        ButterKnife.bind(this);

        initData();
        initView();
        startConnect();
    }

    private void initData() {
        Intent intent = getIntent();
        device = intent.getParcelableExtra(MainActivity.DEVICE);
        if (device == null) return;

    }

    private void initView() {
        String name = device.getName() == null ? device.getAddress():device.getName();
        tvPrintdevice.setText(name);
    }

    /**
     * 连接蓝牙设备
     */
    private void startConnect() {
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();

            isConnection = bluetoothSocket.isConnected();

            if (bluetoothAdapter.isDiscovering()) {
                System.out.println("关闭适配器！");
                bluetoothAdapter.isDiscovering();
            }
            setConnectResult(isConnection);
        } catch (Exception e) {
            setConnectResult(false);
        }
    }

    private void setConnectResult(boolean result){
        tvConnectState.setText(result?"连接成功！":"连接失败！");
        btnConnect.setVisibility(result? View.GONE : View.VISIBLE);
        btnConnect.setEnabled(!result);
    }

    @OnClick({R.id.btn_print, R.id.btn_command,R.id.btn_connect})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_connect:
                startConnect();
                break;
            case R.id.btn_command:
                selectCommand();
                break;
            case R.id.btn_print:
                print();
                break;


        }
    }







    /**
     * 选择指令
     */
    public void selectCommand() {
        new AlertDialog.Builder(this)
                .setTitle("请选择指令")
                .setItems(CommandsUtil.ITEMS, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            outputStream.write(CommandsUtil.BYTE_COMMANDS[which]);
                        } catch (IOException e) {
                            ToastUtil.showToast(PrintActivity.this,"设置指令失败！");
                        }
                    }
                })
                .create()
                .show();
    }

    /**
     * 打印数据
     */
    public void print() {

        String sendData = etPrintData.getText().toString();

        if (TextUtils.isEmpty(sendData)){
            ToastUtil.showToast(PrintActivity.this,"请输入打印内容！");
            return ;
        }
        if (isConnection) {
            System.out.println("开始打印！！");
            try {
                sendData="\n\n\n"+sendData + "\n\n\n\n";
                byte[] data = sendData.getBytes("gbk");
                outputStream.write(data, 0, data.length);
                outputStream.flush();
            } catch (IOException e) {
                ToastUtil.showToast(PrintActivity.this,"发送失败！");
            }
        } else {
            ToastUtil.showToast(PrintActivity.this,"设备未连接，请重新连接！");

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect();
    }

    /**
     * 断开蓝牙设备连接
     */
    public void disconnect() {
        System.out.println("断开蓝牙设备连接");
        try {
            bluetoothSocket.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
