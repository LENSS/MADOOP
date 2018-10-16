package edu.tamu.lenss.distressnet_ng;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pl.sphelper.SPHelper;

import edu.tamu.lenss.R;
import edu.tamu.lenss.madoop4.DetailsActivity;
import edu.tamu.lenss.madoop4.Main2Activity;
import edu.tamu.lenss.util.IPToolBox;

public class HomeActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
//    static {
//        System.loadLibrary("native-lib");
//    }
    private static final int REQEUST_PERMISSION_MADOOP = 1;

    private Button btn_madoop;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btn_madoop = (Button) findViewById(R.id.button_Madoop);
        btn_madoop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {


                Intent intent = new Intent(HomeActivity.this, Main2Activity.class);

                //Pass the image title and url to DetailsActivity
//        intent.putExtra("left", screenLocation[0]).
//                putExtra("top", screenLocation[1]).
//                putExtra("width", imageView.getWidth()).
//                putExtra("height", imageView.getHeight()).
//                putExtra("title", item.getTitle()).
//                putExtra("image", item.getImage());

                //Start details activity
                startActivity(intent);
            }

        });

    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
//    public native String stringFromJNI();
}
