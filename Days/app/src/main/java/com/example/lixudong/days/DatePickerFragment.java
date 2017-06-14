package com.example.jushalo.days;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

/**
 * Created by Read on 2016/12/9.
 */

public class DatePickerFragment extends DialogFragment implements
        DatePickerDialog.OnDateSetListener {
    TheListener listener;
    public interface TheListener{
        public void returnDate(String date);
    }
   // @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
// Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        listener = (TheListener) getActivity();
// Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }
   // @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = sdf.format(c.getTime());

        if (listener != null)
        {
            listener.returnDate(formattedDate);
        }
    }

    //@RequiresApi(api = Build.VERSION_CODES.N)
    //@TargetApi(Build.VERSION_CODES.N)
    public String getDate() {
        //Log.d("OnDateSet", "select year:"+year+";month:"+month+";day:"+ dayOfMonth);
        Calendar c1 = Calendar.getInstance();
        int y = c1.get(Calendar.YEAR);
        int m = c1.get(Calendar.MONTH);
        if (m == 12) {
            y++;
            m = 1;
        } else {
            m++;
        }
        return (y + "-" + m + "-" + c1.get(Calendar.DAY_OF_MONTH));
    }
}