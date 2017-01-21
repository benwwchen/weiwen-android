package com.bencww.learning.weiwen.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.bencww.learning.weiwen.models.ExploreSection;
import com.bencww.learning.weiwen.models.HomepageData;
import com.bencww.learning.weiwen.models.Post;
import com.bencww.learning.weiwen.models.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by BenWwChen on 2017/1/14.
 */

public class WeiwenApiClient {

    private static WeiwenApiClient mInstance;
    private static Context mCtx;
    CookieManager cookieManager;

    private String cookie;
    private User user = null;
    private String mMessage;
    private boolean isLogin = false;

    private static final String ROOT_URL = "http://10.0.0.13:3000";
    private static final String BASE_URL = ROOT_URL + "/api/";
    private static final String SESSION_PATH = "session";
    private static final String USER_PATH = "user";
    private static final String POST_PATH = "post";
    private static final String LIKE_PATH = "like";
    private static final String UNLIKE_PATH = "unlike";
    private static final String COMMENT_PATH = "comment";
    private static final String FOLLOW_PATH = "follow";
    private static final String UNFOLLOW_PATH = "unfollow";
    private static final String EXPLORE_PATH = "explore";
    private static final String IMAGE_PATH = "/images/";

    private static final int SUCCESS_CODE = 1;
    private static final int ERROR_CODE = -1;

    private WeiwenApiClient(Context context) {
        mCtx = context;
        // read cookie from storage if needed
        CookieStore cookieStore = new PersistentCookieStore(context);
        cookieManager = new CookieManager(cookieStore, CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
        // check if login still valid
        user = getUserInfo();
        if (!isLogin) {
           cookieManager.getCookieStore().removeAll();
        }
    }

    public static synchronized WeiwenApiClient getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new WeiwenApiClient(context);
        }
        return mInstance;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void logout() {
        cookieManager.getCookieStore().removeAll();
        isLogin = false;
    }

    public boolean loginAccount(final String userName, final String password) {
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                getAbsoluteUrl(SESSION_PATH), future, future) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", userName);
                params.put("password", password);

