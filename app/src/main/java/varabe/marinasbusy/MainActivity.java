package varabe.marinasbusy;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    final int REQUEST_READ_CALENDAR = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!hasPermission(Manifest.permission.READ_CALENDAR)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CALENDAR}, REQUEST_READ_CALENDAR);
        } else {
            refreshStatus();
        }
    }
    public boolean hasPermission(String permission) {
        return (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_CALENDAR: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
        if (hasPermission(Manifest.permission.READ_CALENDAR)) {
            refreshStatus();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.btnConnectCalendar:
//                connectCalendar(findViewById(R.id.btnConnectCalendar));
                return true;
            default:
                return false;
        }
    }
    private void refreshStatus() {
        String status;
        TextView statusView;
        Event currentEvent = Event.getCurrent(this);
        statusView = findViewById(R.id.textViewStatus);
        if (currentEvent == null) {
            status = getString(R.string.free_status);
        } else {
            status = getString(R.string.status_phrase) + " " + currentEvent.title;
        }
        statusView.setText(status);
    }
}