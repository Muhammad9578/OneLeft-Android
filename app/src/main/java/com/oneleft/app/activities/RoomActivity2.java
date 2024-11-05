package com.oneleft.app.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.oneleft.app.R;
import com.oneleft.app.adapters.PlayerAdapter;
import com.oneleft.app.models.History;
import com.oneleft.app.models.Player;
import com.oneleft.app.models.Room;
import com.oneleft.app.models.User;
import com.oneleft.app.utils.App;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RoomActivity2 extends AppCompatActivity {

    private RecyclerView rvPlayers;
    //private Button btnStart;
    private TextView tvGameScheduleTime;
    private TextView tvGameCountdown;
    private ProgressBar progressBar;

    private ArrayList<String> playerList;
    private PlayerAdapter playerAdapter;

    private DatabaseReference dbRef;
    private String currentRoomId;

    private long gameScheduleTime;
    private CountDownTimer countDownTimer;

    private boolean gameStarted = false;

    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_2);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Players Lounge 2");

        //btnStart = findViewById(R.id.btnStart);
        rvPlayers = findViewById(R.id.rvPlayers);
        tvGameScheduleTime = findViewById(R.id.tvGameScheduleTime);
        tvGameCountdown = findViewById(R.id.tvGameCountdown);
        progressBar = findViewById(R.id.progressBar);

        rvPlayers.setLayoutManager(new LinearLayoutManager(this));
        playerList = new ArrayList<>();
        playerAdapter = new PlayerAdapter(playerList, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        rvPlayers.setAdapter(playerAdapter);

        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        name = user.getUsername();
                    }
                });

    }

    @Override
    protected void onStart() {
        super.onStart();
        playerList.clear();
        playerAdapter.notifyDataSetChanged();
        //first get current time
        Calendar calendar = Calendar.getInstance();
        long currentTime = calendar.getTimeInMillis();

        int minute = calendar.get(Calendar.MINUTE);

        Log.i("mytag", "minutes to add: " + (15- (minute % 15)));

        calendar.add(Calendar.MINUTE, 15 - (minute % 15));
        calendar.set(Calendar.MILLISECOND, 0);
        /*if (minute < 15) {
            calendar.add(Calendar.MINUTE, 15 - minute);
        } else {
            calendar.add(Calendar.MINUTE, 60 - minute);
        }*/
        progressBar.setVisibility(View.VISIBLE);
        //this player has to join a room or we have to create a new room with this player
        dbRef.child("Rooms2").orderByChild("status").equalTo("active").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                Log.i("mytag", "room count: " + count);
                if (count == 0) {
                    //create new room
                    DatabaseReference roomDbRef = dbRef.child("Rooms2").push();
                    Room room = new Room();
                    room.setId(roomDbRef.getKey());
                    room.setStatus("active");
                    room.setLastActivityTime(currentTime);
                    room.setCreationTime(currentTime);
                    room.setGameScheduleTime(calendar.getTimeInMillis());
                    gameScheduleTime = calendar.getTimeInMillis();
                    currentRoomId = room.getId();
                    HashMap<String, Player> userList = new HashMap<>();
                    userList.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), new Player(FirebaseAuth.getInstance().getCurrentUser().getUid(), "dummy", App.getFCMToken(RoomActivity2.this)));
                    room.setUserList(userList);
                    roomDbRef.setValue(room).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                fetchPlayers(currentRoomId);
                                Toast.makeText(RoomActivity2.this, "New room created and player added", Toast.LENGTH_SHORT).show();
                                monitorGameRoom();
                                addToHistory();
                            } else {
                                Toast.makeText(RoomActivity2.this, "Unable to add to room", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    //add to existing room
                    //check if user is already part of a group
                    boolean isInRoom = false;
                    String roomId = "";
                    String currentUserId = FirebaseAuth.getInstance().getUid();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Room roomx = child.getValue(Room.class);
                        if (roomx.getUserList() != null) {
                            for (Map.Entry<String, Player> x : roomx.getUserList().entrySet()) {
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
                        currentRoomId = roomId;
                        fetchPlayers(roomId);
                        monitorGameRoom();
                        return;
                    }
                    //check if any room has space
                    boolean isBreak = false;
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Room roomx = child.getValue(Room.class);
                        if (roomx == null) {
                            continue;
                        }
                        if (!roomx.isGameStarted()) {
                            currentRoomId = roomx.getId();
                            //add player to this room
                            HashMap<String, Player> userList = roomx.getUserList();
                            if (userList == null) {
                                userList = new HashMap<>();
                            }
                            userList.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), new Player(FirebaseAuth.getInstance().getCurrentUser().getUid(), "dummy", App.getFCMToken(RoomActivity2.this)));
                            roomx.setUserList(userList);
                            roomx.setLastActivityTime(System.currentTimeMillis());

                            dbRef.child("Rooms2").child(roomx.getId()).setValue(roomx).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        dbRef.child("Rooms2").child(roomx.getId()).child("totalPlayersJoined").setValue(ServerValue.increment(1));
                                        fetchPlayers(roomx.getId());
                                        Toast.makeText(RoomActivity2.this, "Player added to existing room", Toast.LENGTH_SHORT).show();
                                        monitorGameRoom();
                                        addToHistory();
                                    } else {
                                        Toast.makeText(RoomActivity2.this, "Unable to add to room", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            isBreak = true;
                            break;
                        }
                    }
                    if (!isBreak) {
                        //create new room and add this player to that room
                        DatabaseReference roomDbRef = dbRef.child("Rooms2").push();
                        Room room = new Room();
                        room.setId(roomDbRef.getKey());
                        room.setStatus("active");
                        room.setCreationTime(System.currentTimeMillis());
                        room.setLastActivityTime(System.currentTimeMillis());
                        room.setGameScheduleTime(calendar.getTimeInMillis());
                        room.setTotalPlayersJoined(1);
                        gameScheduleTime = calendar.getTimeInMillis();
                        currentRoomId = room.getId();
                        HashMap<String, Player> userList = new HashMap<>();
                        userList.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), new Player(FirebaseAuth.getInstance().getCurrentUser().getUid(), "dummy", App.getFCMToken(RoomActivity2.this)));
                        room.setUserList(userList);
                        roomDbRef.setValue(room).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(RoomActivity2.this, "New room created and player added 2", Toast.LENGTH_SHORT).show();
                                    fetchPlayers(currentRoomId);
                                    monitorGameRoom();
                                    addToHistory();
                                } else {
                                    Toast.makeText(RoomActivity2.this, "Unable to add to room", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RoomActivity2.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPlayers(String roomId) {
        progressBar.setVisibility(View.VISIBLE);
        dbRef.child("Rooms2")
                .child(roomId)
                .child("userList")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        Player player = snapshot.getValue(Player.class);
                        dbRef.child("Users")
                                .child(player.getId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        progressBar.setVisibility(View.GONE);
                                        if (!snapshot.exists()) {
                                            return;
                                        }
                                        User user = snapshot.getValue(User.class);
                                        if (user == null) {
                                            return;
                                        }
                                        playerList.add(user.getUsername());
                                        Log.i("mytag", "player list size: " + playerList.size());
                                        playerAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (!snapshot.exists()) {
                            return;
                        }
                        Player player = snapshot.getValue(Player.class);
                        if (player == null) {
                            return;
                        }
                        dbRef.child("Users")
                                .child(player.getId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (!snapshot.exists()) {
                                            return;
                                        }
                                        User user = snapshot.getValue(User.class);
                                        if (user == null) {
                                            return;
                                        }
                                        playerList.remove(user.getUsername());
                                        playerAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void startGame() {
        if (gameStarted) {
            return;
        }
        gameStarted = true;
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameStarted", true);
        map.put("gameStartTime", System.currentTimeMillis());
        dbRef.child("Rooms2")
                .child(currentRoomId)
                .updateChildren(map);
        startActivity(new Intent(RoomActivity2.this, QuizActivity.class)
                .putExtra("game", "2")
                .putExtra("room_id", currentRoomId));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_leave) {
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage("Are you sure you want to exit the game?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (currentRoomId != null) {
                                removePlayerFromRoom();
                            }
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.i("mytag", "gameScheduleTime: " + gameScheduleTime);
            if (gameScheduleTime != 0) {
                long millis = gameScheduleTime - System.currentTimeMillis();
                //millis = 0;
                if (millis > 0) {
                    String hms = String.format(Locale.US, "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                            TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                            TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));

                    tvGameScheduleTime.setText(hms);
                    tvGameScheduleTime.setVisibility(View.VISIBLE);
                    showGameScheduleTime();
                    if (millis == 10000) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("notification", true);
                        dbRef.child("Rooms2")
                                .child(currentRoomId)
                                .updateChildren(map);
                    }
                } else {
                    if (playerList.size() >= 2) {
                        //btnStart.setVisibility(View.VISIBLE);
                        if (countDownTimer == null) {

                            countDownTimer = new CountDownTimer(1000, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    //tvGameCountdown.setText("Game will begin in " + (millisUntilFinished / 1000) + " seconds");
                                }

                                @Override
                                public void onFinish() {
                                    Log.i("mytag", "going to start the game");
                                    startGame();
                                }
                            };
                            countDownTimer.start();
                        }
                    } else {
                        //schedule game to next 15 min
                        Calendar calendar = Calendar.getInstance();

                        int minute = calendar.get(Calendar.MINUTE);

                        calendar.add(Calendar.MINUTE, 15 - (minute % 15));
                        /*if (minute < ) 15{
                            calendar.add(Calendar.MINUTE, 15 - minute);
                        } else {
                            calendar.add(Calendar.MINUTE, 60 - minute);
                        }*/
                        dbRef.child("Rooms2").child(currentRoomId).child("gameScheduleTime").setValue(calendar.getTimeInMillis());
                        new AlertDialog.Builder(RoomActivity2.this)
                                .setTitle("Important")
                                .setMessage("2 players did not join so the game is being rescheduled to start in the next time slot")
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }
            }
        }
    };
    Handler handler = new Handler();

    private void showGameScheduleTime() {
        Log.i("mytag", "inside showGameScheduleTime");
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 1000);
    }

    private Room currentRoom;

    ValueEventListener roomValueListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            currentRoom = snapshot.getValue(Room.class);
            if (currentRoom == null) {
                return;
            }
            gameScheduleTime = currentRoom.getGameScheduleTime();
            showGameScheduleTime();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private void monitorGameRoom() {
        dbRef.child("Rooms2").child(currentRoomId)
                .addValueEventListener(roomValueListener);
    }

    @Override
    protected void onStop() {
        dbRef.child("Rooms2").child(currentRoomId)
                .removeEventListener(roomValueListener);
        handler.removeCallbacks(runnable);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_room, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void removePlayerFromRoom() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef.child("Rooms2").child(currentRoomId)
                .child("userList")
                .child(currentUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RoomActivity2.this, "Removed from Room successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(RoomActivity2.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                /*.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Room currentRoom = snapshot.getValue(Room.class);
                        ArrayList<String> currentUserList = currentRoom.getUserList();
                        for (String playerId : currentUserList) {
                            if (playerId.equals(currentUserId)) {
                                currentUserList.remove(playerId);
                            }
                            dbRef.child("Rooms2").child(currentRoomId)
                                    .child("userList").setValue(currentUserList)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(RoomActivity2.this, "Removed from Room successfully", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });*/
    }

    private void addToHistory() {
        dbRef.child("Rooms2").child(currentRoomId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Room currentRoom = snapshot.getValue(Room.class);
                        DatabaseReference historyRef = dbRef.child("History")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child(currentRoomId);

                        User user = new User();
                        History history = new History();
                        history.setId(historyRef.getKey());
                        history.setName(name);
                        history.setGame("Open Table");
                        history.setDateTime(currentRoom.getCreationTime());
                        history.setRewardEarned(0);

                        historyRef.setValue(history);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}