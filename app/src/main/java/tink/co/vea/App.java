package tink.co.vea;

import android.app.Application;
import android.content.Context;

import tink.co.vea.ui.activity.MainActivity;

/**
 * Created by Tourdyiev Roman on 2019-12-10.
 */
public class App extends Application {

    private static Application instance;
    private static MainActivity activity;

    public static Application getInstance() {
        return instance;
    }

    public static void setActivity(MainActivity mainActivity) {
        activity = mainActivity;
    }

    public static MainActivity getActivity() {
        return activity;
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
