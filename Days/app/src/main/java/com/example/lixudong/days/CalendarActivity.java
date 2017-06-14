package com.example.jushalo.days;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jushalo.days.MonthDateView;
import com.example.jushalo.days.MonthDateView.DateClick;

public class CalendarActivity extends FragmentActivity {
	private ImageView iv_left;
	private ImageView iv_right;
	private TextView tv_date;
	private TextView tv_week;
	private ImageView tv_today;
	private MonthDateView monthDateView;
	// 左右滑动
	final int RIGHT = 0;
	final int LEFT = 1;
	private GestureDetector gestureDetector;
	// listView
	private List<Map<String, Object>> temp;
	private myDB myDatabase;
	private ListView listView;

	private SimpleAdapter simpleAdapter;

	private TextView mainPageEvent;
	private TextView mainPageTag;
	private TextView mainPageDays;
	private TextView mainPageDate;

	// 摇一摇
	SensorManager mSensorManager = null;
	Sensor mAccelerometerSensor = null;
	long exitTime = 0;
	float newRotationDegree = 0;
	Sensor mMagneticSensor = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		List<Integer> list = new ArrayList<Integer>();
		//list.add(10);
		//list.add(12);
		//list.add(15);
		//list.add(16);
		setContentView(R.layout.activity_date);

		findViews();
		//setSimpleAdapter();
		//setListener();

		iv_left = (ImageView) findViewById(R.id.iv_left);
		iv_right = (ImageView) findViewById(R.id.iv_right);
		monthDateView = (MonthDateView) findViewById(R.id.monthDateView);
		tv_date = (TextView) findViewById(R.id.date_text);
		tv_week  =(TextView) findViewById(R.id.week_text);
		tv_today = (ImageView) findViewById(R.id.tv_today);
		monthDateView.setTextView(tv_date,tv_week);
		monthDateView.setDaysHasThingList(list);

		String month1 = oprationMonth();
		String day1 = oprationDay();
		final String dateclick1 = monthDateView.getmSelYear() + "-" + month1
				+ "-" + day1;
		setSimpleAdapterByDate(dateclick1);


		monthDateView.setDateClick(new MonthDateView.DateClick() {
			@Override
			public void onClickOnDate() {
				// 在这里可以列出一个显示当天日期的控件
				// date 为当前点击的日期，然后要显示该日期的所有事件
				String month = oprationMonth();
				String day = oprationDay();
				final String dateclick = monthDateView.getmSelYear() + "-" + month
						+ "-" + day;
				setSimpleAdapterByDate(dateclick);
				//Toast.makeText(getApplication(), "点击了：" +  dateclick, Toast.LENGTH_SHORT).show();
			}
		});
		setOnlistener();

		gestureDetector = new GestureDetector(CalendarActivity.this,onGestureListener);

		// 摇一摇
		// 传感器管理器
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		// 加速度传感器
		mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		// 地磁传感器
		mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 第一个参数是Listener，第二个参数是所得传感器类型，第三个参数值获取传感器信息的频率
		//在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		// register magnetic and accelerometer sensor into sensor manager (onResume
		mSensorManager.registerListener(mSensorEventListener, mMagneticSensor,
				SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(mSensorEventListener, mAccelerometerSensor,
				SensorManager.SENSOR_DELAY_GAME);
	}


	@Override
	protected void onPause() {
		super.onPause();
		//在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mSensorManager.unregisterListener(mSensorEventListener);
		// register location update listener
	}


