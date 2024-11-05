package com.oneleft.app.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RoomActivity extends AppCompatActivity {

    private RecyclerView rvPlayers;
    private ProgressBar progressBar;
    private TextView tvGameCountdown;

    private ArrayList<String> playerList;
    private PlayerAdapter playerAdapter;

    private DatabaseReference dbRef;
    private String currentRoomId;

    private String name;
    private Room currentRoom;
    private boolean gameStarted = false;
    private long gameScheduleTime;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Players Lounge");

        progressBar = findViewById(R.id.progressBar);
        rvPlayers = findViewById(R.id.rvPlayers);
        tvGameCountdown = findViewById(R.id.tvGameCountdown);
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

        //this player has to join a room or we have to create a new room with this player
        dbRef.child("Rooms").orderByChild("status").equalTo("active").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                Log.i("mytag", "room count: " + count);
                if (count == 0) {
                    //create new room
                    DatabaseReference roomDbRef = dbRef.child("Rooms").push();
                    Room room = new Room();
                    room.setId(roomDbRef.getKey());
                    room.setStatus("active");
                    long currentTime = System.currentTimeMillis();
                    room.setCreationTime(currentTime);
                    room.setLastActivityTime(currentTime);
                    room.setTotalPlayersJoined(1);
                    currentRoomId = room.getId();
                    HashMap<String, Player> userList = new HashMap<>();
                    String playerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    userList.put(playerId, new Player(playerId, "dummy", App.getFCMToken(RoomActivity.this)));
                    room.setUserList(userList);
                    roomDbRef.setValue(room).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                fetchPlayers(currentRoomId);
                                Toast.makeText(RoomActivity.this, "New room created and player added", Toast.LENGTH_SHORT).show();
                                addToHistory();
                            } else {
                                Toast.makeText(RoomActivity.this, "Unable to add to room", Toast.LENGTH_SHORT).show();
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
                        //Toast.makeText(RoomActivity.this, "Player is already part of an existing room", Toast.LENGTH_SHORT).show();
                        currentRoomId = roomId;
                        fetchPlayers(roomId);
                        return;
                    }
                    //check if any room has space
                    boolean isBreak = false;
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Room roomx = child.getValue(Room.class);
                        if (!roomx.isGameEnded() && !roomx.isGameStarted() && (roomx.getUserList() == null || roomx.getUserList().size() < 10)) {
                            currentRoomId = roomx.getId();
                            //add player to this room
                            HashMap<String, Player> userList;
                            if (roomx.getUserList() == null) {
                                userList = new HashMap<>();
                            } else {
                                userList = roomx.getUserList();
                            }
                            userList.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), new Player(FirebaseAuth.getInstance().getCurrentUser().getUid(), "dummy", App.getFCMToken(RoomActivity.this)));
                            roomx.setUserList(userList);
                            roomx.setLastActivityTime(System.currentTimeMillis());

                            dbRef.child("Rooms").child(roomx.getId()).setValue(roomx).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        dbRef.child("Rooms").child(roomx.getId()).child("totalPlayersJoined").setValue(ServerValue.increment(1));
                                        fetchPlayers(roomx.getId());
                                        Toast.makeText(RoomActivity.this, "Player added to existing room", Toast.LENGTH_SHORT).show();
                                        addToHistory();
                                    } else {
                                        Toast.makeText(RoomActivity.this, "Unable to add to room", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            isBreak = true;
                            break;
                        }
                    }
                    if (!isBreak) {
                        //create new room and add this player to that room
                        DatabaseReference roomDbRef = dbRef.child("Rooms").push();
                        Room room = new Room();
                        room.setId(roomDbRef.getKey());
                        room.setStatus("active");
                        room.setLastActivityTime(System.currentTimeMillis());
                        currentRoomId = room.getId();
                        HashMap<String, Player> userList = new HashMap<>();
                        userList.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), new Player(FirebaseAuth.getInstance().getCurrentUser().getUid(), "dummy", App.getFCMToken(RoomActivity.this)));
                        room.setUserList(userList);
                        roomDbRef.setValue(room).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    fetchPlayers(currentRoomId);
                                    Toast.makeText(RoomActivity.this, "New room created and player added", Toast.LENGTH_SHORT).show();
                                    addToHistory();
                                } else {
                                    Toast.makeText(RoomActivity.this, "Unable to add to room", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RoomActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void addToHistory() {
        dbRef.child("Rooms").child(currentRoomId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentRoom = snapshot.getValue(Room.class);
                        DatabaseReference historyRef = dbRef.child("History")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child(currentRoomId);

                        History history = new History();
                        history.setId(historyRef.getKey());
                        history.setName(name);
                        history.setGame("10 - User Table");
                        history.setDateTime(currentRoom.getCreationTime());
                        history.setRewardEarned(0);

                        historyRef.setValue(history);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private boolean fetchPlayersCalled = false;

    ChildEventListener playerChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            progressBar.setVisibility(View.GONE);
            Player player = snapshot.getValue(Player.class);
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
                            playerList.add(user.getUsername());
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
            Player player = snapshot.getValue(Player.class);
            Log.i("mytag", "player removed:" + player);
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
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private void fetchPlayers(String roomId) {
        if (fetchPlayersCalled) {
            return;
        }
        Log.i("mytag", "inside fetchplayers");
        fetchPlayersCalled = true;
        progressBar.setVisibility(View.VISIBLE);
        dbRef.child("Rooms")
                .child(roomId)
                .child("userList")
                .addChildEventListener(playerChildEventListener);
        monitorGameRoom();
    }

    @Override
    protected void onStop() {
        if (currentRoomId != null) {
            dbRef.child("Rooms")
                    .child(currentRoomId)
                    .child("userList")
                    .removeEventListener(playerChildEventListener);

            dbRef.child("Rooms2").child(currentRoomId)
                    .removeEventListener(roomValueListener);
        }
        super.onStop();
    }

    private void startGame() {
        Log.i("mytag", "inside startGame");
        if (playerList.size() < 3) {
            return;
        }
        if (gameStarted) {
            return;
        }
        gameStarted = true;
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameStarted", true);
        //map.put("gameStartTime", System.currentTimeMillis());
        dbRef.child("Rooms")
                .child(currentRoomId)
                .updateChildren(map);
        startActivity(new Intent(RoomActivity.this, QuizActivity.class)
                .putExtra("game", "1")
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_room, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void removePlayerFromRoom() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef.child("Rooms").child(currentRoomId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Room currentRoom = snapshot.getValue(Room.class);
                        //ArrayList<String> currentUserList = currentRoom.getUserList();
                        /*int idxToRemove = -1;
                        for (String playerId : currentUserList) {
                            if (playerId.equals(currentUserId)) {
                                idxToRemove = playerId.indexOf(currentUserId);
                            }
                        }*/
                        dbRef.child("Rooms").child(currentRoomId)
                                .child("userList").child(currentUserId).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(RoomActivity.this, "Removed from Room successfully", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

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
        dbRef.child("Rooms").child(currentRoomId)
                .addValueEventListener(roomValueListener);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.i("mytag", "gameScheduleTime: " + gameScheduleTime);
            if (gameScheduleTime != 0) {
                long millis = gameScheduleTime - System.currentTimeMillis();
                //millis = 0;
                if (millis > 0) {
                    String hms = String.format(Locale.US, "%02dh:%02dm:%02ds", TimeUnit.MILLISECONDS.toHours(millis),
                            TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                            TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));

                    tvGameCountdown.setText("Game will start in " + hms);
                    tvGameCountdown.setVisibility(View.VISIBLE);
                    showGameScheduleTime();
                } else {
                    startGame();
                }
            }
        }
    };

    private void showGameScheduleTime() {
        Log.i("mytag", "inside showGameScheduleTime");
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 1000);
    }

}
