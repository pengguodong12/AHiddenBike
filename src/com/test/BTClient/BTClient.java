package com.test.BTClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.test.BTClient.DeviceListActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Looper;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class BTClient extends Activity {
	// ������
	private static final String TAG = "BTClientActivity";
	private static final boolean D = true;

	private final static int REQUEST_CONNECT_DEVICE = 1; // �궨���ѯ�豸���

	// ��ͬ��Ϣ�¼�
	private final static int LED_ON = 1;
	private final static int LED_OFF = 2;
	private final static int BUZZ_ON = 3;
	private final static int BUZZ_OFF = 4;
	private final static int RSSI = 5;
	private final static int SEEK_ON = 6;
	private final static int SEEK_OFF = 7;
	private final static int OS_FINISHED = 8;
	
	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB"; // SPP����UUID��
	
	private boolean LedOn = false; // Led״̬
	private boolean BuzzOn = false; // Buzz״̬
	BluetoothDevice mDevice = null; // �����豸
	private String mDeviceAddress = null;	//�����豸MAC��ַ
	BluetoothSocket mSocket = null; // ����ͨ��socket
	boolean OSAvailable = true;		//����������Ƿ���ã�ָ֮ǰռ��OutStream��Thread�Ƿ����
	boolean isRSSIThreadRunning = false;
	private BluetoothAdapter mBluetooth = BluetoothAdapter.getDefaultAdapter(); // ��ȡ�����������������������豸
	private RSSIThread mRSSIThread = new RSSIThread() ;		//��ȡRSSIֵ���߳�
	private Handler rssiHandler;								//���߳���rssi��Ϣ�Ĵ�����
	//Ϊ��ȡrssi�����ҵ��豸���������action������
	private BroadcastReceiver btReceiver=null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (D)
			Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main); // ���û���Ϊ������ main.xml

		// ����޷���ȡ���������豸����ʾ��Ϣ����������
		if (mBluetooth == null) {
			Toast.makeText(this, "�޷���ȡ�ֻ���������ȷ���ֻ��Ƿ����������ܣ�", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		// δ���������豸ʱ�����ơ�������ť����
		Button LedBtn = (Button) findViewById(R.id.button_led);
		Button BuzzBtn = (Button) findViewById(R.id.button_buzz);
		LedBtn.setVisibility(Button.INVISIBLE);
		BuzzBtn.setVisibility(Button.INVISIBLE);

		// ������
		new Thread() {
			public void run() {
				if (mBluetooth.isEnabled() == false) {
					mBluetooth.enable();
				}
			}
		}.start();

	}

	// ��Ϣ������
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RSSI:
				if (D)
        				Log.d(TAG, "calling refreshPaint");
				refreshPaint(Short.parseShort(msg.obj.toString()));
				break;
			case OS_FINISHED:
				OSAvailable = true;
				break;
			default:
				break;
			}
		}
	};

	// ��ȡ��Ϣ���
	public Handler getHandler() {
		return this.mHandler;
	};

	public void refreshPaint(int rssi) {
		Toast.makeText(this, "RSSI: "+rssi, Toast.LENGTH_SHORT)
		.show();		
	}
	
	// Led������Ӧ
	public void onLedButtonClicked(View v) {
		if (D)
			Log.d(TAG, "onLedButtonClicked()");		
		Button LedBtn = (Button) findViewById(R.id.button_led);
		if (!LedOn) {
			if (OSAvailable){
			OSAvailable = false;
			LedBtn.setText(R.string.led_off);
			LedOn = true;
			new Thread() {
				public void run(){
					try{
					mSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
					mSocket.connect();
					OutputStream os = mSocket.getOutputStream(); // �������������
					os.write(1);
					os.close();
					mSocket.close();
					}catch (IOException e) {
					}
					mHandler.sendMessage(Message.obtain(mHandler, OS_FINISHED));
				}
			}.start();
			}
		}
		else {
			if (OSAvailable){
				OSAvailable = false;
			LedBtn.setText(R.string.led_on);
			LedOn = false;
			new Thread() {
				public void run(){
					try{
					mSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
					mSocket.connect();
					OutputStream os = mSocket.getOutputStream(); // �������������
					os.write(2);
					os.close();
					mSocket.close();
					}catch (IOException e) {
					}
					mHandler.sendMessage(Message.obtain(mHandler, OS_FINISHED));
				}
			}.start();
			}
		}
	}

	// Buzz������Ӧ
	public void onBuzzButtonClicked(View v) {
		if (D)
			Log.d(TAG, "onBuzzButtonClicked()");
		Button BuzzBtn = (Button) findViewById(R.id.button_buzz);
		if (!BuzzOn) {//��Buzz��ǰ�ǲ����
			if (OSAvailable){
				OSAvailable = false;
				BuzzBtn.setText(R.string.buzz_off);
				BuzzOn = true;
				new Thread(){
					public void run(){
						try{
							mSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
							mSocket.connect();
							OutputStream os = mSocket.getOutputStream(); // �������������
							os.write(3);
							os.close();
							mSocket.close();
						} catch (IOException e) {
						}
						mHandler.sendMessage(Message.obtain(mHandler, OS_FINISHED));
					}
				}.start();
			}	
		} else {
			if (OSAvailable){
				OSAvailable = false;
				BuzzBtn.setText(R.string.buzz_on);
				BuzzOn = false;
				new Thread(){
					public void run(){
						try{
							mSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
							mSocket.connect();
							OutputStream os = mSocket.getOutputStream(); // �������������
							os.write(4);
							os.close();
						} catch (IOException e) {
						}
						mHandler.sendMessage(Message.obtain(mHandler, OS_FINISHED));
					}
				}.start();
			}		
		}
	}

	// ���ջ�������ӦstartActivityForResult()
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult()");
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE: // ���ӽ������DeviceListActivity���÷���
			// ��Ӧ���ؽ��
			if (resultCode == Activity.RESULT_OK) { // ���ӳɹ�����DeviceListActivity���÷���
				// MAC��ַ����DeviceListActivity���÷���
				mDeviceAddress = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// �õ������豸���
				mDevice = mBluetooth.getRemoteDevice(mDeviceAddress);		
				
				Button btn = (Button) findViewById(R.id.button_select_device);
				btn.setText(R.string.select_device);

				// �������ơ���������
				Button LedBtn = (Button) findViewById(R.id.button_led);
				Button BuzzBtn = (Button) findViewById(R.id.button_buzz);
				LedBtn.setVisibility(Button.VISIBLE);
				BuzzBtn.setVisibility(Button.VISIBLE);
				
				//������RSSI��ѯ��صĹ㲥������
				btReceiver = new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						String action = intent.getAction();

						// ���ҵ��豸action
						if (BluetoothDevice.ACTION_FOUND.equals(action)) {
							if (D)
								Log.d(TAG, "ACTION_FOUND");
							// �õ������豸
							BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
							if (D)
								Log.d(TAG, device.getName()+"  " +device.getAddress() +"  "+intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI)+ "  "+mDeviceAddress);
							// ���MAC��ַ�ǵ�ǰ���ӵ�������MAC��ַ
							if (mDeviceAddress.equals(device.getAddress())) {
								//ֹͣɨ��
								rssiHandler.sendEmptyMessage(SEEK_OFF);
								// ����RSSIֵ
								short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
								if (D)
									Log.d(TAG, "RSSI: "+rssi);
								mHandler.sendMessage(Message.obtain(mHandler, RSSI, rssi));
							}
						// �������action
						} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
							if (D)
								Log.d(TAG, "ACTION_DISCOVERY_FINISHED");
							//��ɨ��
							rssiHandler.sendEmptyMessage(SEEK_ON);
						}
					}
				};
				// ע����ղ��ҵ��豸action������
				IntentFilter filter = new IntentFilter(
					BluetoothDevice.ACTION_FOUND);
				filter.setPriority(200);
				registerReceiver(btReceiver, filter);

				// ע����ҽ���action������
				filter = new IntentFilter(
					BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
				filter.setPriority(200);
				registerReceiver(btReceiver, filter);

				if (D)
					Log.d(TAG, "onActivityResult()-->RSSIThread.start()");
				//��RSSI��ѯ�߳�
				if (isRSSIThreadRunning == false) {
					mRSSIThread.start();
					isRSSIThreadRunning = true;
				}
				if (D)
					Log.d(TAG, "Fisrt start bluetooth discovery");
				new Thread() {
				      	public void run() {
				       		BluetoothAdapter tmpBluetooth = BluetoothAdapter.getDefaultAdapter();
				       		if (D && tmpBluetooth.startDiscovery())
				       			Log.d(TAG, "Fisrt start bluetooth discovery success");
				      	}
				  }.start();
			}
			break;
		default:
			break;
		}
	}

	// ��ѯRSSI�߳�
	public class RSSIThread extends Thread {
		// ���������豸
		private BluetoothAdapter mBtAdapter;
		
		@Override
		public void run() {
			if (D)
				Log.d(TAG, "RSSIThread.run()");
			// �õ������������
			mBtAdapter = BluetoothAdapter.getDefaultAdapter();
			//������Ϣѭ���Ĳ���
		         Looper.prepare();	//��ʼ��Looper
		         rssiHandler = new Handler() {
		                public void handleMessage (Message msg) {
		                	switch(msg.what) {
		                	case SEEK_ON:
		                		mBtAdapter.startDiscovery();
		                		if (D)
		        				Log.d(TAG, "RSSIThread: startDiscovery");
		                		break;
		                	case SEEK_OFF:
		                		mBtAdapter.cancelDiscovery();
		                		if (D)
		        				Log.d(TAG, "RSSIThread: cancelDiscovery()");
		                		break;
		                	default:
		                		break;
		                    }
		                }
		          };	          
		         Looper.loop();	//������Ϣѭ��
			//�˳����߳�
			if (D)
				Log.d(TAG, "RSSIThread: exit");
		}
		@Override
		public void destroy() {
			// �رշ������
			if (mBtAdapter != null) {
				mBtAdapter.cancelDiscovery();
			}
		}
	}
	
	// �رճ�����ô�����
	public void onDestroy() {
		super.onDestroy();
		if (mSocket != null) // �ر�����socket
			try {
				mSocket.close();
			} catch (IOException e) {
			}
		mBluetooth.disable(); // �ر���������
		// ע��action������
		unregisterReceiver(btReceiver);
	}

	// ���Ӱ�����Ӧ����
	public void onConnectButtonClicked(View v) {
		if (mBluetooth.isEnabled() == false) { // ����������񲻿�������ʾ
			Toast.makeText(this, "����������", Toast.LENGTH_LONG).show();
			return;
		}

		// ��DeviceListActivity�����豸����
		if (mSocket != null) {
			// �ر�����socket
			try {
				mSocket.close();
				mSocket = null;
				//bRun = false;

				// δ���������豸ʱ�����ơ�������ť����
				Button LedBtn = (Button) findViewById(R.id.button_led);
				Button BuzzBtn = (Button) findViewById(R.id.button_buzz);
				LedOn = false;
				BuzzOn = false;
				LedBtn.setText(R.string.led_on);
				BuzzBtn.setText(R.string.buzz_on);
				LedBtn.setVisibility(Button.INVISIBLE);
				BuzzBtn.setVisibility(Button.INVISIBLE);
			} catch (IOException e) {
			}
		}
		// ע��action������
		if (btReceiver!=null) {
			if (D)
				Log.d(TAG, "unregistering btReceiver");
			unregisterReceiver(btReceiver);
			btReceiver=null;
		}
		Intent serverIntent = new Intent(this, DeviceListActivity.class); // ��ת���豸ѡ���б�
		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE); // ���÷��غ궨��
		return;
	}

	// �˳�������Ӧ����
	public void onQuitButtonClicked(View v) {
		finish();
	}
}