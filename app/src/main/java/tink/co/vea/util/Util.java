package tink.co.vea.util;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler;
import nl.bravobit.ffmpeg.FFmpeg;
import nl.bravobit.ffmpeg.FFprobe;
import nl.bravobit.ffmpeg.FFtask;
import tink.co.vea.App;
import tink.co.vea.R;
import tink.co.vea.ui.fragment.EditorFragment;
import tink.co.vea.ui.fragment.FrameFragment;

import static tink.co.vea.Config.FOLDER;
import static tink.co.vea.Config.FRAME_PATH;
import static tink.co.vea.Config.VIDEO_PATH;

/**
 * Created by Tourdyiev Roman on 2019-12-17.
 */
public class Util {

    private static SharedPreferences sharedPreferences;
    private static ContentResolver contentResolver;
    private static FragmentManager fragmentManager;
    private static FFmpeg ffmpeg;
    private static FFprobe ffprobe;
    private FramesExtractionListener framesExtractionListener;
    private VideoSaveListener videoSaveListener;
    private VideoListCallback videoListCallback;

    private static File mediaStorageDir;
    private static File mFile;

    private Util() {
    }

    private static Util instance = new Util();

    public static Util getInstance() {
        ffmpeg = FFmpeg.getInstance(App.getContext());
        ffprobe = FFprobe.getInstance(App.getContext());
        contentResolver = App.getContext().getContentResolver();
        fragmentManager = App.getActivity().getSupportFragmentManager();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        mediaStorageDir = new File(Environment.getExternalStorageDirectory() + File.separator + FOLDER);
        return instance;
    }

    public void setFramesExtractionListener(FramesExtractionListener listener) {
        framesExtractionListener = listener;
    }

    public void setVideoSaveListener(VideoSaveListener listener) {
        videoSaveListener = listener;
    }

    public void setVideoListCallback(VideoListCallback callback) {
        videoListCallback = callback;
    }

