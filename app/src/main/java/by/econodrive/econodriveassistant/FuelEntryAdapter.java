// FuelEntryAdapter.java
package by.econodrive.econodriveassistant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FuelEntryAdapter extends RecyclerView.Adapter<FuelEntryAdapter.FuelEntryViewHolder> {

    private List<FuelEntry> fuelEntryList;
    private OnEditButtonClickListener editButtonClickListener;

    public interface OnEditButtonClickListener {
        void onEditButtonClick(FuelEntry fuelEntry);
    }

    public FuelEntryAdapter(List<FuelEntry> fuelEntryList, OnEditButtonClickListener editButtonClickListener) {
        this.fuelEntryList = fuelEntryList;
        this.editButtonClickListener = editButtonClickListener;
    }

    @NonNull
    @Override
    public FuelEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fuel_entry_item, parent, false);
        return new FuelEntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FuelEntryViewHolder holder, int position) {
        FuelEntry fuelEntry = fuelEntryList.get(position);
        holder.dateTextView.setText("Заправка " + fuelEntry.getDate());
        holder.volumeTextView.setText("Объем: " + String.valueOf(fuelEntry.getVolume()) + " литров");
        holder.priceTextView.setText("Стоимость: " + String.valueOf(fuelEntry.getPrice()) + " бел.руб.");
        holder.mileageTextView.setText("Пробег: " + String.valueOf(fuelEntry.getMileage()) + " км");
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editButtonClickListener != null) {
                    editButtonClickListener.onEditButtonClick(fuelEntry);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return fuelEntryList.size();
    }

    public static class FuelEntryViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView, volumeTextView, priceTextView, mileageTextView;
        public ImageButton editButton;

        public FuelEntryViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.date);
            volumeTextView = itemView.findViewById(R.id.volume);
            priceTextView = itemView.findViewById(R.id.price);
            mileageTextView = itemView.findViewById(R.id.mileage);
            editButton = itemView.findViewById(R.id.edit_button);
        }
    }
}
