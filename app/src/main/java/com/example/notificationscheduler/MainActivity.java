package com.example.notificationscheduler;

import androidx.appcompat.app.AppCompatActivity;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int JOB_ID = 0;
    private JobScheduler jobScheduler;

    private SeekBar seekBar;
    private Switch deviceIdleSwitch;
    private Switch deviceChargingSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);

        final TextView seekBarProgress = (TextView)findViewById(R.id.seekBarProgress);
        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i > 0) {
                    seekBarProgress.setText(getString(R.string.seconds, i));
                }
                else {
                    seekBarProgress.setText(getString(R.string.not_set));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        deviceIdleSwitch = (Switch) findViewById(R.id.idleSwitch);
        deviceChargingSwitch = (Switch) findViewById(R.id.chargingSwitch);
    }

    public void scheduleJob(View view) {
        RadioGroup networkOptions = findViewById(R.id.networkOptions);
        int selectedNetworkId = networkOptions.getCheckedRadioButtonId();

        int selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;
        switch (selectedNetworkId) {
            case R.id.noNetwork:
                selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;
                break;
            case R.id.anyNetwork:
                selectedNetworkOption = JobInfo.NETWORK_TYPE_ANY;
                break;
            case R.id.wifiNetwork:
                selectedNetworkOption = JobInfo.NETWORK_TYPE_UNMETERED;
                break;
        }

        int seekBarProgress = seekBar.getProgress();

        ComponentName serviceName = new ComponentName(getPackageName(), NotificationJobService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceName)
            .setRequiredNetworkType(selectedNetworkOption)
            .setRequiresCharging(deviceChargingSwitch.isChecked())
            .setRequiresDeviceIdle(deviceIdleSwitch.isChecked());

        if (seekBarProgress > 0) {
            builder.setOverrideDeadline(seekBarProgress * 1000);
        }

        if (selectedNetworkOption != JobInfo.NETWORK_TYPE_NONE
            || seekBarProgress > 0
            || deviceIdleSwitch.isChecked()
            || deviceChargingSwitch.isChecked()) {
            JobInfo jobInfo = builder.build();

            jobScheduler.schedule(jobInfo);

            Toast.makeText(this, R.string.job_scheduled, Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, R.string.no_constraint_toast, Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelJobs(View view) {
        if (jobScheduler != null) {
            jobScheduler.cancelAll();
            Toast.makeText(this, R.string.jobs_canceled, Toast.LENGTH_SHORT).show();
        }
    }
}