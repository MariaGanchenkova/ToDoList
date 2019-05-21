package ru.nsu.ganchenkova.todolist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "todo_list";
    private static final int DATABASE_VERSION = 1;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static String hashPassword(String password) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] messageDigest = md.digest(password.getBytes());
        BigInteger no = new BigInteger(1, messageDigest);

        StringBuilder hashPassword = new StringBuilder(no.toString(16));
        while (hashPassword.length() < 32)
            hashPassword.insert(0, "0");

        return hashPassword.toString();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "login TEXT UNIQUE, " +
                "password_hash TEXT" +
                ");");

        db.execSQL("CREATE TABLE tasks (" +
                "task_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "description TEXT, " +
                "date INTEGER, " +
                "done INTEGER, " +
                "FOREIGN KEY(user_id) REFERENCES users(user_id)" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void removeTask(int userId, int taskId) {
        getWritableDatabase().delete("tasks", "user_id = " + userId + " AND task_id = " + taskId, null);
    }

    public ToDoTask createTask(int userId, String description) {
        long creationTime = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("description", description);
        values.put("date", (int) (creationTime / 1000));
        values.put("done", 0);

        int taskId = (int) getWritableDatabase().insert("tasks", null, values);
        if (taskId == -1)
            return null;
        return new ToDoTask(taskId, userId, creationTime, description);
    }

    public void updateTask(ToDoTask task) {
        String where = "task_id = " + task.getTaskId() + " AND user_id = " + task.getUserId();
        ContentValues values = new ContentValues();
        values.put("description", task.getDescription());
        values.put("done", task.isDone());
        getWritableDatabase().update("tasks", values, where, null);
    }

    public int registerUser(String login, String password) {
        ContentValues values = new ContentValues();
        values.put("login", login);
        values.put("password_hash", hashPassword(password));
        return (int) getWritableDatabase().insert("users", null, values);
    }

    public List<ToDoTask> getAllUserTasks(int userId) {
        String[] fields = {"task_id", "description", "date", "done"};
        String selection = "user_id = " + userId;
        try (Cursor cursor = getReadableDatabase().query(
                "tasks",
                fields,
                selection,
                null,
                null,
                null,
                null)) {

            List<ToDoTask> tasks = new ArrayList<>();
            while (cursor.moveToNext()) {
                int taskId = cursor.getInt(0);
                String description = cursor.getString(1);
                long date = cursor.getInt(2) * 1000L;
                boolean done = cursor.getInt(3) != 0;

                ToDoTask task = new ToDoTask(taskId, userId, date, description);
                task.setDone(done);
                tasks.add(task);
            }

            return tasks;
        }
    }

    public int getUserId(String login) {
        String[] fields = {"user_id"};
        String selection = "login = ?";
        String[] args = {login};
        try (Cursor cursor = getReadableDatabase().query(
                "users",
                fields,
                selection,
                args,
                null,
                null,
                null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : -1;
        }
    }

    public boolean checkPassword(int userId, String password) {
        String hash = hashPassword(password);
        String[] fields = {"password_hash"};
        String selection = "user_id = " + userId;
        try (Cursor cursor = getReadableDatabase().query("users",
                fields,
                selection,
                null,
                null,
                null,
                null)) {
            return cursor.moveToFirst() && cursor.getString(0).equals(hash);
        }
    }

}
