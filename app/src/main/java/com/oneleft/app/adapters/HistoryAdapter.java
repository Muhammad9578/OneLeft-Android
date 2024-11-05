package com.oneleft.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.oneleft.app.R;
import com.oneleft.app.models.History;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryHolder> {

    private ArrayList<History> dataset;
    private AdapterView.OnItemClickListener onItemClickListener;

    public HistoryAdapter(ArrayList<History> dataset, AdapterView.OnItemClickListener onItemClickListener) {
        this.dataset = dataset;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public HistoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_history, parent, false);
        return new HistoryHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryHolder holder, int position) {
        History currentItem = dataset.get(position);

        holder.tvGame.setText(currentItem.getGame());
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, YY", Locale.US);
        holder.tvDate.setText(sdf.format(currentItem.getDateTime()));
        holder.tvReward.setText("Reward Earned: $" + new DecimalFormat("#.##").format(currentItem.getRewardEarned()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(null, holder.itemView, holder.getAdapterPosition(), 0);
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    class HistoryHolder extends RecyclerView.ViewHolder {

        TextView tvGame;
        TextView tvDate;
        TextView tvReward;

        public HistoryHolder(@NonNull View itemView) {
            super(itemView);

            tvGame = itemView.findViewById(R.id.tvGame);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvReward = itemView.findViewById(R.id.tvReward);
        }
    }
}
