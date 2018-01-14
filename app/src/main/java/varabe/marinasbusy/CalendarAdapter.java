package varabe.marinasbusy;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static varabe.marinasbusy.MainActivity.D;
import static varabe.marinasbusy.MainActivity.TAG;

public class CalendarAdapter extends ArrayAdapter<List<Calendar>> {

    private SharedPreferences prefs;
    private List<Calendar> items;
    private Context context;

    CalendarAdapter(@NonNull Context context, int resource, List<Calendar> items, SharedPreferences prefs) {
        super(context, resource);
        this.context = context;
        this.items = items;
        this.prefs = prefs;
    }

    public int getCount() {
        return items.size();
    }
    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.calendar_list_item, null,true); //TODO: ViewHolder
        CheckBox box = row.findViewById(R.id.checkItem);
        Calendar cal = items.get(position);
        box.setText(cal.name);
        ColorStateList color = ColorStateList.valueOf(cal.color);
        box.setButtonTintList(color);
        box.setId(cal.id);
        HashMap<String, Boolean> calendarStates = (HashMap<String, Boolean>) prefs.getAll();
        box.setChecked(calendarStates.get(cal.id+""));
        return row;
    }
}
