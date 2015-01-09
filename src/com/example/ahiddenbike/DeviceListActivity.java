/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ahiddenbike;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class DeviceListActivity extends Activity {
	// 调试用
	private static final String TAG = "DeviceListActivity";
	private static final boolean D = true;

	// 返回时数据标签
	public static String EXTRA_DEVICE_ADDRESS = "设备地址";

	// 成员域
	private BluetoothAdapter mBtAdapter;
	private ArrayAdapter<String> mPairedDevicesArrayAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 创建并显示窗口
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS); // 设置窗口显示模式为窗口方式
		setContentView(R.layout.device_list);

		// 设定默认返回值为取消
		setResult(Activity.RESULT_CANCELED);

		// 设定扫描按键响应
		Button scanButton = (Button) findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				doDiscovery();
				v.setVisibility(View.GONE);
			}
		});

		// 初使化设备存储数组
		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name);

		// 设置已配队设备列表
		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);

		// 注册接收查找到设备action接收器
		IntentFilter filter = new IntentFilter(
				BluetoothDevice.ACTION_FOUND);
		filter.setPriority(400);
		this.registerReceiver(mReceiver, filter);

		// 注册查找结束action接收器
		filter = new IntentFilter(
				BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.setPriority(400);
		this.registerReceiver(mReceiver, filter);

		// 得到本地蓝牙句柄
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// 关闭服务查找
		if (mBtAdapter != null) {
			mBtAdapter.cancelDiscovery();
		}

		// 注销action接收器
		this.unregisterReceiver(mReceiver);
	}

	public void OnCancel(View v) {
		finish();
	}

	/**
	 * 开始服务和设备查找
	 */
	private void doDiscovery() {
		if (D)
			Log.d(TAG, "doDiscovery()");

		// 在窗口显示查找中信息
		setProgressBarIndeterminateVisibility(true);
		setTitle("查找设备中...");

		// 关闭再进行的服务查找
		if (mBtAdapter.isDiscovering()) {
			mBtAdapter.cancelDiscovery();
		}
		// 并重新开始
		mBtAdapter.startDiscovery();
	}

	// 选择设备响应函数
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2,
				long arg3) {
			
			if (D)
				Log.d(TAG, "Click and Slect Item");
			// 准备连接设备，关闭服务查找
			mBtAdapter.cancelDiscovery();
			if (D)
				Log.d(TAG, "canceling discovery  finished");
			// 得到mac地址
			String info = ((TextView) v).getText().toString();
			String address = info.substring(info.length() - 17);

			// 设置返回数据
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

			// 设置返回值并结束程序
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};

	// 查找到设备和搜索完成action监听器
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// 查找到设备action
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				if (D)
					Log.d(TAG, "ACTION_FOUND");
				// 得到蓝牙设备
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// 如果是已配对的则略过，已得到显示，其余的在添加到列表中进行显示
				if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
					// 添加到已配对设备列表
					String tmp = device.getName() + "\n" + device.getAddress();
					if (mPairedDevicesArrayAdapter.getPosition(tmp)<0)
					mPairedDevicesArrayAdapter.add(tmp);
				}
				// 搜索完成action
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				if (D)
					Log.d(TAG, "ACTION_DISCOVERY_FINISHED");
				setProgressBarIndeterminateVisibility(false);
				if (mPairedDevicesArrayAdapter.getCount() == 0) {
					setTitle("没有找到已配对的设备");
				}
				else {
					setTitle("选择要连接的设备");
				}
			}
		}
	};

}
