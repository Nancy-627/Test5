package test_5;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.slice.Slice;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.graphics.ColorSpace;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.SyncStateContract;
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

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static test_5.MyTimeFormat.getTimeStr;

public class AmendActivity extends AppCompatActivity implements ImagePickerConfig.OnImageSelectedFinishLisenter{

    @BindView(R.id.amend_btn_back)
    ImageView mBackBtn;
    @BindView(R.id.amend_edit_time)
    TextView mCurrentTime;
    @BindView(R.id.amend_edit)
    EditText mTitle;
    @BindView(R.id.amend_content)
    EditText mContent;
    @BindView(R.id.amend_memo_is_time)
    CheckBox mCheckBox;
    @BindView(R.id.amend_check_data)
    TextView mDataPicker;
    @BindView(R.id.amend_check_time)
    TextView mTimePicker;
    @BindView(R.id.amend_btn_save)
    Button mSaveBtn;
    @BindView(R.id.amend_btn_clean)
    Button mCleanBtn;
    @BindView(R.id.amend_insert_image_btn)
    View mInsertImage;
    @BindView(R.id.amend_pic_one)
    ImageView mPicOne;
    @BindView(R.id.amend_pic_two)
    ImageView mPicTwo;
    @BindView(R.id.amend_pic_three)
    ImageView mPicThree;

    private static final int PERMISSION_REQUEST_CODE = 2;
    private static final int MAX_SELECTED_COUNT = 3;
    private static final int PREMISSION_REQUEST_CODE = 1;
    private Calendar mCalendar;
    private int mMinute;
    private int mHour;
    private int mDay;
    private int mMonth;
    private int mSecond;
    private int mYear;

    private DatePickerDialog dialogDate;
    private TimePickerDialog dialogTime;
    private Integer year;
    private Integer month;
    private Integer dayOfMonth;
    private Integer hour;
    private Integer minute;

    private int mMyHourOfDay;
    private int mMyMinute;
    private int mMyDayOfMonth;
    private int mMyYear;
    private int mMyMonth;
    private boolean mCurrentCheckResult;
    private Memo mMemo;
    private boolean mIsNeedTips;
    private boolean isChanged = false;
    private ImagePickerConfig mPickerConfig;
    
    private String mEditDate;


    private static final String TAG = "AmendActivity";


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.amend_activity);
        ButterKnife.bind(this);


        checkCalendarPremission();
        checkImagePremission();
        initConfig();
        getDate();
        initView();
        initData();
        initEvent();

    }



    /**
     * 检查是否获取相册的读写权限
     * 安卓6.0以上需要动态获取权限
     */
