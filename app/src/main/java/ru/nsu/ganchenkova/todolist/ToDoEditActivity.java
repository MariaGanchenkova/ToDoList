package ru.nsu.ganchenkova.todolist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ToDoEditActivity extends AppCompatActivity {

    public static final String TASK_REMOVE_STATUS = "need_remove";

    private EditText editText;
    private int taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_edit);

        String description = getIntent().getStringExtra(ToDoListActivity.TASK_DESCRIPTION_INTENT);
        taskId = getIntent().getIntExtra(ToDoListActivity.TASK_ID_INTENT, -1);
        if (description == null || taskId == -1) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        editText = findViewById(R.id.editToDo);
        editText.setText(description);
    }

    public void approveButtonClick(View view) {
        String description = editText.getText().toString();
        if (description.isEmpty()) {
            Toast.makeText(this, R.string.emptyTaskDescription, Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(ToDoListActivity.TASK_DESCRIPTION_INTENT, description);
        intent.putExtra(ToDoListActivity.TASK_ID_INTENT, taskId);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void removeButtonClick(View view) {
        Intent intent = new Intent();
        intent.putExtra(ToDoListActivity.TASK_ID_INTENT, taskId);
        intent.putExtra(TASK_REMOVE_STATUS, true);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void cancelButtonClick(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }
}
