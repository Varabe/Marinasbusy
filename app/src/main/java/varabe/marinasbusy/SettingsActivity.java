package varabe.marinasbusy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import java.util.List;

import static varabe.marinasbusy.MainActivity.CALENDAR_PREFERENCES;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setActionBarTitle(getString(R.string.settings_label));
        populateList((ListView) findViewById(R.id.calendarListView));
    }
    void setActionBarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(title);
    }
    void populateList(ListView calendarListView) {
        List<CalendarData> calendars = CalendarQuery.getCalendars(this);
        SharedPreferences prefs = getSharedPreferences(CALENDAR_PREFERENCES, Context.MODE_PRIVATE);
        ArrayAdapter adapter = new CalendarAdapter(this, 0, calendars, prefs);
        calendarListView.setAdapter(adapter);
    }
    public void onCheckBoxClicked(View view) {
        CheckBox box = (CheckBox) view;
        SharedPreferences prefs = getSharedPreferences(CALENDAR_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(box.getId()+"", box.isChecked());
        editor.apply();
    }
}