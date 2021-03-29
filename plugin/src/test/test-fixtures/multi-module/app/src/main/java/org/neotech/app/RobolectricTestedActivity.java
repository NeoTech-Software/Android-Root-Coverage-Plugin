package org.neotech.app;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

/**
 * Super simple activity that is unit-tested (non-instrumented) using Robolectric.
 */
public class RobolectricTestedActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewCount;
    private int count = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robolectric_tested);

        findViewById(R.id.button_increment_count).setOnClickListener(this);
        findViewById(R.id.button_decrement_count).setOnClickListener(this);
        textViewCount = findViewById(R.id.text_count);
        setCount(0);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_increment_count) {
            setCount(++count);
        } else {
            setCount(--count);
        }
    }

    public void setCount(int count) {
        textViewCount.setText(String.format(Locale.getDefault(), "Count: %d", count));
    }
}
