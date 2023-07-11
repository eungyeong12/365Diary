package com.diary.a365diary;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(),this,year,month,day);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(year, month, day);

        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy", Locale.KOREA);
        String newDateString1 = dateFormat1.format(selectedDate.getTime());
        TextView dateText1 = getActivity().findViewById(R.id.dateText1);
        dateText1.setText(newDateString1);

        SimpleDateFormat dateFormat2 = new SimpleDateFormat("MM.dd", Locale.KOREA);
        String newDateString2 = dateFormat2.format(selectedDate.getTime());
        TextView dateText2 = getActivity().findViewById(R.id.dateText2);
        dateText2.setText(newDateString2);
    }
}