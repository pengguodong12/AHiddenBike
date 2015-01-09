package com.example.ahiddenbike;

import android.os.Bundle;
import com.example.ahiddenbike.R;
import android.app.Activity;


public class InstructionActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("使用说明");
		setContentView(R.layout.instruction);
	}
}