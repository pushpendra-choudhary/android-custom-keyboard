package com.example.crypsis.customkeyboard;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by crypsis on 18/11/16.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    String searchString="";
    RecyclerAdapterListener listener;

    public RecyclerAdapter(RecyclerAdapterListener listener){
        this.listener = listener;
    }


    public void setAdapter(String val){
        searchString = val;
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
           holder.textView.setText(searchString+ position);
           if(position%2==0){
               holder.linearLayout.setBackgroundResource(R.drawable.fl2);
           }else{
               holder.linearLayout.setBackgroundResource(R.drawable.fl1);
           }
    }

    @Override
    public int getItemCount() {
        return 10;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        TextView textView;

        public MyViewHolder(View itemView) {
            super(itemView);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.list_item);
            textView = (TextView) itemView.findViewById(R.id.title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getLayoutPosition();
                    listener.ola("http://hello.com/search="+searchString+position);
                }
            });
        }
    }

    public interface RecyclerAdapterListener{
        void ola(String url);
    }

}
