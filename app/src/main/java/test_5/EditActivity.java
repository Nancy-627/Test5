package test_5;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.test.R;

import org.litepal.tablemanager.Connector;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

import static test_5.MyTimeFormat.getTimeStr;
import static test_5.MyTimeFormat.myDateFormat;

public class EditActivity extends AppCompatActivity implements ImagePickerConfig.OnImageSelectedFinishLisenter {

    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.content)
    EditText mContextEv;
    @BindView(R.id.btn_save)
    Button mSaveBtn;
    @BindView(R.id.btn_clean)
    Button mCleanBtn;
    @BindView(R.id.memo_title)
    EditText mMomeTitle;
    @BindView(R.id.edit_time)
    TextView mEditTime;
    @BindView(R.id.memo_is_time)
    CheckBox mIsTip;
    @BindView(R.id.check_data)
    TextView mTipsDate;
    @BindView(R.id.check_time)
    TextView mTipsTime;
    @BindView(R.id.insert_image_btn)
    View mInsertImageBtn;
    @BindView(R.id.image_picker_one)
    ImageView mImageViewOne;
    @BindView(R.id.image_picker_two)
    ImageView mImageViewTwo;
    @BindView(R.id.image_picker_three)
    ImageView mImageViewThree;



    private static final String TAG = "EditActivity";
    private static final int PREMISSION_REQUEST_CODE = 1;
    private static final int MAX_SELECTED_COUNT = 3;


    private Integer year;
    private Integer month;
    private Integer dayOfMonth;
    private Integer hour;
    private Integer minute;

    private DatePickerDialog dialogDate;
    private TimePickerDialog dialogTime;
    private String mCreateDate;
    private String mDispCreateDate;

    private int mYear;
    private int mMonth;
    private int mDay;
    private Calendar mCalendar;
    private int mHour;
    private int mMinute;
    private int mSecond;
    private boolean mTipIsChecked;

    //动态获取权限时的请求码
    private static final int PERMISSION_REQUEST_CODE = 1;
    private long mEventId;
    private int mMyHourOfDay;
    private int mMyMinute;
    private int mMyDayOfMonth;
    private int mMyYear;
    private int mMyMonth;
    private String mTitle;
    private String mBody;
    public int mSelectedSize;
    private ImagePickerConfig mPickerConfig;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_activity);
        ButterKnife.bind(this);

        checkCalendarPermission();
        checkImagePermission();
        getDate();
        initCalendar();
        initView();
        initEvent();
        initPickerConfig();
    }

    private void initPickerConfig() {
        mPickerConfig = ImagePickerConfig.getInstance();
        mPickerConfig.setMaxSelectedCount(MAX_SELECTED_COUNT);
        mPickerConfig.setOnImageSelectedFinishLisenter(this);
    }

    //检查是否有读写相册权限
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkImagePermission() {
        int readExStroagePresmission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        Log.d(TAG, "readExStroagePresmission" + readExStroagePresmission);
        if (readExStroagePresmission == PackageManager.PERMISSION_GRANTED) {
            //有权限
        } else {
            //没有权限，需要去申请权限
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PREMISSION_REQUEST_CODE);
        }
    }

    //查询日历，验证URL
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void queryCalender() {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        if (checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Cursor query = contentResolver.query(uri, null, null, null, null);
        String[] columnNames = query.getColumnNames();
        for (String columnName : columnNames) {
            Log.d(TAG, "columnName -- > " + columnName);
        }
        query.close();
    }

    //创建日历用户
    private void initCalendar() {
        TimeZone timeZone = TimeZone.getDefault();
        ContentValues value = new ContentValues();
        value.put(CalendarContract.Calendars.NAME, "yy");
        value.put(CalendarContract.Calendars.ACCOUNT_NAME, "mygmailaddress@gmail.com");
        value.put(CalendarContract.Calendars.ACCOUNT_TYPE, "com.android.exchange");
        value.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, "mytt");
        value.put(CalendarContract.Calendars.VISIBLE, 1);
        value.put(CalendarContract.Calendars.CALENDAR_COLOR, -9206951);
        value.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        value.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        value.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.getID());
        value.put(CalendarContract.Calendars.OWNER_ACCOUNT, "mygmailaddress@gmail.com");
        value.put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0);
        Uri calendarUri = CalendarContract.Calendars.CONTENT_URI;
        calendarUri = calendarUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true").appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, "mygmailaddress@gmail.com").appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, "com.android.exchange").build();
        getContentResolver().insert(calendarUri, value);
    }

    //检查是否获取日历的读写权限
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkCalendarPermission() {
        int writePermission = checkSelfPermission(Manifest.permission.WRITE_CALENDAR);
        int readPermission = checkSelfPermission(Manifest.permission.READ_CALENDAR);
        if (writePermission == PackageManager.PERMISSION_GRANTED && readPermission == PackageManager.PERMISSION_GRANTED) {
            //有读写日历的权限
            Log.d(TAG, "has permission...");
        } else {
            //没有权限
            //需要获取权限
            Log.d(TAG, "no permission...");
            requestPermissions(new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, PERMISSION_REQUEST_CODE);
        }
    }

    //判断请求码结果来决定是否能成功获取权限


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            //权限检查的结果
            if (grantResults.length == 3 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                //此时有权限
                Toast.makeText(this, "有权限", Toast.LENGTH_SHORT).show();
            } else {
                //此时没有权限
                //可以给出一个提示：
                //用户点击确定后重新调用请求权限
                //用户点击取消后就不再获取权限了
                finish();
                Toast.makeText(this, "没有权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //获取当前时间
    private void getDate() {
        mCalendar = Calendar.getInstance();
        mYear = mCalendar.get(Calendar.YEAR);
        mMonth = mCalendar.get(Calendar.MONTH);
        mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        mHour = mCalendar.get(java.util.Calendar.HOUR_OF_DAY);
        mMinute = mCalendar.get(java.util.Calendar.MINUTE);
        mSecond = mCalendar.get(java.util.Calendar.SECOND);

    }

    //点击事件
    private void initEvent() {
        //返回
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取输入内容
                String title = mMomeTitle.getText().toString().trim();
                String body = mContextEv.getText().toString().trim();
                if (!title.equals("") && !body.equals("")) {
                    //如果标题，内容都不为空，则显示dialog
                    showDialog(title, body);
                } else {
                    //如果都为空则返回
                    startIntent();
                }
            }
        });

        //保存
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                //获取输入内容
                mTitle = mMomeTitle.getText().toString().trim();
                mBody = mContextEv.getText().toString().trim();
                Log.d(TAG, "title is " + mTitle);
                Log.d(TAG, "body is " + mBody);
                if (canDoSave(mTitle, mBody)) {
                    saveInToDb(mTitle, mBody);
                    if (mTipIsChecked) {
                        Toast.makeText(EditActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                        setTipIntoCalendar();
                        startIntent();
                    } else {
                        startIntent();
                    }
                }

            }
        });

        mCleanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContextEv.setText(" ");
            }
        });
        //是否设置提醒事件,checkbox是否被选中
        mIsTip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mTipIsChecked = isChecked;
                Log.d(TAG, "mChecked" + isChecked);
                if (isChecked) {
                    //选择设置提醒的话就选择时间
                    mTipsDate.setVisibility(View.VISIBLE);
                } else {
                    //无法设置提醒
                    mTipsDate.setVisibility(View.GONE);
                    mTipsTime.setVisibility(View.GONE);
                }
            }
        });

        //选择事件日期
        mTipsDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(EditActivity.this, "1111", Toast.LENGTH_SHORT).show();
                //选择事件时间
                showDatePickerDialog();
                //设置time为可见
                mTipsTime.setVisibility(View.VISIBLE);

            }
        });

        //选择提醒时间
        mTipsTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //选择时间
                showTimePickerDialog();
            }
        });

        //插入图片被点击了，跳转到picker界面
        mInsertImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditActivity.this, PickerActivity.class));
            }
        });

}

    //设置提醒时间
    private void showTimePickerDialog() {
        TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                //记录下当前选择的时间，设置到提醒事件中去
                mMyHourOfDay = hourOfDay;
                mMyMinute = minute;
                mTipsTime.setText(hourOfDay + ":" + minute);
            }
        };
        TimePickerDialog dialog = new TimePickerDialog(this, 0, listener, mHour, mMinute, true);
        dialog.show();
    }

    //设置事件日期
    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                //显示设置的会见
                //记录下当前所选的日子，设置到提醒事件中去
                mMyYear = year;
                mMyMonth = ++month;
                mMyDayOfMonth = dayOfMonth;
                mTipsDate.setText(year + "-" + mMyMonth + "-" + dayOfMonth);
            }
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, 0, listener, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    //将内容保存到数据库
    private void saveInToDb(String title, String body) {
        Memo mMemo = new Memo();
        mMemo.setTitle(title);
        mMemo.setText(body);
        mMemo.setCreateTime(mDispCreateDate);
        mMemo.setTipsChecked(mTipIsChecked);
        mMemo.save();


    }

    //向日历中插入提醒事件
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setTipIntoCalendar() {
        //插入事件
        //前面查询出来的
        long calID = 1;
        //时间创建
        java.util.Calendar beginTime = java.util.Calendar.getInstance();
        //Month value is 0-based. e.g., 0 for January.
        //开始时间
        int month = mMyMonth - 1;
        beginTime.set(mMyYear, month, mMyDayOfMonth, mMyHourOfDay, mMyMinute);
        long startMillis = beginTime.getTimeInMillis();
        java.util.Calendar endTime = java.util.Calendar.getInstance();
        //结束时间
        endTime.set(mMyYear, month, mMyDayOfMonth, mMyHourOfDay, mMyMinute + 45);
        long endMillis = endTime.getTimeInMillis();
        Log.d(TAG, "beginTime -- > " + mMyYear + "-" + month + "-" + mMyDayOfMonth + "-" + mMyHourOfDay + "-" + mMyMinute);
        Log.d(TAG, "endTime -- > " + mMyYear + "-" + month + "-" + mMyDayOfMonth + "-" + mMyHourOfDay + "-" + mMyMinute + 45);
        //准备好插入事件数据库的内容
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        //开始时间
        values.put(CalendarContract.Events.DTSTART, startMillis);
        //结束时间
        values.put(CalendarContract.Events.DTEND, endMillis);
        //标题
        values.put(CalendarContract.Events.TITLE, mTitle);
        //描述
        values.put(CalendarContract.Events.DESCRIPTION, mBody);
        //日历ID
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        //时间时区
        String timeZone = TimeZone.getDefault().getID();
        Log.d(TAG, "time zone -- > " + timeZone);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone);
        if (checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
        Log.d(TAG, "insert result --- > " + uri);

        //插入提醒
        //获取当前插入事件的ID
        String strUri = String.valueOf(uri);
        String currentId = strUri.replace("content://com.android.calendar/events/", "");
        mEventId = Integer.valueOf(currentId);
        ContentValues remindValues = new ContentValues();
        //15分钟前进行提醒
        remindValues.put(CalendarContract.Reminders.MINUTES, 15);
        remindValues.put(CalendarContract.Reminders.EVENT_ID, mEventId);
        remindValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        Uri reminduri = cr.insert(CalendarContract.Reminders.CONTENT_URI, remindValues);
        Log.d(TAG, "result uri -- > " + reminduri);

    }

    //判断能否保存
    private boolean canDoSave(String title, String body) {
        //用于判断能否进行保存动作
        //true:此时说明可以保存内容,保存内容至数据库中
        //flase:不能保存，需要写内容
        boolean flag = true;
        if (title.equals("")) {
            Toast.makeText(this, "标题不能为空", Toast.LENGTH_SHORT).show();
            flag = false;
        }
        if (title.length() > 10) {
            Toast.makeText(this, "标题过长", Toast.LENGTH_SHORT).show();
            flag = false;
        }
        if (body.length() > 200) {
            Toast.makeText(this, "内容过长", Toast.LENGTH_SHORT).show();
            flag = false;
        }
        if (body.equals("")) {
            Toast.makeText(this, "内容不能为空", Toast.LENGTH_SHORT).show();
            flag = false;
        }
        if (mTipIsChecked) {
            //如果选中“添加提醒事件”,就需要将日期时间填写完整才可以
            if (mTipsDate.getText().toString().equals("请选择日期") || mTipsTime.getText().toString().equals("请选择时间")) {
                Toast.makeText(this, "日期/时间不能为空", Toast.LENGTH_SHORT).show();
                flag = false;
            }
        }

        return flag;
    }

    //显示dialog
    private void showDialog(final String title, final String body) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(EditActivity.this);
        dialog.setTitle("提示：");
        dialog.setMessage("是否保存当前内容");
        dialog.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "保存");
                //将数据保存至数据库，设置提醒事件
                if (canDoSave(title, body)) {
                    saveInToDb(title, body);
                    if (mTipIsChecked) {
                        setTipIntoCalendar();
                        startIntent();
                    } else {
                        startIntent();
                    }
                }
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //停留在当前页面
                Log.d(TAG, "取消");
            }
        });
        dialog.show();
    }

    //页面跳转
    private void startIntent() {
        Intent intent = new Intent(EditActivity.this, MainActivity.class);
        startActivity(intent);
    }

    //初始化操作
    private void initView() {

        //初始化当前时间
        Date date = new Date(System.currentTimeMillis());
        //创建的时间，存入数据库，数据库根据时间先后显示列表
        mCreateDate = myDateFormat(date, DateFormat.NORMAL_TIME);
        mDispCreateDate = getTimeStr(date);

        if (mEditTime.getText().length() != 0) {
            mEditTime.setText(mDispCreateDate);
        }

        dialogDate = null;
        dialogTime = null;
        hour = 0;
        minute = 0;
        year = 0;
        month = 0;
        dayOfMonth = 0;

        //初始化数据库
        Connector.getDatabase();
    }

    @Override
    public void onSelectedFinish(List<ImageItem> selectedResult) {
        //所选择的图片列表在该处回来了
        for (ImageItem imageItem : selectedResult) {
            Log.d(TAG, "image item is " + imageItem);
        }
        mSelectedSize = selectedResult.size();
        Log.d(TAG, "selected size is " + mSelectedSize);
        mPickerConfig.setSelectResult(selectedResult);
        mPickerConfig.setSeletcedSize(mSelectedSize);

        setPics(selectedResult);
    }

    private void setPics(List<ImageItem> selectedResult) {
        switch (mSelectedSize) {
            case 1:
                Glide.with(mImageViewOne.getContext()).load(selectedResult.get(0).getPath()).into(mImageViewOne);
                mImageViewOne.setVisibility(View.VISIBLE);
                mImageViewTwo.setVisibility(View.GONE);
                mImageViewThree.setVisibility(View.GONE);
                break;
            case 2:
                Glide.with(mImageViewOne.getContext()).load(selectedResult.get(0).getPath()).into(mImageViewOne);
                Glide.with(mImageViewTwo.getContext()).load(selectedResult.get(1).getPath()).into(mImageViewTwo);
                mImageViewOne.setVisibility(View.VISIBLE);
                mImageViewTwo.setVisibility(View.VISIBLE);
                mImageViewThree.setVisibility(View.GONE);
                break;
            case 3:
                Glide.with(mImageViewOne.getContext()).load(selectedResult.get(0).getPath()).into(mImageViewOne);
                Glide.with(mImageViewTwo.getContext()).load(selectedResult.get(1).getPath()).into(mImageViewTwo);
                Glide.with(mImageViewThree.getContext()).load(selectedResult.get(2).getPath()).into(mImageViewThree);
                mImageViewOne.setVisibility(View.VISIBLE);
                mImageViewTwo.setVisibility(View.VISIBLE);
                mImageViewThree.setVisibility(View.VISIBLE);
                break;
        }
    }
}