package com.oneleft.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.oneleft.app.R;
import com.oneleft.app.models.Question;

import java.util.ArrayList;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionHolder> {

    private ArrayList<Question> dataset;
    private AdapterView.OnItemClickListener onItemClickListener;

    public QuestionAdapter(ArrayList<Question> dataset, AdapterView.OnItemClickListener onItemClickListener) {
        this.dataset = dataset;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public QuestionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_question, parent, false);
        return new QuestionHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionHolder holder, int position) {
        Question currentItem = dataset.get(position);
        holder.tvQuestionNumber.setText(String.valueOf(position + 1));
        /*holder.tvQuestionText.setText(currentItem.getText());
        holder.rbOption1.setText(currentItem.getOption1());
        holder.rbOption2.setText(currentItem.getOption2());
        holder.rbOption3.setText(currentItem.getOption3());
        holder.rbOption4.setText(currentItem.getOption4());*/

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(null, v, holder.getAdapterPosition(), 0);
            }
        };

        holder.rbOption1.setOnClickListener(onClickListener);
        holder.rbOption2.setOnClickListener(onClickListener);
        holder.rbOption3.setOnClickListener(onClickListener);
        holder.rbOption4.setOnClickListener(onClickListener);
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    class QuestionHolder extends RecyclerView.ViewHolder {

        TextView tvQuestionNumber;
        TextView tvQuestionText;
        RadioButton rbOption1;
        RadioButton rbOption2;
        RadioButton rbOption3;
        RadioButton rbOption4;

        public QuestionHolder(@NonNull View itemView) {
            super(itemView);

            tvQuestionNumber = itemView.findViewById(R.id.tvQuestionNumber);
            tvQuestionText = itemView.findViewById(R.id.tvQuestionText);
            rbOption1 = itemView.findViewById(R.id.rbOption1);
            rbOption2 = itemView.findViewById(R.id.rbOption2);
            rbOption3 = itemView.findViewById(R.id.rbOption3);
            rbOption4 = itemView.findViewById(R.id.rbOption4);
        }
    }
}
