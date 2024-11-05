package com.oneleft.app.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.oneleft.app.R;
import com.oneleft.app.adapters.GameAdapter;
import com.oneleft.app.models.Category;
import com.oneleft.app.models.Game;
import com.oneleft.app.models.Player;
import com.oneleft.app.models.Room;
import com.oneleft.app.models.User;
import com.oneleft.app.utils.ApiConfig;
import com.oneleft.app.utils.App;
import com.oneleft.app.utils.PaymentsUtil;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.googlepaylauncher.GooglePayEnvironment;
import com.stripe.android.googlepaylauncher.GooglePayLauncher;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView rvGames;
    private ProgressBar progressBar;
    private TextView tvUsername;

    private ArrayList<Game> gameList;
    private GameAdapter gameAdapter;

    private ArrayList<Category> categoryList;
    private Spinner spCategories;

    private DatabaseReference dbRef;

    private PaymentsClient paymentsClient;

    private ProgressDialog progressDialog;

    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 190;
    private int positionSelected = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View view = getWindow().getDecorView();
            int flags = view.getSystemUiVisibility();
            //flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR; //make status bar light
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR; //remove light status bar
            view.setSystemUiVisibility(flags);
            //getWindow().setStatusBarColor(Color.WHITE);
        }
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(getSupportActionBar().getTitle());
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        rvGames = findViewById(R.id.rvGames);
        progressBar = findViewById(R.id.progressBar);
        spCategories = findViewById(R.id.spCategories);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait");

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.action_settings, R.string.already_have_an_account);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        tvUsername = navigationView.getHeaderView(0).findViewById(R.id.tvUsername);

        dbRef = FirebaseDatabase.getInstance().getReference();
        PaymentConfiguration.init(this, "pk_test_51JlfHUHLDhes5Kw78oY4gC8oZOEMu56luPzFvNdIVh5APgjbEiLJK2oqX1c6bMmzLGQhOA6NIxv8yusw8KJY8RPe00fXNJ4Fcr");
        final GooglePayLauncher googlePayLauncher = new GooglePayLauncher(
                this,
                new GooglePayLauncher.Config(
                        GooglePayEnvironment.Test,
                        "US",
                        getString(R.string.app_name)
                ),
                new GooglePayLauncher.ReadyCallback() {
                    @Override
                    public void onReady(boolean b) {

                    }
                },
                new GooglePayLauncher.ResultCallback() {
                    @Override
                    public void onResult(@NotNull GooglePayLauncher.Result result) {
                        if (result instanceof GooglePayLauncher.Result.Completed) {
                            // Payment succeeded, show a receipt view
                            Toast.makeText(MainActivity.this, "Payment Successful", Toast.LENGTH_SHORT).show();
                            if (positionSelected == 0) {
                                startActivity(new Intent(MainActivity.this, RoomActivity.class));
                            } else {
                                startActivity(new Intent(MainActivity.this, RoomActivity2.class));
                            }
                        } else if (result instanceof GooglePayLauncher.Result.Canceled) {
                            // User cancelled the operation
                            Toast.makeText(MainActivity.this, "Payment Cancelled", Toast.LENGTH_SHORT).show();
                        } else if (result instanceof GooglePayLauncher.Result.Failed) {
                            // Operation failed; inspect `result.getError()` for more details
                            ((GooglePayLauncher.Result.Failed) result).getError().printStackTrace();
                            Toast.makeText(MainActivity.this, "Payment failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        gameList = new ArrayList<>();
        gameAdapter = new GameAdapter(gameList, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//                String accountId = PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
//                        .getString("STRIPE_ACCOUNT_ID", "");
//                if (accountId.isEmpty()) {
//                    Toast.makeText(MainActivity.this, "You must setup stripe account before playing", Toast.LENGTH_SHORT).show();
//                    startActivity(new Intent(MainActivity.this, StripeAccountActivity.class));
//                    return;
//                }

                progressDialog.show();
                //check if user is already inside a lobby
                String ROOM_NODE = "Rooms";
                if (position == 1) {
                    ROOM_NODE = "Rooms2";
                }
                dbRef.child(ROOM_NODE).orderByChild("status").equalTo("active").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        boolean isInRoom = false;
                        String roomId = "";
                        String currentUserId = FirebaseAuth.getInstance().getUid();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Room roomx = child.getValue(Room.class);
                            if (roomx == null || roomx.isGameEnded()) {
                                continue;
                            }
                            if (roomx.getUserList() != null) {
                                for (Map.Entry<String, Player> x : roomx.getUserList().entrySet()) {
                                    if (x == null)
                                        continue;
                                    if (x.getKey().equals(currentUserId)) {
                                        isInRoom = true;
                                        roomId = roomx.getId();
                                        break;
                                    }
                                }
                            }
                            if (isInRoom) {
                                break;
                            }
                        }
                        if (isInRoom) {
                            if (position == 0) {
                                startActivity(new Intent(MainActivity.this, RoomActivity.class));
                            } else {
                                startActivity(new Intent(MainActivity.this, RoomActivity2.class));
                            }
                        } else {
                            Dialog dialog = new Dialog(MainActivity.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                            dialog.setContentView(R.layout.dialog_pay);
                            dialog.show();

                            ImageView ivClose = dialog.findViewById(R.id.ivClose);
                            ImageButton btnPayNow = dialog.findViewById(R.id.btnPayNow);
                            ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
                            //btnPayNow.setVisibility(View.GONE);

                            ivClose.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });

                            btnPayNow.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();

                                    //without payment start
                                    if (position == 0) {
                                        startActivity(new Intent(MainActivity.this, RoomActivity.class));
                                    } else {
                                        startActivity(new Intent(MainActivity.this, RoomActivity2.class));
                                    }
                                    // without payment end

                                    //PaymentDataRequest paymentDataRequest = PaymentDataRequest.fromJson(PaymentsUtil.getPaymentDataRequest(1).toString());
                                    GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
                                    int connectionResult = googleApiAvailability.isGooglePlayServicesAvailable(MainActivity.this);
                                    if (connectionResult == ConnectionResult.SUCCESS) {
                                        progressDialog.show();
                                        StringRequest stringRequest = new StringRequest(Request.Method.POST, ApiConfig.PAYMENT_INTENT_URL, new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                try {
                                                    progressDialog.dismiss();
                                                    JSONObject jsonResponse = new JSONObject(response);
                                                    int status = jsonResponse.getInt("status");
                                                    if (status == 0) {
                                                        String message = jsonResponse.getString("message");
                                                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        String clientSecret = jsonResponse.getString("clientSecret");
                                                        try {
                                                            googlePayLauncher.presentForPaymentIntent(clientSecret);
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                            Log.i("ppp", ""+e);
                                                            Toast.makeText(MainActivity.this, "Google Pay is not available", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    Toast.makeText(MainActivity.this, "Unable to process server response", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                progressDialog.dismiss();
                                                Toast.makeText(MainActivity.this, "Unable to connect server", Toast.LENGTH_SHORT).show();
                                            }
                                        }) {
                                            @Override
                                            protected Map<String, String> getParams() throws AuthFailureError {
                                                Map<String, String> params = new HashMap<>();
                                                params.put("amount", "0.99");
                                                params.put("currency", "USD");
                                                return params;
                                            }
                                        };

                                        Volley.newRequestQueue(MainActivity.this).add(stringRequest);

                                        //AutoResolveHelper.resolveTask(paymentsClient.loadPaymentData(paymentDataRequest), MainActivity.this, LOAD_PAYMENT_DATA_REQUEST_CODE);
                                    } else {
                                        if (googleApiAvailability.isUserResolvableError(connectionResult)) {
                                            startActivity(googleApiAvailability.getErrorResolutionIntent(MainActivity.this, connectionResult, null));
                                        } else {
                                            Toast.makeText(MainActivity.this, "Google play services not available on device", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });

                            IsReadyToPayRequest isReadyToPayRequest = IsReadyToPayRequest.fromJson(PaymentsUtil.getIsReadyToPayRequest().toString());

                            paymentsClient.isReadyToPay(isReadyToPayRequest)
                                    .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Boolean> task) {
                                            progressBar.setVisibility(View.GONE);
                                            if (task.isSuccessful()) {
                                                btnPayNow.setVisibility(View.VISIBLE);
                                                if (task.getResult()) {
                                                    btnPayNow.setVisibility(View.VISIBLE);
                                                } else {
                                                    dialog.dismiss();
                                                    Toast.makeText(MainActivity.this, "Google Pay is not available on this device", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(MainActivity.this, "Google Pay not supported", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        categoryList = new ArrayList<>();

        rvGames.setLayoutManager(new LinearLayoutManager(this));
        rvGames.setAdapter(gameAdapter);

        fetchGames();
        fetchCategories();

        paymentsClient = PaymentsUtil.createPaymentsClient(this);

        String secretKey = "6669e582ed9dij6EqoKMhZHp12irg9dh";
        //String iv = "6669e582ed9dij6E";
        /*String str = "Mz5wIaI9+5cxMBwJorkd1p8fV4TKayVO0E+f12FKDSxkZGHss/14P7oui67fSq9EWmUB5ScRzC5CP+dvhlpyWVyQq3TN0950wMctMKiWZELSMaTgw/vE2cswLsTlHCbJHMhs9GRXARHiaOrPRSfjPhGNVeVkeINwqiMlYYAKRlrPwK5paCof/AWVcD4VtqjoWa0nYeBMyEipfeZUP4z0pfntJQ2fUBfif2fuXnHOze5AukOEx2iJ83zXHWL4yqGAyyBaRa4hvzAp3gqrQRYojFQxxPuk7tfHRGUR52KE+c4zbL9Sa8dVW8OzewWR76tG/Fw5U6lP8W1ywDHgdvB3uX+t4u+Etx64Bkj581Za4HcTjp0WjjFoSErHovf7eaEFfO3hv8tCNchrf5ezmv6G/8Jf3FOMu4wyTX95UbEkwh/nCOFcPsW0C0XocDKMX2sZ3BMtFfTD5TaEwsDXcSFx3n2FY9uczgUxJ+vzTTbldmLG23EDZQlA8sTuclhTDcqlXD7xX+GkEySzkVjEMveDKu8BWZWxrAe6L2xKf2Fq+zxtIOK4gyRWL05Ge/ck2cHQtFqFXGPMb0ZHrWteEKK7wJy7EDNVJ9hwel1ldYZUt3++DPy99EBMP+AR8KfcW+Z82ybiSu6CMciU5pwNz+FzeygIRWPOVzjEqDYKX3ha5JGuWIz9cqDR9pcMDVO3WmnOUBjryYl0qmbKcbPaOfHkQlWLdnQTHs1i6QWps/Db3vGuioRk4aOyeEKfqrk217KdYUn7WY2bm/WeNGtOyTVU1fFLK2dwy93lVKvEE/hxXVH4l5KXJSItXV4Lg6IvzyjbhzQhgZLzyKKkYxRzIB5iMaBusnCe1nImhw5FtDTny2se/L+o63mwSWLbtoJbqK8px2w9oGe3aTxOhIyGWVldtp6jIzwWuPNhI5OOCYMrl/uXdDvaoswVfvPWwfnQFYzEoE752svI6BZSTuG7eRx4vq2YC/Nejboa+Cp1FgY9GrMZC7N7hIry7YVWcXG5y6yNqPfpki68uq9qoLBmSJmpMEZ1iIcVFhyDofQCpNRsg2XMydoPVb7nHKz8LXzhLyBt4p90aXsbP2496+FvcGT6cSWhF/n6agDCH96sija5WXM7HMhymQMB5Vk4t4rUSpz40egS7toDmGJl3JIJoO9h8w1VOcyc7+ULZ42d12t+8G7PVkdfVBq9De/iQnfKtperhE3rjt5mBbHxkxqqhWjHj0+NKdzVZcGW+oicgOCBilk=";

        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec sk = new SecretKeySpec(secretKey.getBytes(Charsets.UTF_8), "AES");
            IvParameterSpec iv = new IvParameterSpec(secretKey.substring(0, 16).getBytes(Charsets.UTF_8));
            c.init(Cipher.DECRYPT_MODE, sk, iv);

            String dec = new String(c.doFinal(android.util.Base64.decode(str.getBytes(), android.util.Base64.DEFAULT)));
            Log.i("mytag", "decrypted: " + dec);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        loadUserDetails();

        checkFirebaseMessagingToken();
    }

    private void checkFirebaseMessagingToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.i("mytag", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        App.saveFCMToken(MainActivity.this, token);
                        /*dbRef.child("Users")
                                .child(FirebaseAuth.getInstance().getUid())
                                .child("NotificationTokens")
                                .child(token)
                                .child("updateTime")
                                .setValue(System.currentTimeMillis());*/
                    }
                });
    }

    private void loadUserDetails() {
        Log.i("mytag", "inside loaduserdetails");
        dbRef.child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        Log.i("mytag", "userdetails success");
                        User user = dataSnapshot.getValue(User.class);
                        tvUsername.setText(user.getUsername());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("mytag", "userdetails fail");
                        e.printStackTrace();
                    }
                });
    }

    private void fetchCategories() {
        categoryList.clear();

        categoryList.add(new Category("All"));

        dbRef.child("Categories")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot datasnapshot : snapshot.getChildren()) {
                            categoryList.add(datasnapshot.getValue(Category.class));
                        }
                        ArrayAdapter<Category> arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, categoryList);
                        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spCategories.setAdapter(arrayAdapter);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchGames() {
        progressBar.setVisibility(View.GONE);

        gameList.add(new Game("1", "10 - User Table", 0.99));
        gameList.add(new Game("2", "Open Table", 0.99));

        gameAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        drawerLayout.closeDrawer(GravityCompat.START);
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        } else if (id == R.id.nav_payment) {
            startActivity(new Intent(MainActivity.this, CardsActivity.class));
        } else if (id == R.id.nav_history) {
            startActivity(new Intent(MainActivity.this, HistoryActivity.class));
        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else if (id == R.id.nav_contact) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:")); // only email apps should handle this
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"oneleftapp@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " Feedback");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        } else if (id == R.id.nav_terms) {
            startActivity(new Intent(MainActivity.this, TermsOfServiceActivity.class));
        } else if (id == R.id.nav_stripe) {
            startActivity(new Intent(MainActivity.this, StripeAccountActivity.class));
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            switch (resultCode) {

                case Activity.RESULT_OK:
                    PaymentData paymentData = PaymentData.getFromIntent(data);
                    handlePaymentSuccess(paymentData);
                    break;

                case AutoResolveHelper.RESULT_ERROR:
                    Status status = AutoResolveHelper.getStatusFromIntent(data);
                    handleError(status.getStatusCode());
                    break;
            }
            Toast.makeText(this, "result rcivd", Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePaymentSuccess(PaymentData paymentData) {

        // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
        final String paymentInfo = paymentData.toJson();
        /*if (paymentInfo == null) {
            return;
        }*/

        try {
            JSONObject paymentMethodData = new JSONObject(paymentInfo).getJSONObject("paymentMethodData");
            // If the gateway is set to "example", no payment information is returned - instead, the
            // token will only consist of "examplePaymentMethodToken".

            final JSONObject tokenizationData = paymentMethodData.getJSONObject("tokenizationData");
            final String token = tokenizationData.getString("token");
            final JSONObject info = paymentMethodData.getJSONObject("info");
            final String billingName = info.getJSONObject("billingAddress").getString("name");
            /*Toast.makeText(
                    this, getString(R.string.payments_show_name, billingName),
                    Toast.LENGTH_LONG).show();*/

            // Logging token string.
            Log.d("mytag", "Google Pay token:" + token);
            Toast.makeText(this, "Google Pay token:" + token, Toast.LENGTH_LONG).show();

        } catch (JSONException e) {
            throw new RuntimeException("The selected garment cannot be parsed from the list of elements");
        }
    }

    private void handleError(int statusCode) {
        Log.e("loadPaymentData failed", String.format("Error code: %d", statusCode));
    }
}
