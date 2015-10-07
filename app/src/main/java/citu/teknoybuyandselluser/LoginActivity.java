package citu.teknoybuyandselluser;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {
    public static final String MY_PREFS_NAME = "MyPreferences";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    private static final String TAG = "LoginActivity";

    private EditText mTxtUsername;
    private EditText mTxtPassword;
    private TextView mTxtSignUp;
    private TextView mTxtForgotPassword;
    private TextView mTxtErrorMessage;
    private ProgressDialog mLoginProgress;

    private String mStrUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mTxtUsername = (EditText) findViewById(R.id.txtUsername);
        mTxtPassword = (EditText) findViewById(R.id.txtPassword);

        mLoginProgress = new ProgressDialog(this);

        SharedPreferences sp = this.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        if(sp.getString(USERNAME, null) != null) {
            mTxtUsername.setText(sp.getString(USERNAME, null));
            mTxtPassword.setText(sp.getString(PASSWORD, null));
        }

        mTxtForgotPassword = (TextView) findViewById(R.id.txtForgotPassword);
        mTxtSignUp = (TextView) findViewById(R.id.txtSignup);
        mTxtErrorMessage = (TextView) findViewById(R.id.txtLoginErrorMessage);

        mTxtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(LoginActivity.this, ChangePasswordActivity.class);
                mTxtPassword.setText("");
                startActivity(intent);
            }
        });

        mTxtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(LoginActivity.this, MainActivity.class);
                mTxtPassword.setText("");
                startActivity(intent);
            }
        });
    }

    public void onLogin(View view){
        Map<String,String> data = new HashMap<>();
        mStrUsername = mTxtUsername.getText().toString();
        data.put(USERNAME, mStrUsername);
        data.put(PASSWORD, mTxtPassword.getText().toString());

        mLoginProgress.setIndeterminate(true);
        mLoginProgress.setMessage("Please wait. . .");

        Server.login(data, mLoginProgress, new Ajax.Callbacks() {
            @Override
            public void success(String responseBody) {
                Log.d(TAG, "LOGIN: " + responseBody);
                try {
                    JSONObject json = new JSONObject(responseBody);
                    String response = json.getString("statusText");
                    if(response.equals("Successful Login")) {
                        Server.getUser(mStrUsername, new Ajax.Callbacks() {
                            @Override
                            public void success(String responseBody) {
                                JSONArray jsonArray = null;
                                try {
                                    jsonArray = new JSONArray(responseBody);
                                    if(jsonArray.length() != 0) {
                                        JSONObject json = jsonArray.getJSONObject(0);
                                        JSONObject jsonUser = json.getJSONObject("student");

                                        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                                        editor.putString("username", mStrUsername);
                                        editor.putString("first_name", jsonUser.getString("first_name"));
                                        editor.putString("last_name", jsonUser.getString("last_name"));
                                        editor.putInt("stars_collected", json.getInt("stars_collected"));
                                        editor.apply();

                                        mTxtErrorMessage.setText("");

                                        Intent intent;
                                        intent = new Intent(LoginActivity.this, NotificationsActivity.class);
                                        finish();
                                        startActivity(intent);
                                    } else {
                                        mTxtPassword.setText("");
                                        mTxtErrorMessage.setText("User not found");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void error(int statusCode, String responseBody, String statusText) {
                                Log.d(TAG, "Error: " + statusCode + " " + responseBody);
                            }
                        });
                    } else {
                        //Toast.makeText(LoginActivity.this, response, Toast.LENGTH_SHORT).show();
                        mTxtErrorMessage.setText(response);
                        mTxtPassword.setText("");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void error(int statusCode, String responseBody, String statusText) {
                mTxtPassword.setText("");
                mTxtErrorMessage.setText("Cannot connect to server");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
