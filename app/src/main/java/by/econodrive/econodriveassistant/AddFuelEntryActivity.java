package by.econodrive.econodriveassistant;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class AddFuelEntryActivity extends AppCompatActivity {

    private EditText dateEditText, mileageEditText, volumeEditText, priceEditText;
    private Spinner expenseTypeSpinner;
    private Button saveButton;
    private DatabaseReference fuelDatabaseReference, otherDatabaseReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_fuel_entry);

        // Инициализация полей ввода и кнопки
        dateEditText = findViewById(R.id.date_edit_text);
        mileageEditText = findViewById(R.id.mileage_edit_text);
        volumeEditText = findViewById(R.id.volume_edit_text);
        priceEditText = findViewById(R.id.price_edit_text);
        expenseTypeSpinner = findViewById(R.id.entry_type_spinner);
        saveButton = findViewById(R.id.save_button);

        // Получение userId из SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");

        // Установка ссылки на базу данных Firebase
        if (!userId.isEmpty()) {
            fuelDatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("fuel_entries");
            otherDatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("other_entries");
            fetchLastMileage();
        } else {
            // Логика для случая, если userId пустой
        }

        // Настройка Spinner для выбора типа расходов
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.expense_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expenseTypeSpinner.setAdapter(adapter);

        // Обработчик нажатия на кнопку сохранения
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEntry();
            }
        });

        // Обработчик нажатия на поле даты
        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String selectedDate = String.format("%02d.%02d.%d", dayOfMonth, monthOfYear + 1, year);
                        dateEditText.setText(selectedDate);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void fetchLastMileage() {
        final DatabaseReference userDatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
        final int[] lastMileage = {0};

        // Fetch fuel entries
        userDatabaseReference.child("fuel_entries").orderByChild("mileage").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot fuelSnapshot) {
                if (fuelSnapshot.exists()) {
                    for (DataSnapshot snapshot : fuelSnapshot.getChildren()) {
                        FuelEntry fuelEntry = snapshot.getValue(FuelEntry.class);
                        if (fuelEntry != null && fuelEntry.getMileage() > lastMileage[0]) {
                            lastMileage[0] = fuelEntry.getMileage();
                        }
                    }
                }
                fetchOtherEntries(lastMileage[0]);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    private void fetchOtherEntries(final int currentLastMileage) {
        final DatabaseReference userDatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // Fetch other entries
        userDatabaseReference.child("other_entries").orderByChild("mileage").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot otherSnapshot) {
                int lastMileage = currentLastMileage;
                if (otherSnapshot.exists()) {
                    for (DataSnapshot snapshot : otherSnapshot.getChildren()) {
                        OtherEntry otherEntry = snapshot.getValue(OtherEntry.class);
                        if (otherEntry != null && otherEntry.getMileage() > lastMileage) {
                            lastMileage = otherEntry.getMileage();
                        }
                    }
                }
                updateLastMileageHint(lastMileage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    private void updateLastMileageHint(int lastMileage) {
        if (lastMileage > 0) {
            mileageEditText.setHint("Пробег (+" + lastMileage + " км)");
        } else {
            mileageEditText.setHint("Пробег (км)");
        }
    }


    private void saveEntry() {
        String date = dateEditText.getText().toString().trim();
        String mileageStr = mileageEditText.getText().toString().trim();
        String priceStr = priceEditText.getText().toString().trim();
        String volumeStr = volumeEditText.getText().toString().trim();
        String expenseType = expenseTypeSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(mileageStr) || TextUtils.isEmpty(priceStr)) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        int mileage;
        double price;
        double volume = 0;

        try {
            mileage = Integer.parseInt(mileageStr);
            price = Double.parseDouble(priceStr);
            if ("Заправка".equals(expenseType)) {
                volume = Double.parseDouble(volumeStr);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Пожалуйста, введите корректные числовые значения", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("Заправка".equals(expenseType)) {
            String entryId = fuelDatabaseReference.push().getKey();
            FuelEntry fuelEntry = new FuelEntry(entryId, date, volume, price, mileage, userId);
            if (entryId != null) {
                fuelDatabaseReference.child(entryId).setValue(fuelEntry);
            }
        } else {
            String entryId = otherDatabaseReference.push().getKey();
            OtherEntry otherEntry = new OtherEntry(entryId, date, expenseType, price, mileage, userId);
            if (entryId != null) {
                otherDatabaseReference.child(entryId).setValue(otherEntry);
            }
        }
        Toast.makeText(this, "Запись успешно добавлена", Toast.LENGTH_SHORT).show();
        finish();
    }
}
