package com.brauer.android.yofyz.options;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.brauer.android.yofyz.MainActivity;
import com.brauer.android.yofyz.R;
import com.brauer.android.yofyz.YogaClassAdapter;
import com.brauer.android.yofyz.db.Database;
import com.brauer.android.yofyz.db.YogaDbItem;
import com.brauer.android.yofyz.service.BackgroundService;
import com.brauer.android.yofyz.service.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OptionsActivity extends Activity implements AdapterView.OnItemSelectedListener {

    private TextView serviceStatus;

    private EditText editUrl;
    private Spinner instructorSpinner;
    private Switch serviceToggle;

    private Button changeButton;
    private Button clearButton;

    private Options options;

    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        database = Database.getInstance(getApplicationContext());

        serviceStatus = findViewById(R.id.options_service_status);

        instructorSpinner = (Spinner) findViewById(R.id.options_spinner_instructor);

        changeButton = findViewById(R.id.options_changed_button);
        changeButton.setOnClickListener(view -> {
            options.writeDown();
            Toast.makeText(this, "Settings saved.", Toast.LENGTH_SHORT).show();
        });

        clearButton = findViewById(R.id.options_clear_db);
        clearButton.setOnClickListener(view -> {
            Database.getInstance(getApplicationContext()).getDatabase().optionsItemDao().deleteAll();
            Toast.makeText(this, "Settings cleared.", Toast.LENGTH_SHORT).show();
        });

//        debugTextView = findViewById(R.id.options_title);

        options = Options.fromDb(getApplicationContext());
        if (options.getOptions().isEmpty()) {
            setupSpinner(null);
        } else {
            setupSpinner(options.getOption(OptionsKey.INSTRUCTOR));
        }

        editUrl = findViewById(R.id.options_url_edit);

        serviceToggle = findViewById(R.id.options_tservice_toggle);

        boolean serviceIsRunning = Options.fromDb(getApplicationContext())
                .isServiceRunning();
        serviceToggle.setOnCheckedChangeListener((button, checked) -> {
            editUrl.setEnabled(checked);
            instructorSpinner.setEnabled(checked);
            changeButton.setEnabled(checked);
            clearButton.setEnabled(checked);
            options.writeDown();
            toggleService(checked);
        });

//        serviceToggle.toggle();
        serviceToggle.setChecked(serviceIsRunning);
    }

    private void toggleService(boolean isOn) {
        Intent serviceIntent = new Intent(getApplicationContext(), BackgroundService.class);
        boolean isRunning = MainActivity.isMyServiceRunning(BackgroundService.class, (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        if (isOn) {
            if (!isRunning) {
                startForegroundService(serviceIntent);
            }
            serviceStatus.setText(Status.RUNNING.getValueAsString());
            serviceStatus.setTextColor(YogaClassAdapter.COLOR_GREEN);
            options.putOption(OptionsKey.ENABLED, new Options.ServiceRunning(true));
        } else {
            if (isRunning) {
                stopService(serviceIntent);
            }
            serviceStatus.setText(Status.STOPPED.getValueAsString());
            serviceStatus.setTextColor(YogaClassAdapter.COLOR_RED);
            options.putOption(OptionsKey.ENABLED, new Options.ServiceRunning(false));
        }
        options.writeDown();
    }

    private void setupSpinner(Options.Option option) {

        Set<String> instructors;
        String selected;

        if (option == null) {
            instructors = Database.getInstance(getApplicationContext())
                    .getDatabase()
                    .yogaClassDao()
                    .getAll()
                    .stream()
                    .map(YogaDbItem::getInstructor)
                    .collect(Collectors.toSet());
            selected = Options.NO_FILTER;
        } else {
            instructors = option.getPossibilities();
            selected = option.getValue();
        }

        instructors.add(selected);
        List<String> instructorsList = new ArrayList<>(instructors);
        int selectedPosition = instructorsList.indexOf(selected);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, instructorsList);

        instructorSpinner.setAdapter(adapter);
        instructorSpinner.setVisibility(View.VISIBLE);

        instructorSpinner.setOnItemSelectedListener(this);
        instructorSpinner.setSelection(selectedPosition);

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String selected = (String) adapterView.getItemAtPosition(i);

        List<String> possibilities = new ArrayList<>();
        for (int j = 0; j < adapterView.getCount(); j++) {
            String value = (String) adapterView.getItemAtPosition(j);
            possibilities.add(value);
        }

        options.putOption(OptionsKey.INSTRUCTOR, new Options.Instructor(selected, possibilities));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
