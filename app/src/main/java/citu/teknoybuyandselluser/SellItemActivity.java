package citu.teknoybuyandselluser;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import citu.teknoybuyandselluser.models.ImageInfo;

public class SellItemActivity extends BaseActivity {

    private static final String TAG = "SellItemActivity";

    private EditText mTxtItem;
    private EditText mTxtDescription;
    private EditText mTxtPrice;
    private ProgressDialog mProgressDialog;

    private Button mBtnBrowse;
    private ImageView mImgPreview;
    private ProgressBar mProgressBar;
    ImageInfo mImgInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_item);
        setupUI();

        mTxtItem = (EditText) findViewById(R.id.inputItem);
        mTxtDescription = (EditText) findViewById(R.id.inputDescription);
        mTxtPrice = (EditText) findViewById(R.id.inputPrice);

        mProgressDialog = new ProgressDialog(this);

        mProgressBar = (ProgressBar) findViewById(R.id.progressUpload);
        mProgressBar.setVisibility(View.INVISIBLE);

        mBtnBrowse = (Button) findViewById(R.id.btnBrowse);
        mImgPreview =  (ImageView) findViewById(R.id.preview);
        mBtnBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
    }

    private void selectImage() {
        Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                Server.upload(picturePath, mProgressBar, new Ajax.Callbacks() {
                    @Override
                    public void success(String responseBody) {
                        Log.v(TAG, "successfully posted");
                        Log.v(TAG, responseBody);

                        JSONObject json = null;
                        try {
                            json = new JSONObject(responseBody);
                            ImageInfo image = new ImageInfo();
                            mImgInfo = image.getImageInfo(json);
                            Picasso.with(SellItemActivity.this)
                                    .load(mImgInfo.getLink())
                                    .placeholder(R.drawable.thumbsq_24dp)
                                    .into(mImgPreview);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void error(int statusCode, String responseBody, String statusText) {
                        Log.v(TAG, "Request error");
                    }

                });

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sell_item, menu);
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

    @Override
    public boolean checkItemClicked(MenuItem menuItem) {
        return menuItem.getItemId() != R.id.nav_sell_items;
    }

    public void onSell(View view) {
        Map<String, String> data = new HashMap<>();
        SharedPreferences prefs = getSharedPreferences(LoginActivity.MY_PREFS_NAME, Context.MODE_PRIVATE);
        String user = prefs.getString("username", "");

        data.put(Constants.OWNER, user);
        data.put(Constants.NAME, mTxtItem.getText().toString());
        data.put(Constants.DESCRIPTION, mTxtDescription.getText().toString());
        data.put(Constants.PRICE, mTxtPrice.getText().toString());
        data.put(Constants.IMAGE_URL, mImgInfo.getLink());

        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage("Please wait. . .");

        Server.sellItem(data, mProgressDialog, new Ajax.Callbacks() {
            @Override
            public void success(String responseBody) {
                JSONObject json = null;
                try {
                    json = new JSONObject(responseBody);
                    String response = json.getString("statusText");
                    Log.d(TAG, response);
                    Toast.makeText(SellItemActivity.this, response, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void error(int statusCode, String responseBody, String statusText) {
                Log.d(TAG, "Sell Item error " + statusCode + " " + responseBody + " " + statusText);
                Toast.makeText(SellItemActivity.this, "Sell Item ERROR: " + statusCode + " " + responseBody + " " + statusText, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