@RequiresApi(api = Build.VERSION_CODES.M)
private void checkImagePremission() {
    int readExStroagePermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
    Log.d(TAG, "readExStroagePermission" + readExStroagePermission);
    if (readExStroagePermission == PackageManager.PERMISSION_GRANTED ) {
        //有权限
    } else {
        //没有权限
        requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }
}
private void initConfig() {
    mPickerConfig = ImagePickerConfig.getInstance();
    mPickerConfig.setMaxSelectedCount(MAX_SELECTED_COUNT);
    mPickerConfig.setOnImageSelectedFinishLisenter((ImagePickerConfig.OnImageSelectedFinishLisenter) this);
}
    
    
    //返回
    private void initEvent() {

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //输入获取内容
                String title = mTitle.getText().toString().trim();
                String body = mContent.getText().toString().trim();
                if (!title.equals("") && !body.equals("")) {
                    showBackDialog(title, body);
                } else {
                    startIntent();
                }
            }
        });

        //保存
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取输入内容
                String title = mTitle.getText().toString().trim();
                String body = mContent.getText().toString().trim();
                Log.d(TAG, "title is " + title);
                Log.d(TAG, "body is " + body);
                if (canDoSave(title, body)) {
                    updateDb(title, body);
                    startIntent();
                }
            }

        });

        //清空
        mCleanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContent.setText(" ");
            }
        });

        //监听checkBox的变化
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isChanged = true;
                mCurrentCheckResult = isChecked;
                //如果被选中
                if (mCurrentCheckResult) {
                    mDataPicker.setVisibility(View.VISIBLE);
                    mTimePicker.setVisibility(View.VISIBLE);
                } else {
                    //如果没有被选中
                    mDataPicker.setVisibility(View.GONE);
                    mTimePicker.setVisibility(View.GONE);
                }
            }
        });

        //选择事件日期
        mDataPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AmendActivity.this, "选择时间" , Toast.LENGTH_SHORT).show();
                //选择事件时间
                showDataPickerDialog();
            }
        });

        //选择提醒时间
        mTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //选择时间
                showTimePickerDialog();
            }
        });

        //插入图片
        mInsertImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AmendActivity.this, PickerActivity.class));
            }
        });
    }



    //指定日历的相关操作，增删改
    private void doCalendarEvent() {
        if (isChanged) {
            //如果状态改变了
            if (mCurrentCheckResult) {
                //当前选中了，insert
                Log.d(TAG, "do insert");
            } else {
                //当前没选中，delete
                Log.d(TAG, "do delete");
            }
        } else {
            //如果状态没改变
            if (mIsNeedTips) {
                //需要事件提醒,update
                Log.d(TAG, "do update");
            } else {
                //不需要事件提醒,nothing
                Log.d(TAG, "do nothing");
            }
        }
    }

    //设置提醒事件
    private void showTimePickerDialog() {
        TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mMyHourOfDay = hourOfDay;
                mMyMinute = minute;
                mTimePicker.setText(hourOfDay + ":" + minute);
            }
        };
        TimePickerDialog dialog = new TimePickerDialog(this, 0, listener, mHour, mMinute, true);
        dialog.show();
    }

    //设置事件日期
    private void showDataPickerDialog() {
        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                //
                mMyYear = year;
                mMyMonth = ++month;
                mMyDayOfMonth = dayOfMonth;
                mDataPicker.setText(year + "-" +mMyMonth + "-" + dayOfMonth);
            }
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, 0, listener, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    //能否保存
    private boolean canDoSave(String title, String body) {
        //用于判断能否保存动作
        //true:可以保存内容,保存内容至数据库中
        //false:不能保存，需要写内容
        boolean flag = true;
        if (title.equals("")) {
            flag = false;
        }
        if (title.length() >10) {
            flag = false;
        }
        if (body.length() > 200) {
            flag = false;
        }
        if (body.equals("")) {
            flag = false;
        }
        if (mCurrentCheckResult) {
            //如果选中“添加提醒事件”，就需要将日期时间填写完整
            if (mDataPicker.getText().toString().equals("") || mTimePicker.getText().toString().equals("")) {
                flag = false;
            }
        }
        return flag;
    }

    //显示dialog
    private void showBackDialog(final String title, final String body) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(AmendActivity.this);
        dialog.setTitle("提示：");
        dialog.setMessage("是否保存当前内容");
        dialog.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //将更新保存至数据库中
                if (canDoSave(title, body)) {
                    updateDb(title, body);
                    startIntent();
                }

            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //跳转至首页
                startIntent();

            }
        });
        dialog.show();

    }

    //将修改内容保存到数据库中
    private void updateDb(String title, String body) {
        ContentValues values = new ContentValues();
        values.put(Constants.MEMO_TITLE, title);
        values.put(Constants.MEMO_BODY, body);
        values.put(Constants.MEMO_MODIFY_TIME, mEditDate);
        values.put(Constants.MEMO_NEED_TIPS, mCurrentCheckResult);
        Log.d(TAG, "mCurrentCheckResult" + mCurrentCheckResult);
        DataSupport.update(Memo.class, values, mMemo.getId());
        Toast.makeText(this, "修改成功！", Toast.LENGTH_SHORT).show();
    }

    //从MainActivity点击到该页面，将数据回显
    private void initData() {
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        //获取bundle中的数据
        if (bundle != null) {
            String memoTitle = intent.getStringExtra(Constants.MEMO_TITLE);
            String memoContent = intent.getStringExtra(Constants.MEMO_BODY);
            int memoId = intent.getIntExtra(Constants.MEMO_ID, 0);
            mIsNeedTips = intent.getBooleanExtra(Constants.MEMO_NEED_TIPS, false);

            //设置数据
            mMemo = new Memo();
            mMemo.setTitle(memoTitle);
            mMemo.setText(memoContent);
            mMemo.setId(memoId);
            mMemo.setTipsChecked(mIsNeedTips);
            if (mIsNeedTips) {
                //如果设置了提醒事件，就将日期和时间显现出来
                mDataPicker.setVisibility(View.VISIBLE);
                mTimePicker.setVisibility(View.VISIBLE);
                mCheckBox.setChecked(mIsNeedTips);
                setDateAndTime(memoTitle);
            }
            mContent.setText(memoContent);
            mContent.setText(memoTitle);
        }
    }

    //根据title获取到设置提醒的日期和时间
    private void setDateAndTime(String memoTitle) {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = Uri.parse("content://com.android.calendar/events");
        Cursor cursor = contentResolver.query(uri, new String[]{CalendarContract.Events.DTSTART}, CalendarContract.Events.TITLE + "=" + memoTitle, null, null, null);
        String[] columnNames = cursor.getColumnNames();
        while (cursor.moveToNext()) {
            for (String columnName : columnNames) {
                String startTimeStr = cursor.getString(cursor.getColumnIndex(columnName));
                //将秒数转化为日期
                getTipsDate(startTimeStr);
                Log.d(TAG, columnName + "==" + startTimeStr);
            }
        }
    }


    //通过数据库中设置的时间获取提醒事件的时间
    private void getTipsDate(String startTimeStr) {
        long time = Long.parseLong(startTimeStr);
        Date date = new Date(time);
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(date);
        //将日期格式进行分割：为date+time
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String currentTime = dateFormat.format(gregorianCalendar.getTime());
        Log.d(TAG, "currentTime is" +currentTime);
        String[] dateAndTime = currentTime.split(" ");
        String tipsDate = dateAndTime[0];
        String tipsTime = dateAndTime[1];
        Log.d(TAG, "tipsDate" + tipsDate);
        Log.d(TAG, "tipsTime" + tipsTime);
        mDataPicker.setText(tipsDate);
        mTimePicker.setText(tipsTime);
    }

    //初始化当前时间
    private void initView() {
        Date date = new Date(System.currentTimeMillis());
        //创建的时间，存入数据库，数据库根据时间先后显示列表
        mEditDate = getTimeStr(date);
        if (mCurrentTime.getText().length() != 0) {
            mCurrentTime.setText(mEditDate);
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

        //获取到之前选择的图片并显示出来
        List<ImageItem> selectResult = mPickerConfig.getSelectResult();
        int seletcedSize = mPickerConfig.getSeletcedSize();
        if (selectResult != null) {
            for (ImageItem imageItem : selectResult) {
                Log.d(TAG, "image item is " + imageItem.getPath());
            }
            Log.d(TAG, "selected size is " + seletcedSize);
        }
        if (selectResult != null) {
            //显示图片
            switch (seletcedSize) {
                case 1:
                    Glide.with(mPicOne.getContext()).load(selectResult.get(0).getPath()).into(mPicOne);
                    mPicOne.setVisibility(View.VISIBLE);
                    mPicTwo.setVisibility(View.GONE);
                    mPicThree.setVisibility(View.GONE);
                    break;
                case 2:
                    Glide.with(mPicOne.getContext()).load(selectResult.get(0).getPath()).into(mPicOne);
                    Glide.with(mPicTwo.getContext()).load(selectResult.get(1).getPath()).into(mPicTwo);
                    mPicOne.setVisibility(View.VISIBLE);
                    mPicTwo.setVisibility(View.VISIBLE);
                    mPicThree.setVisibility(View.GONE);
                    break;
                case 3:
                    Glide.with(mPicOne.getContext()).load(selectResult.get(0).getPath()).into(mPicOne);
                    Glide.with(mPicTwo.getContext()).load(selectResult.get(1).getPath()).into(mPicTwo);
                    Glide.with(mPicThree.getContext()).load(selectResult.get(2).getPath()).into(mPicThree);
                    mPicOne.setVisibility(View.VISIBLE);
                    mPicTwo.setVisibility(View.VISIBLE);
                    mPicThree.setVisibility(View.VISIBLE);
                    break;
            }
        } else {
            mPicOne.setVisibility(View.GONE);
            mPicTwo.setVisibility(View.GONE);
            mPicThree.setVisibility(View.GONE);
        }

    }
    //页面跳转
    private void startIntent() {
        Intent intent = new Intent(AmendActivity.this, MainActivity.class);
        startActivity(intent);
    }

    //检查是否有读写日历的权限
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkCalendarPremission() {
        int writePermission = checkSelfPermission(Manifest.permission.WRITE_CALENDAR);
        int readPermission = checkSelfPermission(Manifest.permission.READ_CALENDAR);
        if (writePermission == PackageManager.PERMISSION_GRANTED && readPermission == PackageManager.PERMISSION_GRANTED) {
            //有读写权限
            Log.d(TAG, "has permission...");
        } else  {
            //没有读写权限
            Log.d(TAG, "no permission");
            requestPermissions(new String[] {Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, PERMISSION_REQUEST_CODE);
        }
    }
    //判断请求码结果来决定是否能成功获得权限

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length == 3 &&
            grantResults[0] ==PackageManager.PERMISSION_GRANTED &&
            grantResults[1] ==PackageManager.PERMISSION_GRANTED &&
            grantResults[2] ==PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "有权限", Toast.LENGTH_SHORT).show();
            } else {
                //此时没有权限
                finish();
                Toast.makeText(this, "没有权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //获取当前时间
    private void getDate() {
        mCalendar = Calendar.getInstance();
        mYear = mCalendar.get(Calendar.YEAR);
        mCalendar.get(Calendar.MONTH);
        mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        mHour = mCalendar.get(java.util.Calendar.HOUR_OF_DAY);
        mMinute = mCalendar.get(java.util.Calendar.MINUTE);
        mSecond = mCalendar.get(Calendar.SECOND);
    }


    @Override
    public void onSelectedFinish(List<ImageItem> selectedResult) {
        //所选择的图片列表在该处回来了
        //设置图片
        if (selectedResult != null) {
            setPics(selectedResult);
        }
    }

    private void setPics(List<ImageItem> selectedResult) {
        switch (selectedResult.size()) {
            case 1:
                Glide.with(mPicOne.getContext()).load(selectedResult.get(0).getPath()).into(mPicOne);
                mPicOne.setVisibility(View.VISIBLE);
                mPicTwo.setVisibility(View.GONE);
                mPicThree.setVisibility(View.GONE);
                break;
            case 2:
                Glide.with(mPicOne.getContext()).load(selectedResult.get(0).getPath()).into(mPicOne);
                Glide.with(mPicTwo.getContext()).load(selectedResult.get(1).getPath()).into(mPicTwo);
                mPicOne.setVisibility(View.VISIBLE);
                mPicTwo.setVisibility(View.VISIBLE);
                mPicThree.setVisibility(View.GONE);
                break;
            case 3:
                Glide.with(mPicOne.getContext()).load(selectedResult.get(0).getPath()).into(mPicOne);
                Glide.with(mPicTwo.getContext()).load(selectedResult.get(1).getPath()).into(mPicTwo);
                Glide.with(mPicThree.getContext()).load(selectedResult.get(2).getPath()).into(mPicThree);
                mPicOne.setVisibility(View.VISIBLE);
                mPicTwo.setVisibility(View.VISIBLE);
                mPicThree.setVisibility(View.VISIBLE);
                break;
        }
    }

}