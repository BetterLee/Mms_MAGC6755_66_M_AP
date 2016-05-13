package com.android.mms.ramos;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.os.Bundle;
import com.android.mms.R;
import android.app.ActionBar;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.text.format.DateFormat;
import java.util.Calendar;
import android.widget.LinearLayout;
import android.text.format.Time;
import com.android.mms.util.MmsLog;
import android.widget.DatePicker;
import android.widget.TimePicker;
import java.util.Date;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;


public class RamosTimerActivity extends Activity{

	private Context mContext;
	private static final String TAG = "RamosTimerActivity";
    private long mDateandTime = 0;
	private TextView mDate_value;
	private TextView mTime_value;
    private java.text.DateFormat mDateFormater;
    private java.text.DateFormat mTimeFormater;
	private Calendar calendar;
    private Intent mResultIntent;
    private Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
        setContentView(R.layout.ramos_timer);
        mContext = this;
        calendar = Calendar.getInstance();
		
        setupActionBar();
        mResultIntent = getIntent();
        mDateandTime = mResultIntent.getLongExtra("TimeInMillis", 0);
        Log.d("litingnew","mDateandTime:  "+mDateandTime);
        if (mDateandTime == 0) {
            mDateandTime = calendar.getTimeInMillis();
        } else {
            calendar.setTime(new Date(mDateandTime));
        }

        LinearLayout mDateLayout=(LinearLayout)findViewById(R.id.date);
        mDateLayout.setVisibility(View.VISIBLE);
        mDateLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    DatePickerDialog mDatePickerDialog = new DatePickerDialog(mContext, new OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            setDate(mContext, year, monthOfYear, dayOfMonth);
                            updateTimeAndDateDisplay(mContext);
                        }
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                    setDateRange(mDatePickerDialog);
                    showDatePickerDialog(mDatePickerDialog);
                }
        });
        LinearLayout mTimeLayout=(LinearLayout)findViewById(R.id.time);
        mTimeLayout.setVisibility(View.VISIBLE);
        mTimeLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {		
                    TimePickerDialog mTimePickerDialog = new TimePickerDialog(mContext, new OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            setTime(mContext, hourOfDay, minute);
                            updateTimeAndDateDisplay(mContext);
                        }
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(mContext));
                    showTimePickerDialog(mTimePickerDialog);
                }
        });

        mDate_value = (TextView) findViewById(R.id.date_value);
        mTime_value = (TextView) findViewById(R.id.time_value);
		
        mDateFormater = DateFormat.getDateFormat(mContext);
        mDate_value.setText(mDateFormater.format(new Date(mDateandTime)));
        mTimeFormater = DateFormat.getTimeFormat(mContext);
        mTime_value.setText(mTimeFormater.format(mDateandTime));
	}

	
    private void showDatePickerDialog(DatePickerDialog datePickerDialog) {
        if (datePickerDialog.isShowing()) {
            datePickerDialog.dismiss();
        }
        calendar.setTime(new Date(mDateandTime));
        datePickerDialog.show();
        if (calendar != null) {
            datePickerDialog.updateDate(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
        }
    }

    private void showTimePickerDialog(TimePickerDialog timePickerDialog) {
        if (timePickerDialog.isShowing()) {
            timePickerDialog.dismiss();
        }
        calendar.setTime(new Date(mDateandTime));
        timePickerDialog.show();
        if (calendar != null) {
            timePickerDialog.updateTime(calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE));
        }
    }
	
    public void updateTimeAndDateDisplay(Context context) {
        mDate_value.setText(DateFormat.getLongDateFormat(context).format(calendar.getTime()));
        mTime_value.setText(DateFormat.getTimeFormat(context).format(calendar.getTime()));
    }


    private void setDate(Context context, int year, int month, int day) {
        MmsLog.d(TAG, "setDate(): year = " + year + ", month = " + month + ", day = " + day);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        long when = calendar.getTimeInMillis();

        mDateandTime = when;
    }
	
    private void setTime(Context context, int hourOfDay, int minute) {
        MmsLog.d(TAG, "setTime(): hourOfDay = " + hourOfDay + ", minute = " + minute);
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long when = calendar.getTimeInMillis();
        mDateandTime = when;
    }

    private void setDateRange(DatePickerDialog dialog) {
		
        Calendar c = Calendar.getInstance();
        if (dialog != null) {
            Time minTime = new Time();
            Time maxTime = new Time();
            /// M: 1970/1/1
            minTime.set(0, 0, 0, 
            	c.get(Calendar.DAY_OF_MONTH), 
            	c.get(Calendar.MONTH), 
            	c.get(Calendar.YEAR));
            /// M: 2037/12/31
            maxTime.set(59, 59, 23, 31, 11, 2037);
            long maxDate = maxTime.toMillis(false);
            /// M: in millsec
            maxDate = maxDate + 999;
            long minDate = minTime.toMillis(false);
            /// M: set min date
            dialog.getDatePicker().setMinDate(minDate);
            /// M: set max date;
            dialog.getDatePicker().setMaxDate(maxDate);
        }
    }


	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	
	
    private void setupActionBar() {
        ActionBar actionBar = getActionBar();

        ViewGroup v = (ViewGroup)LayoutInflater.from(this)
            .inflate(R.layout.conversation_list_actionbar, null);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.ramos_actionbar_timer);
		TextView mCancel=(TextView)findViewById(R.id.cancel);
		TextView mSure=(TextView)findViewById(R.id.sure);
		mCancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
					finish();	
                }
            });
		mSure.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
					checktime();
                }
            });

    }
	
    private void checktime() {

		final Calendar now = Calendar.getInstance();
		long when = calendar.getTimeInMillis();
		MmsLog.d(TAG,"checktime when= "+when);
		MmsLog.d(TAG,"checktime now= "+now.getTimeInMillis());
		if (when < now.getTimeInMillis()) {
			Toast.makeText(mContext, R.string.error_wrong_date, Toast.LENGTH_SHORT)
					.show();
			return;
		} else {
			mUri = Uri.parse(Long.toString(mDateandTime));
			mResultIntent.setData(mUri);
			setResult(RESULT_OK, mResultIntent);
			finish();
		}
	}
}