    public void getAllVideos() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                List<String> videos = new ArrayList<>();
                String[] projection = {
                        MediaStore.Video.Media._ID,
                        MediaStore.Video.Media.DATA,
                        MediaStore.Video.Media.DISPLAY_NAME,
                        MediaStore.Video.Media.SIZE,
                        MediaStore.Video.Media.DATE_TAKEN
                };
                Cursor cursor = contentResolver.query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        null,
                        null,
                        MediaStore.Video.Media.DATE_TAKEN + " DESC");

                try {
                    cursor.moveToFirst();
                    do {
                        videos.add((cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))));
                    } while (cursor.moveToNext());
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (videoListCallback != null) {
                    videoListCallback.onVideoListReady(videos);
                }
            }
        });
    }

    public Bitmap getVideoThumbnail(String path, boolean highQuality) {
        return ThumbnailUtils.createVideoThumbnail(path, highQuality ? MediaStore.Video.Thumbnails.FULL_SCREEN_KIND : MediaStore.Video.Thumbnails.MICRO_KIND);
    }

    public String getVideoLength(String path) {
        if (path == null) {
            return "";
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        long millis = Long.parseLong(time);
        int hours = (int) TimeUnit.MILLISECONDS.toHours(millis);
        millis = millis - hours * TimeUnit.HOURS.toMillis(1);
        int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(millis);
        millis = millis - minutes * TimeUnit.MINUTES.toMillis(1);
        int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(millis);

        return hours == 0 ? String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds) : String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void showEditor(String path) {
        final EditorFragment fragment = new EditorFragment();
        Bundle bundle = new Bundle();

        bundle.putString(VIDEO_PATH, path);
        fragment.setArguments(bundle);

        fragmentManager
                .beginTransaction()
                .replace(R.id.frame_container, fragment, VIDEO_PATH)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(VIDEO_PATH)
                .commit();
    }

    public void showFrameEditor(String path) {
        final FrameFragment fragment = new FrameFragment();
        Bundle bundle = new Bundle();

        bundle.putString(FRAME_PATH, path);
        fragment.setArguments(bundle);

        fragmentManager
                .beginTransaction()
                .replace(R.id.frame_container, fragment, FRAME_PATH)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(VIDEO_PATH)
                .commit();
    }

    public void processVideo(final String path) {

        String prevPath = sharedPreferences.getString(VIDEO_PATH, "");

        if (prevPath.equalsIgnoreCase(path)) {
            notifyFramesReady();
        } else {

            if (ffmpeg.isSupported()) {
                Log.d("videoFrames", "ffmpeg is supported");
            } else {
                Log.d("videoFrames", "ffmpeg is not supported");
                return;
            }

            if (!mediaStorageDir.exists()) {
                mediaStorageDir.mkdirs();
            }

            final String[] cmd = {
                    "-i",
                    path,
                    "-f",
                    "image2",
                    mediaStorageDir.getAbsolutePath() + "/frame-%05d.png"
            };

            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {
                    Log.d("videoFrames", " starting to get frames from video");
                    if (mediaStorageDir.isDirectory() && mediaStorageDir.list().length > 0) {
                        String[] children = mediaStorageDir.list();
                        for (int i = 0; i < children.length; i++) {
                            if (!children[i].contains("outputVideo")) {
                                new File(mediaStorageDir, children[i]).delete();
                            }
                        }
                    }
                }

                @Override
                public void onProgress(String message) {
                    Log.d("videoFrames", " progress getting frames from video");
                }

                @Override
                public void onFailure(String message) {
                    Log.d("videoFrames", " failure reason " + message);
                }

                @Override
                public void onSuccess(String message) {
                    Log.d("videoFrames", " success getting frames from video");
                }

                @Override
                public void onFinish() {
                    Log.d("videoFrames", " finished getting frames from video");
                    sharedPreferences.edit().putString(VIDEO_PATH, path).apply();
                    notifyFramesReady();
                }
            });
        }
    }

    private void notifyFramesReady() {
        if (framesExtractionListener != null) {
            if (mediaStorageDir.isDirectory() && mediaStorageDir.list().length > 0) {
                String[] files = mediaStorageDir.list();
                List<String> frames = new ArrayList<>(Arrays.asList(files));
                framesExtractionListener.onFramesReady(frames);
            }
        }
    }

    public void saveVideo(RelativeLayout imageContainer) {

        Bitmap result = Bitmap.createBitmap(imageContainer.getWidth(), imageContainer.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        Log.d("bitmaps", imageContainer.getChildCount() + "");

        for (int i = 1; i < imageContainer.getChildCount(); i++) {
            ImageView image = (ImageView) imageContainer.getChildAt(i);
            int marginTop = ((RelativeLayout.LayoutParams) image.getLayoutParams()).topMargin;
            int leftMargin = ((RelativeLayout.LayoutParams) image.getLayoutParams()).leftMargin;
            Bitmap clipart = ((BitmapDrawable) image.getDrawable()).getBitmap();
            Bitmap clipartScaled = Bitmap.createScaledBitmap(clipart, image.getWidth(), image.getHeight(), false);
            canvas.drawBitmap(clipartScaled, leftMargin, marginTop, null);
        }


        try {
            BitmapDrawable bitmapDrawable = new BitmapDrawable(result);
            Bitmap mNewSaving = bitmapDrawable.getBitmap();
            mFile = new File(mediaStorageDir + File.separator + "overlay.png");
            FileOutputStream mFileOutputStream = new FileOutputStream(mFile);
            mNewSaving.compress(Bitmap.CompressFormat.PNG, 100, mFileOutputStream);
            mFileOutputStream.flush();
            mFileOutputStream.close();
        } catch (FileNotFoundException e) {
            Log.e("fileOverlay", "FileNotFoundExceptionError " + e.toString());
        } catch (IOException e) {
            Log.e("fileOverlay", "IOExceptionError " + e.toString());
        }


        String path = sharedPreferences.getString(VIDEO_PATH, "");

        final String videoPath = new File(mediaStorageDir + File.separator + "outputVideo.mp4").getAbsolutePath();
        Log.d("videoPath", videoPath);
        final String[] cmd = {
                "-i",
                path,
                "-i",
                mFile.getAbsolutePath(),
                "-filter_complex",
                "overlay=0:0",
                "-codec:a",
                "copy",
                videoPath
        };

        ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

            @Override
            public void onStart() {
                Log.d("videoExport", " starting to export video");
            }

            @Override
            public void onProgress(String message) {
                Log.d("videoExport", " progress exporting video");
            }

            @Override
            public void onFailure(String message) {
                Log.d("videoExport", " failure reason " + message);
            }

            @Override
            public void onSuccess(String message) {
                Log.d("videoExport", " success exporting video");
            }

            @Override
            public void onFinish() {
                Log.d("videoFrames", " finished exporting video");
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(new File(videoPath)));
                App.getActivity().sendBroadcast(intent);
                if (videoSaveListener != null) {
                    videoSaveListener.onVideoSaved();
                }
            }
        });
    }
}
