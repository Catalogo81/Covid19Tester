package com.example.covid19tester;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements CovidAdapter.ItemClicked{

    private View mProgressView;
    private View mDashboardFormView;
    private TextView tvRefresh;

    RecyclerView rvList;
    RecyclerView.Adapter myAdapter;
    RecyclerView.LayoutManager layoutManager;

    TextView tvPositive, tvPending;
    Button btnAdd, btnRefresh;

    List<CovidEntry> covidEntryList;

    final int ADD_ENTRY_ACTIVITY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.welcome));

        mProgressView = findViewById(R.id.main_progress);
        tvRefresh = findViewById(R.id.tvRefresh);
        mDashboardFormView = findViewById(R.id.dashboard_form);

        rvList = findViewById(R.id.rvList);
        rvList.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        rvList.setLayoutManager(layoutManager);

        tvPositive = findViewById(R.id.tvPositive);
        tvPending = findViewById(R.id.tvPending);
        btnAdd = findViewById(R.id.btnAdd);
        btnRefresh = findViewById(R.id.btnRefresh);


        //This button will navigate us to the AddEntry activity sending also a unique request code
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, AddEntry.class);
                startActivityForResult(intent, ADD_ENTRY_ACTIVITY);
            }
        });

        //This button will refresh the data on the main activity list showing the updated values
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showProgress(true);//shows the progress bar and getting the Backendless data back
                tvRefresh.setText(getString(R.string.refreshing_data_please_wait));
                refreshData();//calls the refreshData method
            }
        });

    }//end onCreate()


    //This method accepts a result from the previous activity AddEntry getting all the data passed
    //and the unique result code if it is true(get the data) or false(show no data)
    //and also a request which was set as ADD_ENTRY_ACTIVITY = 1
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_ENTRY_ACTIVITY)
        {
            if (resultCode == RESULT_OK)
            {
                refreshData();//calls the refreshData method
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, getString(R.string.no_entry_added), Toast.LENGTH_SHORT).show();
            }
        }
    }//end onActivityResult()

    //refreshData method will be reused in the Refresh button in the onCreate() method
    //this method will first show the progress before and go to Backendless to read the table CovidEnty data
    //then show it but also update the tvPending and tvPositive counts by using a whereClause
    //then it will show all the data from the CovidEntry table as a list in the Main Activity
    public void refreshData()
     {
        try
        {
            DataQueryBuilder queryBuilder = DataQueryBuilder.create();
            queryBuilder.setRelated();
            queryBuilder.setPageSize(100).setOffset(0);
            queryBuilder.setGroupBy("fullName");

            //Going in the Background and getting all the CovidEnty list entries
            Backendless.Persistence.of(CovidEntry.class).find(queryBuilder, new AsyncCallback<List<CovidEntry>>() {
                @Override
                public void handleResponse(List<CovidEntry> response) {

                    covidEntryList = response;

                    ApplicationClass.covidEntryList = response;//sets the Application.covidEntryList to the response
                    myAdapter = new CovidAdapter(MainActivity.this, ApplicationClass.covidEntryList);
                    rvList.setAdapter(myAdapter);//sets myAdapter to the covidEntryList
                    showProgress(false);

                    DataQueryBuilder dataQueryBuilderPending = DataQueryBuilder.create();
                    DataQueryBuilder dataQueryBuilderPositive = DataQueryBuilder.create();

                    //setWhereClause for dataQueryBuilderPending to search for data in the Backendless table where positive = FALSE
                    dataQueryBuilderPending.setWhereClause("positive = FALSE");

                    //setWhereClause for dataQueryBuilderPending to search for data in the Backendless table where positive = TRUE
                    dataQueryBuilderPositive.setWhereClause("positive = TRUE");

                    dataQueryBuilderPending.setPageSize(100).setOffset(0);
                    dataQueryBuilderPositive.setPageSize(100).setOffset(0);

                    //Goes in the Backendless CovidEntry table and gets all the number of positive values which are equals to 'FALSE'
                    Backendless.Data.of(CovidEntry.class).getObjectCount(dataQueryBuilderPending, new AsyncCallback<Integer>() {
                        @Override
                        public void handleResponse(Integer response) {
                            tvPending.setText(String.valueOf(response));//sets the count value of positive = FALSE to tvPending converting it the string

                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {

                        }
                    });

                    //Goes in the Backendless CovidEntry table and gets all the number of positive values which are equals to 'TRUE'
                    Backendless.Data.of(CovidEntry.class).getObjectCount(dataQueryBuilderPositive, new AsyncCallback<Integer>() {
                        @Override
                        public void handleResponse(Integer response) {
                            tvPositive.setText(String.valueOf(response));//sets the count value of positive = TRUE to tvPositive converting it the string
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {

                        }
                    });

                }

                @Override
                public void handleFault(BackendlessFault fault) {

                    Toast.makeText(MainActivity.this, getString(R.string.error) + fault.getMessage(), Toast.LENGTH_SHORT).show();
                    showProgress(false);
                }
            });
        }
        catch (Exception e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }//end refreshData()


    /**
     * Shows the progress UI and hides the dashboard form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mDashboardFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mDashboardFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mDashboardFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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

            tvRefresh.setVisibility(show ? View.VISIBLE : View.GONE);
            tvRefresh.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    tvRefresh.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            tvRefresh.setVisibility(show ? View.VISIBLE : View.GONE);
            mDashboardFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }//end showProgress()


    @Override
    public void onItemClicked(final int index)
    {
        try
        {

            DataQueryBuilder queryBuilder = DataQueryBuilder.create();//new queryBiulder
            queryBuilder.setRelated();
            queryBuilder.setPageSize(100).setOffset(0);
            queryBuilder.setGroupBy("fullName");//groups the list by fullName

            //Going in the Background and getting all the CovidEnty list entries
            Backendless.Persistence.of(CovidEntry.class).find(queryBuilder, new AsyncCallback<List<CovidEntry>>() {
                @Override
                public void handleResponse(List<CovidEntry> response) {

                    covidEntryList = response;

                    ApplicationClass.covidEntryList = response;
                    myAdapter = new CovidAdapter(MainActivity.this, ApplicationClass.covidEntryList);
                    rvList.setAdapter(myAdapter);

                    //checks if the item clicked positive value = "TRUE" then delete the item also on the Backendless CovidEntry table
                    if (covidEntryList.get(index).isPositive())
                    {
                        Toast.makeText(MainActivity.this, "deleting entry result", Toast.LENGTH_SHORT).show();

                        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
                        queryBuilder.setRelated();
                        queryBuilder.setPageSize(100).setOffset(0);
                        queryBuilder.setGroupBy("fullName");

                        //Goes in the Backendless CovidEntry table to delete the item clicked
                        Backendless.Persistence.of(CovidEntry.class).remove(covidEntryList.get(index), new AsyncCallback<Long>() {
                            @Override
                            public void handleResponse(Long response) {
                                Toast.makeText(MainActivity.this, "Successfully deleted!", Toast.LENGTH_SHORT).show();

                                DataQueryBuilder queryBuilder = DataQueryBuilder.create();
                                queryBuilder.setRelated();
                                queryBuilder.setPageSize(100).setOffset(0);
                                queryBuilder.setGroupBy("fullName");

                                //Gets the data from the Backendless CovidEntry table and updates my list in the main activity with the items left in the database
                                Backendless.Persistence.of(CovidEntry.class).find(queryBuilder, new AsyncCallback<List<CovidEntry>>() {
                                    @Override
                                    public void handleResponse(List<CovidEntry> response) {

                                        covidEntryList = response;

                                        ApplicationClass.covidEntryList = response;
                                        myAdapter = new CovidAdapter(MainActivity.this, ApplicationClass.covidEntryList);
                                        rvList.setAdapter(myAdapter);
                                        showProgress(false);

                                        DataQueryBuilder dataQueryBuilderPending = DataQueryBuilder.create();
                                        DataQueryBuilder dataQueryBuilderPositive = DataQueryBuilder.create();

                                        //setWhereClause for dataQueryBuilderPending to search for data in the Backendless table where positive = FALSE
                                        dataQueryBuilderPending.setWhereClause("positive = FALSE");

                                        //setWhereClause for dataQueryBuilderPending to search for data in the Backendless table where positive = TRUE
                                        dataQueryBuilderPositive.setWhereClause("positive = TRUE");

                                        dataQueryBuilderPending.setPageSize(100).setOffset(0);
                                        dataQueryBuilderPositive.setPageSize(100).setOffset(0);

                                        //Goes in the Backendless CovidEntry table and gets all the number of positive values which are equals to 'FALSE'
                                        Backendless.Data.of(CovidEntry.class).getObjectCount(dataQueryBuilderPending, new AsyncCallback<Integer>() {
                                            @Override
                                            public void handleResponse(Integer response) {
                                                tvPending.setText(String.valueOf(response));//sets the count value of positive = FALSE to tvPending converting it the string

                                            }

                                            @Override
                                            public void handleFault(BackendlessFault fault) {

                                            }
                                        });

                                        //Goes in the Backendless CovidEntry table and gets all the number of positive values which are equals to 'TRUE'
                                        Backendless.Data.of(CovidEntry.class).getObjectCount(dataQueryBuilderPositive, new AsyncCallback<Integer>() {
                                            @Override
                                            public void handleResponse(Integer response) {
                                                tvPositive.setText(String.valueOf(response));//sets the count value of positive = TRUE to tvPositive converting it the string
                                            }

                                            @Override
                                            public void handleFault(BackendlessFault fault) {

                                            }
                                        });

                                    }

                                    @Override
                                    public void handleFault(BackendlessFault fault) {

                                        Toast.makeText(MainActivity.this, getString(R.string.error) + fault.getMessage(), Toast.LENGTH_SHORT).show();
                                        showProgress(false);
                                    }
                                });

                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                Toast.makeText(MainActivity.this, getString(R.string.error) + fault.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else//checks if the item clicked positive value = "FALSE" then it will just change
                    // it to TRUE and udate it on the Backendless CovidEntry table
                    {
                        Toast.makeText(MainActivity.this, getString(R.string.changing_data_please_wait), Toast.LENGTH_SHORT).show();

                        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
                        queryBuilder.setRelated();
                        queryBuilder.setPageSize(100).setOffset(0);
                        queryBuilder.setGroupBy("fullName");

                        //Method Name: Backendless.Persistence.of(CovidEntry.class).find(queryBuilder, new AsyncCallback<List<CovidEntry>>()
                        //Usage: Changes the positive status to 'true', and displays it in the RecyclerView
                        Backendless.Persistence.of(CovidEntry.class).find(queryBuilder, new AsyncCallback<List<CovidEntry>>() {
                            @Override
                            public void handleResponse(List<CovidEntry> response) {

                                ApplicationClass.covidEntryList = response;
                                response.get(index).setPositive(true);
                                myAdapter = new CovidAdapter(MainActivity.this, ApplicationClass.covidEntryList);
                                rvList.setAdapter(myAdapter);


                                //Method Name: Backendless.Persistence.save(response.get(index), new AsyncCallback<CovidEntry>()
                                //Usage: Saves the positive status to Backendless CovidEntry table
                                Backendless.Persistence.save(response.get(index), new AsyncCallback<CovidEntry>() {
                                    @Override
                                    public void handleResponse(CovidEntry response) {
                                        Toast.makeText(MainActivity.this, "Data changed and updated", Toast.LENGTH_SHORT).show();

                                        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
                                        queryBuilder.setRelated();
                                        queryBuilder.setPageSize(100).setOffset(0);
                                        queryBuilder.setGroupBy("fullName");

                                        Backendless.Persistence.of(CovidEntry.class).find(queryBuilder, new AsyncCallback<List<CovidEntry>>() {
                                            @Override
                                            public void handleResponse(List<CovidEntry> response) {

                                                covidEntryList = response;

                                                ApplicationClass.covidEntryList = response;
                                                myAdapter = new CovidAdapter(MainActivity.this, ApplicationClass.covidEntryList);
                                                rvList.setAdapter(myAdapter);
                                                showProgress(false);

                                                DataQueryBuilder dataQueryBuilderPending = DataQueryBuilder.create();
                                                DataQueryBuilder dataQueryBuilderPositive = DataQueryBuilder.create();

                                                //setWhereClause for dataQueryBuilderPending to search for data in the Backendless table where positive = FALSE
                                                dataQueryBuilderPending.setWhereClause("positive = FALSE");

                                                //setWhereClause for dataQueryBuilderPending to search for data in the Backendless table where positive = TRUE
                                                dataQueryBuilderPositive.setWhereClause("positive = TRUE");

                                                dataQueryBuilderPending.setPageSize(100).setOffset(0);
                                                dataQueryBuilderPositive.setPageSize(100).setOffset(0);

                                                //Goes in the Backendless CovidEntry table and gets all the number of positive values which are equals to 'FALSE'
                                                Backendless.Data.of(CovidEntry.class).getObjectCount(dataQueryBuilderPending, new AsyncCallback<Integer>() {
                                                    @Override
                                                    public void handleResponse(Integer response) {
                                                        tvPending.setText(String.valueOf(response));//sets the count value of positive = FALSE to tvPending converting it the string

                                                    }

                                                    @Override
                                                    public void handleFault(BackendlessFault fault) {

                                                    }
                                                });

                                                Backendless.Data.of(CovidEntry.class).getObjectCount(dataQueryBuilderPositive, new AsyncCallback<Integer>() {
                                                    @Override
                                                    public void handleResponse(Integer response) {
                                                        tvPositive.setText(String.valueOf(response));//sets the count value of positive = TRUE to tvPositive converting it the string
                                                    }

                                                    @Override
                                                    public void handleFault(BackendlessFault fault) {

                                                    }
                                                });

                                            }

                                            @Override
                                            public void handleFault(BackendlessFault fault) {

                                                Toast.makeText(MainActivity.this, getString(R.string.error) + fault.getMessage(), Toast.LENGTH_SHORT).show();
                                                showProgress(false);
                                            }
                                        });

                                    }

                                    @Override
                                    public void handleFault(BackendlessFault fault) {

                                        Toast.makeText(MainActivity.this, getString(R.string.error) + fault.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {

                                Toast.makeText(MainActivity.this, getString(R.string.error) + fault.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }

                @Override
                public void handleFault(BackendlessFault fault) {

                }
            });

        }
        catch (Exception e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }//end showProgress()

}//end class MainActivity

