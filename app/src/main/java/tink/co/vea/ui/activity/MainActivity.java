package tink.co.vea.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tink.co.vea.App;
import tink.co.vea.R;
import tink.co.vea.ui.adapter.VideoAdapter;
import tink.co.vea.ui.fragment.EditorFragment;
import tink.co.vea.util.Util;
import tink.co.vea.util.VideoListCallback;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static androidx.recyclerview.widget.RecyclerView.VERTICAL;
import static tink.co.vea.Config.REQUEST_PERMISSIONS_CODE;
import static tink.co.vea.Config.VIDEO_PATH;

public class MainActivity extends AppCompatActivity implements VideoListCallback {

    private Util util;
    private VideoAdapter videoAdapter;

    private RecyclerView recyclerView;
    private ProgressBar progressbar;
    private TextView error;

    private List<String> videos = new ArrayList<>();

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            initApp();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.setActivity(this);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(App.getContext(), WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            initApp();
        } else {
            requestPermission();
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS}, REQUEST_PERMISSIONS_CODE);
    }

    private void initApp() {
        util = Util.getInstance();
        findViews();
        initViews();
        util.setVideoListCallback(this);
        util.getAllVideos();
    }

    private void findViews() {
        recyclerView = findViewById(R.id.recycler_view);
        progressbar = findViewById(R.id.progressbar);
        error = findViewById(R.id.error);
    }

    private void initViews() {
        error.setVisibility(View.GONE);
        progressbar.setVisibility(View.VISIBLE);

        videos.clear();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        videoAdapter = new VideoAdapter(videos);
        recyclerView.setAdapter(videoAdapter);
    }

    @Override
    public void onVideoListReady(List<String> videos) {
        this.videos.clear();
        this.videos.addAll(videos);
        videoAdapter.notifyDataSetChanged();
        progressbar.setVisibility(View.GONE);
    }
}
