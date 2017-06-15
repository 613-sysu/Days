package com.example.lixudong.days;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DatePickerFragment.TheListener {

    private List<Map<String, Object>> temp;
    private myDB myDatabase;
    private ListView listView;

    private SimpleAdapter simpleAdapter;

    private TextView mainPageEvent;
    private TextView mainPageTag;
    private TextView mainPageDays;
    private TextView mainPageDate;
    private TextView edit_date = null;
    private LinearLayout bg;

    private ImageButton noComtentAddButton;
    private TextView noComtentHint;
    DatePickerFragment datePicker =  new DatePickerFragment();
    private int bgNum = 1;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    // 摇一摇
    SensorManager mSensorManager = null;
    Sensor mAccelerometerSensor = null;
    long exitTime = 0;
    float newRotationDegree = 0;
    Sensor mMagneticSensor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //read bg image number and set it
        bg = (LinearLayout)findViewById(R.id.bg);
        preferences = getSharedPreferences("MY_PREFERENCE", Context.MODE_PRIVATE);
        editor = preferences.edit();

        int bgnumber = preferences.getInt("bgNum", R.mipmap.bg1);
        bg.setBackgroundResource(bgnumber);


        //
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        findViews();
        setSimpleAdapter();
        setListener();
        updateMainPageTopInfo();

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

    /*
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mSensorManager.unregisterListener(mSensorEventListener);
        // register location update listener
    }
    */

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
                    Toast.makeText(MainActivity.this, "日历视图",
                            Toast.LENGTH_SHORT).show();
                    exitTime = System.currentTimeMillis();
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, CalendarActivity.class);
                    startActivity(intent);
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
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mSensorManager.unregisterListener(mSensorEventListener);
        // register location update listener
        Intent intent= new Intent("com.example.jushalo.days.receiver");
        Bundle bundle= new Bundle();
        Map<String, Object> a = null;
        try {
            a = myDatabase.getTopDataForList();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (a != null) {
            whichday which = new whichday((int) a.get("tag"), (int) a.get("when"), a.get("title").toString(), a.get("str_days").toString());
            bundle.putSerializable("which", which);
            intent.putExtras(bundle);
            sendBroadcast(intent);
        } else {
            whichday which = null;
            bundle.putSerializable("which", which);
            intent.putExtras(bundle);
            sendBroadcast(intent);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, AddItem.class);
            startActivityForResult(intent, 0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        bg = (LinearLayout)findViewById(R.id.bg);
        preferences = getSharedPreferences("MY_PREFERENCE", Context.MODE_PRIVATE);
        editor = preferences.edit();

        if (id == R.id.nav_all) {
            setSimpleAdapter();
        } else if (id == R.id.nav_work) {
            setSimpleAdapterByTag(R.mipmap.icon_work);
        } else if (id == R.id.nav_study) {
            setSimpleAdapterByTag(R.mipmap.icon_study);
        } else if (id == R.id.nav_anniversary) {
            setSimpleAdapterByTag(R.mipmap.icon_love);
        } else if (id == R.id.nav_birthday) {
            setSimpleAdapterByTag(R.mipmap.icon_birthday);
        } else if (id == R.id.nav_life) {
            setSimpleAdapterByTag(R.mipmap.icon_life);
        } else if (id == R.id.nav_event) {
            setSimpleAdapterByTag(R.mipmap.icon_event);
        } else if (id == R.id.nav_change) {
            //更改背景
            bgNum = (bgNum+ 1)% 5;

            switch (bgNum) {
                case 1:
                    bg.setBackgroundResource(R.mipmap.bg1);
                    editor.putInt("bgNum",R.mipmap.bg1);
                    editor.commit();
                    break;
                case 2:
                    bg.setBackgroundResource(R.mipmap.bg2);
                    editor.putInt("bgNum",R.mipmap.bg2);
                    editor.commit();
                    break;
                case 3:
                    bg.setBackgroundResource(R.mipmap.bg4);
                    editor.putInt("bgNum",R.mipmap.bg4);
                    editor.commit();
                    break;
                case 4:
                    bg.setBackgroundResource(R.mipmap.bg5);
                    editor.putInt("bgNum",R.mipmap.bg5);
                    editor.commit();
                    break;
                case 0:
                    bg.setBackgroundResource(R.mipmap.bg7);
                    editor.putInt("bgNum",R.mipmap.bg7);
                    editor.commit();
                    break;
                default:
                    break;
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == 0) {
            setSimpleAdapter();
            updateMainPageTopInfo();
        }
    }

    private void findViews() {
        listView = (ListView) findViewById(R.id.list);
        mainPageEvent = (TextView) findViewById(R.id.main_page_event);
        mainPageTag = (TextView) findViewById(R.id.main_page_tag);
        mainPageDays = (TextView) findViewById(R.id.main_page_days);
        mainPageDate = (TextView) findViewById(R.id.main_page_date);
        noComtentAddButton = (ImageButton) findViewById(R.id.main_page_null_button);
        noComtentHint = (TextView) findViewById(R.id.main_page_null_hint);
    }

    private void setSimpleAdapter() {
        //获取数据库添加到simpleAdapter
        myDatabase = new myDB(MainActivity.this);
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

    private void setSimpleAdapterByTag(int tag) {
        //获取数据库添加到simpleAdapter
        myDatabase = new myDB(MainActivity.this);
        try {
            temp = myDatabase.returnByTag(tag);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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

                        updateMainPageTopInfo();
                    }
                });
                builder.create().show();
                return true;
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //添加点击修改
                LayoutInflater factory = LayoutInflater.from(MainActivity.this);
                final View v = factory.inflate(R.layout.dialoglayout, null);
                AlertDialog.Builder modifiy = new AlertDialog.Builder(MainActivity.this);


                final TextView edit_title = (TextView)v.findViewById(R.id.tittle);
                edit_date = (TextView)v.findViewById(R.id.date);
                final Spinner tag = (Spinner)v.findViewById(R.id.item_type);
                final Switch top = (Switch)v.findViewById(R.id.item_top);

                final String title = temp.get(position).get("title").toString();
                final String date = temp.get(position).get("date").toString();
                final String setTop = temp.get(position).get("setTop").toString();
                final int Tag = (int)temp.get(position).get("tag");

                edit_title.setText(title);
                edit_date.setText(date);
                if (setTop.equals("是")) {
                    top.setChecked(true);
                } else {
                    top.setChecked(false);
                }
                switch(Tag) {
                    case R.mipmap.icon_work:
                        tag.setSelection(5);
                        break;
                    case R.mipmap.icon_study:
                        tag.setSelection(1);
                        break;
                    case R.mipmap.icon_love:
                        tag.setSelection(2);
                        break;
                    case R.mipmap.icon_birthday:
                        tag.setSelection(3);
                        break;
                    case R.mipmap.icon_life:
                        tag.setSelection(4);
                        break;
                    case R.mipmap.icon_event:
                        tag.setSelection(0);
                        break;
                    default:
                        break;
                }

                modifiy.setView(v);
                modifiy.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public  void onClick(DialogInterface dialog, int which) {
                        String newTitle = edit_title.getText().toString();
                        String newDate = edit_date.getText().toString();

                        String get_tag = (String) tag.getSelectedItem();
                        int newTag = R.mipmap.icon_event;
                        switch (get_tag) {
                            case "工作":
                                newTag = R.mipmap.icon_work;
                                break;
                            case "学习":
                                newTag = R.mipmap.icon_study;
                                break;
                            case "纪念日":
                                newTag = R.mipmap.icon_love;
                                break;
                            case "生日":
                                newTag = R.mipmap.icon_birthday;
                                break;
                            case "生活":
                                newTag = R.mipmap.icon_life;
                                break;
                            case "事件":
                                newTag = R.mipmap.icon_event;
                                break;
                            default:
                                break;
                        }
                        String newSetTop;
                        if (top.isChecked()) {
                            newSetTop = "是";
                            myDatabase.update(title, date, newTitle, newDate, newTag, newSetTop);
                            simpleAdapter.notifyDataSetChanged();
                            setSimpleAdapter();
                            updateMainPageTopInfo();
                        } else {
                            if (setTop.equals("是")) {
                                Toast.makeText(MainActivity.this, "至少选择一个日子置顶哦！", Toast.LENGTH_SHORT).show();
                            } else {
                                newSetTop = "否";
                                myDatabase.update(title, date, newTitle, newDate, newTag, newSetTop);
                                simpleAdapter.notifyDataSetChanged();
                                setSimpleAdapter();
                                updateMainPageTopInfo();
                            }
                        }


                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create().show();
            }
        });

        noComtentAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, AddItem.class);
                startActivityForResult(intent, 0);
            }
        });
    }

    public void showDatePickerDialog(View view) {
        FragmentManager fragmentManager = getFragmentManager();
        datePicker.show(fragmentManager, "datePicker");
        Log.w("AddItem", "showDatePickerDialog");
    }


    @Override
    public void returnDate(String date1) {
        // TODO Auto-generated method stub
        edit_date.setText(date1);
    }

    private void updateMainPageTopInfo() {
        // 设置首页数据
        try {
            Map<String, Object> topData = myDatabase.getTopData();
            if (topData != null) {
                showMainPageViews();
                mainPageEvent.setText((String) topData.get("title"));
                mainPageDays.setText((String) topData.get("str_days"));
                mainPageDate.setText((String) topData.get("date"));
                if (Objects.equals(topData.get("when"), "天后")) {
                    mainPageTag.setText("还有");
                } else {
                    mainPageTag.setText("已经");
                }
                Log.e("topDate", "NotNull");
            } else {
                hideMainPageViews();
                Log.e("topDate", "null");
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("e", "test");
        }
    }

    private void hideMainPageViews() {
        mainPageEvent.setVisibility(View.INVISIBLE);
        mainPageTag.setVisibility(View.INVISIBLE);
        mainPageDays.setVisibility(View.INVISIBLE);
        mainPageDate.setVisibility(View.INVISIBLE);
        listView.setVisibility(View.INVISIBLE);

        noComtentAddButton.setVisibility(View.VISIBLE);
        noComtentHint.setVisibility(View.VISIBLE);
    }

    private void showMainPageViews() {
        mainPageEvent.setVisibility(View.VISIBLE);
        mainPageTag.setVisibility(View.VISIBLE);
        mainPageDays.setVisibility(View.VISIBLE);
        mainPageDate.setVisibility(View.VISIBLE);
        listView.setVisibility(View.VISIBLE);

        noComtentAddButton.setVisibility(View.INVISIBLE);
        noComtentHint.setVisibility(View.INVISIBLE);
    }
}
