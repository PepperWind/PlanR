package fr.planr.ppr.planr;

// <div>Icons made by <a href="http://www.freepik.com" title="Freepik">Freepik</a> from <a href="http://www.flaticon.com" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_PATH = "StoredTasks";

    public Button bt_date;
    public Button bt_yesterday;
    public Button bt_tomorrow;
    public Button bt_addTask;
    public EditText txt_addTask;

    private ListView lv;

    private int y;
    private int m;
    private int d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt_date = (Button)findViewById(R.id.BT_DATE);
        bt_yesterday = (Button) findViewById(R.id.BT_YESTERDAY);
        bt_tomorrow = (Button) findViewById(R.id.BT_TOMORROW);
        bt_addTask = (Button)findViewById(R.id.BT_ADDTASK);
        txt_addTask = (EditText) findViewById(R.id.TXT_ADDTASK);

        lv = (ListView) findViewById(R.id.LV_TASKS);

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH)+1;
        int day = c.get(Calendar.DAY_OF_MONTH);

        changeDate(year, month, day);

        bt_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });

        bt_addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Fermer le clavier soft

                View view = MainActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                if(txt_addTask.getText() != null && txt_addTask.getText().length() > 0) {
                    addTask(dateToString(y, m, d), txt_addTask.getText().toString());
                    txt_addTask.setText("");
                }
            }
        });

        bt_yesterday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                yesterday();
            }
        });

        bt_tomorrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tomorrow();
            }
        });

        // Fermer le clavier soft
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                View view = MainActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }, 100);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("year", y);
        outState.putInt("month", m);
        outState.putInt("day", d);

        Log.d("Flavien", "Save !");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH)+1;
        int day = c.get(Calendar.DAY_OF_MONTH);

        changeDate(savedInstanceState.getInt("year", year), savedInstanceState.getInt("month", month), savedInstanceState.getInt("day", day));

        Log.d("Flavien", "Load !");
    }

    public void changeDate(int year, int month, int day) {
        bt_date.setText(dateToString(year, month, day));

        y = year;
        m = month;
        d = day;

        Calendar c = Calendar.getInstance();
        c.set(y, m-1, d);

        //Toast.makeText(getApplicationContext(), ""+c.get(Calendar.DAY_OF_WEEK), Toast.LENGTH_SHORT).show();

        switch (c.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                setTitle("Lundi");
                break;
            case Calendar.TUESDAY:
                setTitle("Mardi");
                break;
            case Calendar.WEDNESDAY:
                setTitle("Mercredi");
                break;
            case Calendar.THURSDAY:
                setTitle("Jeudi");
                break;
            case Calendar.FRIDAY:
                setTitle("Vendredi");
                break;
            case Calendar.SATURDAY:
                setTitle("Samedi");
                break;
            default :
                setTitle("Dimanche");
        }


        refreshTasks();
    }

    public void tomorrow() {
        Calendar c = Calendar.getInstance();
        c.set(y, m-1, d);
        c.add(Calendar.DATE, 1);

        y = c.get(Calendar.YEAR);
        m = c.get(Calendar.MONTH)+1;
        d = c.get(Calendar.DAY_OF_MONTH);

        changeDate(y, m, d);
    }

    public void yesterday() {
        Calendar c = Calendar.getInstance();
        c.set(y, m-1, d);
        c.add(Calendar.DATE, -1);

        y = c.get(Calendar.YEAR);
        m = c.get(Calendar.MONTH)+1;
        d = c.get(Calendar.DAY_OF_MONTH);

        changeDate(y, m, d);
    }


    private void refreshTasks() {
        SharedPreferences prefs = getSharedPreferences(PREFS_PATH, MODE_PRIVATE);

        String raw_tasks = prefs.getString(dateToString(y, m, d), "");

        if(raw_tasks == null || raw_tasks == "") {
            lv.setAdapter(null);
            return;
        }

        StableArrayAdapter adap = new StableArrayAdapter(getApplicationContext(), R.layout.list_item, separateTasks(raw_tasks));
        lv.setAdapter(adap);

        Log.d("Flavien", raw_tasks);
    }

    public void addTask(String date, String task) {
        SharedPreferences prefs = getSharedPreferences(PREFS_PATH, MODE_PRIVATE);
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_PATH, MODE_PRIVATE).edit();

        editor.putString(date, prefs.getString(dateToString(y, m, d), "")+"#"+task);
        editor.commit();

        refreshTasks();

    }

    public void removeTask(String text) {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_PATH, MODE_PRIVATE);
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_PATH, MODE_PRIVATE).edit();

            editor.putString(bt_date.getText().toString(), removeFromTasks(prefs.getString(bt_date.getText().toString(), ""), text));
            editor.commit();

            refreshTasks();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showDatePickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.setActivity(this);
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public ArrayList<String> separateTasks(String s) {
        ArrayList<String> a = new ArrayList<String>();

        String sub = "";

        for(int i = 0; i < s.length(); i++) {
            if(s.charAt(i) == '#') {
                if(i == 0)
                    continue;
                if(sub != "")
                    a.add(sub);
                sub = "";
            }
            else
                sub += s.charAt(i);
        }

        if(sub != "" & sub != "#")
            a.add(sub);

        return a;
    }

    public String concatTasks(ArrayList<String> tasks) {
        if(tasks.isEmpty())
            return "";

        String s = "";

        for (String a : tasks) {
            s += a + "#";
        }

        return s;
    }

    public static String dateToString(int year, int month, int day) {
        String s = "";
        if(day < 10)
            s += "0";
        s += day+"/";
        if(month < 10)
            s += "0";
        s += month+"/";
        return s+year;
    }

    private String removeFromTasks(String original, String toRemove) {
        ArrayList<String> a = separateTasks(original);
        a.remove(toRemove);
        return concatTasks(a);
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        class ViewHolder {
            public TextView textView;
            public Button removeButton;
        }

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item, null);
                holder = new ViewHolder();
                holder.textView = (TextView) convertView.findViewById(R.id.TXT_TASK);
                holder.removeButton = (Button) convertView.findViewById(R.id.BT_REMOVETASK);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            final String task = getItem(position);
            holder.textView.setText(task);

            holder.removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.this.removeTask(task);
                }
            });

            return convertView;
        }
        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}

