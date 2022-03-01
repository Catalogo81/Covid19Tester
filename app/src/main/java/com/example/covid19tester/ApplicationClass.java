package com.example.covid19tester;

import android.app.Application;
import com.backendless.Backendless;
import java.util.List;

public class ApplicationClass extends Application
{
    public static final String APPLICATION_ID = "1FD764FE-C56C-3770-FF52-670240A06C00";
    public static final String API_KEY = "37EACFE2-6169-4B70-9D48-CB7D3805D37D";
    public static final String SERVER_URL = "https://api.backendless.com";

    //public static BackendlessUser user;
    public static List <CovidEntry> covidEntryList;


    @Override
    public void onCreate() {
        super.onCreate();

        Backendless.setUrl( SERVER_URL );
        Backendless.initApp( getApplicationContext(),
                APPLICATION_ID,
                API_KEY );
    }
}