                return params;
            }
        };
        VolleyUtil.getInstance(mCtx).addToRequestQueue(stringRequest);
        try {
            String response = future.get(); // this will block
            Log.d("LOGIN", "response" + response.toString());
            if (response.contains("success")) {
                isLogin = true;

                // retrive user info
                this.user = getUserInfo();
                if (user == null) {
                    // something went wrong
                    mMessage = "无法获取用户信息";
                    return false;
                }

                return true;
            } else {
                mMessage = getMessageFromJSON(response);
            }
        } catch (InterruptedException e) {
            Log.d("ERROR", "error => " + e.toString());
        } catch (ExecutionException e) {
            Log.d("ERROR", "error => " + e.toString());
        }
        return false;
    }

    private User getUserInfo() {
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                getAbsoluteUrl(SESSION_PATH), future, future);
        VolleyUtil.getInstance(mCtx).addToRequestQueue(stringRequest);
        try {
            String response = future.get();
            if (response.contains("user")) {
                isLogin = true;

                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                User user = gson.fromJson(response, User.class);

                // get the absolute avatar url
                user.setAvatar(ROOT_URL + user.getAvatar());
                return user;
            } else {
                mMessage = getMessageFromJSON(response);
            }
        } catch (InterruptedException e) {
            Log.d("ERROR", "error => " + e.toString());
        } catch (ExecutionException e) {
            Log.d("ERROR", "error => " + e.toString());
        }
        return null;
    }

    public User getUser() {
        return user;
    }

    public void getTimelinePosts(final Handler handler) {
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                getAbsoluteUrl(POST_PATH), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Message message = new Message();

                if (response.contains("posts")) {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        List<Post> posts = gson.fromJson(jsonObject.getString("posts"), new TypeToken<List<Post>>(){}.getType());
                        message.what = SUCCESS_CODE;
                        message.obj = posts;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } else {
                    message.what = ERROR_CODE;
                    message.obj = getMessageFromJSON(response);
                }

                handler.sendMessage(message);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Message message = new Message();
                message.what = ERROR_CODE;
                message.obj = String.valueOf(volleyError.networkResponse.statusCode);
                handler.sendMessage(message);
            }
        });
        VolleyUtil.getInstance(mCtx).addToRequestQueue(stringRequest);
    }

    public void getExplorePosts(final Handler handler) {
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                getAbsoluteUrl(EXPLORE_PATH), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Message message = new Message();

                if (response.contains("explore_sections")) {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        List<ExploreSection> exploreSections = gson.fromJson(
                                jsonObject.getString("explore_sections"),
                                new TypeToken<List<ExploreSection>>(){}.getType());
                        message.what = SUCCESS_CODE;
                        message.obj = exploreSections;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } else {
                    message.what = ERROR_CODE;
                    message.obj = getMessageFromJSON(response);
                }

                handler.sendMessage(message);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Message message = new Message();
                message.what = ERROR_CODE;
                message.obj = String.valueOf(volleyError.networkResponse.statusCode);
                handler.sendMessage(message);
            }
        });
        VolleyUtil.getInstance(mCtx).addToRequestQueue(stringRequest);
    }

    public void getPost(final int postId, final Handler handler) {
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                getAbsoluteUrl(POST_PATH + "/" + String.valueOf(postId)), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Message message = new Message();

                if (response.contains("post")) {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        Post post = gson.fromJson(jsonObject.getString("post"), Post.class);
                        message.what = SUCCESS_CODE;
                        message.obj = post;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } else {
                    message.what = ERROR_CODE;
                    message.obj = getMessageFromJSON(response);
                }

                handler.sendMessage(message);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Message message = new Message();
                message.what = ERROR_CODE;
                message.obj = String.valueOf(volleyError.networkResponse.statusCode);
                handler.sendMessage(message);
            }
        });
        VolleyUtil.getInstance(mCtx).addToRequestQueue(stringRequest);
    }

    public void deletePost(final int postId, final Handler handler) {
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest stringRequest = new StringRequest(Request.Method.DELETE,
                getAbsoluteUrl(POST_PATH + "/" + String.valueOf(postId)),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Message message = new Message();

                        if (response.contains("success")) {
                            message.what = SUCCESS_CODE;
                            message.obj = true;
                        } else {
                            message.what = ERROR_CODE;
                            message.obj = getMessageFromJSON(response);
                        }

                        handler.sendMessage(message);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Message message = new Message();
                message.what = ERROR_CODE;
                message.obj = String.valueOf(volleyError.networkResponse.statusCode);
                handler.sendMessage(message);
            }
        });
        VolleyUtil.getInstance(mCtx).addToRequestQueue(stringRequest);
    }

    public void getHomepageData(String username, final Handler handler) {
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                getAbsoluteUrl(USER_PATH + "/" + username), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Message message = new Message();

                if (response.contains("user_data")) {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        HomepageData homepageData = gson.fromJson(
                                jsonObject.getString("user_data"),
                                HomepageData.class);
                        message.what = SUCCESS_CODE;
                        message.obj = homepageData;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    message.what = ERROR_CODE;
                    message.obj = getMessageFromJSON(response);
                }

                handler.sendMessage(message);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Message message = new Message();
                message.what = ERROR_CODE;
                message.obj = String.valueOf(volleyError.networkResponse.statusCode);
                handler.sendMessage(message);
            }
        });
        VolleyUtil.getInstance(mCtx).addToRequestQueue(stringRequest);
    }

    public void setLikeForAPost(final int postId, boolean like, final Handler handler) {
        int method = like? Request.Method.POST : Request.Method.DELETE;
        String path = like? LIKE_PATH : UNLIKE_PATH;
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest stringRequest = new StringRequest(method,
                getAbsoluteUrl(path + "/" + String.valueOf(postId)),
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Message message = new Message();

                if (response.contains("success")) {
                    message.what = SUCCESS_CODE;
                    message.obj = true;
                } else {
                    message.what = ERROR_CODE;
                    message.obj = getMessageFromJSON(response);
                }

                handler.sendMessage(message);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Message message = new Message();
                message.what = ERROR_CODE;
                message.obj = String.valueOf(volleyError.networkResponse.statusCode);
                handler.sendMessage(message);
            }
        });
        VolleyUtil.getInstance(mCtx).addToRequestQueue(stringRequest);
    }

    public void commentOnAPost(final int postId, final String content, final Handler handler) {
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                getAbsoluteUrl(COMMENT_PATH),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Message message = new Message();

                        if (response.contains("success")) {
                            message.what = SUCCESS_CODE;
                            message.obj = true;
                        } else {
                            message.what = ERROR_CODE;
                            message.obj = getMessageFromJSON(response);
                        }

                        handler.sendMessage(message);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Message message = new Message();
                message.what = ERROR_CODE;
                message.obj = String.valueOf(volleyError.networkResponse.statusCode);
                handler.sendMessage(message);
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("postId", String.valueOf(postId));
                params.put("commentContent", content);

                return params;
            }
        };
        VolleyUtil.getInstance(mCtx).addToRequestQueue(stringRequest);
    }

    public void setFollowByUsername(final String username, boolean follow, final Handler handler) {
        int method = follow? Request.Method.POST : Request.Method.DELETE;
        String path = follow? FOLLOW_PATH : UNFOLLOW_PATH;
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest stringRequest = new StringRequest(method,
                getAbsoluteUrl(path + "/" + username),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Message message = new Message();

                        if (response.contains("success")) {
                            message.what = SUCCESS_CODE;
                            message.obj = true;
                        } else {
                            message.what = ERROR_CODE;
                            message.obj = getMessageFromJSON(response);
                        }

                        handler.sendMessage(message);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Message message = new Message();
                message.what = ERROR_CODE;
                message.obj = String.valueOf(volleyError.networkResponse.statusCode);
                handler.sendMessage(message);
            }
        });
        VolleyUtil.getInstance(mCtx).addToRequestQueue(stringRequest);
    }

    public static String getAvatarUrl(String avatar) {
        return ROOT_URL + avatar;
    }

    public static String getPicUrl(String url) {
        return ROOT_URL + IMAGE_PATH + url;
    }

    private String getMessageFromJSON(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            if (jsonObject.has("success")) {
                return jsonObject.getString("success");
            } else if (jsonObject.has("error")) {
                return jsonObject.getString("error");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getMessage() {
        return mMessage;
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
