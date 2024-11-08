package com.example.quackify;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Habit {
    private String habit;
    private boolean completed;

    public Habit() {
    }

    public Habit(String habit, boolean completed) {
        this.habit = habit;
        this.completed = completed;
    }

    public String getHabit() {
        return habit;
    }

    public boolean isChecked() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public JSONObject toJsonObject() {
        JSONObject jsonHabit = new JSONObject();
        try {
            jsonHabit.put("habit", habit);
            jsonHabit.put("completed", completed);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonHabit;
    }
}