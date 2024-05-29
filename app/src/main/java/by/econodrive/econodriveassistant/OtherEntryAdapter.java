package by.econodrive.econodriveassistant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OtherEntryAdapter extends RecyclerView.Adapter<OtherEntryAdapter.OtherEntryViewHolder> {
    private List<OtherEntry> otherEntryList;
    private OnEditButtonClickListener editButtonClickListener;

    public interface OnEditButtonClickListener {
        void onEditButtonClick(OtherEntry otherEntry);
    }

    public OtherEntryAdapter(List<OtherEntry> otherEntryList, OnEditButtonClickListener editButtonClickListener) {
        this.otherEntryList = otherEntryList;
        this.editButtonClickListener = editButtonClickListener;
    }

    @NonNull
    @Override
    public OtherEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_other_entry, parent, false);
        return new OtherEntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OtherEntryViewHolder holder, int position) {
        OtherEntry otherEntry = otherEntryList.get(position);
        holder.bind(otherEntry);
    }

    @Override
    public int getItemCount() {
        return otherEntryList.size();
    }

    class OtherEntryViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView priceTextView;
        TextView mileageTextView;
        ImageButton editButton;

        OtherEntryViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.date_text_view);
            priceTextView = itemView.findViewById(R.id.price_text_view);
            editButton = itemView.findViewById(R.id.edit_button);
            mileageTextView = itemView.findViewById(R.id.mileage);

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && editButtonClickListener != null) {
                        editButtonClickListener.onEditButtonClick(otherEntryList.get(position));
                    }
                }
            });
        }

        void bind(OtherEntry otherEntry) {
            dateTextView.setText(otherEntry.getExpenseType() + " " + otherEntry.getDate());
            priceTextView.setText("Стоимость: " + String.valueOf(otherEntry.getPrice()));
            mileageTextView.setText("Пробег: " + String.valueOf(otherEntry.getMileage()));
        }
    }
}
