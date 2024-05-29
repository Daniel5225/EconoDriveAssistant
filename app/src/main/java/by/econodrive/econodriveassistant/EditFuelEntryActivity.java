// EditFuelEntryActivity.java
package by.econodrive.econodriveassistant;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class EditFuelEntryActivity extends AppCompatActivity {

    private EditText dateEditText, volumeEditText, priceEditText, mileageEditText;
    private Button saveChangesButton;
    private DatabaseReference databaseReference;
    private String entryId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_fuel_entry);

        dateEditText = findViewById(R.id.date_edit_text);
        volumeEditText = findViewById(R.id.volume_edit_text);
        priceEditText = findViewById(R.id.price_edit_text);
        mileageEditText = findViewById(R.id.mileage_edit_text);
        saveChangesButton = findViewById(R.id.save_changes_button);

        entryId = getIntent().getStringExtra("entryId");
        userId = getIntent().getStringExtra("userId");

        String date = getIntent().getStringExtra("date");
        double volume = getIntent().getDoubleExtra("volume", 0);
        double price = getIntent().getDoubleExtra("price", 0);
        int mileage = getIntent().getIntExtra("mileage", 0);

        dateEditText.setText(date);
        volumeEditText.setText(String.valueOf(volume));
        priceEditText.setText(String.valueOf(price));
        mileageEditText.setText(String.valueOf(mileage));

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        Button deleteEntryButton = findViewById(R.id.delete_entry_button);
        deleteEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFuelEntry();
            }
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("fuel_entries").child(entryId);

        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newDate = dateEditText.getText().toString().trim();
                double newVolume = Double.parseDouble(volumeEditText.getText().toString().trim());
                double newPrice = Double.parseDouble(priceEditText.getText().toString().trim());
                int newMileage = Integer.parseInt(mileageEditText.getText().toString().trim());

                FuelEntry updatedEntry = new FuelEntry(entryId, newDate, newVolume, newPrice, newMileage, userId);

                databaseReference.setValue(updatedEntry)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(EditFuelEntryActivity.this, "Изменения сохранены", Toast.LENGTH_SHORT).show();
                            finish(); // Закрыть активность после успешного сохранения
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(EditFuelEntryActivity.this, "Ошибка при сохранении изменений", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(EditFuelEntryActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        dateEditText.setText(String.format("%02d.%02d.%d", dayOfMonth, monthOfYear + 1, year));
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void deleteFuelEntry() {
        databaseReference.removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditFuelEntryActivity.this, "Запись удалена", Toast.LENGTH_SHORT).show();
                        finish(); // Закрываем активность после успешного удаления
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditFuelEntryActivity.this, "Ошибка при удалении записи", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
