package com.amazonproductscraping.ui.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonproductscraping.ui.Interface.OnGet_ItemListener;
import com.amazonproductscraping.ui.R;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private List<String> imageUrls;
    private static final String TAG = "ImageAdapter";
    private final OnGet_ItemListener mListener;
    private Context mContext;

    public ImageAdapter(List<String> imageUrls,OnGet_ItemListener Listener, Context mContext) {
        this.imageUrls = imageUrls;
        mListener = Listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String imageUrl = imageUrls.get(position);
        //Glide.with(mContext).load(imageUrls.get(position)).into(holder.imageView);
        Picasso.get()
                .load(imageUrl)
                .into(holder.imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Image loaded successfully");
                        Toast.makeText(holder.itemView.getContext(), "Image loaded successfully", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Image load failed", e);
                        Toast.makeText(holder.itemView.getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                });


        // Check if listener is not null before calling
        if (mListener != null) {
            mListener.onGetItem(imageUrls.get(position));
        } else {
            Log.e(TAG, "mListener is null. Unable to call onGetItem.");
        }

    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
