package gavinli.translator.imagelink;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayoutManager;

import java.util.ArrayList;
import java.util.List;

import gavinli.translator.R;
import gavinli.translator.util.imageloader.ImageLoader;

/**
 * Created by GavinLi
 * on 17-3-12.
 */

public class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<String> mImageLinks = new ArrayList<>();
    private OnItemClickLinstener mLinstener;

    private Context mContext;

    private boolean hasFooter = false;
    private static final int TYPE_FOOTER = -1;

    private int mLessThanWidth;

    public ImageAdapter(Context context) {
        mContext = context;
        mLessThanWidth = mContext.getResources().getDisplayMetrics().widthPixels / 2;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_FOOTER) {
            return new LoadingFooterHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_loading, parent, false));
        } else {
            return new ImageViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_image, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(hasFooter && position ==  mImageLinks.size()) {
            return TYPE_FOOTER;
        } else {
            return super.getItemViewType(position);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(!hasFooter || position != mImageLinks.size() &&
                holder instanceof ImageViewHolder) {
            ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
            imageViewHolder.mExplainView.setImageResource(R.drawable.img_placeholder);
            ImageLoader.with(mContext)
                    .load(mImageLinks.get(position))
                    .lessThan(mLessThanWidth, ImageLoader.DEFAULT_SIZE)
                    .into(imageViewHolder.mExplainView);
            ViewGroup.LayoutParams params = imageViewHolder.mExplainView.getLayoutParams();
            if (params instanceof FlexboxLayoutManager.LayoutParams) {
                FlexboxLayoutManager.LayoutParams layoutParams = (FlexboxLayoutManager.LayoutParams) params;
                layoutParams.setFlexGrow(1.0f);
            }
            imageViewHolder.mExplainView.setOnClickListener(view -> {
                if (mLinstener != null) mLinstener.onClick(imageViewHolder.mExplainView, position);
            });
        } else if (hasFooter &&
                position == mImageLinks.size()) {
            LoadingFooterHolder loadingFooterHolder = (LoadingFooterHolder) holder;
            loadingFooterHolder.mProgressBar.setVisibility(View.GONE);
            loadingFooterHolder.mInfoTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return hasFooter ? mImageLinks.size() + 1 : mImageLinks.size();
    }

    public void addImageLinks(String link) {
        mImageLinks.add(link);
    }

    public void showNotMoreImages() {
        if (!hasFooter) {
            hasFooter = true;
            notifyItemChanged(mImageLinks.size());
        }
    }

    public List<String> getImageLinks() {
        return mImageLinks;
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView mExplainView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            mExplainView = itemView.findViewById(R.id.iv_explain);
        }
    }

    class LoadingFooterHolder extends RecyclerView.ViewHolder {
        private ProgressBar mProgressBar;
        private TextView mInfoTextView;

        public LoadingFooterHolder(View itemView) {
            super(itemView);
            mProgressBar = itemView.findViewById(R.id.progress_bar);
            mInfoTextView = itemView.findViewById(R.id.tv_info);
        }
    }

    public void setOnItemClickLinstener(OnItemClickLinstener linstener) {
        mLinstener = linstener;
    }

    public interface OnItemClickLinstener {
        void onClick(View view, int postion);
    }
}