package com.byteshaft.laundry;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.byteshaft.laundry.account.CodeConfirmationActivity;
import com.byteshaft.laundry.account.LoginActivity;
import com.byteshaft.laundry.account.ResetPassword;
import com.byteshaft.laundry.account.UpdateProfile;
import com.byteshaft.laundry.laundry.LaundryCategoriesActivity;
import com.byteshaft.laundry.utils.AppGlobals;
import com.byteshaft.laundry.utils.HeadingTextView;
import com.byteshaft.requests.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.byteshaft.laundry.utils.BitmapWithCharacter;

import java.util.Random;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        HttpRequest.OnReadyStateChangeListener {

    public static MainActivity sInstance;
    private View header;
    private RecyclerView mRecyclerView;
    private CustomAdapter mAdapter;
    private TextView mName;
    private TextView mEmail;
    NavigationView navigationView;
    private ProgressDialog progress;
    HeadingTextView laundryText;
    private String mToken;
    private JSONArray array;
    private CustomAdapter listAdapter;

    public static MainActivity getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sInstance = this;
        overridePendingTransition(R.anim.anim_left_in, R.anim.anim_left_out);
        setContentView(R.layout.activity_main);
        overridePendingTransition(R.anim.anim_left_in, R.anim.anim_left_out);
        laundryText = (HeadingTextView) findViewById(R.id.laundry_text);
        AppGlobals.sActivity = MainActivity.this;
        mToken = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN);
        Log.i("TAG", "" + AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Hello there");
                startActivity(new Intent(MainActivity.this, LaundryCategoriesActivity.class));
            }
        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        header = navigationView.getHeaderView(0);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.canScrollVertically(LinearLayoutManager.VERTICAL);
        mRecyclerView.setHasFixedSize(true);

//        mAdapter = new CustomAdapter(arrayList);
//        mRecyclerView.setAdapter(mAdapter);
//        mRecyclerView.addOnItemTouchListener(new CustomAdapter(arrayList , AppGlobals.getContext()
//                , new CustomAdapter.OnItemClickListener() {
//            @Override
//            public void onItem(String item) {
//                Intent intent = new Intent(getActivity().getApplicationContext(),
//                        SelectedCategoryList.class);
//                intent.putExtra(AppGlobals.CATEGORY_INTENT_KEY, item);
//                startActivity(intent);
//            }
//        }));
    }

    private void laundryRequestDetails() {
        progress = ProgressDialog.show(this, "Please wait..",
                "Getting data", true);
        HttpRequest mRequest = new HttpRequest(AppGlobals.getContext());
        mRequest.setOnReadyStateChangeListener(this);
        mRequest.open("GET", AppGlobals.LAUNDRY_REQUEST_URL);
        mRequest.setRequestHeader("Authorization", "Token " + mToken);
        mRequest.send();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!AppGlobals.isUserActive() && !AppGlobals.isUserLoggedIn()) {
            laundryText.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            laundryText.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            laundryRequestDetails();
        }

        MenuItem login, logout, active;
        Menu menu = navigationView.getMenu();
        if (!AppGlobals.isUserLoggedIn()) {
            login = menu.findItem(R.id.login);
            logout = menu.findItem(R.id.nav_logout);
            login.setVisible(true);
            logout.setVisible(false);
        } else {
            login = menu.findItem(R.id.login);
            active = menu.findItem(R.id.active);
            if (!AppGlobals.isUserActive()) {
                active.setVisible(true);
            } else {
                active.setVisible(false);
            }
            logout = menu.findItem(R.id.nav_logout);
            login.setVisible(false);
            logout.setVisible(true);
        }
        mName = (TextView) header.findViewById(R.id.nav_user_name);
        mEmail = (TextView) header.findViewById(R.id.nav_user_email);
        mName.setTypeface(AppGlobals.typefaceBold);
        mEmail.setTypeface(AppGlobals.typefaceNormal);
        if (!AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_FULL_NAME).equals("")
                && AppGlobals.isUserActive()) {
            String simpleName = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_FULL_NAME);
            String firstUpperCaseName = simpleName.substring(0, 1).toUpperCase() + simpleName.substring(1);
            mName.setText(firstUpperCaseName);
        } else {
            mName.setText("username");
        }
        if (!AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_EMAIL).equals("")) {
            mEmail.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_EMAIL));
        } else {
            mEmail.setText("abc@xyz.com");
        }
