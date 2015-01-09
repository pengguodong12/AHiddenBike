package com.example.ahiddenbike;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class LocationView extends View implements Runnable {
	private Paint mPaint;
	private Context mContext;
	private float distance = 35.0f;
	private float angle = 0;
	private float k; // 系数，实现distance向屏幕尺寸的转换
	private Object lock; // 为实现synchronized代码块而设置的类实例
	private final float MaxDist = 20.0f; // 最大距离限定为20m
	private final boolean D = false;
	private final String TAG = "LocationView";
	private Thread mThread;
	private RectF mRectF;
	private Bitmap mBitmap;
	public LocationView(Context context) {
		super(context);
		mContext = context;
		mPaint = new Paint();
		lock = new Object();
		mRectF = new RectF();
		mBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.out_of_distance);
		// 启动线程
		mThread = new Thread(this);
		mThread.start();
	}

	public LocationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mPaint = new Paint();
		lock = new Object();
		mRectF = new RectF();
		mBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.out_of_distance);
		// 启动线程
		mThread = new Thread(this);
		mThread.start();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mPaint.setAntiAlias(true);
		mPaint.setTextAlign(Align.LEFT);
		mPaint.setTextSize(36.0f);
		synchronized (lock) {
			if (distance <= MaxDist) {
				mPaint.setStrokeWidth(3.0f);
				mPaint.setColor(Color.WHITE);
				mPaint.setStyle(Paint.Style.STROKE);// 设置空心
				canvas.rotate(angle, this.getWidth() / 2, this.getHeight() / 2);
				k = (float) Math.min(this.getHeight(), this.getWidth()) / 12.0f;
				canvas.drawCircle(this.getWidth() / 2, this.getHeight() / 2, distance * k * 5.0f
						/ MaxDist, mPaint);
				mPaint.setColor(0xFFFA364A);
				canvas.drawLine(this.getWidth() / 2, this.getHeight() / 2, this.getWidth() / 2
						+ distance * k * 5.0f / MaxDist + k * 2 / 3, this.getHeight() / 2,
						mPaint);
				canvas.drawLine(this.getWidth() / 2 + distance * k * 5.0f / MaxDist, this.getHeight()
						/ 2 + k / 3, this.getWidth() / 2 + distance * k * 5.0f / MaxDist,
						this.getHeight() / 2 - k / 3, mPaint);
				mPaint.setStrokeWidth(1.0f);
				mPaint.setColor(0xFF00A1E0);
				mPaint.setStyle(Paint.Style.FILL);
				canvas.rotate(-angle, this.getWidth() / 2 + distance * k * 5.0f / MaxDist + k / 4,
						this.getHeight() / 2 - k / 6);
				canvas.drawText("距离 " + String.valueOf(distance).substring(0, 4) + " m",
						this.getWidth() / 2 + distance * k * 5.0f / MaxDist + k / 4,
						this.getHeight() / 2 - k / 6, mPaint);
				if (D)
					Log.d(TAG, "Circle draw finished with radius " + distance * k * 5.0f / MaxDist);
			} else {
				mRectF.set(10.0f, 10.0f, this.getWidth() - 10.0f, this.getHeight() - 10.0f);
				canvas.drawBitmap(mBitmap, null, mRectF, mPaint);
			}
		}

	}

	public Thread getThread() {
		return this.mThread;
	}

	public void setDistance(float d) {
		synchronized (lock) {
			distance = d;
			if (D)
				Log.d(TAG, "setDistance: " + d);
		}
	}

	@Override
	public void run() {
		while (true) {
			angle = (angle + 2f) % 360;
			if (D)
				Log.d(TAG, "add angle to: " + angle);
			// 不断的调用View中的postInvalidate方法，让界面重新绘制
			this.postInvalidate();
			// this.invalidate(); 此方法要求在UI主线程调用
			try {
				// 暂停40ms继续
				Thread.sleep(20);
			} catch (InterruptedException e) {
			}
		}
	}
}