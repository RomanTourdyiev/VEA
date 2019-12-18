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
import tink.co.vea.util.ClipartSelectListener;
import tink.co.vea.util.Util;

import static tink.co.vea.Config.FOLDER;

/**
 * Created by Tourdyiev Roman on 2019-12-17.
 */
public class ClipartAdapter extends RecyclerView.Adapter<ClipartAdapter.ClipartViewHolder> {

    private Util util;
    private ClipartSelectListener clipartSelectListener;

    private List<Integer> list;

    public ClipartAdapter(List<Integer> list) {
        util = Util.getInstance();
        this.list = list;
    }

    public void setClipartSelectListener(ClipartSelectListener listener){
        clipartSelectListener = listener;
    }



    @NonNull
    @Override
    public ClipartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_thumbnail, parent, false);
        return new ClipartAdapter.ClipartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClipartViewHolder holder, int i) {

        View itemView = holder.itemView;
        final int position = holder.getAdapterPosition();

        holder.thumbnail.setImageResource(list.get(position));

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clipartSelectListener!=null){
                    clipartSelectListener.onClipartSelected(list.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ClipartViewHolder extends RecyclerView.ViewHolder {

        protected ImageView thumbnail;

        public ClipartViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
        }
    }

}

