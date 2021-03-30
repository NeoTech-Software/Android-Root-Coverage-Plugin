package org.neotech.app;

import android.widget.Button;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

/**
 * Super simple Robolectric activity test that is solely used to verify whether coverage is
 * correctly reported when enabling jacoco.includeNoLocationClasses.
 */
@RunWith(RobolectricTestRunner.class)
public class RobolectricUnitTest {

    @Test
    public void counter_is_incremented_after_increment_button_click() {
        ActivityScenario.launch(RobolectricTestedActivity.class).onActivity(activity -> {
            Button button = activity.findViewById(R.id.button_increment_count);
            button.performClick();
            
            TextView textView = activity.findViewById(R.id.text_count);
            assertEquals("Count: 1", textView.getText());
        });
    }
    
    @Test
    public void counter_is_decrement_after_decrement_button_click() {
        ActivityScenario.launch(RobolectricTestedActivity.class).onActivity(activity -> {
            Button button = activity.findViewById(R.id.button_decrement_count);
            button.performClick();
            
            TextView textView = activity.findViewById(R.id.text_count);
            assertEquals("Count: -1", textView.getText());
        });
    }
}