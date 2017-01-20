package com.bencww.learning.weiwen.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.bencww.learning.weiwen.CommentActivity;
import com.bencww.learning.weiwen.R;
import com.bencww.learning.weiwen.adapters.PostsAdapter;
import com.bencww.learning.weiwen.models.Post;
import com.bencww.learning.weiwen.utils.WeiwenApiClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TimelineFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TimelineFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TimelineFragment extends Fragment implements PostsAdapter.PostsAdapterCallback {

    private Context context;

    RecyclerView postsRecyclerView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    ImageLoader mImageLoader;

    // data
    ArrayList<Post> mPosts;

    // adapter, handler
    PostsAdapter mPostsAdapter;
    Handler mPostsHandler;

    private OnFragmentInteractionListener mListener;

    private static int COMMENT_CODE = 1;

    public TimelineFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TimelineFragment.
     */
    public static TimelineFragment newInstance() {
        TimelineFragment fragment = new TimelineFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getContext();
        // Inflate the layout for this fragment
        View timelineView =  inflater.inflate(R.layout.fragment_timeline, container, false);

        // recycler view for posts
        postsRecyclerView = (RecyclerView) timelineView.findViewById(R.id.posts_recycler_view);

        // use a linear layout manager
        RecyclerView.LayoutManager postsLayoutManager = new LinearLayoutManager(context);
        postsRecyclerView.setLayoutManager(postsLayoutManager);

        // initialize empty data, adapter
        mPosts = new ArrayList<>();
        mPostsAdapter = new PostsAdapter(mPosts, context, this);
        postsRecyclerView.setAdapter(mPostsAdapter);

        // get/update posts handler
        mPostsHandler = new Handler() {
            // 处理子线程给我们发送的消息。
            @Override
            public void handleMessage(android.os.Message msg) {
                List<Post> posts;

                if(msg.what == 1){
                    posts = (List<Post>) msg.obj;
                } else {
                    Log.d("error", (String) msg.obj);
                    posts = Collections.EMPTY_LIST;
                }

                // specify an adapter
                mPosts.clear();
                mPosts.addAll(posts);
                mPostsAdapter.notifyDataSetChanged();
                if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            };
        };

        // set up swipe to refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) timelineView.findViewById(R.id.layout_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            public void onRefresh() {
                // update data
                WeiwenApiClient.getInstance(context).getTimelinePosts(mPostsHandler);
            }
        });

        // get data
        refresh();

        return timelineView;
    }

    // trigger swipe to refresh
    public void refresh() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                WeiwenApiClient.getInstance(context).getTimelinePosts(mPostsHandler);
            }
        });
    }

    // refresh data only
    public void refreshData() {
        WeiwenApiClient.getInstance(context).getTimelinePosts(mPostsHandler);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onLikeButtonClick(int postId, boolean like) {
        Handler likePostHandler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                boolean isSuccess;

                if(msg.what == 1){

                    isSuccess = (boolean) msg.obj;
                } else {
                    Log.d("error", (String) msg.obj);
                    Toast.makeText(context, "错误:" + msg.obj, Toast.LENGTH_SHORT).show();
                    isSuccess = false;
                }

                refreshData();
            }
        };
        WeiwenApiClient.getInstance(context).setLikeForAPost(postId, like, likePostHandler);
    }

    @Override
    public void onCommentButtonClick(int postId) {
        Intent intent = new Intent(context, CommentActivity.class);
        intent.putExtra("postId", postId);
        startActivityForResult(intent, COMMENT_CODE);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