//        CircularImageView circularImageView = (CircularImageView) header.findViewById(R.id.imageView);
//        if (AppGlobals.isUserLoggedIn()) {
//            final Resources res = getResources();
//            int[] array = getResources().getIntArray(R.array.letter_tile_colors);
//            final BitmapWithCharacter tileProvider = new BitmapWithCharacter();
//            final Bitmap letterTile = tileProvider.getLetterTile(AppGlobals.
//                            getStringFromSharedPreferences(AppGlobals.KEY_FULL_NAME),
//                    String.valueOf(array[new Random().nextInt(array.length)]), 100, 100);
//            circularImageView.setImageBitmap(letterTile);
//        }

        if (!AppGlobals.isUserActive() && !AppGlobals.dialogCancel && AppGlobals.isUserLoggedIn()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("User not active");
            alertDialogBuilder.setMessage("please activate your account")
                    .setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    startActivity(new Intent(getApplicationContext(), CodeConfirmationActivity.class));
                }
            });
            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    AppGlobals.dialogCancel = true;
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.anim_right_in, R.anim.anim_right_out);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
//            case R.id.add_address:
//                startActivity(new Intent(getApplicationContext(), AddressesActivity.class));
//                break;
//            case R.id.request_laundry:
//                startActivity(new Intent(getApplicationContext(), LaundryCategoriesActivity.class));
//                break;
            case R.id.login:
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                break;
            case R.id.active:
                startActivity(new Intent(getApplicationContext(), CodeConfirmationActivity.class));
                break;
            case R.id.nav_update_profile:
                startActivity(new Intent(getApplicationContext(), UpdateProfile.class));
                break;
            case R.id.nav_reset_password:
                startActivity(new Intent(getApplicationContext(), ResetPassword.class));
                break;
            case R.id.nav_logout:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Confirmation");
                alertDialogBuilder.setMessage("Do you really want to logout?").setCancelable(false).setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SharedPreferences sharedpreferences = AppGlobals.getPreferenceManager();
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.clear();
                                editor.commit();
                                AppGlobals.logout = true;
                                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                            }
                        });
                alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                break;
            default:
                startActivity(new Intent(getApplicationContext(), MainActivity.class));

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                progress.dismiss();
                System.out.println("Ok kro :   " + request.getResponseText());
                try {
                    array = new JSONArray(request.getResponseText());
                    try {
                        JSONObject jsonObject = array.getJSONObject(0);
                        Log.i("Tag", jsonObject.toString());
                        JSONObject addressObject = jsonObject.getJSONObject("address");
                        JSONArray itemsArray = jsonObject.getJSONArray("service_items");
                        listAdapter = new CustomAdapter(itemsArray, addressObject,
                                jsonObject.getString("created"), jsonObject.getBoolean("done"));
                        mRecyclerView.setAdapter(listAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }
    }

    static class CustomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
            RecyclerView.OnItemTouchListener {

        private JSONArray items;
        private JSONObject mAddresses;
        private String mCreatedDate;
        private boolean mIsDone;
        private CustomView viewHolder;
        private OnItemClickListener mListener;
        private GestureDetector mGestureDetector;

        public interface OnItemClickListener {
            void onItem(String item);
        }

        public CustomAdapter(JSONArray categories, Context context,
                             OnItemClickListener listener) {
            this.items = categories;
            mListener = listener;
            mGestureDetector = new GestureDetector(context,
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onSingleTapUp(MotionEvent e) {
                            return true;
                        }
                    });
        }

        public CustomAdapter(JSONArray categories, JSONObject addresses, String createdDate, boolean isDone) {
            mAddresses = addresses;
            mCreatedDate = createdDate;
            mIsDone = isDone;
            items = categories;
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.delegate_laundry_items, parent, false);
            viewHolder = new CustomView(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            holder.setIsRecyclable(false);
            try {
                viewHolder.address.setText("Address: " + mAddresses.getString("name"));
                JSONObject jsonObject = items.getJSONObject(position);
                viewHolder.textItem.setText("Items: " +jsonObject.getString("item"));
                viewHolder.itemName.setText("Item Name: " + jsonObject.getString("name"));
                viewHolder.itemQuantity.setText("Quantity: " + jsonObject.getString("quantity"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return items.length();
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View childView = rv.findChildViewUnder(e.getX(), e.getY());
            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
//                mListener.onItem(items.get(rv.getChildPosition(childView)));
                return true;
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }

        /* Member class that extends RecyclerView.ViewHolder allows us to access the elements inside
           xml it takes view in constructor
         */
        public class CustomView extends RecyclerView.ViewHolder {
            public TextView address;
            public TextView textItem;
            public TextView itemName;
            public TextView itemQuantity;

            public CustomView(View itemView) {
                super(itemView);
                address = (TextView) itemView.findViewById(R.id.tv_address);
                textItem = (TextView) itemView.findViewById(R.id.tv_item);
                itemName = (TextView) itemView.findViewById(R.id.tv_item_name);
                itemQuantity = (TextView) itemView.findViewById(R.id.tv_quantity);
            }
        }
    }

}
