package varabe.marinasbusy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setTitle(getString(R.string.settings_label));
        populateList((ListView) findViewById(R.id.calendarListView));

    }
    void populateList(ListView calendarListView) {
        List<Calendar> calendars = CalendarQuery.getCalendars(this);
        String[] calendarTitles = new String[calendars.size()];
        for(int i = 0; i < calendars.size(); i++) {
            calendarTitles[i] = calendars.get(i).name;
        }
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, calendarTitles);
        calendarListView.setAdapter(adapter);
    }
}
