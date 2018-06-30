package com.devwilfred.journally;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DiaryAdapter extends FirestoreRecyclerAdapter<Thought, RecyclerView.ViewHolder> {

    RecyclerView mRecyclerView;
    TextView mErrorView;
    StorageReference mStorageReference;
    ThoughtClickListener mListener;

    interface  ThoughtClickListener {

        void onThoughtClicked(int pAdapterPosition, Thought pModel, ImageView pImageView);
        void onThoughtNoImageClicked(int pAdapterPosition, Thought pModel);
    }

    DiaryAdapter(@NonNull FirestoreRecyclerOptions<Thought> options,
                 TextView pErrorView, StorageReference pReference, ThoughtClickListener pListener) {
        super(options);
        mErrorView = pErrorView;
        mStorageReference = pReference;
        mListener = pListener;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView pRecyclerView) {
        super.onAttachedToRecyclerView(pRecyclerView);
        this.mRecyclerView = pRecyclerView;
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        if (getItemCount() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            mErrorView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mErrorView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).getPhotoUrl() != null) return 0;
        else return 1;
    }

    @Override
    protected void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position, @NonNull final Thought model) {
        holder.itemView.setTag(model.getIdentifier());

        switch (holder.getItemViewType()) {
            case 0:
                final HolderWithImage holderWithImage = ((HolderWithImage) holder);
                ViewCompat.setTransitionName(holderWithImage.mImageView, model.getIdentifier());
                holderWithImage.mTitle.setText(model.getTitle());
                holderWithImage.mTag.setText(model.getTag());
                holderWithImage.mDescription.setText(model.getDescription());
                holderWithImage.mWhen.setText(DateUtils.getRelativeTimeSpanString(model.getWhen().getTime()));
                Glide.with(holder.itemView.getContext())
                        .using(new FirebaseImageLoader())
                        .load(mStorageReference.child(model.getPhotoUrl()))
                        .placeholder(R.drawable.ic_loading_image)
                        .into(holderWithImage.mImageView);

                holderWithImage.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View pView) {
                        mListener.onThoughtClicked(holder.getAdapterPosition(), model, holderWithImage.mImageView);
                    }
                });
                break;

            case 1:
                final Holder holderNoImage= ((Holder) holder);

                holderNoImage.mTitle.setText(model.getTitle());
                holderNoImage.mTag.setText(model.getTag());
                holderNoImage.mDescription.setText(model.getDescription());
                holderNoImage.mWhen.setText(DateUtils.getRelativeTimeSpanString(model.getWhen().getTime()));

                holderNoImage.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View pView) {
                        mListener.onThoughtNoImageClicked(holder.getAdapterPosition(), model);
                    }
                });
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup pViewGroup, int viewType) {

        switch (viewType) {
            case 0:
                return new HolderWithImage(LayoutInflater.from(pViewGroup.getContext())
                        .inflate(R.layout.item_thought_image_type, pViewGroup, false));

            case 1:
                return new Holder(LayoutInflater.from(pViewGroup.getContext())
                        .inflate(R.layout.item_thought_no_image, pViewGroup, false));
        }

        return null;
    }


    class Holder extends RecyclerView.ViewHolder {

        TextView mTitle, mDescription, mWhen, mTag;
        ImageView mImageView;

        Holder(@NonNull View itemView) {
            super(itemView);

            mTag = itemView.findViewById(R.id.thought_tag);
            mDescription = itemView.findViewById(R.id.thought_description);
            mTitle = itemView.findViewById(R.id.thought_title);
            mWhen = itemView.findViewById(R.id.thought_date);
            mImageView = itemView.findViewById(R.id.thought_image);
        }

    }

    class HolderWithImage extends RecyclerView.ViewHolder {

        TextView mTitle, mDescription, mWhen, mTag;
        ImageView mImageView;

        HolderWithImage(@NonNull View itemView) {
            super(itemView);

            mTag = itemView.findViewById(R.id.thought_tag);
            mDescription = itemView.findViewById(R.id.thought_description);
            mTitle = itemView.findViewById(R.id.thought_title);
            mWhen = itemView.findViewById(R.id.thought_date);
            mImageView = itemView.findViewById(R.id.thought_image);
        }

    }
}
