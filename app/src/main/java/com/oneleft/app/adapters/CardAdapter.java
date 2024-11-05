package com.oneleft.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.oneleft.app.R;
import com.oneleft.app.models.Card;

import java.util.ArrayList;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardHolder> {

    private ArrayList<Card> dataset;
    private AdapterView.OnItemClickListener onItemClickListener;

    public CardAdapter(ArrayList<Card> dataset, AdapterView.OnItemClickListener onItemClickListener) {
        this.dataset = dataset;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public CardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_card, parent, false);
        return new CardHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CardHolder holder, int position) {

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

    class CardHolder extends RecyclerView.ViewHolder {

        ImageView ivCardType;
        TextView tvCardName;
        TextView tvCardNumber;


        public CardHolder(@NonNull View itemView) {
            super(itemView);

            ivCardType = itemView.findViewById(R.id.ivCardType);
            tvCardName = itemView.findViewById(R.id.tvCardName);
            tvCardNumber = itemView.findViewById(R.id.tvCardNumber);
        }
    }
}
