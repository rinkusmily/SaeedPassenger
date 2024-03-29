package com.mylibrary.volley;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cabily.app.SingUpAndSignIn;
import com.cabily.iconstant.Iconstant;
import com.casperon.app.cabily.R;
import com.mylibrary.dialog.PkDialog;
import com.cabily.utils.SessionManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Prem Kumar and Anitha on 11/26/2015.
 */
public class ServiceRequest {
    private Context context;
    private ServiceListener mServiceListener;
    private StringRequest stringRequest;
    SessionManager session;
    private String UserID = "", gcmID = "";

    private String userID = "";

    public interface ServiceListener {
        void onCompleteListener(String response);
        void onErrorListener();
    }

    public ServiceRequest(Context context) {
        this.context = context;
        session=new SessionManager(context);
        HashMap<String, String> user = session.getUserDetails();
        userID = user.get(SessionManager.KEY_USERID);
        gcmID = user.get(SessionManager.KEY_GCM_ID);
        System.out.println("topuserid2--------"+userID);
        System.out.println("topgcmID2--------"+gcmID);
    }

    public void cancelRequest()
    {
        if (stringRequest != null) {
            stringRequest.cancel();
        }
    }

    public void makeServiceRequest(final String url, int method, final HashMap<String, String> param,ServiceListener listener) {

        this.mServiceListener=listener;

        stringRequest = new StringRequest(method, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    mServiceListener.onCompleteListener(response);
                    JSONObject object = new JSONObject(response);

                    if (object.has("is_dead")) {
                        System.out.println("-----------is dead----------------");
                        final PkDialog mDialog = new PkDialog(context);
                        mDialog.setDialogTitle(context.getResources().getString(R.string.action_session_expired_title));
                        mDialog.setDialogMessage(context.getResources().getString(R.string.action_session_expired_message));
                        mDialog.setPositiveButton(context.getResources().getString(R.string.action_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDialog.dismiss();
                                session.logoutUser();
                                Intent intent = new Intent(context, SingUpAndSignIn.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                context.startActivity(intent);
                            }
                        });
                        mDialog.show();

                    }

                /*    if (object.has("is_dead")) {
                        System.out.println("-----------is dead----------------");
                        final PkDialog mDialog = new PkDialog(context);
                        mDialog.setDialogTitle(context.getResources().getString(R.string.action_session_expired_title));
                        mDialog.setDialogMessage(context.getResources().getString(R.string.action_session_expired_message));
                        mDialog.setPositiveButton(context.getResources().getString(R.string.action_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDialog.dismiss();
                                sessionManager.logoutUser();
                                Intent intent = new Intent(context, SingUpAndSignIn.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                context.startActivity(intent);
                            }
                        });
                        mDialog.show();

                    }*/

                } catch (Exception e) {
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                        Toast.makeText(context, "Unable to fetch data from server", Toast.LENGTH_LONG).show();
                    } else if (error instanceof AuthFailureError) {
                        Toast.makeText(context, "AuthFailureError", Toast.LENGTH_LONG).show();
                    } else if (error instanceof ServerError) {
                        Toast.makeText(context, "ServerError", Toast.LENGTH_LONG).show();
                    } else if (error instanceof NetworkError) {
                        Toast.makeText(context, "NetworkError", Toast.LENGTH_LONG).show();
                    } else if (error instanceof ParseError) {
                        Toast.makeText(context, "ParseError", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                }
                mServiceListener.onErrorListener();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return param;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                System.out.println("------------apptype------cabily---------" + Iconstant.cabily_AppType);
                System.out.println("------------userid----------cabily-----" + userID);
                System.out.println("------------apptoken----------cabily-----" + gcmID);
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent", Iconstant.cabily_userAgent);
                headers.put("isapplication",Iconstant.cabily_IsApplication);
                headers.put("applanguage",Iconstant.cabily_AppLanguage);
                headers.put("apptype", Iconstant.cabily_AppType);
                headers.put("userid",userID);
                headers.put("apptoken",gcmID);
              /*  System.out.println("servicereques  apptype------------------"+Iconstant.cabily_AppType);
                System.out.println("servicereques apptoken------------------"+gcmID);
                System.out.println("servicereques userid------------------"+UserID);
                Map<String, String> headers = new HashMap<String, String>();

                headers.put("User-agent",Iconstant.cabily_userAgent);
                headers.put("isapplication",Iconstant.cabily_IsApplication);
                headers.put("applanguage",Iconstant.cabily_AppLanguage);
                headers.put("apptype",Iconstant.cabily_AppType);
                headers.put("apptoken",gcmID);
                headers.put("userid",UserID);*/

                return headers;
            }
        };

        //to avoid repeat request Multiple Time
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        stringRequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

}
