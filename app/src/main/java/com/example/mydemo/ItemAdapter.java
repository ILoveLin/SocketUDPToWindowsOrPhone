package com.example.mydemo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mydemo.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2022/4/28 8:53
 * desc：
 */

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private ArrayList<ItemBean> mActiveList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        ProgressBar mProcess;
        TextView mTitle;

        public ViewHolder(View view) {
            super(view);
            mProcess = view.findViewById(R.id.progressBar);
            mTitle = view.findViewById(R.id.tv_title);
        }

    }

    public ArrayList<ItemBean> getData(){
        return mActiveList;
    }
    public void setData(ArrayList<ItemBean> list){
        mActiveList.clear();
        mActiveList.addAll(list);
        notifyDataSetChanged();
    }


    /**
     * 更新某个位置上的数据
     */
    public void setItem(@IntRange(from = 0) int position, @NonNull ItemBean item) {
        if (mActiveList == null) {
            mActiveList = new ArrayList<>();
        }
        mActiveList.set(position, item);
        notifyItemChanged(position);
    }


    public ItemAdapter(ArrayList<ItemBean> activeList) {
        mActiveList = activeList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_text, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ItemBean active = mActiveList.get(position);
        LogUtils.e("adapter===="+active.getTag()+"");

        holder.mProcess.setMax((int) active.maxLength);
        holder.mProcess.setProgress((int) active.currentLength);
        holder.mTitle.setText(active.getTag()+"");
    }

    @Override
    public int getItemCount() {
        return mActiveList.size();
    }
}
