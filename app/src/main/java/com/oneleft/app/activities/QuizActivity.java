package com.oneleft.app.activities;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.transition.TransitionManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.oneleft.app.R;
import com.oneleft.app.models.Answer;
import com.oneleft.app.models.Player;
import com.oneleft.app.models.Question;
import com.oneleft.app.models.User;

import java.util.ArrayList;
import java.util.Collections;

public class QuizActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private ProgressBar progressBarTime;
    //private RecyclerView rvQuestions;
    private TextView tvQuestionNumber;
    private TextView tvQuestionText;
    private TextView tvRemainingTime;
    private RadioButton rbOption1;
    private RadioButton rbOption2;
    private RadioButton rbOption3;
    private RadioButton rbOption4;
    private Button btnFinish;

    private ArrayList<Question> questionList;
    //private QuestionAdapter questionAdapter;

    private String currentRoomId;

    private DatabaseReference dbRef;

    private int currentQuestionNumber = -1;

    private CountDownTimer countDownTimer;

    private static String ROOM_NODE = "Rooms";
    private boolean isGameEnd = false;

    private ArrayList<User> playerList;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.questions));

        progressBar = findViewById(R.id.progressBar);
        progressBarTime = findViewById(R.id.progressBarTime);
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        tvRemainingTime = findViewById(R.id.tvRemainingTime);
        rbOption1 = findViewById(R.id.rbOption1);
        rbOption2 = findViewById(R.id.rbOption2);
        rbOption3 = findViewById(R.id.rbOption3);
        rbOption4 = findViewById(R.id.rbOption4);
        btnFinish = findViewById(R.id.btnFinish);
        radioGroup = findViewById(R.id.radioGroup);

        playerList = new ArrayList<>();

        currentRoomId = getIntent().getStringExtra("room_id");
        if (currentRoomId == null) {
            currentRoomId = "";
        }

        ROOM_NODE = getIntent().getStringExtra("game").equals("1") ? "Rooms" : "Rooms2";

        dbRef = FirebaseDatabase.getInstance().getReference();

        /*rvQuestions.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return super.canScrollVertically();
                //return false;
            }
        });*/

        questionList = new ArrayList<>();

        fetchQuestions();

        fetchPlayers(currentRoomId);
    }

    private void fetchQuestions() {
        dbRef.child("Mcqs")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        questionList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            for (DataSnapshot ds2 : ds.getChildren()) {
                                Question question = ds2.getValue(Question.class);
                                if (question != null) {
                                    question.setId(ds2.getKey());
                                    questionList.add(question);
                                }
                            }
                        }
                        Log.i("mytag", "size: " + questionList.size());
                        Collections.shuffle(questionList);
                        showNextQuestion();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showNextQuestion() {

        if (isGameEnd) {
            return;
        }
        if (currentQuestionNumber >= 0) {
            String selectedOption = "";
            if (rbOption1.isChecked()) {
                selectedOption = "option1";
            } else if (rbOption2.isChecked()) {
                selectedOption = "option2";
            } else if (rbOption3.isChecked()) {
                selectedOption = "option3";
            } else if (rbOption4.isChecked()) {
                selectedOption = "option4";
            }
            String correctOption = questionList.get(currentQuestionNumber).getCorrect_answer();
            if (selectedOption.isEmpty()) {
                //user did not answer the question
                isGameEnd = true;
                playWrongAnswerSound();
                removePlayerFromRoom(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    /*if (task.isSuccessful()) {
                        Toast.makeText(QuizActivity.this, "Removed from Room successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }*/
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Dialog dialog = new Dialog(QuizActivity.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                        dialog.setContentView(R.layout.dialog_game_end_wrong);
                        dialog.show();

                        ImageView ivClose = dialog.findViewById(R.id.ivClose);
                        Button btnOk = dialog.findViewById(R.id.btnOK);
                        TextView tvMessage = dialog.findViewById(R.id.tvMessage);

                        tvMessage.setText("Oops! You did not answer this question\nSorry, you are out of the game");

                        ivClose.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                finish();
                            }
                        });

                        btnOk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                finish();
                            }
                        });
                    }
                }, 1000);
                return;
            } else if (selectedOption.equals(correctOption)) {
                playRightAnswerSound();
            } else {
                isGameEnd = true;
                playWrongAnswerSound();
                removePlayerFromRoom(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    /*if (task.isSuccessful()) {
                        Toast.makeText(QuizActivity.this, "Removed from Room successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }*/
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Dialog dialog = new Dialog(QuizActivity.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                        dialog.setContentView(R.layout.dialog_game_end_wrong);
                        dialog.show();

                        ImageView ivClose = dialog.findViewById(R.id.ivClose);
                        Button btnOk = dialog.findViewById(R.id.btnOK);

                        ivClose.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                }, 500);
                            }
                        });

                        btnOk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                }, 500);
                            }
                        });
                    }
                }, 1000);
                return;
            }
        }

        currentQuestionNumber++;
        //Log.i("mytag", "lastanswer: " + lastQuestionAnswered + ", current:" + (currentQuestionNumber - 1));
        if (currentQuestionNumber >= questionList.size()) {
            tvRemainingTime.setVisibility(View.GONE);
            progressBarTime.setVisibility(View.GONE);
            startActivity(new Intent(QuizActivity.this, QuizResultActivity.class)
                    .putExtra("game", getIntent().getStringExtra("game"))
                    .putExtra("room_id", currentRoomId));
            finish();
            return;
        }
        TransitionManager.beginDelayedTransition((ViewGroup) tvQuestionNumber.getRootView());
        Question currentItem = questionList.get(currentQuestionNumber);
        tvQuestionNumber.setText(String.valueOf(currentQuestionNumber + 1));
        tvQuestionText.setText(currentItem.getQuestion());
        rbOption1.setText(currentItem.getOption1());
        rbOption2.setText(currentItem.getOption2());
        rbOption3.setText(currentItem.getOption3());
        rbOption4.setText(currentItem.getOption4());

        radioGroup.clearCheck();

        /*rbOption1.setChecked(false);
        rbOption2.setChecked(false);
        rbOption3.setChecked(false);
        rbOption4.setChecked(false);*/

        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean isChecked) {
                if (isChecked) {
                    int id = v.getId();
                    Log.i("mytag", "id:" + id);
                    String selectedOption;
                    if (id == R.id.rbOption1) {
                        selectedOption = "option1";
                    } else if (id == R.id.rbOption2) {
                        selectedOption = "option2";
                    } else if (id == R.id.rbOption3) {
                        selectedOption = "option3";
                    } else {
                        selectedOption = "option4";
                    }
                    String correctOption = questionList.get(currentQuestionNumber).getCorrect_answer();
                    Answer answer = new Answer();
                    answer.setQuestionId(questionList.get(currentQuestionNumber).getId());
                    answer.setSelectedOption(selectedOption);
                    answer.setCorrectOption(correctOption);

                    DatabaseReference answerRef = dbRef.child("Answers")
                            .child(currentRoomId)
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(questionList.get(currentQuestionNumber).getId());
                    //answer.setId(answerRef.getKey());
                    answerRef.setValue(answer)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //Toast.makeText(QuizActivity.this, "Answer saved for question: " + position, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(QuizActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        };

        rbOption1.setOnCheckedChangeListener(onCheckedChangeListener);
        rbOption2.setOnCheckedChangeListener(onCheckedChangeListener);
        rbOption3.setOnCheckedChangeListener(onCheckedChangeListener);
        rbOption4.setOnCheckedChangeListener(onCheckedChangeListener);

        /*rbOption1.setOnClickListener(onClickListener);
        rbOption2.setOnClickListener(onClickListener);
        rbOption3.setOnClickListener(onClickListener);
        rbOption4.setOnClickListener(onClickListener);*/
        progressBarTime.setMax(6000);
        if (countDownTimer == null) {
            countDownTimer = new CountDownTimer(6000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (isGameEnd) {
                        countDownTimer.cancel();
                    }
                    // will update the "progress" propriety of seekbar until it reaches progress
                    ObjectAnimator animation = ObjectAnimator.ofInt(progressBarTime, "progress", (int) millisUntilFinished);
                    animation.setDuration(1000); // 0.5 second
                    animation.setInterpolator(new DecelerateInterpolator());
                    animation.start();
                    //progressBarTime.setProgress((int) millisUntilFinished);
                    tvRemainingTime.setText((millisUntilFinished / 1000) + "s");
                }

                @Override
                public void onFinish() {
                    if (isGameEnd) {
                        return;
                    }
                    showNextQuestion();
                }
            };
        }
        countDownTimer.start();
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
                                removePlayerFromRoom(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(QuizActivity.this, "Removed from Room successfully", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(QuizActivity.this, "Something went wrong! Check your internet connection", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
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

    private void playRightAnswerSound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.ding);
        mediaPlayer.start();
    }

    private void playWrongAnswerSound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.buzzer);
        mediaPlayer.start();
    }

    private void removePlayerFromRoom(OnCompleteListener<Void> onCompleteListener) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef.child(ROOM_NODE).child(currentRoomId)
                .child("userList").child(currentUserId).removeValue()
                .addOnCompleteListener(onCompleteListener);
        /*dbRef.child(ROOM_NODE).child(currentRoomId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Room currentRoom = snapshot.getValue(Room.class);
                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        ArrayList<String> currentUserList = currentRoom.getUserList();
                        int idxToRemove = -1;
                        for (int i = 0; i < currentUserList.size(); i++) {
                            String playerId = currentUserList.get(i);
                            if (playerId == null) {
                                continue;
                            }
                            if (playerId.equals(currentUserId)) {
                                idxToRemove = i;
                                break;
                            }
                        }
                        if (idxToRemove != -1) {
                            currentUserList.remove(idxToRemove);
                            dbRef.child(ROOM_NODE).child(currentRoomId)
                                    .child("userList").child(currentUserId).removeValue()
                                    .addOnCompleteListener(onCompleteListener);
                        }
                        //ArrayList<String> userList = roomx.getUserList();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });*/
    }

    ChildEventListener childEventListener = new ChildEventListener() {
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
                            playerList.add(user);
                            Log.i("mytag", "quiz player list size: " + playerList.size());
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
            int idxToRemove = -1;
            for (int i = 0; i < playerList.size(); i++) {
                User u = playerList.get(i);
                if (u.getId().equals(player.getId())) {
                    idxToRemove = i;
                }
            }
            if (idxToRemove != -1) {
                playerList.remove(idxToRemove);
            }
            Toast.makeText(QuizActivity.this, "players:" + playerList.size(), Toast.LENGTH_SHORT).show();
            if (player.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                //Toast.makeText(QuizActivity.this, "current player removed", Toast.LENGTH_SHORT).show();
                //current player has been removed
                //finish();
                return;
            }
            if (playerList.size() == 1) {
                if (playerList.get(0).getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    isGameEnd = true;
                    //last player is remaining, just go for reward
                    startActivity(new Intent(QuizActivity.this, QuizResultActivity.class)
                            .putExtra("game", getIntent().getStringExtra("game"))
                            .putExtra("room_id", currentRoomId));
                    finish();
                }
            }

            /*dbRef.child("Users")
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
                            Toast.makeText(QuizActivity.this, "players:" + playerList.size(), Toast.LENGTH_SHORT).show();
                            if (user.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                //Toast.makeText(QuizActivity.this, "current player removed", Toast.LENGTH_SHORT).show();
                                //current player has been removed
                                //finish();
                                return;
                            }
                            if (playerList.size() == 1) {
                                if (playerList.get(0).equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    Toast.makeText(QuizActivity.this, "equal true", Toast.LENGTH_SHORT).show();
                                    isGameEnd = true;
                                    //last player is remaining, just go for reward
                                    startActivity(new Intent(QuizActivity.this, QuizResultActivity.class)
                                            .putExtra("game", getIntent().getStringExtra("game"))
                                            .putExtra("room_id", currentRoomId));
                                    finish();
                                } else {
                                    Toast.makeText(QuizActivity.this, "equal not true", Toast.LENGTH_SHORT).show();
                                }
                            }
                            Log.i("mytag", "player list size: " + playerList.size());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });*/
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private void fetchPlayers(String roomId) {
        progressBar.setVisibility(View.VISIBLE);
        dbRef.child(ROOM_NODE)
                .child(roomId)
                .child("userList")
                .addChildEventListener(childEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        dbRef.child(ROOM_NODE)
                .child(currentRoomId)
                .child("userList")
                .removeEventListener(childEventListener);
    }
}
