package com.example.jushalo.days;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;

public class AddItem extends Activity implements DatePickerFragment.TheListener {

    TextView data = null;
    DatePickerFragment datePicker = null;

    //@RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        data = (TextView) findViewById(R.id.dateView);
        datePicker = new DatePickerFragment();
        //datePicker.show(getFragmentManager(), "datePicker");
        data.setText(datePicker.getDate());

        final ImageButton confirm = (ImageButton) findViewById(R.id.add_item_confirm);
        final ImageButton cancel = (ImageButton) findViewById(R.id.add_item_cancel);
        final EditText edit_title = (EditText) findViewById(R.id.add_item_tittle);
        final Switch ifsetTop = (Switch) findViewById(R.id.add_item_top);
        final Spinner set_tag = (Spinner) findViewById(R.id.add_item_type);
        final myDB myDatabase = new myDB(AddItem.this);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = edit_title.getText().toString();
                String date1 = data.getText().toString();
                String get_tag = (String) set_tag.getSelectedItem();
                Log.e("tag", get_tag);
                int tag = 0;
                switch (get_tag) {
                    case "工作":
                        tag = R.mipmap.icon_work;
                        break;
                    case "学习":
                        tag = R.mipmap.icon_study;
                        break;
                    case "纪念日":
                        tag = R.mipmap.icon_love;
                        break;
                    case "生日":
                        tag = R.mipmap.icon_birthday;
                        break;
                    case "生活":
                        tag = R.mipmap.icon_life;
                        break;
                    case "事件":
                        tag = R.mipmap.icon_event;
                        break;
                    default:
                        break;
                }
                String setTop;
                if (ifsetTop.isChecked()) {
                    setTop = "是";
                } else {
                    setTop = "否";
                }

                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(AddItem.this, "title为空，请完善", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        myDatabase.insert2DB(title, date1, tag, setTop);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(AddItem.this, "新建成功", Toast.LENGTH_SHORT).show();
                    setResult(0);
                    finish();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    public void showDatePickerDialog(View view) {
        FragmentManager fragmentManager = getFragmentManager();
        datePicker.show(fragmentManager, "datePicker");
        Log.w("AddItem", "showDatePickerDialog");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void returnDate(String date1) {
        // TODO Auto-generated method stub
        data.setText(date1);
    }

}
