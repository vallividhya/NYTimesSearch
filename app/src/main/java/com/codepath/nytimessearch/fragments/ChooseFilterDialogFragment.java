package com.codepath.nytimessearch.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Spinner;

import com.codepath.nytimessearch.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * A simple {@link DialogFragment} subclass.
 * Use the {@link ChooseFilterDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChooseFilterDialogFragment extends BottomSheetDialogFragment {

    private DatePicker datePicker;
    private Spinner spinner;
    private Button btnFilterSave, btnFilterClear;
    private CheckBox cbArts, cbFashion, cbSports;
    private SharedPreferences mSettings;
    public ChooseFilterDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title Parameter 1.
     * @return A new instance of fragment ChooseFilterDialogFragment.
     */

    private OnChooseFilterDialogListener listener;

    public static ChooseFilterDialogFragment newInstance(String title) {
        ChooseFilterDialogFragment fragment = new ChooseFilterDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mSettings = getActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);

        getDialog().setTitle(getResources().getString(R.string.filterDialogTitle));
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_filter, container, false);
    }

    public interface OnChooseFilterDialogListener {
        //void onFilterSave(long beginDate, String sortOrder);
        void onFilterSave();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title");
        getDialog().setTitle(title);

        // Get Date picker from view
        datePicker = (DatePicker) view.findViewById(R.id.dpBeginDate);
        // Set max date as today's date
        datePicker.setMaxDate(System.currentTimeMillis() - 1000);

        String date = mSettings.getString("beginDate", "");
        Calendar cal = Calendar.getInstance();
        if (!date.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            try {
                cal.setTime(sdf.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            datePicker.updateDate(year, month, day);
        } else {
            cal.clear();
        }



        // Get spinner from view
        spinner = (Spinner) view.findViewById(R.id.spSortOrder);

        int pos =  mSettings.getString("sortOrder", "oldest").equals("oldest") ? 0 : 1;
        spinner.setSelection(pos);
        // Get button Save
        btnFilterSave = (Button) view.findViewById(R.id.btnFilterSave);

        btnFilterClear = (Button) view.findViewById(R.id.btnFilterClear);

        cbArts = (CheckBox) view.findViewById(R.id.checkbox_arts);
        cbArts.setChecked(mSettings.getBoolean("isArts", false));

        cbFashion = (CheckBox) view.findViewById(R.id.checkbox_fashionStyle);
        cbFashion.setChecked(mSettings.getBoolean("isFashion", false));

        cbSports = (CheckBox) view.findViewById(R.id.checkbox_sports);
        cbSports.setChecked(mSettings.getBoolean("isSports", false));

        btnFilterSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get Begin date selected from date Picker
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth();
                int year = datePicker.getYear();
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, day);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                String beginDate = simpleDateFormat.format(cal.getTime());

                SharedPreferences.Editor editor = mSettings.edit();
                editor.putBoolean("isFilterSet", true);
                editor.putString("beginDate", beginDate);
                // Get Sort order - Oldest or newest from Spinner
                String spinnerVal = spinner.getSelectedItem().toString();
                editor.putString("sortOrder", spinnerVal.toLowerCase());

                // Get News desk values
                editor.putBoolean("isArts", cbArts.isChecked());
                editor.putBoolean("isFashion", cbFashion.isChecked());
                editor.putBoolean("isSports", cbSports.isChecked());
                editor.apply();
                OnChooseFilterDialogListener listener = (OnChooseFilterDialogListener) getActivity();
                listener.onFilterSave();
                dismiss();
            }
        });

        btnFilterClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putBoolean("isFilterSet", false);
                editor.putString("beginDate", "");
                editor.putBoolean("isArts", false);
                editor.putBoolean("isFashion", false);
                editor.putBoolean("isSports", false);
                editor.putString("sortOrder", "");
                editor.apply();
                cbArts.setChecked(false);
                cbFashion.setChecked(false);
                cbSports.setChecked(false);
                spinner.setSelection(2); // Setting none
                dismiss();
            }
        });
        // Set title for dialog
        //getDialog().setTitle(getResources().getString(R.string.filterDialogTitle));
        // Request focus on date picker when dialog opens
        datePicker.requestFocus();

    }



}
