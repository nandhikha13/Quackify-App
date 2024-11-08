package com.example.quackify;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;

public class AddHabitActivity extends AppCompatActivity {

    private ConstraintLayout checkboxContainer;
    private Button addTaskButton;
    private EditText habitText;
    private int lastCheckboxId = View.generateViewId();
    Button homeButton;
    Button habitButton;
    Button reminderButton;
    private ArrayList<Habit> habits = new ArrayList<>();
    String fileName = "info.json", dataToWrite;
    private static String name = "";
    private static boolean completed = false;
    private static int numOfHabits = 0;
    private static int completedTasks = 0;
    FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);

        checkboxContainer = findViewById(R.id.checkboxContainer);
        addTaskButton = findViewById(R.id.addTaskButton);
        habitText = findViewById(R.id.habitEditText);
        homeButton = findViewById(R.id.homeButton3);
        habitButton = findViewById(R.id.habitButton3);
        reminderButton = findViewById(R.id.reminderButton3);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if(user == null)
        {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
        else
        {
            name = user.getEmail();
            getHabitData();
        }

        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewTask();
                numOfHabits++;
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        reminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ReminderActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void addNewTask() {
        String taskName = habitText.getText().toString();
        System.out.println("Nandhu:"+taskName);
        if (!taskName.isEmpty() && !taskName.contains("\n")) {
            Habit newHabit = new Habit(taskName, false);
            habits.add(newHabit);
            Log.d("Habits", String.valueOf(habits));
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(taskName);
            checkBox.setId(View.generateViewId());
            String userKey = name.replace(".", "_");


            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            DatabaseReference usersRef = database.child("users");

            String userId = usersRef.push().getKey();
            usersRef.child(userId).setValue(userKey);

            DatabaseReference habitsRef = FirebaseDatabase.getInstance().getReference().child("users").child(userKey).child("habits");

            String habitId = habitsRef.push().getKey();
            habitsRef.child(habitId).setValue(taskName);

            DatabaseReference completedRef = FirebaseDatabase.getInstance().getReference().child("users").child(userKey).child("habits").child(taskName).child("completed");

            completedRef.setValue(false);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    completedRef.setValue(isChecked);
                }
            });

            checkboxContainer.addView(checkBox);

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(checkboxContainer);

            if (lastCheckboxId == View.generateViewId()) {
                constraintSet.connect(checkBox.getId(), ConstraintSet.TOP, checkboxContainer.getId(), ConstraintSet.TOP);
            }

            else {
                constraintSet.connect(checkBox.getId(), ConstraintSet.TOP, lastCheckboxId, ConstraintSet.BOTTOM);
            }

            constraintSet.connect(checkBox.getId(), ConstraintSet.START, checkboxContainer.getId(), ConstraintSet.START);
            constraintSet.connect(checkBox.getId(), ConstraintSet.END, checkboxContainer.getId(), ConstraintSet.END);

            constraintSet.applyTo(checkboxContainer);

            lastCheckboxId = checkBox.getId();
            habitText.setText("");
        }
    }

    private void getHabitData() {
        String userKey = name.replace(".", "_");
        DatabaseReference habitsRef = FirebaseDatabase.getInstance().getReference().child("users").child(userKey).child("habits");

        habitsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                habits.clear();
                completedTasks = 0;
                for (DataSnapshot habitSnapshot : dataSnapshot.getChildren()) {
                    String habitName = habitSnapshot.getKey();
                    Boolean completed = habitSnapshot.child("completed").getValue(Boolean.class);

                    if (completed != null) {
                        boolean isCompleted = completed;
                        Habit habit = new Habit(habitName, isCompleted);
                        habits.add(habit);
                        if(isCompleted)
                            completedTasks++;
                    }
                }
                numOfHabits = habits.size();
                displayHabits();

                try {
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(fileName, Context.MODE_PRIVATE));
                    dataToWrite = completedTasks + " / " + numOfHabits;
                    outputStreamWriter.write(dataToWrite);
                    outputStreamWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void displayHabits() {
        checkboxContainer.removeAllViews();
        lastCheckboxId = View.generateViewId();

        for (Habit habit : habits) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(habit.getHabit());
            checkBox.setId(View.generateViewId());
            checkBox.setChecked(habit.isChecked());

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        habit.setCompleted(true);
                    }

                    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference usersRef = database.child("users");

                    String userKey = name.replace(".", "_");
                    String userId = usersRef.push().getKey();
                    usersRef.child(userId).setValue(userKey);

                    DatabaseReference habitsRef = FirebaseDatabase.getInstance().getReference().child("users").child(userKey).child("habits");

                    String habitId = habitsRef.push().getKey();
                    habitsRef.child(habitId).setValue(habit.getHabit());
                    DatabaseReference completedRef = FirebaseDatabase.getInstance().getReference().child("users").child(userKey).child("habits").child(habit.getHabit()).child("completed");

                    completedRef.setValue(isChecked);

                }
            });

            checkboxContainer.addView(checkBox);

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(checkboxContainer);

            if (lastCheckboxId == View.generateViewId()) {
                constraintSet.connect(checkBox.getId(), ConstraintSet.TOP, checkboxContainer.getId(), ConstraintSet.TOP);
            } else {
                constraintSet.connect(checkBox.getId(), ConstraintSet.TOP, lastCheckboxId, ConstraintSet.BOTTOM);
            }

            constraintSet.connect(checkBox.getId(), ConstraintSet.START, checkboxContainer.getId(), ConstraintSet.START);
            constraintSet.connect(checkBox.getId(), ConstraintSet.END, checkboxContainer.getId(), ConstraintSet.END);

            constraintSet.applyTo(checkboxContainer);

            lastCheckboxId = checkBox.getId();
        }
    }
}