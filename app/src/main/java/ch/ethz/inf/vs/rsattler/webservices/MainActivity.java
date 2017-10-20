package ch.ethz.inf.vs.rsattler.webservices;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void task1(View view) {
        Intent intent = new Intent(this, RestActivity.class);
        startActivity(intent);
    }

    //LS
    public void task2(View view) {
        Intent intent = new Intent(this, SoapActivity.class);
        startActivity(intent);
    }

    public void task3(View view) {
        Intent intent = new Intent(this, RestServerActivity.class);
        startActivity(intent);
    }
}
