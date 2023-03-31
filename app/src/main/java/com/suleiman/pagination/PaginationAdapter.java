package com.suleiman.pagination;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.suleiman.pagination.databinding.ItemListBinding;
import com.suleiman.pagination.databinding.ItemProgressBinding;
import com.suleiman.pagination.models.Result;
import com.suleiman.pagination.utils.GlideApp;
import com.suleiman.pagination.utils.GlideRequest;
import com.suleiman.pagination.utils.PaginationAdapterCallback;

import java.util.ArrayList;
import java.util.List;



public class PaginationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ItemListBinding itemListBinding;
    ItemProgressBinding itemProgressBinding;

    // View Types
    private static final int ITEM = 0;
    private static final int LOADING = 1;
//    private static final int HERO = 2;


    private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/w200";

    private List<Result> movieResults;
    private Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private PaginationAdapterCallback mCallback;

    private String errorMsg;

    PaginationAdapter(Context context) {
        this.context = context;
        this.mCallback = (PaginationAdapterCallback) context;
        movieResults = new ArrayList<>();
    }

    public List<Result> getMovies() {
        return movieResults;
    }

    public void setMovies(List<Result> movieResults) {
        this.movieResults = movieResults;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM:
                itemListBinding = ItemListBinding.inflate(inflater, parent, false);
                viewHolder = new MovieVH(itemListBinding);
                break;
            case LOADING:
                itemProgressBinding = ItemProgressBinding.inflate(inflater, parent, false);
                viewHolder = new LoadingVH(itemProgressBinding);
                break;
//            case HERO:
//                View viewHero = inflater.inflate(R.layout.item_hero, parent, false);
//                viewHolder = new HeroVH(viewHero);
//                break;
        }
        assert viewHolder != null;
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Result result = movieResults.get(position); // Movie

        switch (getItemViewType(position)) {

//            case HERO:
//                final HeroVH heroVh = (HeroVH) holder;
//
//                heroVh.mMovieTitle.setText(result.getTitle());
//                heroVh.mYear.setText(formatYearLabel(result));
//                heroVh.mMovieDesc.setText(result.getOverview());
//
//                loadImage(result.getBackdropPath())
//                        .into(heroVh.mPosterImg);
//                break;

            case ITEM:
                itemListBinding.movieTitle.setText(result.getTitle());
                itemListBinding.movieYear.setText(formatYearLabel(result));
                itemListBinding.movieDesc.setText(result.getOverview());


                loadImage(result.getPosterPath())
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                itemListBinding.movieProgress.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                itemListBinding.movieProgress.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(itemListBinding.moviePoster);

                break;

            case LOADING:
                if (retryPageLoad) {
                 itemProgressBinding.loadmoreErrorlayout.setVisibility(View.VISIBLE);
                  itemProgressBinding.loadmoreProgress.setVisibility(View.GONE);

                    itemProgressBinding.loadmoreErrortxt.setText(
                            errorMsg != null ?
                                    errorMsg :
                                    context.getString(R.string.error_msg_unknown));

                } else {
                    itemProgressBinding.loadmoreErrorlayout.setVisibility(View.GONE);
                    itemProgressBinding.loadmoreProgress.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return movieResults == null ? 0 : movieResults.size();
    }

    @Override
    public int getItemViewType(int position) {
//        if (position == 0) {
//            return HERO;
//        } else {
        return (position == movieResults.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
//        }
    }


    private String formatYearLabel(Result result) {
        return result.getReleaseDate().substring(0, 4)  // we want the year only
                + " | "
                + result.getOriginalLanguage().toUpperCase();
    }


    private GlideRequest<Drawable> loadImage(@NonNull String posterPath) {
        return GlideApp
                .with(context)
                .load(BASE_URL_IMG + posterPath)
                .centerCrop();
    }


    public void add(Result r) {
        movieResults.add(r);
        notifyItemInserted(movieResults.size() - 1);
    }

    public void addAll(List<Result> moveResults) {
        for (Result result : moveResults) {
            add(result);
        }
    }

    public void remove(Result r) {
        int position = movieResults.indexOf(r);
        if (position > -1) {
            movieResults.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }


    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new Result());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = movieResults.size() - 1;
        Result result = getItem(position);

        if (result != null) {
            movieResults.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Result getItem(int position) {
        return movieResults.get(position);
    }

    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(movieResults.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }


//    protected class HeroVH extends RecyclerView.ViewHolder {
//        private TextView mMovieTitle;
//        private TextView mMovieDesc;
//        private TextView mYear; // displays "year | language"
//        private ImageView mPosterImg;
//
//        public HeroVH(View itemView) {
//            super(itemView);
//
//            mMovieTitle = itemView.findViewById(R.id.movie_title);
//            mMovieDesc = itemView.findViewById(R.id.movie_desc);
//            mYear = itemView.findViewById(R.id.movie_year);
//            mPosterImg = itemView.findViewById(R.id.movie_poster);
//        }
//    }


    protected static class MovieVH extends RecyclerView.ViewHolder {

        public MovieVH(ItemListBinding itemView) {
            super(itemView.getRoot());

        }
    }


    protected class LoadingVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        public LoadingVH(ItemProgressBinding itemView) {
            super(itemView.getRoot());
            itemProgressBinding.loadmoreRetry.setOnClickListener(this);
            itemProgressBinding.loadmoreErrorlayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.loadmore_retry:
                case R.id.loadmore_errorlayout:

                    showRetry(false, null);
                    mCallback.retryPageLoad();

                    break;
            }
        }
    }

}
