package test_5;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.ViewHolder> {

    private static final String TAG = "MemoAdapter";
    private List<Memo> mData = new ArrayList<>();
    private OnItemClickLinstener mLinstener = null;
    private OnItemLongClickListener mLongClickListner = null;



    @NonNull
    @Override
    public MemoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.memo_item,parent,false);
        return new MemoAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemoAdapter.ViewHolder holder, final int position) {

        //获取数据
        int itemId = mData.get(position).getId();
        final String itemTitle = mData.get(position).getTitle();
        final String itemBody = mData.get(position).getText();
        final String itemCreateTime = mData.get(position).getCreateTime();
        final boolean tipsChecked = mData.get(position).isTipsChecked();

        //设置数据
        holder.mItemId.setText(itemId + "");
        holder.mItemTitle.setText(itemTitle);
        holder.mItemBody.setText(itemBody);
        holder.mCreateTime.setText("create:" + itemCreateTime);

        //设置点击事件，跳转到编辑页面
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLinstener.onClick(position,itemTitle,itemBody,tipsChecked);
            }
        });

        //设置item的长按事件，长按删除
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mLongClickListner.onLongClick(position,itemTitle,itemBody);
                return true;
            }
        });

    }
    //item长按事件
    public void setOnItemLongClickLinstener(OnItemLongClickListener listener) {
        this.mLongClickListner = listener;
    }
    public interface OnItemLongClickListener {
        void onLongClick(int position, String title, String body);
    }

    public void removeItem(int position) {
        this.mData.remove(position);
    }

    //item点击事件
    public void setOnItemClickLinstener(OnItemClickLinstener linstener) {
        this.mLinstener = linstener;
    }

    public  interface OnItemClickLinstener {
        void onClick(int position, String title, String body, boolean tipsChecked);
    }
    //数据库中有几条数据就显示几条
    @Override
    public int getItemCount() {
        return mData.size();
    }


    //将外部数据库中遍历的数据传到adapter,将数据设置到itemView中
    public void setData (List<Memo> memoList) {
        mData.clear();
        this.mData = memoList;
        notifyDataSetChanged();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.memo_id)
        TextView mItemId;
        @BindView(R.id.memo_title)
        TextView mItemTitle;
        @BindView(R.id.memo_body)
        TextView mItemBody;
        @BindView(R.id.memo_create_time)
        TextView mCreateTime;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);


        }
    }
}
