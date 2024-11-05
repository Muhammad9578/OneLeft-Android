package com.oneleft.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.oneleft.app.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ResultHolder> {

    private ArrayList<ResultItem> dataset;
    private AdapterView.OnItemClickListener onItemClickListener;

    private double amountPerPlayer = 0;

    public ResultAdapter(ArrayList<ResultItem> dataset, AdapterView.OnItemClickListener onItemClickListener) {
        this.dataset = dataset;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ResultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_result, parent, false);
        return new ResultHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultHolder holder, int position) {
        ResultItem currentItem = dataset.get(position);

        holder.tvPlayerName.setText(currentItem.getName());
        holder.tvCorrect.setText("Correct Answers:  " + currentItem.getNumberOfCorrectAnswers());
        holder.tvReward.setText("Reward: $" + new DecimalFormat("#.##").format(amountPerPlayer));

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

    class ResultHolder extends RecyclerView.ViewHolder {

        TextView tvPlayerName;
        TextView tvCorrect;
        TextView tvReward;

        public ResultHolder(@NonNull View itemView) {
            super(itemView);
            tvPlayerName = itemView.findViewById(R.id.tvPlayerName);
            tvCorrect = itemView.findViewById(R.id.tvCorrect);
            tvReward = itemView.findViewById(R.id.tvReward);
        }
    }

    public void setAmountPerPlayer(double amountPerPlayer) {
        this.amountPerPlayer = amountPerPlayer;
    }

    public double getAmountPerPlayer() {
        return amountPerPlayer;
    }
}