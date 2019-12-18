package tink.co.vea.ui.fragment;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import tink.co.vea.App;
import tink.co.vea.R;
import tink.co.vea.ui.adapter.FramesAdapter;
import tink.co.vea.ui.adapter.VideoAdapter;
import tink.co.vea.ui.customView.CustomVideoView;
import tink.co.vea.util.FramesExtractionListener;
import tink.co.vea.util.Util;

import static androidx.recyclerview.widget.RecyclerView.VERTICAL;
import static tink.co.vea.Config.VIDEO_PATH;

/**
 * Created by Tourdyiev Roman on 2019-12-17.
 */
public class EditorFragment
        extends Fragment
        implements View.OnClickListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        FramesExtractionListener {

    private MediaController mediaController;
    private Util util;
    private FramesAdapter framesAdapter;

    private ImageView close;
    private TextView title;
    private RecyclerView recyclerView;
    private ImageView videoThumbnail;
    private CustomVideoView videoView;
    private ProgressBar progressBar;

    private List<String> frames = new ArrayList<>();
    private String path;

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.close) {
            App.getActivity().onBackPressed();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                mediaController = new MediaController(getActivity());
                mediaController.setAnchorView(videoView);
                mediaController.setMediaPlayer(videoView);
                videoView.setMediaController(mediaController);
                mediaController.show(0);
            }
        });
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        videoThumbnail.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFramesReady(List<String> frames) {
        progressBar.setVisibility(View.GONE);
        this.frames.clear();
        this.frames.addAll(frames);
        framesAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_editor, container, false);
        framesAdapter = new FramesAdapter(frames);
        util = Util.getInstance();
        findViews(rootView);
        initViews();
        util.setFramesExtractionListener(this);
        util.processVideo(path);
        return rootView;
    }

    @Override
    public void onDetach() {
        util.setFramesExtractionListener(null);
        super.onDetach();
    }

    private void findViews(View rootView) {
        close = rootView.findViewById(R.id.close);
        title = rootView.findViewById(R.id.title);
        recyclerView = rootView.findViewById(R.id.recycler_view);
        videoThumbnail = rootView.findViewById(R.id.video_thumbnail);
        videoView = rootView.findViewById(R.id.video_view);
        progressBar = rootView.findViewById(R.id.progressbar);
    }

    private void initViews() {
        close.setOnClickListener(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(VIDEO_PATH)) {
                String path = bundle.getString(VIDEO_PATH);
                String[] pathArray = path.split("/");
                title.setText(pathArray[pathArray.length - 1]);
                initVideo(path);
                initFramesList();
                this.path = path;

                videoThumbnail.setVisibility(View.VISIBLE);
                videoThumbnail.setImageBitmap(util.getVideoThumbnail(path, true));
            }
        }
    }

    private void initVideo(final String path) {
        videoView.setVideoPath(path);
        mediaController = new MediaController(getActivity());
        videoView.setMediaController(mediaController);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mediaController.show(0);
            }
        }, 100);
        videoView.setOnPreparedListener(this);
        videoView.setOnCompletionListener(this);
        videoView.setPlayPauseListener(new CustomVideoView.PlayPauseListener() {

            @Override
            public void onPlay() {
                videoThumbnail.setVisibility(View.GONE);
            }

            @Override
            public void onPause() {

            }
        });
    }

    private void initFramesList(){
        progressBar.setVisibility(View.VISIBLE);
        frames.clear();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(App.getContext());
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(framesAdapter);
    }
}
