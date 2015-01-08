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
	// 调试用
	private static final String TAG = "BTClientActivity";
	private static final boolean D = true;

	private final static int REQUEST_CONNECT_DEVICE = 1; // 宏定义查询设备句柄

	// 不同消息事件
	private final static int LED_ON = 1;
	private final static int LED_OFF = 2;
	private final static int BUZZ_ON = 3;
	private final static int BUZZ_OFF = 4;
	private final static int RSSI = 5;
	private final static int SEEK_ON = 6;
	private final static int SEEK_OFF = 7;
	private final static int OS_FINISHED = 8;
	
	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB"; // SPP服务UUID号
	
	private boolean LedOn = false; // Led状态
	private boolean BuzzOn = false; // Buzz状态
	BluetoothDevice mDevice = null; // 蓝牙设备
	private String mDeviceAddress = null;	//蓝牙设备MAC地址
	BluetoothSocket mSocket = null; // 蓝牙通信socket
	boolean OSAvailable = true;		//蓝牙输出流是否可用，指之前占用OutStream的Thread是否结束
	boolean isRSSIThreadRunning = false;
	private BluetoothAdapter mBluetooth = BluetoothAdapter.getDefaultAdapter(); // 获取本地蓝牙适配器，即蓝牙设备
	private RSSIThread mRSSIThread = new RSSIThread() ;		//获取RSSI值得线程
	private Handler rssiHandler;								//子线程中rssi消息的处理器
	//为获取rssi，查找到设备和搜索完成action监听器
	private BroadcastReceiver btReceiver=null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (D)
			Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main); // 设置画面为主画面 main.xml

		// 如果无法获取本地蓝牙设备，提示信息，结束程序
		if (mBluetooth == null) {
			Toast.makeText(this, "无法获取手机蓝牙，请确认手机是否有蓝牙功能！", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		// 未连接蓝牙设备时，亮灯、发声按钮禁用
		Button LedBtn = (Button) findViewById(R.id.button_led);
		Button BuzzBtn = (Button) findViewById(R.id.button_buzz);
		LedBtn.setVisibility(Button.INVISIBLE);
		BuzzBtn.setVisibility(Button.INVISIBLE);

		// 打开蓝牙
		new Thread() {
			public void run() {
				if (mBluetooth.isEnabled() == false) {
					mBluetooth.enable();
				}
			}
		}.start();

	}

	// 消息处理句柄
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

	// 获取消息句柄
	public Handler getHandler() {
		return this.mHandler;
	};

	public void refreshPaint(int rssi) {
		Toast.makeText(this, "RSSI: "+rssi, Toast.LENGTH_SHORT)
		.show();		
	}
	
	// Led按键响应
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
					OutputStream os = mSocket.getOutputStream(); // 蓝牙连接输出流
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
					OutputStream os = mSocket.getOutputStream(); // 蓝牙连接输出流
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

	// Buzz按键响应
	public void onBuzzButtonClicked(View v) {
		if (D)
			Log.d(TAG, "onBuzzButtonClicked()");
		Button BuzzBtn = (Button) findViewById(R.id.button_buzz);
		if (!BuzzOn) {//若Buzz当前是不响的
			if (OSAvailable){
				OSAvailable = false;
				BuzzBtn.setText(R.string.buzz_off);
				BuzzOn = true;
				new Thread(){
					public void run(){
						try{
							mSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
							mSocket.connect();
							OutputStream os = mSocket.getOutputStream(); // 蓝牙连接输出流
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
							OutputStream os = mSocket.getOutputStream(); // 蓝牙连接输出流
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

	// 接收活动结果，响应startActivityForResult()
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult()");
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE: // 连接结果，由DeviceListActivity设置返回
			// 响应返回结果
			if (resultCode == Activity.RESULT_OK) { // 连接成功，由DeviceListActivity设置返回
				// MAC地址，由DeviceListActivity设置返回
				mDeviceAddress = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// 得到蓝牙设备句柄
				mDevice = mBluetooth.getRemoteDevice(mDeviceAddress);		
				
				Button btn = (Button) findViewById(R.id.button_select_device);
				btn.setText(R.string.select_device);

				// 启用亮灯、发声功能
				Button LedBtn = (Button) findViewById(R.id.button_led);
				Button BuzzBtn = (Button) findViewById(R.id.button_buzz);
				LedBtn.setVisibility(Button.VISIBLE);
				BuzzBtn.setVisibility(Button.VISIBLE);
				
				//建立与RSSI查询相关的广播接收器
				btReceiver = new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						String action = intent.getAction();

						// 查找到设备action
						if (BluetoothDevice.ACTION_FOUND.equals(action)) {
							if (D)
								Log.d(TAG, "ACTION_FOUND");
							// 得到蓝牙设备
							BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
							if (D)
								Log.d(TAG, device.getName()+"  " +device.getAddress() +"  "+intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI)+ "  "+mDeviceAddress);
							// 如果MAC地址是当前连接的蓝牙的MAC地址
							if (mDeviceAddress.equals(device.getAddress())) {
								//停止扫描
								rssiHandler.sendEmptyMessage(SEEK_OFF);
								// 返回RSSI值
								short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
								if (D)
									Log.d(TAG, "RSSI: "+rssi);
								mHandler.sendMessage(Message.obtain(mHandler, RSSI, rssi));
							}
						// 搜索完成action
						} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
							if (D)
								Log.d(TAG, "ACTION_DISCOVERY_FINISHED");
							//打开扫描
							rssiHandler.sendEmptyMessage(SEEK_ON);
						}
					}
				};
				// 注册接收查找到设备action接收器
				IntentFilter filter = new IntentFilter(
					BluetoothDevice.ACTION_FOUND);
				filter.setPriority(200);
				registerReceiver(btReceiver, filter);

				// 注册查找结束action接收器
				filter = new IntentFilter(
					BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
				filter.setPriority(200);
				registerReceiver(btReceiver, filter);

				if (D)
					Log.d(TAG, "onActivityResult()-->RSSIThread.start()");
				//打开RSSI查询线程
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

	// 查询RSSI线程
	public class RSSIThread extends Thread {
		// 本地蓝牙设备
		private BluetoothAdapter mBtAdapter;
		
		@Override
		public void run() {
			if (D)
				Log.d(TAG, "RSSIThread.run()");
			// 得到本地蓝牙句柄
			mBtAdapter = BluetoothAdapter.getDefaultAdapter();
			//建立消息循环的步骤
		         Looper.prepare();	//初始化Looper
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
		         Looper.loop();	//启动消息循环
			//退出该线程
			if (D)
				Log.d(TAG, "RSSIThread: exit");
		}
		@Override
		public void destroy() {
			// 关闭服务查找
			if (mBtAdapter != null) {
				mBtAdapter.cancelDiscovery();
			}
		}
	}
	
	// 关闭程序调用处理部分
	public void onDestroy() {
		super.onDestroy();
		if (mSocket != null) // 关闭连接socket
			try {
				mSocket.close();
			} catch (IOException e) {
			}
		mBluetooth.disable(); // 关闭蓝牙服务
		// 注销action接收器
		unregisterReceiver(btReceiver);
	}

	// 连接按键响应函数
	public void onConnectButtonClicked(View v) {
		if (mBluetooth.isEnabled() == false) { // 如果蓝牙服务不可用则提示
			Toast.makeText(this, "蓝牙不可用", Toast.LENGTH_LONG).show();
			return;
		}

		// 打开DeviceListActivity进行设备搜索
		if (mSocket != null) {
			// 关闭连接socket
			try {
				mSocket.close();
				mSocket = null;
				//bRun = false;

				// 未连接蓝牙设备时，亮灯、发声按钮禁用
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
		// 注销action接收器
		if (btReceiver!=null) {
			if (D)
				Log.d(TAG, "unregistering btReceiver");
			unregisterReceiver(btReceiver);
			btReceiver=null;
		}
		Intent serverIntent = new Intent(this, DeviceListActivity.class); // 跳转到设备选择列表
		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE); // 设置返回宏定义
		return;
	}

	// 退出按键响应函数
	public void onQuitButtonClicked(View v) {
		finish();
	}
}