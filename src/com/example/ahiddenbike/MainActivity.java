package com.example.ahiddenbike;


import android.os.Bundle;

import com.example.ahiddenbike.R;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		find_and_modify_button();
	}

	private void find_and_modify_button() {
		ImageView findbike= (ImageView)findViewById(R.id.find_bike_button);
		findbike.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, BTClient.class);
				startActivity(intent);
			}
		});
		
		ImageView analysis= (ImageView)findViewById(R.id.analysis_button);
		analysis.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, AnalysisActivity.class);
				startActivity(intent);
			}
		});
		
		ImageView setting= (ImageView)findViewById(R.id.setting_button);
		setting.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, SettingActivity.class);
				startActivity(intent);
			}
		});
		
		ImageView about= (ImageView)findViewById(R.id.about_button);
		about.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, AboutActivity.class);
				startActivity(intent);
			}
		});		
	
		
	}


	};
	
