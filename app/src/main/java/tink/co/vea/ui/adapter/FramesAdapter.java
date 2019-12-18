package tink.co.vea.ui.adapter;

import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;
import java.util.Locale;

import tink.co.vea.App;
import tink.co.vea.R;
import tink.co.vea.util.Util;

import static tink.co.vea.Config.FOLDER;

/**
 * Created by Tourdyiev Roman on 2019-12-17.
 */
public class FramesAdapter extends RecyclerView.Adapter<FramesAdapter.FramesViewHolder> {

    private Util util;

    private List<String> list;

    public FramesAdapter(List<String> list) {
        util = Util.getInstance();
        this.list = list;
    }

    @NonNull
    @Override
    public FramesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_frame, parent, false);
        return new FramesAdapter.FramesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FramesViewHolder holder, int i) {

        View itemView = holder.itemView;
        final int position = holder.getAdapterPosition();

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + File.separator + FOLDER);

        final File image = new File(mediaStorageDir.getAbsolutePath() + File.separator + list.get(position));
        Log.d("filePath", image.getAbsolutePath());

        holder.thumbnail.setImageBitmap(BitmapFactory.decodeFile(image.getAbsolutePath()));

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                util.showFrameEditor(image.getAbsolutePath());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class FramesViewHolder extends RecyclerView.ViewHolder {

        protected ImageView thumbnail;

        public FramesViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
        }
    }

}

