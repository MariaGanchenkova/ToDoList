package ru.nsu.ganchenkova.todolist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class ToDoListActivity extends AppCompatActivity {

    public static final String TASK_DESCRIPTION_INTENT = "task_description";
    public static final String TASK_ID_INTENT = "task_id";

    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance();
    private static final int REQUEST_EDIT_CODE = 1;

    private EditText editText;
    private Database database;
    private int userId;
    private List<ToDoTask> tasks;
    private ToDoListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list);

        userId = getIntent().getIntExtra(LoginActivity.USER_ID_INTENT, -1);
        if (userId == -1) {
            finish();
            return;
        }

        database = new Database(this);
        tasks = database.getAllUserTasks(userId);
        adapter = new ToDoListAdapter();

        editText = findViewById(R.id.newToDoInput);
        ListView toDoList = findViewById(R.id.toDoList);
        toDoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ToDoTask task = tasks.get(position);
                task.setDone(!task.isDone());
                database.updateTask(task);
                adapter.notifyDataSetChanged();
            }
        });
        toDoList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ToDoTask task = tasks.get(position);
                Intent intent = new Intent(ToDoListActivity.this, ToDoEditActivity.class);
                intent.putExtra(TASK_DESCRIPTION_INTENT, task.getDescription());
                intent.putExtra(TASK_ID_INTENT, task.getTaskId());
                startActivityForResult(intent, REQUEST_EDIT_CODE);
                return true;
            }
        });

        toDoList.setAdapter(adapter);
    }

    public void addButtonClick(View view) {
        String description = editText.getText().toString();
        if (description.isEmpty()) {
            Toast.makeText(this, R.string.emptyTaskDescription, Toast.LENGTH_LONG).show();
            return;
        }

        ToDoTask task = database.createTask(userId, description);
        if (task == null) {
            Toast.makeText(this, R.string.createTaskError, Toast.LENGTH_LONG).show();
            return;
        }
        editText.setText("");

        tasks.add(task);
        Toast.makeText(this, R.string.taskAdded, Toast.LENGTH_LONG).show();
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null || requestCode != REQUEST_EDIT_CODE || resultCode != RESULT_OK)
            return;

        int taskId = data.getIntExtra(TASK_ID_INTENT, -1);
        if (taskId == -1)
            return;

        if (data.getBooleanExtra(ToDoEditActivity.TASK_REMOVE_STATUS, false))
            removeTask(taskId);
        else {
            String description = data.getStringExtra(TASK_DESCRIPTION_INTENT);
            if (description == null)
                return;
            updateTask(taskId, description);
        }
    }

    private void removeTask(int taskId) {
        for (int i = 0; i < tasks.size(); ++i) {
            if (tasks.get(i).getTaskId() == taskId) {
                tasks.remove(i);
                database.removeTask(userId, taskId);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, R.string.taskRemoved, Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    private void updateTask(int taskId, String newDescription) {
        for (ToDoTask task : tasks)
            if (task.getTaskId() == taskId) {
                task.setDescription(newDescription);
                database.updateTask(task);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, R.string.taskUpdated, Toast.LENGTH_LONG).show();
            }
    }

    private class ToDoListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return tasks.size();
        }

        @Override
        public ToDoTask getItem(int position) {
            return tasks.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater)
                        ToDoListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            } else
                view = convertView;

            TextView description = view.findViewById(android.R.id.text1);
            TextView date = view.findViewById(android.R.id.text2);

            ToDoTask task = getItem(position);

            description.setText(task.getDescription());
            int flags = description.getPaintFlags();
            if (task.isDone())
                description.setPaintFlags(flags | Paint.STRIKE_THRU_TEXT_FLAG);
            else
                description.setPaintFlags(flags & ~Paint.STRIKE_THRU_TEXT_FLAG);
            date.setText(DATE_FORMAT.format(new Date(task.getCreationTime())));

            return view;
        }
    }
}
