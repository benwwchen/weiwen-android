package com.bencww.learning.weiwen.fragments;

import android.content.Context;
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
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.bencww.learning.weiwen.R;
import com.bencww.learning.weiwen.adapters.ExploreSectionsAdapter;
import com.bencww.learning.weiwen.adapters.PostsAdapter;
import com.bencww.learning.weiwen.models.ExploreSection;
import com.bencww.learning.weiwen.models.Post;
import com.bencww.learning.weiwen.utils.WeiwenApiClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ExploreFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ExploreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExploreFragment extends Fragment {

    private Context context;
    RecyclerView exploreSectionsRecyclerView;
    SwipeRefreshLayout mSwipeRefreshLayout;

    ArrayList<ExploreSection> mExploreSections;
    ExploreSectionsAdapter mExploreSectionsAdapter;
    Handler mExploreSectionsHandler;

    private OnFragmentInteractionListener mListener;

    public ExploreFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     // @param param1 Parameter 1.
     // @param param2 Parameter 2.
     * @return A new instance of fragment ExploreFragment.
     */
    public static ExploreFragment newInstance() {
        ExploreFragment fragment = new ExploreFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getContext();
        View exploreView = inflater.inflate(R.layout.fragment_explore, container, false);

        // recycler view for posts
        exploreSectionsRecyclerView = (RecyclerView) exploreView.findViewById(R.id.explore_recycler_view);

        // use a linear layout manager
        RecyclerView.LayoutManager exploreSectionsLayoutManager = new LinearLayoutManager(context);
        exploreSectionsRecyclerView.setLayoutManager(exploreSectionsLayoutManager);

        // initialize empty data, adapter
        mExploreSections = new ArrayList<>();
        mExploreSectionsAdapter = new ExploreSectionsAdapter(mExploreSections, context);
        exploreSectionsRecyclerView.setAdapter(mExploreSectionsAdapter);


        // get explore sections
        mExploreSectionsHandler = new Handler() {
            // 处理子线程给我们发送的消息。
            @Override
            public void handleMessage(android.os.Message msg) {
                List<ExploreSection> exploreSections;

                if(msg.what == 1){
                    exploreSections = (List<ExploreSection>) msg.obj;
                    Log.d("handler", "got sections");
                } else {
                    Log.d("error", (String) msg.obj);
                    exploreSections = Collections.EMPTY_LIST;
                }

                // update data
                mExploreSections.clear();
                mExploreSections.addAll(exploreSections);
                mExploreSectionsAdapter.notifyDataSetChanged();
                if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            };
        };

        // set up swipe to refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) exploreView.findViewById(R.id.layout_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            public void onRefresh() {
                // update data
                WeiwenApiClient.getInstance(context).getExplorePosts(mExploreSectionsHandler);
            }
        });

        // get data
        refresh();

        return exploreView;
    }

    public void refresh() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                WeiwenApiClient.getInstance(context).getExplorePosts(mExploreSectionsHandler);
            }
        });
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
