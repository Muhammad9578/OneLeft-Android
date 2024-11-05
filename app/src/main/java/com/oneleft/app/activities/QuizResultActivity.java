package com.oneleft.app.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.oneleft.app.R;
import com.oneleft.app.adapters.ResultAdapter;
import com.oneleft.app.adapters.ResultItem;
import com.oneleft.app.models.Answer;
import com.oneleft.app.models.Player;
import com.oneleft.app.models.Room;
import com.oneleft.app.models.User;
import com.oneleft.app.utils.ApiConfig;
import com.oneleft.app.utils.App;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QuizResultActivity extends AppCompatActivity {

    private RecyclerView rvResults;
    private Button btnClaimReward;
    private ProgressBar progressBar;

    private DatabaseReference dbRef;
    private Room currentRoom;

    private String currentRoomId;

    private ArrayList<ResultItem> resultList;
    private ResultAdapter resultAdapter;
    private ProgressDialog progressDialog;

    private static String ROOM_NODE = "Rooms";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Quiz Result");

        btnClaimReward = findViewById(R.id.btnClaimReward);
        progressBar = findViewById(R.id.progressBar);
        rvResults = findViewById(R.id.rvResults);
        rvResults.setLayoutManager(new LinearLayoutManager(this));

        currentRoomId = getIntent().getStringExtra("room_id");
        Log.i("mytag", "game value:" + getIntent().getStringExtra("game"));
        ROOM_NODE = getIntent().getStringExtra("game").equals("1") ? "Rooms" : "Rooms2";

        dbRef = FirebaseDatabase.getInstance().getReference();

        dbRef.child(ROOM_NODE)
                .child(currentRoomId)
                .child("gameEnded")
                .setValue(true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please Wait");

        resultList = new ArrayList<>();
        resultAdapter = new ResultAdapter(resultList, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        rvResults.setAdapter(resultAdapter);

        dbRef.child(ROOM_NODE).child(currentRoomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentRoom = snapshot.getValue(Room.class);
                if (currentRoom == null || currentRoom.getUserList() == null) {
                    return;
                }
                for (Map.Entry<String, Player> entry : currentRoom.getUserList().entrySet()) {
                    if (entry == null)
                        continue;
                    ResultItem resultItem = new ResultItem();
                    resultItem.setPlayerId(entry.getKey());

                    dbRef.child("Users")
                            .child(entry.getKey())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshoty) {
                                    User user = snapshoty.getValue(User.class);
                                    resultItem.setName(user.getUsername());
                                    resultAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                    DatabaseReference answersRef = dbRef.child("Answers")
                            .child(currentRoomId)
                            .child(entry.getKey());

                    answersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshotx) {
                            long answersCount = snapshotx.getChildrenCount();
                            Log.i("mytag", entry.getKey() + "-answer count:" + answersCount);
                            resultItem.setNumberOfAnswers((int) answersCount);
                            int correctAnswers = 0;
                            for (DataSnapshot snapx : snapshotx.getChildren()) {
                                Answer answer = snapx.getValue(Answer.class);
                                if (answer.getCorrectOption().equals(answer.getSelectedOption())) {
                                    correctAnswers++;
                                }
                            }
                            Log.i("mytag", "correctAnswers are:" + correctAnswers);
                            if (correctAnswers == 0) {
                                resultAdapter.notifyDataSetChanged();
                                calculateReward();
                                return;
                            }
                            resultItem.setNumberOfCorrectAnswers(correctAnswers);
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (currentUser != null) {
                                if (resultItem.getPlayerId().equals(currentUser.getUid())) {
                                    btnClaimReward.setVisibility(View.VISIBLE);
                                }
                            }
                            resultList.add(resultItem);
                            resultAdapter.notifyDataSetChanged();
                            calculateReward();

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                error.toException();
                Toast.makeText(QuizResultActivity.this, "Failed to fetch result", Toast.LENGTH_SHORT).show();
            }
        });

        btnClaimReward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                String accountId = PreferenceManager.getDefaultSharedPreferences(QuizResultActivity.this)
                        .getString("STRIPE_ACCOUNT_ID", "");
                if (accountId.isEmpty()) {
                    Toast.makeText(QuizResultActivity.this, "You must setup stripe account before playing", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(QuizResultActivity.this, StripeAccountActivity.class));
                    return;
                }
//                String accountId = PreferenceManager.getDefaultSharedPreferences(QuizResultActivity.this)
//                        .getString("STRIPE_ACCOUNT_ID", "");

                StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.CLAIM_REWARD_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            progressDialog.dismiss();
                            JSONObject jsonResponse = new JSONObject(response);
                            JSONObject transferObject = jsonResponse.optJSONObject("transfer");
                            if (transferObject != null) {
                                Toast.makeText(QuizResultActivity.this, "Transfer Successful", Toast.LENGTH_SHORT).show();
                                dbRef.child(ROOM_NODE)
                                        .child(currentRoomId)
                                        .removeValue();
                                finish();
                            } else {
                                Toast.makeText(QuizResultActivity.this, "Transfer Failed", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(QuizResultActivity.this, "Unable to process server response", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        error.printStackTrace();
                        Toast.makeText(QuizResultActivity.this, "Unable to fetch data from server", Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("account_id", accountId);
                        params.put("amount", new DecimalFormat("#.##").format(resultAdapter.getAmountPerPlayer() * 100));
                        return params;
                    }
                };
                App.getRequestQueue().add(request);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void calculateReward() {
        int totalPlayers = currentRoom.getTotalPlayersJoined();
        //(Total sum of inscription fees / users left) - 25% app commission
        double totalFeeRcvd = totalPlayers * 0.99;
        int usersLeft = currentRoom.getUserList().size();
        double amountPerPlayer = totalFeeRcvd / (float) usersLeft;
        double appCommission = amountPerPlayer * 0.25;
        amountPerPlayer = amountPerPlayer - appCommission;
        resultAdapter.setAmountPerPlayer(amountPerPlayer);
        resultAdapter.notifyDataSetChanged();

        if (amountPerPlayer > 0) {
            dbRef.child("History")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(currentRoomId)
                    .child("rewardEarned")
                    .setValue(amountPerPlayer);
        }

    }
}