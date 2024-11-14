package com.example.messagereader;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SmsAdapter extends RecyclerView.Adapter<SmsAdapter.SmsViewHolder> {
    private List<SmsMessage> smsList;

    public SmsAdapter(List<SmsMessage> smsList) {
        this.smsList = smsList;
    }

    @NonNull
    @Override
    public SmsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sms, parent, false);
        return new SmsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SmsViewHolder holder, int position) {
        SmsMessage message = smsList.get(position);
        holder.senderTextView.setText(message.getSender());
        holder.messageTextView.setText(message.getMessageBody());
        holder.dateTextView.setText(message.getFormattedDate());
    }

    @Override
    public int getItemCount() {
        return smsList.size();
    }

    static class SmsViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView;
        TextView messageTextView;
        TextView dateTextView;

        public SmsViewHolder(@NonNull View itemView) {
            super(itemView);
            senderTextView = itemView.findViewById(R.id.textViewSender);
            messageTextView = itemView.findViewById(R.id.textViewMessage);
            dateTextView = itemView.findViewById(R.id.textViewDate);
        }
    }
}