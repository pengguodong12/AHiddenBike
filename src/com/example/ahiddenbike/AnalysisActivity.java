package com.example.ahiddenbike;

import android.os.Bundle;

import com.example.ahiddenbike.R;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class AnalysisActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("设置");
		setContentView(R.layout.analysis);
		find_and_modify_button();

	}
	private void find_and_modify_button() {

		TextView clear= (TextView)findViewById(R.id.clear_button);
		clear.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View arg0) {
				//清空函数
			}
		});
	
	}

}