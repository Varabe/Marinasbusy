package varabe.marinasbusy;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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
    }
    @Override
    protected void onResume() {
        super.onResume();
        checkSettings();
        refreshStatus();
    }
    public boolean hasPermission(String permission) {
        return (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_CALENDAR: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    refreshStatus();
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
    public void btnRefreshStatus(View v) {
        if(D) Log.d(TAG, "User pressed btnRefreshStatus");
        refreshStatus();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.btnSettings:
                goToSettings();
                return true;
            default:
                return false;
        }
    }
    private void goToSettings() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }
    private void refreshStatus() {
        if (hasPermission(Manifest.permission.READ_CALENDAR)) {
            String status;
            String formatTime;
            Event currentEvent = EventQuery.getCurrent(this);
            TextView statusView = findViewById(R.id.textViewStatus);
            TextView timeView = findViewById(R.id.textViewTime);
            if (currentEvent == null) {
                status = getString(R.string.free_status);
                formatTime = "";
            } else {
                status = getString(R.string.status_phrase) + " " + currentEvent.getTitle();
                formatTime = currentEvent.getFormatTime();
            }
            statusView.setText(status);
            timeView.setText(formatTime);
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.READ_CALENDAR},
                    REQUEST_READ_CALENDAR);
        }
    }
    private void checkSettings() {
        SharedPreferences prefs = getSharedPreferences(CALENDAR_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Map allPrefs = prefs.getAll();
        List<Calendar> calendars = CalendarQuery.getCalendars(this);
        for(Calendar c: calendars) {
            if (!allPrefs.containsKey(c.id + ""))
                editor.putBoolean(c.id+"", true);
        }
        editor.apply();
    }
}