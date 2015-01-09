package com.example.ahiddenbike;


import android.os.Bundle;
import com.example.ahiddenbike.R;
import android.app.Activity;

public class SetWorktimeActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("设置设备时间");
		setContentView(R.layout.set_worktime);
	}
	
	
}