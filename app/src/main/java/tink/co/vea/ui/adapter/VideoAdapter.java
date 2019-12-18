package tink.co.vea.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import tink.co.vea.App;
import tink.co.vea.R;
import tink.co.vea.util.Util;

/**
 * Created by Tourdyiev Roman on 2019-12-17.
 */
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private Util util;

    private List<String> list;

    public VideoAdapter(List<String> list) {
        util = Util.getInstance();
        this.list = list;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoAdapter.VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int i) {

        View itemView = holder.itemView;
        final int position = holder.getAdapterPosition();

        String[] pathArray = list.get(position).split("/");

        holder.title.setText(pathArray[pathArray.length-1]);
        holder.path.setText(list.get(i));
        holder.duration.setText(util.getVideoLength(list.get(i)));
        holder.thumbnail.setImageBitmap(util.getVideoThumbnail(list.get(i),false));

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                util.showEditor(list.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {

        protected ImageView thumbnail;
        protected TextView title;
        protected TextView path;
        protected TextView duration;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            title = itemView.findViewById(R.id.title);
            path = itemView.findViewById(R.id.path);
            duration = itemView.findViewById(R.id.duration);
        }
    }

}
