package varabe.marinasbusy;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "Marinasbusy";
    static final boolean D = true; // D = Debug
    static final int REQUEST_READ_CALENDAR = 1;
    static final String CALENDAR_PREFERENCES = "calendars";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (D) Log.d(TAG, "Began execution");

        FloatingActionButton fab = findViewById(R.id.btnRefresh);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(D) Log.d(TAG, "User pressed btnRefreshStatus");
                refreshStatus();
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermission(Manifest.permission.READ_CALENDAR, REQUEST_READ_CALENDAR)) {
            checkSettings();
            refreshStatus();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_CALENDAR: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onResume();
                }
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnSettings:
                goToSettings();
                return true;
            default:
                return false;
        }
    }
    private boolean checkPermission(String permission, int requestCode) {
        if (hasPermission(permission))
            return true;
        else {
            ActivityCompat.requestPermissions(this, new String[] {permission}, requestCode);
            return false;
        }
    }
    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }
    private void goToSettings() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }
    private void refreshStatus() {
        String time, status;
        Event currentEvent = EventQuery.getCurrent(this);
        if (currentEvent == null) {
            status = getString(R.string.free_status);
            time = "";
        } else {
            status = getString(R.string.status_phrase) + " " + currentEvent.getTitle();
            time = currentEvent.getFormatTime();
        }
        setNewStatus(status, time);
    }
    private void setNewStatus(String status, String time) {
        TextView statusView = findViewById(R.id.textViewStatus);
        TextView timeView = findViewById(R.id.textViewTime);
        statusView.setText(status);
        timeView.setText(time);
    }
    private void checkSettings() {
        SharedPreferences prefs = getSharedPreferences(CALENDAR_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Map<String, ?> allPrefs = prefs.getAll();
        List<Calendar> calendars = CalendarQuery.getCalendars(this);
        enterNewCalendars(editor, allPrefs, calendars);
        removeNonExistingCalendars(editor, allPrefs, calendars);
        editor.apply();
    }
    private void enterNewCalendars(SharedPreferences.Editor editor, Map<String, ?> allPrefs, List<Calendar> calendars) {
        for(Calendar c: calendars) {
            if (!allPrefs.containsKey(c.id + ""))
                editor.putBoolean(c.id+"", true);
        }
    }
    private void removeNonExistingCalendars(SharedPreferences.Editor editor, Map<String, ?> allPrefs, List<Calendar> calendars) {
        // If a calendar was removed from the device, it should (but doesn't really have to) be removed from prefs
        ArrayList<Integer> calendarIds = new ArrayList<Integer>(); // Ids are gained for easier iteration
        for (Calendar c: calendars) {
            calendarIds.add(c.id);
        }
        for (String rawKey: allPrefs.keySet()) {
            int key = Integer.valueOf(rawKey);
            if (!calendarIds.contains(key))
                editor.remove(rawKey);
        }
    }
}