package com.example.newsapp.adapter;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.newsapp.model.NewsModel;
import com.example.newsapp.R;
import com.example.newsapp.utils.DateHelper;
import com.squareup.picasso.Picasso;
import java.util.Collections;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private LayoutInflater inflater;
    List<NewsModel> data= Collections.emptyList();
    // create constructor to innitilize context and data sent from MainActivity
    public NewsAdapter(Context context, List<NewsModel> data){
        this.context=context;
        inflater= LayoutInflater.from(context);
        this.data=data;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.item_news, parent,false);
        MyHolder holder=new MyHolder(view);
        return holder;
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Get current position of item in recyclerview to bind data and assign values from list
        MyHolder myHolder= (MyHolder) holder;
        NewsModel current=data.get(position);
        myHolder.newsTitle.setText(current.title);
        myHolder.newsTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = myHolder.getAdapterPosition();
                String url = data.get(position).url;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(browserIntent);
            }
        });
        myHolder.newsDescription.setText(current.description);
        myHolder.newsPublishDate.setText(DateHelper.parseDateToddMMyyyy(current.publishAt));
        Picasso.with(context).load(current.urltoImage).fit().centerCrop().placeholder(R.mipmap.moengage).error(R.mipmap.moengage).into(myHolder.newsImage);
    }
    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView newsImage;
        TextView newsTitle,newsDescription,newsPublishDate;
        public MyHolder(View itemView) {
            super(itemView);
            newsImage= (ImageView) itemView.findViewById(R.id.newsImage);
            newsTitle= (TextView) itemView.findViewById(R.id.newsTitle);
            newsDescription = (TextView) itemView.findViewById(R.id.newsDescription);
            newsPublishDate = (TextView) itemView.findViewById(R.id.newsPublishDate);
        }
}
}