package ru.nsu.ganchenkova.todolist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    public static final String USER_ID_INTENT = "user_id";

    private Database database;
    private EditText loginField, passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        database = new Database(this);

        loginField = findViewById(R.id.emailInput);
        passwordField = findViewById(R.id.passwordInput);
    }

    public void loginButtonClick(View view) {
        String login = loginField.getText().toString();
        String password = passwordField.getText().toString();
        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.emptyLoginOrPassword, Toast.LENGTH_LONG).show();
            return;
        }

        int userId = database.getUserId(login);
        if (userId == -1 || !database.checkPassword(userId, password)) {
            Toast.makeText(this, R.string.wrongLogin, Toast.LENGTH_LONG).show();
            return;
        }
        loginField.setText("");
        passwordField.setText("");

        startListActivity(userId);
    }

    public void registerButtonClick(View view) {
        String login = loginField.getText().toString();
        String password = passwordField.getText().toString();
        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.emptyLoginOrPassword, Toast.LENGTH_LONG).show();
            return;
        }

        if (database.getUserId(login) != -1) {
            Toast.makeText(this, R.string.userAlreadyExists, Toast.LENGTH_LONG).show();
            return;
        }

        int userId = database.registerUser(login, password);
        if (userId == -1) {
            Toast.makeText(this, R.string.userCreateError, Toast.LENGTH_LONG).show();
            return;
        }
        loginField.setText("");
        passwordField.setText("");

        startListActivity(userId);
    }

    private void startListActivity(int userId) {
        Intent intent = new Intent(this, ToDoListActivity.class);
        intent.putExtra(USER_ID_INTENT, userId);
        startActivity(intent);
    }
}
