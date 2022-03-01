package com.example.covid19tester;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import java.util.Objects;

public class AddEntry extends AppCompatActivity {

    private View mProgressView;
    private View mAddFormView;
    private TextView tvLoad;

    EditText etFullName, etID;
    SwitchCompat switchPos;
    Button btnAdd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.add_new_entry));

        mProgressView = findViewById(R.id.add_progress);
        tvLoad = findViewById(R.id.tvLoad);
        mAddFormView = findViewById(R.id.add_form);

        etFullName = findViewById(R.id.etFullName);
        etID = findViewById(R.id.etID);
        switchPos = findViewById(R.id.switchPos);
        btnAdd = findViewById(R.id.btnAdd);



        switchPos.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //checks if the switch position state is true
                if(isChecked)
                {
                    switchPos.setText(R.string.results_positive);

                }
                else
                {
                    switchPos.setText(R.string.pending_results);
                }
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check if the user has entered any data
                if(etFullName.getText().toString().isEmpty() || etID.getText().toString().isEmpty())
                {
                    Toast.makeText(AddEntry.this, getString(R.string.please_enter_full_name_and_id), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //checks if the switchPos is true
                    if(switchPos.isChecked())
                    {
                        CovidEntry covidEntry = new CovidEntry();

                        //sets the full name entered by the user to the variable 'etFullName'
                        covidEntry.setFullName(etFullName.getText().toString().trim());

                        //sets the id entered by the user to the variable 'etID'
                        covidEntry.setId(etID.getText().toString().trim());

                        //sets the positive to true
                        covidEntry.setPositive(true);


                        tvLoad.setText(getString(R.string.adding_new_entry_please_wait));
                        showProgress(true);

                        //gets all the data entered and sends it to Backendless saving it to the CovidEntry table
                        Backendless.Persistence.save(covidEntry, new AsyncCallback<CovidEntry>() {
                            @Override
                            public void handleResponse(CovidEntry response) {

                                Toast.makeText(AddEntry.this, getString(R.string.new_entry_successfully_saved), Toast.LENGTH_LONG).show();
                                etFullName.setText(null);
                                etID.setText(null);
                                switchPos.setText(R.string.pending_results);
                                Intent intent = new Intent();
                                intent.putExtra("fullName", response.getFullName().trim());
                                intent.putExtra("id", response.getId().trim());
                                intent.putExtra("positive", true);
                                setResult(RESULT_OK,intent);
                                showProgress(false);
                                AddEntry.this.finish();
                            }

                            //if was unable to save the hadleFault will display a message why it could not save
                            @Override
                            public void handleFault(BackendlessFault fault) {

                                Toast.makeText(AddEntry.this, getString(R.string.error) + fault.getMessage(), Toast.LENGTH_LONG).show();
                                showProgress(false);

                            }
                        });

                    }
                    else//if switchPost was set to false by the user
                    {
                        CovidEntry covidEntry = new CovidEntry();

                        //sets the full name entered by the user to the variable 'etFullName'
                        covidEntry.setFullName(etFullName.getText().toString().trim());

                        //sets the id entered by the user to the variable 'etID'
                        covidEntry.setId(etID.getText().toString().trim());

                        //sets the positive to true
                        covidEntry.setPositive(false);

                        tvLoad.setText(getString(R.string.adding_new_entry_please_wait));
                        showProgress(true);

                        //gets all the data entered and sends it to Backendless saving it to the CovidEntry table
                        Backendless.Persistence.save(covidEntry, new AsyncCallback<CovidEntry>() {
                            @Override
                            public void handleResponse(CovidEntry response) {

                                Toast.makeText(AddEntry.this, getString(R.string.new_entry_successfully_saved), Toast.LENGTH_LONG).show();
                                etFullName.setText(null);
                                etID.setText(null);
                                switchPos.setText(R.string.pending_results);
                                Intent intent = new Intent();
                                intent.putExtra("fullName", response.getFullName().trim());
                                intent.putExtra("id", response.getId().trim());
                                intent.putExtra("positive", false);
                                setResult(RESULT_OK,intent);
                                showProgress(false);
                                AddEntry.this.finish();
                            }

                            //if was unable to save the hadleFault will display a message why it could not save
                            @Override
                            public void handleFault(BackendlessFault fault) {

                                Toast.makeText(AddEntry.this, getString(R.string.error) + fault.getMessage(), Toast.LENGTH_LONG).show();
                                showProgress(false);
                            }
                        });

                    }
                }

            }
        });

    }


        /**
         * Shows the progress UI and hides the add entry form.
         */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mAddFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mAddFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mAddFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });

            tvLoad.setVisibility(show ? View.VISIBLE : View.GONE);
            tvLoad.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    tvLoad.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            tvLoad.setVisibility(show ? View.VISIBLE : View.GONE);
            mAddFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
