package tink.co.vea.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tink.co.vea.App;
import tink.co.vea.R;
import tink.co.vea.ui.adapter.ClipartAdapter;
import tink.co.vea.util.ClipartSelectListener;
import tink.co.vea.util.Util;
import tink.co.vea.util.VideoSaveListener;

import static tink.co.vea.Config.FRAME_PATH;
import static tink.co.vea.Config.VIDEO_PATH;

/**
 * Created by Tourdyiev Roman on 2019-12-17.
 */
public class FrameFragment extends Fragment implements View.OnClickListener, ClipartSelectListener, View.OnTouchListener, VideoSaveListener {

    private Util util;
    private ClipartAdapter clipartAdapter;

    private ImageView close;
    private ImageView done;
    private ImageView frame;
    private RecyclerView recyclerView;
    private RelativeLayout imageContainer;
    private ProgressBar progressBar;

    private int xDelta;
    private int yDelta;
    private String videoPath;

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.close) {
            App.getActivity().onBackPressed();
        }
        if (v.getId() == R.id.done) {
            progressBar.setVisibility(View.VISIBLE);
            close.setVisibility(View.GONE);
            done.setVisibility(View.GONE);
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    util.saveVideo(imageContainer);
                }
            });
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {

        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();

        Log.d("moveImage", X + "|" + Y);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                xDelta = X - view.getLeft();
                yDelta = Y - view.getTop();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();

                lp.leftMargin = X - xDelta;
                lp.topMargin = Y - yDelta;
                lp.rightMargin = view.getWidth() - lp.leftMargin - imageContainer.getWidth();
                lp.bottomMargin = view.getHeight() - lp.topMargin - imageContainer.getHeight();
                view.setLayoutParams(lp);
                break;
        }

        return true;
    }

    @Override
    public void onClipartSelected(int resId) {
        ImageView clipart = new ImageView(App.getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.width = getResources().getDimensionPixelSize(R.dimen.frame_height);
        layoutParams.height = getResources().getDimensionPixelSize(R.dimen.frame_height);

        clipart.setImageResource(resId);
        clipart.setLayoutParams(layoutParams);
        imageContainer.addView(clipart);

        clipart.setOnTouchListener(this);
        done.setVisibility(View.VISIBLE);
    }

    @Override
    public void onVideoSaved() {
        App.getActivity().onBackPressed();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_frame, container, false);
        util = Util.getInstance();
        util.setVideoSaveListener(this);
        findViews(rootView);
        initViews();
        return rootView;
    }

    @Override
    public void onDetach() {
        clipartAdapter.setClipartSelectListener(null);
        util.setVideoSaveListener(null);
        super.onDetach();
    }

    private void findViews(View rootView) {
        close = rootView.findViewById(R.id.close);
        done = rootView.findViewById(R.id.done);
        frame = rootView.findViewById(R.id.frame);
        recyclerView = rootView.findViewById(R.id.recycler_view);
        imageContainer = rootView.findViewById(R.id.image_container);
        progressBar = rootView.findViewById(R.id.progressbar);
    }

    private void initViews() {
        close.setOnClickListener(this);
        done.setOnClickListener(this);

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(FRAME_PATH)) {
                String path = bundle.getString(FRAME_PATH);
                videoPath = bundle.getString(VIDEO_PATH);
                frame.setImageBitmap(BitmapFactory.decodeFile(path));
            }
        }

        List<Integer> cliparts = new ArrayList<>();
        cliparts.add(R.drawable.crown);
        cliparts.add(R.drawable.lips);
        cliparts.add(R.drawable.smile);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(App.getContext());
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        clipartAdapter = new ClipartAdapter(cliparts);
        recyclerView.setAdapter(clipartAdapter);
        clipartAdapter.setClipartSelectListener(this);
    }
}
