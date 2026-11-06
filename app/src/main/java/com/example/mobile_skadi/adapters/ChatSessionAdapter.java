package com.example.mobile_skadi.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_skadi.R;
import com.example.mobile_skadi.data.ChatSession;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatSessionAdapter extends RecyclerView.Adapter<ChatSessionAdapter.SessionViewHolder> {

    public interface OnSessionClickListener {
        void onSessionClick(ChatSession session);
        void onSessionDelete(ChatSession session);
    }

    private final List<ChatSession> sessions;
    private final OnSessionClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM 'às' HH:mm", Locale.getDefault());

    public ChatSessionAdapter(List<ChatSession> sessions, OnSessionClickListener listener) {
        this.sessions = sessions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        ChatSession session = sessions.get(position);
        holder.tvTitle.setText(session.getTitle() != null ? session.getTitle() : "Conversa sem título");

        String dateStr = dateFormat.format(new Date(session.getCreatedAt()));
        String subtitle = dateStr + " • " + session.getMessageCount() + " msg(s)";
        holder.tvDate.setText(subtitle);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSessionClick(session);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onSessionDelete(session);
        });
    }

    @Override
    public int getItemCount() {
        return sessions != null ? sessions.size() : 0;
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDate;
        ImageView btnDelete;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSessionTitle);
            tvDate = itemView.findViewById(R.id.tvSessionDate);
            btnDelete = itemView.findViewById(R.id.btnDeleteSession);
        }
    }
}
