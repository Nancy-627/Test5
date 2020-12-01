package test_5;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.test.R;

import org.litepal.crud.DataSupport;
import org.litepal.exceptions.DataSupportException;
import org.litepal.tablemanager.Connector;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.test.R.id.img_add;

public class MainActivity extends AppCompatActivity {






    private ArrayList <Integer> memoId = new ArrayList<>();
    private List<Memo> mMemoList;

    @BindView(R.id.img_add)
    ImageView mAddImage;
    @BindView(R.id.memo_list)
    RecyclerView mDataList;

    private static final String TAG = "MainActivity";
    private MemoAdapter mMemoDataAdapter;
    private String mTitle;
    private String mBody;
    private String mCreateTime;

    private String mValues;
    private String mModifyTime;
    private String mIsTipsChecked;




    //设置点击事件，点击图片跳转至编辑页面
    @OnClick({R.id.img_add})
    public void onClicked() {
        Intent intent = new Intent(MainActivity.this, EditActivity.class);
        startActivity(intent);
    }

    //点击item，编辑已有内容的备忘录



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
      ButterKnife.bind(this);

        initView();
        initData();
        initEvent();

    }

    //初始化数据
    private void initData() {
        //获取数据库里的内容
        mMemoList = new ArrayList<>();
        Connector.getDatabase();
        mMemoList = DataSupport.findAll(Memo.class);
        for (Memo memo: mMemoList) {
            Memo mMemo = new Memo();
            int id = memo.getId();
            mMemo.setId(id);
            memoId.add(id);
            mMemo.setTitle(memo.getTitle());
            mMemo.setText(memo.getText());
            mMemo.setCreateTime(memo.getCreateTime());
            mMemo.setTipsChecked(memo.isTipsChecked());
            mMemoList.add(mMemo);

        }
        mMemoDataAdapter.setData(mMemoList);
    }

    private void initEvent() {
        //点击图标跳转到编辑页面
        mAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                startActivity(intent);
            }
        });

        //点击item跳转到编辑页面
        mMemoDataAdapter.setOnItemClickLinstener(new MemoAdapter.OnItemClickLinstener() {
            @Override
            public void onClick(int position, String title, String body, boolean tipsChecked) {
                Toast.makeText(MainActivity.this, "clicked..." + position, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, AmendActivity.class);
                //将数据显示在AmendActivity中
                Bundle bundle = new Bundle();
                bundle.putInt(Constants.MEMO_ID, position + 1);
                bundle.putString(Constants.MEMO_TITLE, title);
                bundle.putString(Constants.MEMO_BODY, body);
                bundle.putBoolean(Constants.MEMO_NEED_TIPS, tipsChecked);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        //长按item删除
        mMemoDataAdapter.setOnItemLongClickLinstener(new MemoAdapter.OnItemLongClickListener() {
            @Override
            public void onLongClick(int position, String title, String body) {
                Toast.makeText(MainActivity.this, "long click", Toast.LENGTH_SHORT).show();
                //提示dialog
                Memo mMemo = new Memo();
                mMemo.setTitle(title);
                mMemo.setText(body);
                Integer currentId = memoId.get(position);
                mMemo.setId(currentId);

                showDialog(position, mMemo);
            }
        });

    }
//    提示dialog是否删除该记录
    private void showDialog(final int position, final Memo record) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("提示：");
        dialog.setMessage("是否删除当前记录(添加的提醒事件将会同时被删除)");
        dialog.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //删除数据库中的记录
                doDelete(position, record);
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //停留在当前页面
                Toast.makeText(MainActivity.this, "已取消删除", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    //删除数据库中的记录
    // 同时删除提醒事件
    public void doDelete(int position, Memo record) {
        int currentMemoId = record.getId();
        Log.d(TAG, "currente memo id  is " + currentMemoId);
        Log.d(TAG, "currente memo position  is " + position);
        //memo.db删除数据
        DataSupport.deleteAll(Memo.class,  "id = ?",  "currentMemoId");
        //列表中删除该记录
        mMemoDataAdapter.removeItem(position);

        //判断是否删除提醒事件
        boolean tipsChecked = record.isTipsChecked();
        Log.d(TAG, "tipsChecked" + tipsChecked);
        if (tipsChecked) {
            //删除提醒事件
            //可以获取到备忘录的title和body
            String deleteTitle = record.getTitle();
            //根据title和body查询calendar中的id
            queryPosition(deleteTitle);
        }

        //更新数据/ui
        mDataList.post(new Runnable() {
            @Override
            public void run() {
                mMemoDataAdapter.notifyDataSetChanged();
            }
        });
    }

    private void queryPosition(String deleteTitle) {
        //遍历calendar的数据库来找到对应memo的id
        //查询事件
        ContentResolver contentResolver = getContentResolver();
        Uri uri = Uri.parse("content://com.android.calendar/events");
        Cursor cursor = contentResolver.query(uri, new String[]{CalendarContract.Events._ID}, CalendarContract.Events.TITLE + "=" + deleteTitle, null, null, null);
        String[] columnNames = cursor.getColumnNames();
        while (cursor.moveToNext()) {
            for (String columnName : columnNames) {
                mValues = cursor.getString(cursor.getColumnIndex(columnName));
                Log.d(TAG, columnName + "==" + mValues);
            }
        }

        long deleteEventId = Integer.parseInt(mValues);
        Log.d(TAG, "deleteEventId is " + deleteEventId);
        cursor.close();
        //根据ID删除calendar表中的数据
        if (deleteEventId != 0) {
            Uri deleteEventUri = ContentUris.withAppendedId(Uri.parse("content://com.android.calendar/events"), deleteEventId);
            getContentResolver().delete(deleteEventUri, null, null);
        }
    }

    //初始化控件

    private void initView() {
        //数据列表
        mDataList = findViewById(R.id.memo_list);
        //recyclerview的基本设置
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        mDataList.setLayoutManager(linearLayoutManager);
        mMemoDataAdapter = new MemoAdapter();
        mDataList.setAdapter(mMemoDataAdapter);

        //创建数据库
        Connector.getDatabase();
    }




}