	// sensor event listener
	private SensorEventListener mSensorEventListener = new SensorEventListener() {
		float[] accValues = new float[3];
		float[] magValues = new float[3];
		long lastShakeTime = 0;

		@Override
		public void onSensorChanged(SensorEvent event) {
			switch (event.sensor.getType()) {
				case Sensor.TYPE_ACCELEROMETER:
					// do something about values of accelerometer
					// 深度复制，可避免部分设备下 SensorManager.getRotationMatrix 返回false的问题
					for (int i = 0; i < 3; i++) {
						accValues[i] = event.values[i];
					}
					break;
				case Sensor.TYPE_MAGNETIC_FIELD:
					// do something about values of magnetic field
					for (int i = 0; i < 3; i++) {
						magValues[i] = event.values[i];
					}
					break;
				default:
					break;
			}
			calculateOrientation(accValues, magValues);

			// 摇一摇功能
			float x = accValues[0];
			float y = accValues[1];
			float z = accValues[2];
			int medumValue = 18;
			if (Math.abs(x) > medumValue || Math.abs(y) > medumValue || Math.abs(z) > medumValue) {
				// 检测摇动的频率，防止摇动过快
				// 检测是否快速点击间隔<3000
				if ((System.currentTimeMillis() - exitTime) < 2000) {
					return;
				} else {
					exitTime = System.currentTimeMillis();
					Toast.makeText(CalendarActivity.this, "正常模式",
							Toast.LENGTH_SHORT).show();
					finish();
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};
	/**
	 * 计算方向
	 *
	 * @return
	 */
	private void calculateOrientation(float[] accValues, float[] magValues) {
		float[] R = new float[9];
		float[] values = new float[3];
		SensorManager.getRotationMatrix(R, null, accValues, magValues);
		SensorManager.getOrientation(R, values);
		// 相反方向所以为负
		newRotationDegree = (float) Math.toDegrees(values[0]);
	}

	private GestureDetector.OnGestureListener onGestureListener =
			new GestureDetector.SimpleOnGestureListener() {
				@Override
				public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
									   float velocityY) {
					float x = e2.getX() - e1.getX();
					float y = e2.getY() - e1.getY();

					if (x > 0) {
						doResult(LEFT);
					} else if (x < 0) {
						doResult(RIGHT);
					}
					return true;
				}
			};

	public boolean onTouchEvent(MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}

	public void doResult(int action) {

		switch (action) {
			case RIGHT:
				System.out.println("go right");
				monthDateView.onRightClick();
				break;

			case LEFT:
				System.out.println("go left");
				monthDateView.onLeftClick();
				break;

		}
	}


	private void setOnlistener(){
		iv_left.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				monthDateView.onLeftClick();
			}
		});

		iv_right.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				monthDateView.onRightClick();
			}
		});

		tv_today.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				monthDateView.setTodayToView();
			}
		});
	}

	// listView
	private void findViews() {
		listView = (ListView) findViewById(R.id.calendar_list);
		mainPageEvent = (TextView) findViewById(R.id.main_page_event);
		mainPageTag = (TextView) findViewById(R.id.main_page_tag);
		mainPageDays = (TextView) findViewById(R.id.main_page_days);
		mainPageDate = (TextView) findViewById(R.id.main_page_date);
	}

	private void setSimpleAdapter() {
		//获取数据库添加到simpleAdapter
		myDatabase = new myDB(CalendarActivity.this);
		try {
			temp = myDatabase.returndata();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (temp == null) return;
		simpleAdapter = new SimpleAdapter(this, temp, R.layout.activity_item,
				new String[]{"title", "date" ,"tag", "str_days", "when"},
				new int[]{ R.id.item_title, R.id.item_date, R.id.item_image, R.id.sub_day, R.id.start_end });
		listView.setAdapter(simpleAdapter);
		listView.setClickable(true);
	}

	private void setSimpleAdapterByDate(String date) {
		//获取数据库添加到simpleAdapter
		myDatabase = new myDB(CalendarActivity.this);
		try {
			temp = myDatabase.returnByDate(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (temp == null) return;
		simpleAdapter = new SimpleAdapter(this, temp, R.layout.activity_item,
				new String[]{"title", "date" ,"tag", "str_days", "when"},
				new int[]{R.id.item_title, R.id.item_date, R.id.item_image, R.id.sub_day, R.id.start_end});
		listView.setAdapter(simpleAdapter);
		listView.setClickable(true);
	}

	private void setListener() {
		// 长按删除
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long id) {
				final String title = temp.get(position).get("title").toString();
				final String date = temp.get(position).get("date").toString();
				// 创建提示框
				AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);
				builder.setTitle("Delete");
				builder.setMessage("是否删除？");
				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
					}
				});
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						temp.remove(position);
						//删除用户信息； 更新列表
						myDatabase.delete(title, date);
						simpleAdapter.notifyDataSetChanged();
						listView.invalidate();

						//updateMainPageTopInfo();
					}
				});
				builder.create().show();
				return true;
			}
		});
	}

	private String oprationMonth() {
		int numMonth = monthDateView.getmSelMonth();
		if (numMonth < 12) {
			numMonth++;
		}else {
			numMonth = 1;
		}
		String stringMonth = null;
		if (numMonth < 10) {
			stringMonth = "0" + String.valueOf(numMonth);
		} else {
			stringMonth = String.valueOf(numMonth);
		}
		return stringMonth;
	}

	private String oprationDay() {
		int numDay = monthDateView.getmSelDay();
		String stringDay = null;
		if (numDay < 10 && numDay > 0) {
			stringDay = "0" + String.valueOf(numDay);
		} else {
			stringDay = String.valueOf(numDay);
		}
		return stringDay;
	}
}
