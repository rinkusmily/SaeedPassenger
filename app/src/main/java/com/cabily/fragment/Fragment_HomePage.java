package com.cabily.fragment;

/**
 * Created by Prem Kumar on 10/1/2015.
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cabily.HockeyApp.FragmentHockeyApp;
import com.cabily.adapter.SelectCarTypeAdapter;
import com.cabily.app.EstimatePage;
import com.cabily.app.FavoriteList;
import com.cabily.app.LocationSearch;
import com.cabily.app.NavigationDrawer;
import com.cabily.app.TimerPage;
import com.cabily.adapter.BookMyRide_Adapter;
import com.mylibrary.gps.GPSTracker;
import com.cabily.iconstant.Iconstant;
import com.cabily.pojo.HomePojo;
import com.mylibrary.volley.AppController;
import com.cabily.utils.ConnectionDetector;
import com.cabily.utils.HorizontalListView;
import com.cabily.utils.SessionManager;
import com.casperon.app.cabily.R;
import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mylibrary.volley.VolleyErrorResponse;
import com.mylibrary.xmpp.ChatService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;


public class Fragment_HomePage extends FragmentHockeyApp {
    private RelativeLayout drawer_layout;
    private RelativeLayout address_layout, favorite_layout, bottom_layout;
    private RelativeLayout loading_layout;
    private RelativeLayout alert_layout;
    private TextView alert_textview;
    private ImageView center_marker, currentLocation_image;
    private TextView map_address;
    private RelativeLayout rideLater_layout, rideNow_layout;
    private TextView rideLater_textview, rideNow_textview;
    Context context;

    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;

    private GoogleMap googleMap;
    MarkerOptions marker;
    static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;

    StringRequest postrequest;
    private SessionManager session;
    private String UserID = "", CategoryID = "";
    private String CarAvailable = "";
    private String ScarType = "";
    private String selectedType = "";
    GPSTracker gps;
    String SselectedAddress="";
    String Sselected_latitude="",Sselected_longitude="";

    ArrayList<HomePojo> driver_list = new ArrayList<HomePojo>();
    ArrayList<HomePojo> category_list = new ArrayList<HomePojo>();
    ArrayList<HomePojo> ratecard_list = new ArrayList<HomePojo>();

    private boolean driver_status = false;
    private boolean category_status = false;
    private boolean ratecard_status = false;
    private boolean main_response_status = false;

    private double MyCurrent_lat = 0.0, MyCurrent_long = 0.0;
    private double Recent_lat = 0.0, Recent_long = 0.0;

    private BookMyRide_Adapter adapter;
    private HorizontalListView listview;

    private RelativeLayout ridenow_option_layout;
    private RelativeLayout carType_layout, pickTime_layout;
    private LinearLayout ratecard_layout, estimate_layout, coupon_layout;
    private TextView tv_carType, tv_pickuptime, tv_coupon_label;

    private SimpleDateFormat mFormatter = new SimpleDateFormat("MMM/dd,hh:mm aa");
    private SimpleDateFormat coupon_mFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat coupon_time_mFormatter = new SimpleDateFormat("hh:mm aa");
    private SimpleDateFormat mTime_Formatter = new SimpleDateFormat("HH");

    private static View rootview;

    //------Declaration for Coupon code-----
    private RelativeLayout coupon_apply_layout, coupon_loading_layout;
    private MaterialDialog coupon_dialog;
    private EditText coupon_edittext;
    private String coupon_selectedDate = "";
    private String coupon_selectedTime = "";

    //------Declaration for Confirm Ride-----
    private String response_time = "", riderId = "";
    Dialog dialog;
    private int timer_request_code = 100;
    private int placeSearch_request_code = 200;
    private int favoriteList_request_code = 300;

    BroadcastReceiver logoutReciver;
    private boolean ratecard_clicked = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootview != null) {
            ViewGroup parent = (ViewGroup) rootview.getParent();
            if (parent != null)
                parent.removeView(rootview);
        }
        try {
            rootview = inflater.inflate(R.layout.homepage, container, false);
        } catch (InflateException e) {
        /* map is already there, just return view as it is */
        }

        context = getActivity();
        initialize(rootview);
        initilizeMap();

        //Start XMPP Chat Service
        ChatService.startUserAction(getActivity());

        // Finishing the activity using broadcast
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.app.logout");
        filter.addAction("com.pushnotification.updateBottom_view");
        logoutReciver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("com.app.logout")) {
                    getActivity().finish();
                }
                else if(intent.getAction().equals("com.pushnotification.updateBottom_view"))
                {
                    googleMap.getUiSettings().setAllGesturesEnabled(true);
                    ridenow_option_layout.setVisibility(View.GONE);
                    listview.setVisibility(View.VISIBLE);
                    rideLater_textview.setText(getResources().getString(R.string.home_label_ride_later));
                    rideNow_textview.setText(getResources().getString(R.string.home_label_ride_now));
                    currentLocation_image.setClickable(true);
                    pickTime_layout.setEnabled(true);
                    drawer_layout.setEnabled(true);
                    address_layout.setEnabled(true);
                    favorite_layout.setEnabled(true);
                    NavigationDrawer.enableSwipeDrawer();
                    double Dlatitude = gps.getLatitude();
                    double Dlongitude = gps.getLongitude();
                    // Move the camera to last position with a zoom level
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Dlatitude, Dlongitude)).zoom(17).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }

            }
        };
        getActivity().registerReceiver(logoutReciver, filter);


        drawer_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationDrawer.openDrawer();
            }
        });

        address_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LocationSearch.class);
                startActivityForResult(intent, placeSearch_request_code);
                getActivity().overridePendingTransition(R.anim.slideup, R.anim.slidedown);
            }
        });

        favorite_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if(map_address.getText().toString().length()>0)
                {
                    Intent intent = new Intent(getActivity(), FavoriteList.class);
                    intent.putExtra("SelectedAddress",SselectedAddress);
                    intent.putExtra("SelectedLatitude",Sselected_latitude);
                    intent.putExtra("SelectedLongitude",Sselected_longitude);
                    startActivityForResult(intent, favoriteList_request_code);
                    getActivity().overridePendingTransition(R.anim.enter, R.anim.exit);
                }
                else
                {
                    Alert(getActivity().getResources().getString(R.string.alert_label_title), getActivity().getResources().getString(R.string.favorite_list_label_select_location));
                }

            }
        });

        carType_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                select_carType_Dialog();
            }
        });


        pickTime_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new SlideDateTimePicker.Builder(getActivity().getSupportFragmentManager())
                        .setListener(Sublistener)
                        .setInitialDate(new Date())
                        .setMinDate(new Date())
                                //.setMaxDate(maxDate)
                                //.setIs24HourTime(true)
                        .setTheme(SlideDateTimePicker.HOLO_LIGHT)
                        .setIndicatorColor(Color.parseColor("#F83C6F"))
                        .build()
                        .show();
            }
        });


        ratecard_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ratecard_clicked) {
                    ratecard_clicked = false;
                    showRateCard();
                }

            }
        });

        estimate_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EstimatePage.class);
                intent.putExtra("UserId", UserID);
                intent.putExtra("PickUp", map_address.getText().toString());
                intent.putExtra("PickUp_Lat", String.valueOf(Recent_lat));
                intent.putExtra("PickUp_Long", String.valueOf(Recent_long));
                intent.putExtra("Category", CategoryID);
                intent.putExtra("Type", selectedType);
                intent.putExtra("PickUp_Date", coupon_selectedDate);
                intent.putExtra("PickUp_Time", coupon_selectedTime);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        coupon_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showCoupon();
            }
        });

        rideLater_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (rideLater_textview.getText().toString().equalsIgnoreCase(getResources().getString(R.string.home_label_ride_later))) {

                    selectedType = "1";

                    if (CarAvailable.equalsIgnoreCase("no cabs")) {
                        Alert(getActivity().getResources().getString(R.string.alert_label_title), getActivity().getResources().getString(R.string.alert_label_content1));
                    } else {
                        new SlideDateTimePicker.Builder(getActivity().getSupportFragmentManager())
                                .setListener(listener)
                                .setInitialDate(new Date())
                                .setMinDate(new Date())
                                        //.setMaxDate(maxDate)
                                        //.setIs24HourTime(true)
                                .setTheme(SlideDateTimePicker.HOLO_LIGHT)
                                .setIndicatorColor(Color.parseColor("#F83C6F"))
                                .build()
                                .show();
                    }
                } else if (rideLater_textview.getText().toString().equalsIgnoreCase(getResources().getString(R.string.home_label_cancel))) {
                    googleMap.getUiSettings().setAllGesturesEnabled(true);
                    ridenow_option_layout.setVisibility(View.GONE);
                    listview.setVisibility(View.VISIBLE);
                    rideLater_textview.setText(getResources().getString(R.string.home_label_ride_later));
                    rideNow_textview.setText(getResources().getString(R.string.home_label_ride_now));
                    currentLocation_image.setClickable(true);
                    pickTime_layout.setEnabled(true);
                    drawer_layout.setEnabled(true);
                    address_layout.setEnabled(true);
                    favorite_layout.setEnabled(true);
                    NavigationDrawer.enableSwipeDrawer();
                }

            }
        });

        rideNow_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {


                if (rideNow_textview.getText().toString().equalsIgnoreCase(getResources().getString(R.string.home_label_ride_now))) {
                    selectedType = "0";

                    if (CarAvailable.equalsIgnoreCase("no cabs")) {
                        Alert(getActivity().getResources().getString(R.string.alert_label_title), getActivity().getResources().getString(R.string.alert_label_content1));
                    } else {
                        //-------getting current date and time---------
                        coupon_selectedDate = coupon_mFormatter.format(new Date());
                        coupon_selectedTime = coupon_time_mFormatter.format(new Date());
                        String displaytime = CarAvailable + " " + getResources().getString(R.string.home_label_fromNow);

                        //--------Disabling the map functionality---------
                        googleMap.getUiSettings().setAllGesturesEnabled(false);
                        currentLocation_image.setClickable(false);

                        ridenow_option_layout.setVisibility(View.VISIBLE);
                        listview.setVisibility(View.GONE);
                        rideLater_textview.setText(getResources().getString(R.string.home_label_cancel));
                        rideNow_textview.setText(getResources().getString(R.string.home_label_confirm));

                        tv_carType.setText(ScarType);
                        tv_pickuptime.setText(displaytime);

                        //----Disabling onClick Listener-----
                        pickTime_layout.setEnabled(false);
                        drawer_layout.setEnabled(false);
                        address_layout.setEnabled(false);
                        favorite_layout.setEnabled(false);
                        NavigationDrawer.disableSwipeDrawer();
                    }
                } else if (rideNow_textview.getText().toString().equalsIgnoreCase(getResources().getString(R.string.home_label_confirm))) {
                    cd = new ConnectionDetector(getActivity());
                    isInternetPresent = cd.isConnectingToInternet();

                    if (isInternetPresent) {
                        HashMap<String, String> code = session.getCouponCode();
                        String coupon = code.get(SessionManager.KEY_COUPON_CODE);

                        ConfirmRideRequest(Iconstant.confirm_ride_url, coupon, coupon_selectedDate, coupon_selectedTime, selectedType, CategoryID, map_address.getText().toString(), String.valueOf(Recent_lat), String.valueOf(Recent_long), "");
                    } else {
                        Alert(getActivity().getResources().getString(R.string.alert_label_title), getActivity().getResources().getString(R.string.alert_nointernet));
                    }
                }
            }
        });


        listview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                CarAvailable = category_list.get(position).getCat_time();
                CategoryID = category_list.get(position).getCat_id();
                ScarType = category_list.get(position).getCat_name();

                cd = new ConnectionDetector(getActivity());
                isInternetPresent = cd.isConnectingToInternet();

                if (Recent_lat != 0.0) {
                    googleMap.clear();

                    if (isInternetPresent) {
                        if (postrequest != null) {
                            postrequest.cancel();
                        }

                        PostRequest(Iconstant.BookMyRide_url, Recent_lat, Recent_long);
                    } else {
                        alert_layout.setVisibility(View.VISIBLE);
                        alert_textview.setText(getResources().getString(R.string.alert_nointernet));
                    }
                }

                adapter.notifyDataSetChanged();
            }
        });

        currentLocation_image.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(getActivity());
                isInternetPresent = cd.isConnectingToInternet();
                gps = new GPSTracker(getActivity());

                if (gps.canGetLocation() && gps.isgpsenabled()) {

                     MyCurrent_lat = gps.getLatitude();
                     MyCurrent_long = gps.getLongitude();

                    if (postrequest != null) {
                        postrequest.cancel();
                    }

                    // Move the camera to last position with a zoom level
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(MyCurrent_lat, MyCurrent_long)).zoom(17).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                } else {
                    Toast.makeText(getActivity(), "GPS not Enabled !!!", Toast.LENGTH_LONG).show();
                }
            }
        });

        OnCameraChangeListener mOnCameraChangeListener = new OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                double latitude = cameraPosition.target.latitude;
                double longitude = cameraPosition.target.longitude;

                cd = new ConnectionDetector(getActivity());
                isInternetPresent = cd.isConnectingToInternet();

                Log.e("latitude--on_camera_change---->", "" + latitude);
                Log.e("longitude--on_camera_change---->", "" + longitude);

                if (latitude != 0.0) {
                    googleMap.clear();

                    Recent_lat = latitude;
                    Recent_long = longitude;

                    if (isInternetPresent) {
                        if (postrequest != null) {
                            postrequest.cancel();
                        }
                        PostRequest(Iconstant.BookMyRide_url, latitude, longitude);
                    } else {
                        alert_layout.setVisibility(View.VISIBLE);
                        alert_textview.setText(getResources().getString(R.string.alert_nointernet));
                        bottom_layout.setVisibility(View.GONE);
                    }
                }
            }
        };

        if (CheckPlayService()) {
            googleMap.setOnCameraChangeListener(mOnCameraChangeListener);
            googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    String tittle = marker.getTitle();
                    Log.e("tittle--on_camera_change---->", "" + tittle);
                    return true;
                }
            });
        } else {
            Toast.makeText(getActivity(), "Install Google Play service To View Location !!!", Toast.LENGTH_LONG).show();
        }

        return rootview;
    }

    private void initilizeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.book_my_ride_mapview)).getMap();

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getActivity(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
            }
        }

        // Changing map type
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Showing / hiding your current location
        googleMap.setMyLocationEnabled(false);
        // Enable / Disable zooming controls
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        // Enable / Disable my location button
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        // Enable / Disable Compass icon
        googleMap.getUiSettings().setCompassEnabled(false);
        // Enable / Disable Rotate gesture
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        // Enable / Disable zooming functionality
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.setMyLocationEnabled(false);

        if (gps.canGetLocation() && gps.isgpsenabled()) {
            double Dlatitude = gps.getLatitude();
            double Dlongitude = gps.getLongitude();

                MyCurrent_lat = Dlatitude;
                MyCurrent_long = Dlongitude;

                Recent_lat = Dlatitude;
                Recent_long = Dlongitude;

                // Move the camera to last position with a zoom level
                CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Dlatitude, Dlongitude)).zoom(17).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        } else {
            alert_layout.setVisibility(View.VISIBLE);
            alert_textview.setText(getResources().getString(R.string.alert_gpsEnable));
        }
    }

    private void initialize(View rooView) {
        cd = new ConnectionDetector(getActivity());
        isInternetPresent = cd.isConnectingToInternet();
        session = new SessionManager(getActivity());
        gps = new GPSTracker(getActivity());

        drawer_layout = (RelativeLayout) rooView.findViewById(R.id.book_navigation_layout);
        address_layout = (RelativeLayout) rooView.findViewById(R.id.book_navigation_address_layout);
        favorite_layout = (RelativeLayout) rooView.findViewById(R.id.book_navigation_favorite_layout);
        bottom_layout = (RelativeLayout) rooView.findViewById(R.id.book_my_ride_bottom_layout);
        map_address = (TextView) rooView.findViewById(R.id.book_navigation_search_address);
        loading_layout = (RelativeLayout) rooView.findViewById(R.id.book_my_ride_loading_layout);
        center_marker = (ImageView) rooView.findViewById(R.id.book_my_ride_center_marker);
        alert_layout = (RelativeLayout) rooView.findViewById(R.id.book_my_ride_alert_layout);
        alert_textview = (TextView) rooView.findViewById(R.id.book_my_ride_alert_textView);
        currentLocation_image = (ImageView) rooView.findViewById(R.id.book_current_location_imageview);

        rideLater_layout = (RelativeLayout) rooView.findViewById(R.id.book_my_ride_rideLater_layout);
        rideNow_layout = (RelativeLayout) rooView.findViewById(R.id.book_my_ride_rideNow_layout);
        rideLater_textview = (TextView) rooView.findViewById(R.id.book_my_ride_rideLater_textView);
        rideNow_textview = (TextView) rooView.findViewById(R.id.book_my_ride_rideNow_textview);
        listview = (HorizontalListView) rooView.findViewById(R.id.book_my_ride_listview);

        ridenow_option_layout = (RelativeLayout) rooView.findViewById(R.id.book_my_ride_ridenow_option_layout);
        carType_layout = (RelativeLayout) rooView.findViewById(R.id.book_my_ride_cabtype_layout);
        pickTime_layout = (RelativeLayout) rooView.findViewById(R.id.book_my_ride_pickup_layout);
        ratecard_layout = (LinearLayout) rooView.findViewById(R.id.book_my_ride_ratecard_layout);
        estimate_layout = (LinearLayout) rooView.findViewById(R.id.book_my_ride_estimate_layout);
        coupon_layout = (LinearLayout) rooView.findViewById(R.id.book_my_ride_applycoupon_layout);
        tv_carType = (TextView) rooView.findViewById(R.id.cartype_textview);
        tv_pickuptime = (TextView) rooView.findViewById(R.id.pickup_textview);
        tv_coupon_label = (TextView) rooView.findViewById(R.id.applycoupon_label);


        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        UserID = user.get(SessionManager.KEY_USERID);
        CategoryID = user.get(SessionManager.KEY_CATEGORY);

    }


    //-------------------Show Coupon Code Method--------------------
    private void showCoupon() {
        coupon_dialog = new MaterialDialog(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.coupon_code_dialog, null);

        TextView tv_apply = (TextView) view.findViewById(R.id.couponcode_apply_textView);
        TextView tv_cancel = (TextView) view.findViewById(R.id.couponcode_cancel_textView);
        final TextView tv_nointernet = (TextView) view.findViewById(R.id.couponcode_nointernet_textView);
        coupon_edittext = (EditText) view.findViewById(R.id.couponcode_editText);
        coupon_apply_layout = (RelativeLayout) view.findViewById(R.id.couponcode_apply_layout);
        coupon_loading_layout = (RelativeLayout) view.findViewById(R.id.couponcode_loading_layout);

        coupon_apply_layout.setVisibility(View.VISIBLE);
        coupon_loading_layout.setVisibility(View.GONE);

        coupon_edittext.addTextChangedListener(EditorWatcher);

        coupon_edittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(coupon_edittext.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });

        tv_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                coupon_dialog.dismiss();
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            }
        });

        tv_apply.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if (coupon_edittext.length() == 0) {
                    coupon_edittext.setHint(getResources().getString(R.string.couponcode_label_invalid_code));
                    coupon_edittext.setHintTextColor(Color.RED);
                    Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
                    coupon_edittext.startAnimation(shake);
                } else {
                    cd = new ConnectionDetector(getActivity());
                    isInternetPresent = cd.isConnectingToInternet();

                    if (isInternetPresent) {
                        tv_nointernet.setVisibility(View.INVISIBLE);
                        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                        CouponCodeRequest(Iconstant.couponCode_apply_url, coupon_edittext.getText().toString(), coupon_selectedDate);
                    } else {
                        tv_nointernet.setVisibility(View.VISIBLE);
                    }

                }
            }
        });

        coupon_dialog.setView(view).show();
    }


    //-------------------Show RateCard Method--------------------
    private void showRateCard() {
        if (ratecard_status) {
            final MaterialDialog dialog = new MaterialDialog(getActivity());
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.ratecard_dialog, null);

            TextView tv_cartype = (TextView) view.findViewById(R.id.ratecard_caretype_textview);
            TextView tv_firstprice = (TextView) view.findViewById(R.id.first_price_textView);
            TextView tv_firstKm = (TextView) view.findViewById(R.id.first_km_textView);
            TextView tv_afterprice = (TextView) view.findViewById(R.id.after_price_textView);
            TextView tv_afterKm = (TextView) view.findViewById(R.id.after_km_textView);
            TextView tv_otherprice = (TextView) view.findViewById(R.id.other_price_textView);
            TextView tv_otherKm = (TextView) view.findViewById(R.id.other_km_textView);
            TextView tv_note = (TextView) view.findViewById(R.id.ratecard_note_textview);
            TextView tv_ok = (TextView) view.findViewById(R.id.ratecard_ok_textview);

            if (ratecard_list.size() > 0) {
                Currency currencycode = Currency.getInstance(getLocale(ratecard_list.get(0).getCurrencyCode()));

                tv_cartype.setText(ratecard_list.get(0).getRate_cartype());
                tv_firstprice.setText(currencycode.getSymbol() + ratecard_list.get(0).getMinfare_amt());
                tv_firstKm.setText(ratecard_list.get(0).getMinfare_km());
                tv_afterprice.setText(currencycode.getSymbol() + ratecard_list.get(0).getAfterfare_amt());
                tv_afterKm.setText(ratecard_list.get(0).getAfterfare_km());
                tv_otherprice.setText(currencycode.getSymbol() + ratecard_list.get(0).getOtherfare_amt());
                tv_otherKm.setText(ratecard_list.get(0).getOtherfare_km());
                tv_note.setText(ratecard_list.get(0).getRate_note());
            }

            tv_ok.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    dialog.dismiss();
                    ratecard_clicked = true;
                }
            });
            dialog.setView(view).show();
        }
    }

    //-------------------Show CarType Method--------------------
    private void select_carType_Dialog()
    {
        final MaterialDialog dialog = new MaterialDialog(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.home_cartype_dialog, null);

        ListView car_listview = (ListView) view.findViewById(R.id.car_type_dialog_listView);

        SelectCarTypeAdapter car_adapter = new SelectCarTypeAdapter(getActivity(), category_list);
        car_listview.setAdapter(car_adapter);
        car_adapter.notifyDataSetChanged();

        dialog.setTitle(getActivity().getResources().getString(R.string.car_type_select_dialog_label_carType));
        dialog.setPositiveButton(getActivity().getResources().getString(R.string.car_type_select_dialog_label_cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                }
        );

        car_listview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();
                CategoryID = category_list.get(position).getCat_id();
                SelectCar_Request(Iconstant.BookMyRide_url, Recent_lat, Recent_long);
            }
        });
        dialog.setView(view).show();
    }

    //----------------------Code for TextWatcher-------------------------
    private final TextWatcher EditorWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            //clear error symbol after entering text
            if (coupon_edittext.getText().length() > 0) {
                coupon_edittext.setHint("");
            }
        }
    };


    //method to convert currency code to currency symbol
    private static Locale getLocale(String strCode) {
        for (Locale locale : NumberFormat.getAvailableLocales()) {
            String code = NumberFormat.getCurrencyInstance(locale).getCurrency().getCurrencyCode();
            if (strCode.equals(code)) {
                return locale;
            }
        }
        return null;
    }


    //-----------Check Google Play Service--------
    private boolean CheckPlayService() {
        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        }
        return true;
    }

    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                final Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode, getActivity(), REQUEST_CODE_RECOVER_PLAY_SERVICES);
                if (dialog == null) {
                    Toast.makeText(getActivity(), "incompatible version of Google Play Services", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    //-------------Method to get Complete Address------------
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
            } else {
                Log.e("My Current loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("My Current loction address", "Canont get Address!");
        }
        return strAdd;
    }


    //--------------Alert Method-----------
    private void Alert(String title, String alert) {
        final MaterialDialog dialog = new MaterialDialog(getActivity());
        dialog.setTitle(title)
                .setMessage(alert)
                .setPositiveButton(
                        "OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        }
                )
                .show();
    }


    //----------------DatePicker Listener------------
    private SlideDateTimeListener listener = new SlideDateTimeListener() {
        @Override
        public void onDateTimeSet(Date date) {

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, 1);
            Date d = cal.getTime();
            String currenttime = mTime_Formatter.format(d);
            String selecedtime = mTime_Formatter.format(date);
            String displaytime = mFormatter.format(date);

            System.out.println("-----------------current date---------------------" + currenttime);
            System.out.println("-----------------selected date---------------------" + selecedtime);

            if (selecedtime.equalsIgnoreCase("00")) {
                selecedtime = "24";
            }

            if (Integer.parseInt(currenttime) <= Integer.parseInt(selecedtime)) {

                coupon_selectedDate = coupon_mFormatter.format(date);
                coupon_selectedTime = coupon_time_mFormatter.format(date);

                //--------Disabling the map functionality---------
                googleMap.getUiSettings().setAllGesturesEnabled(false);
                currentLocation_image.setClickable(false);

                pickTime_layout.setEnabled(true);
                drawer_layout.setEnabled(false);
                address_layout.setEnabled(false);
                favorite_layout.setEnabled(false);
                ridenow_option_layout.setVisibility(View.VISIBLE);
                listview.setVisibility(View.GONE);
                rideLater_textview.setText(getResources().getString(R.string.home_label_cancel));
                rideNow_textview.setText(getResources().getString(R.string.home_label_confirm));

                tv_carType.setText(ScarType);
                tv_pickuptime.setText(displaytime);
                NavigationDrawer.disableSwipeDrawer();

            } else {
                Alert(getActivity().getResources().getString(R.string.alert_label_ridelater_title), getActivity().getResources().getString(R.string.alert_label_ridelater_content));
            }
        }

        // Optional cancel listener
        @Override
        public void onDateTimeCancel() {
            Toast.makeText(getActivity(), "Canceled", Toast.LENGTH_SHORT).show();
        }
    };


    //----------------DatePicker Secondary Listener------------
    private SlideDateTimeListener Sublistener = new SlideDateTimeListener() {
        @Override
        public void onDateTimeSet(Date date) {

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, 1);
            Date d = cal.getTime();
            String currenttime = mTime_Formatter.format(d);
            String selecedtime = mTime_Formatter.format(date);
            String displaytime = mFormatter.format(date);

            if (selecedtime.equalsIgnoreCase("00")) {
                selecedtime = "24";
            }

            if (Integer.parseInt(currenttime) <= Integer.parseInt(selecedtime)) {
                coupon_selectedDate = coupon_mFormatter.format(date);
                coupon_selectedTime = coupon_time_mFormatter.format(date);

                tv_pickuptime.setText(displaytime);
            } else {
                Alert(getActivity().getResources().getString(R.string.alert_label_ridelater_title), getActivity().getResources().getString(R.string.alert_label_ridelater_content));
            }
        }

        // Optional cancel listener
        @Override
        public void onDateTimeCancel() {
        }
    };


    //-------------------AsynTask To get the current Address----------------
    private void PostRequest(String Url, final double latitude, final double longitude) {
        loading_layout.setVisibility(View.VISIBLE);
        center_marker.setVisibility(View.GONE);
        rideNow_layout.setEnabled(false);
        rideLater_layout.setEnabled(false);

        System.out.println("--------------Book My ride url-------------------" + Url);

        Sselected_latitude=String.valueOf(latitude);
        Sselected_longitude=String.valueOf(longitude);

        postrequest = new StringRequest(Request.Method.POST, Url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                System.out.println("--------------Book My ride reponse-------------------" + response);
                String fail_response = "";

                try {
                    JSONObject object = new JSONObject(response);

                    if (object.length() > 0) {
                        if (object.getString("status").equalsIgnoreCase("1")) {
                            JSONObject jobject = object.getJSONObject("response");
                            if (jobject.length() > 0) {
                                for (int i = 0; i < jobject.length(); i++) {
                                    JSONArray driver_array = jobject.getJSONArray("drivers");
                                    if (driver_array.length() > 0) {
                                        driver_list.clear();

                                        for (int j = 0; j < driver_array.length(); j++) {
                                            JSONObject driver_object = driver_array.getJSONObject(j);

                                            HomePojo pojo = new HomePojo();
                                            pojo.setDriver_lat(driver_object.getString("lat"));
                                            pojo.setDriver_long(driver_object.getString("lon"));

                                            driver_list.add(pojo);
                                        }
                                        driver_status = true;
                                    } else {
                                        driver_list.clear();
                                        driver_status = false;
                                    }


                                    JSONObject ratecard_object = jobject.getJSONObject("ratecard");
                                    if (ratecard_object.length() > 0) {
                                        ratecard_list.clear();
                                        HomePojo pojo = new HomePojo();

                                        pojo.setRate_cartype(ratecard_object.getString("category"));
                                        pojo.setRate_note(ratecard_object.getString("note"));
                                        pojo.setCurrencyCode(jobject.getString("currency"));

                                        JSONObject farebreakup_object = ratecard_object.getJSONObject("farebreakup");
                                        if (farebreakup_object.length() > 0) {
                                            JSONObject minfare_object = farebreakup_object.getJSONObject("min_fare");
                                            if (minfare_object.length() > 0) {
                                                pojo.setMinfare_amt(minfare_object.getString("amount"));
                                                pojo.setMinfare_km(minfare_object.getString("text"));
                                            }

                                            JSONObject afterfare_object = farebreakup_object.getJSONObject("after_fare");
                                            if (afterfare_object.length() > 0) {
                                                pojo.setAfterfare_amt(afterfare_object.getString("amount"));
                                                pojo.setAfterfare_km(afterfare_object.getString("text"));
                                            }

                                            JSONObject otherfare_object = farebreakup_object.getJSONObject("other_fare");
                                            if (otherfare_object.length() > 0) {
                                                pojo.setOtherfare_amt(otherfare_object.getString("amount"));
                                                pojo.setOtherfare_km(otherfare_object.getString("text"));
                                            }
                                        }

                                        ratecard_list.add(pojo);
                                        ratecard_status = true;
                                    } else {
                                        ratecard_list.clear();
                                        ratecard_status = false;
                                    }


                                    JSONArray cat_array = jobject.getJSONArray("category");
                                    if (cat_array.length() > 0) {
                                        category_list.clear();

                                        for (int k = 0; k < cat_array.length(); k++) {

                                            JSONObject cat_object = cat_array.getJSONObject(k);

                                            HomePojo pojo = new HomePojo();
                                            pojo.setCat_name(cat_object.getString("name"));
                                            pojo.setCat_time(cat_object.getString("eta"));
                                            pojo.setCat_id(cat_object.getString("id"));
                                            pojo.setIcon_normal(cat_object.getString("icon_normal"));
                                            pojo.setIcon_active(cat_object.getString("icon_active"));
                                            pojo.setSelected_Cat(jobject.getString("selected_category"));

                                            if (cat_object.getString("id").equals(jobject.getString("selected_category"))) {
                                                CarAvailable = cat_object.getString("eta");
                                                ScarType = cat_object.getString("name");
                                            }

                                            category_list.add(pojo);
                                        }

                                        category_status = true;
                                    } else {
                                        category_list.clear();
                                        category_status = false;
                                    }
                                }
                            }

                            main_response_status = true;
                        } else {
                            fail_response = object.getString("response");
                            main_response_status = false;
                        }

                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


                if (main_response_status) {
                    alert_layout.setVisibility(View.GONE);
                    bottom_layout.setVisibility(View.VISIBLE);

                    if (driver_status) {
                        for (int i = 0; i < driver_list.size(); i++) {
                            double Dlatitude = Double.parseDouble(driver_list.get(i).getDriver_lat());
                            double Dlongitude = Double.parseDouble(driver_list.get(i).getDriver_long());

                            // create marker double Dlatitude = gps.getLatitude();
                            MarkerOptions marker = new MarkerOptions().position(new LatLng(Dlatitude, Dlongitude));
                            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.car_icon));

                            // adding marker
                            googleMap.addMarker(marker);
                        }
                    }

                    if (category_status) {
                        listview.setVisibility(View.VISIBLE);

                        adapter = new BookMyRide_Adapter(getActivity(), category_list);
                        listview.setAdapter(adapter);
                    } else {
                        listview.setVisibility(View.GONE);
                    }

                } else {
                    alert_layout.setVisibility(View.VISIBLE);
                    bottom_layout.setVisibility(View.GONE);
                    alert_textview.setText(fail_response);
                }

                String address = getCompleteAddressString(latitude, longitude);
                map_address.setText(address);
                SselectedAddress=address;

                loading_layout.setVisibility(View.GONE);
                center_marker.setVisibility(View.VISIBLE);
                rideNow_layout.setEnabled(true);
                rideLater_layout.setEnabled(true);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                loading_layout.setVisibility(View.GONE);
                center_marker.setVisibility(View.VISIBLE);
                rideNow_layout.setEnabled(true);
                rideLater_layout.setEnabled(true);

                alert_layout.setVisibility(View.VISIBLE);
                bottom_layout.setVisibility(View.GONE);
                alert_textview.setText(error.getMessage());

                VolleyErrorResponse.volleyError(getActivity(), error);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent",Iconstant.cabily_userAgent);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("user_id", UserID);
                jsonParams.put("lat", String.valueOf(latitude));
                jsonParams.put("lon", String.valueOf(longitude));
                jsonParams.put("category", CategoryID);
                return jsonParams;
            }
        };
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(postrequest);
    }


    //-------------------Coupon Code Post Request----------------

    private void CouponCodeRequest(String Url, final String code, final String pickpudate) {
        System.out.println("--------------coupon code url-------------------" + Url);

        coupon_apply_layout.setVisibility(View.GONE);
        coupon_loading_layout.setVisibility(View.VISIBLE);

        postrequest = new StringRequest(Request.Method.POST, Url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                System.out.println("--------------coupon code reponse-------------------" + response);

                try {
                    JSONObject object = new JSONObject(response);
                    if (object.length() > 0) {
                        String status = object.getString("status");
                        if (status.equalsIgnoreCase("1")) {

                            JSONObject result_object = object.getJSONObject("response");

                            coupon_apply_layout.setVisibility(View.VISIBLE);
                            coupon_loading_layout.setVisibility(View.GONE);
                            coupon_dialog.dismiss();

                            String code = result_object.getString("code");
                            session.setCouponCode(code);
                            tv_coupon_label.setText(getResources().getString(R.string.couponcode_label_verifed));
                            tv_coupon_label.setTextColor(getResources().getColor(R.color.darkgreen_color));
                        } else {
                            coupon_apply_layout.setVisibility(View.VISIBLE);
                            coupon_loading_layout.setVisibility(View.GONE);

                            coupon_edittext.setText("");
                            coupon_edittext.setHint(getResources().getString(R.string.couponcode_label_invalid_code));
                            coupon_edittext.setHintTextColor(Color.RED);
                            Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
                            coupon_edittext.startAnimation(shake);
                        }
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                coupon_apply_layout.setVisibility(View.VISIBLE);
                coupon_loading_layout.setVisibility(View.GONE);
                coupon_dialog.dismiss();
                VolleyErrorResponse.volleyError(getActivity(), error);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent",Iconstant.cabily_userAgent);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("user_id", UserID);
                jsonParams.put("code", code);
                jsonParams.put("pickup_date", pickpudate);
                return jsonParams;
            }

        };
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(postrequest);
    }


    //-------------------Confirm Ride Post Request----------------

    private void ConfirmRideRequest(String Url, final String code, final String pickpudate, final String pickup_time, final String type, final String category, final String pickup_location, final String pickup_lat, final String pickup_lon, final String try_value) {

        dialog = new Dialog(getActivity());
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView loading = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        loading.setText(getResources().getString(R.string.action_pleasewait));

        System.out.println("--------------Confirm Ride url-------------------" + Url);

        postrequest = new StringRequest(Request.Method.POST, Url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                System.out.println("--------------Confirm Ride reponse-------------------" + response);

                String selected_type = "",Sacceptance="";
                try {
                    JSONObject object = new JSONObject(response);
                    if (object.length() > 0) {
                        String status = object.getString("status");
                        if (status.equalsIgnoreCase("1")) {
                            JSONObject response_object = object.getJSONObject("response");

                            selected_type = response_object.getString("type");
                            Sacceptance = object.getString("acceptance");
                            if(Sacceptance.equalsIgnoreCase("No"))
                            {
                                response_time = response_object.getString("response_time");
                            }
                            riderId = response_object.getString("ride_id");

                            if (selected_type.equalsIgnoreCase("1"))
                            {
                                final MaterialDialog dialog = new MaterialDialog(getActivity());
                                dialog.setTitle(getActivity().getResources().getString(R.string.action_success))
                                        .setMessage(getActivity().getResources().getString(R.string.ridenow_label_confirm_success))
                                        .setPositiveButton(
                                                "OK", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        dialog.dismiss();

                                                        //---------Hiding the bottom layout after success request--------
                                                        googleMap.getUiSettings().setAllGesturesEnabled(true);
                                                        ridenow_option_layout.setVisibility(View.GONE);
                                                        listview.setVisibility(View.VISIBLE);
                                                        rideLater_textview.setText(getResources().getString(R.string.home_label_ride_later));
                                                        rideNow_textview.setText(getResources().getString(R.string.home_label_ride_now));
                                                        currentLocation_image.setClickable(true);
                                                        pickTime_layout.setEnabled(true);
                                                        drawer_layout.setEnabled(true);
                                                        address_layout.setEnabled(true);
                                                        favorite_layout.setEnabled(true);
                                                        NavigationDrawer.enableSwipeDrawer();
                                                    }
                                                }
                                        )
                                        .show();
                            }
                            else if (selected_type.equalsIgnoreCase("0"))
                            {
                                if(Sacceptance.equalsIgnoreCase("Yes"))
                                {
                                    //Move to ride Detail page
                                }
                                else
                                {
                                    Intent intent = new Intent(getActivity(), TimerPage.class);
                                    intent.putExtra("Time", response_time);
                                    intent.putExtra("retry_count", try_value);
                                    startActivityForResult(intent, timer_request_code);
                                    getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                }
                            }
                        } else {
                            String Sresponse = object.getString("response");
                            Alert(getActivity().getResources().getString(R.string.alert_label_title), Sresponse);
                        }
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                dialog.dismiss();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                dialog.dismiss();
                VolleyErrorResponse.volleyError(getActivity(), error);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent",Iconstant.cabily_userAgent);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> jsonParams = new HashMap<String, String>();


                System.out.println("---------------user_id----------" + UserID);
                System.out.println("---------------code----------" + code);
                System.out.println("---------------pickpudate----------" + pickpudate);
                System.out.println("---------------pickup_time----------" + pickup_time);
                System.out.println("---------------type----------" + type);
                System.out.println("---------------category----------" + category);
                System.out.println("---------------pickup----------" + pickup_location);
                System.out.println("---------------pickup_lat----------" + pickup_lat);
                System.out.println("---------------pickup_lon----------" + pickup_lon);
                System.out.println("---------------try----------" + try_value);
                System.out.println("---------------riderId----------" + riderId);

                jsonParams.put("user_id", UserID);
                jsonParams.put("code", code);
                jsonParams.put("pickup_date", pickpudate);
                jsonParams.put("pickup_time", pickup_time);
                jsonParams.put("type", type);
                jsonParams.put("category", category);
                jsonParams.put("pickup", pickup_location);
                jsonParams.put("pickup_lat", pickup_lat);
                jsonParams.put("pickup_lon", pickup_lon);
                jsonParams.put("ride_id", riderId);

                //jsonParams.put("try", try_value);

                return jsonParams;
            }

        };
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(postrequest);
    }


    //-------------------Delete Ride Post Request----------------

    private void DeleteRideRequest(String Url) {

        dialog = new Dialog(getActivity());
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView loading = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        loading.setText(getResources().getString(R.string.action_pleasewait));

        System.out.println("--------------Delete Ride url-------------------" + Url);

        postrequest = new StringRequest(Request.Method.POST, Url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                System.out.println("--------------Delete Ride reponse-------------------" + response);

                try {
                    JSONObject object = new JSONObject(response);
                    if (object.length() > 0) {
                        String status = object.getString("status");
                        String response_value = object.getString("response");
                        if (status.equalsIgnoreCase("1")) {
                            riderId="";
                            Alert(getActivity().getResources().getString(R.string.alert_label_title), response_value);
                        } else {
                            Alert(getActivity().getResources().getString(R.string.alert_label_title), getActivity().getResources().getString(R.string.alert_servererror));
                        }


                        //---------Hiding the bottom layout after cancel request--------
                        googleMap.getUiSettings().setAllGesturesEnabled(true);
                        ridenow_option_layout.setVisibility(View.GONE);
                        listview.setVisibility(View.VISIBLE);
                        rideLater_textview.setText(getResources().getString(R.string.home_label_ride_later));
                        rideNow_textview.setText(getResources().getString(R.string.home_label_ride_now));
                        currentLocation_image.setClickable(true);
                        pickTime_layout.setEnabled(true);
                        drawer_layout.setEnabled(true);
                        address_layout.setEnabled(true);
                        favorite_layout.setEnabled(true);
                        NavigationDrawer.enableSwipeDrawer();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                dialog.dismiss();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                VolleyErrorResponse.volleyError(getActivity(), error);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent",Iconstant.cabily_userAgent);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("user_id", UserID);
                jsonParams.put("ride_id", riderId);
                return jsonParams;
            }
        };
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(postrequest);
    }


    //-------------------Select Car Type Request---------------
    private void SelectCar_Request(String Url, final double latitude, final double longitude) {

      final Dialog mdialog = new Dialog(getActivity());
        mdialog.getWindow();
        mdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mdialog.setContentView(R.layout.custom_loading);
        mdialog.setCanceledOnTouchOutside(false);
        mdialog.show();

        TextView dialog_title=(TextView)mdialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_updating));

        System.out.println("--------------Select Car Type url-------------------" + Url);
        postrequest = new StringRequest(Request.Method.POST, Url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                System.out.println("--------------Select Car Type reponse-------------------" + response);
                String fail_response = "";

                try {
                    JSONObject object = new JSONObject(response);

                    if (object.length() > 0) {
                        if (object.getString("status").equalsIgnoreCase("1")) {
                            JSONObject jobject = object.getJSONObject("response");
                            if (jobject.length() > 0) {
                                for (int i = 0; i < jobject.length(); i++) {
                                    JSONArray driver_array = jobject.getJSONArray("drivers");
                                    if (driver_array.length() > 0) {
                                        driver_list.clear();

                                        for (int j = 0; j < driver_array.length(); j++) {
                                            JSONObject driver_object = driver_array.getJSONObject(j);

                                            HomePojo pojo = new HomePojo();
                                            pojo.setDriver_lat(driver_object.getString("lat"));
                                            pojo.setDriver_long(driver_object.getString("lon"));

                                            driver_list.add(pojo);
                                        }
                                        driver_status = true;
                                    } else {
                                        driver_list.clear();
                                        driver_status = false;
                                    }


                                    JSONObject ratecard_object = jobject.getJSONObject("ratecard");
                                    if (ratecard_object.length() > 0) {
                                        ratecard_list.clear();
                                        HomePojo pojo = new HomePojo();

                                        pojo.setRate_cartype(ratecard_object.getString("category"));
                                        pojo.setRate_note(ratecard_object.getString("note"));
                                        pojo.setCurrencyCode(jobject.getString("currency"));

                                        JSONObject farebreakup_object = ratecard_object.getJSONObject("farebreakup");
                                        if (farebreakup_object.length() > 0) {
                                            JSONObject minfare_object = farebreakup_object.getJSONObject("min_fare");
                                            if (minfare_object.length() > 0) {
                                                pojo.setMinfare_amt(minfare_object.getString("amount"));
                                                pojo.setMinfare_km(minfare_object.getString("text"));
                                            }

                                            JSONObject afterfare_object = farebreakup_object.getJSONObject("after_fare");
                                            if (afterfare_object.length() > 0) {
                                                pojo.setAfterfare_amt(afterfare_object.getString("amount"));
                                                pojo.setAfterfare_km(afterfare_object.getString("text"));
                                            }

                                            JSONObject otherfare_object = farebreakup_object.getJSONObject("other_fare");
                                            if (otherfare_object.length() > 0) {
                                                pojo.setOtherfare_amt(otherfare_object.getString("amount"));
                                                pojo.setOtherfare_km(otherfare_object.getString("text"));
                                            }
                                        }

                                        ratecard_list.add(pojo);
                                        ratecard_status = true;
                                    } else {
                                        ratecard_list.clear();
                                        ratecard_status = false;
                                    }


                                    JSONArray cat_array = jobject.getJSONArray("category");
                                    if (cat_array.length() > 0) {
                                        category_list.clear();

                                        for (int k = 0; k < cat_array.length(); k++) {

                                            JSONObject cat_object = cat_array.getJSONObject(k);

                                            HomePojo pojo = new HomePojo();
                                            pojo.setCat_name(cat_object.getString("name"));
                                            pojo.setCat_time(cat_object.getString("eta"));
                                            pojo.setCat_id(cat_object.getString("id"));
                                            pojo.setIcon_normal(cat_object.getString("icon_normal"));
                                            pojo.setIcon_active(cat_object.getString("icon_active"));
                                            pojo.setSelected_Cat(jobject.getString("selected_category"));

                                            if (cat_object.getString("id").equals(jobject.getString("selected_category"))) {
                                                CarAvailable = cat_object.getString("eta");
                                                ScarType = cat_object.getString("name");
                                            }

                                            category_list.add(pojo);
                                        }

                                        category_status = true;
                                    } else {
                                        category_list.clear();
                                        category_status = false;
                                    }
                                }
                            }

                            main_response_status = true;
                        } else {
                            fail_response = object.getString("response");
                            main_response_status = false;
                        }

                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


                if (main_response_status)
                {
                    tv_carType.setText(ScarType);
                    if (driver_status) {
                        googleMap.clear();
                        for (int i = 0; i < driver_list.size(); i++) {
                            double Dlatitude = Double.parseDouble(driver_list.get(i).getDriver_lat());
                            double Dlongitude = Double.parseDouble(driver_list.get(i).getDriver_long());

                            // create marker double Dlatitude = gps.getLatitude();
                            MarkerOptions marker = new MarkerOptions().position(new LatLng(Dlatitude, Dlongitude));
                            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.car_icon));

                            // adding marker
                            googleMap.addMarker(marker);
                        }
                    }
                    else
                    {
                        googleMap.clear();
                    }


                    if (category_status) {
                        listview.setVisibility(View.GONE);

                        adapter = new BookMyRide_Adapter(getActivity(), category_list);
                        listview.setAdapter(adapter);
                    } else {
                        listview.setVisibility(View.GONE);
                    }

                    mdialog.dismiss();

                } else {
                    mdialog.dismiss();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                mdialog.dismiss();
                VolleyErrorResponse.volleyError(getActivity(), error);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent",Iconstant.cabily_userAgent);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> jsonParams = new HashMap<String, String>();

                Log.e("user_id on json---->", "" + UserID);
                Log.e("CategoryID on json---->", "" + CategoryID);
                Log.e("latitude on json---->", "" + latitude);
                Log.e("longitude on json---->", "" + longitude);

                jsonParams.put("user_id", UserID);
                jsonParams.put("lat", String.valueOf(latitude));
                jsonParams.put("lon", String.valueOf(longitude));
                jsonParams.put("category", CategoryID);
                return jsonParams;
            }
        };
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(postrequest);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // code to get country name
        if (requestCode == timer_request_code && resultCode == Activity.RESULT_OK && data != null) {
            String ride_accepted = data.getStringExtra("Accepted_or_Not");
            String retry_count = data.getStringExtra("Retry_Count");

            if (retry_count.equalsIgnoreCase("1") && ride_accepted.equalsIgnoreCase("not")) {

                View view = View.inflate(getActivity(), R.layout.material_alert_dialog, null);
                final MaterialDialog mdialog = new MaterialDialog(getActivity());
                mdialog.setContentView(view)
                        .setPositiveButton(
                                getResources().getString(R.string.timer_label_alert_retry), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mdialog.dismiss();

                                        cd = new ConnectionDetector(getActivity());
                                        isInternetPresent = cd.isConnectingToInternet();

                                        if (isInternetPresent) {
                                            HashMap<String, String> code = session.getCouponCode();
                                            String coupon = code.get(SessionManager.KEY_COUPON_CODE);

                                            ConfirmRideRequest(Iconstant.confirm_ride_url, coupon, coupon_selectedDate, coupon_selectedTime, selectedType, CategoryID, map_address.getText().toString(), String.valueOf(Recent_lat), String.valueOf(Recent_long), "2");
                                        } else {
                                            Alert(getActivity().getResources().getString(R.string.alert_label_title), getActivity().getResources().getString(R.string.alert_nointernet));
                                        }
                                    }
                                }
                        )
                        .setNegativeButton(
                                getResources().getString(R.string.timer_label_alert_cancel), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mdialog.dismiss();

                                        cd = new ConnectionDetector(getActivity());
                                        isInternetPresent = cd.isConnectingToInternet();

                                        if (isInternetPresent) {
                                            DeleteRideRequest(Iconstant.delete_ride_url);
                                        } else {
                                            Alert(getActivity().getResources().getString(R.string.alert_label_title), getActivity().getResources().getString(R.string.alert_nointernet));
                                        }
                                    }
                                }
                        )
                        .show();

                TextView alert_title=(TextView)view.findViewById(R.id.material_alert_message_label);
                TextView alert_message=(TextView)view.findViewById(R.id.material_alert_message_textview);
                alert_title.setText(getResources().getString(R.string.timer_label_alert_sorry));
                alert_message.setText(getResources().getString(R.string.timer_label_alert_content));

            } else if (retry_count.equalsIgnoreCase("2") && ride_accepted.equalsIgnoreCase("not")) {
                DeleteRideRequest(Iconstant.delete_ride_url);
            } else if ((retry_count.equalsIgnoreCase("1") || retry_count.equalsIgnoreCase("2")) && ride_accepted.equalsIgnoreCase("Accepted")) {
                //want to write functionality
            }

        }
        else if (requestCode == placeSearch_request_code && resultCode == Activity.RESULT_OK && data != null) {

            String SselectedLatitude = data.getStringExtra("Selected_Latitude");
            String SselectedLongitude = data.getStringExtra("Selected_Longitude");
            String SselectedLocation = data.getStringExtra("Selected_Location");

            // Move the camera to last position with a zoom level
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Double.parseDouble(SselectedLatitude), Double.parseDouble(SselectedLongitude))).zoom(17).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            map_address.setText(SselectedLocation);
        }
        else if (requestCode == favoriteList_request_code && resultCode == Activity.RESULT_OK && data != null) {

            String SselectedLatitude = data.getStringExtra("Selected_Latitude");
            String SselectedLongitude = data.getStringExtra("Selected_Longitude");

            // Move the camera to last position with a zoom level
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Double.parseDouble(SselectedLatitude), Double.parseDouble(SselectedLongitude))).zoom(17).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(logoutReciver);
        super.onDestroy();
    }
}
