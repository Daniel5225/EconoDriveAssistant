package by.econodrive.econodriveassistant;

import android.app.DatePickerDialog;
import android.content.Intent;
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

public class EditOtherEntryActivity extends AppCompatActivity {
    private EditText dateEditText;
    private EditText typeEditText; // Изменено на EditText для отображения типа
    private EditText priceEditText;
    private EditText mileageEditText;
    private Button saveButton;

    private String entryId;
    private String userId;
    private DatabaseReference otherDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_other_entry);

        dateEditText = findViewById(R.id.edit_date);
        typeEditText = findViewById(R.id.edit_type); // Изменено на EditText
        priceEditText = findViewById(R.id.edit_price);
        mileageEditText = findViewById(R.id.mileage_edit_text);
        saveButton = findViewById(R.id.save_button);

        Intent intent = getIntent();
        entryId = intent.getStringExtra("entryId");
        String date = intent.getStringExtra("date");
        String type = intent.getStringExtra("type"); // Получаем тип расхода
        double price = intent.getDoubleExtra("price", 0);
        int mileage = intent.getIntExtra("mileage", 0);
        userId = intent.getStringExtra("userId");

        // Проверка, что данные успешно переданы
        if (entryId == null || userId == null) {
            finish(); // Закрываем активность, если данные не переданы
            return;
        }

        dateEditText.setText(date);
        typeEditText.setText(type); // Устанавливаем тип
        priceEditText.setText(String.valueOf(price));
        mileageEditText.setText(String.valueOf(mileage));

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        Button deleteEntryButton = findViewById(R.id.delete_button);
        deleteEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteOtherEntry();
            }
        });

        otherDatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("other_entries");

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges();
            }
        });
    }

    private void saveChanges() {
        String updatedDate = dateEditText.getText().toString();
        String updatedType = typeEditText.getText().toString(); // Получаем обновленный тип
        double updatedPrice = Double.parseDouble(priceEditText.getText().toString());
        int updatedMileage = Integer.parseInt(mileageEditText.getText().toString());

        // Создаем обновленную запись с текущим типом расхода
        OtherEntry updatedEntry = new OtherEntry(entryId,updatedDate, updatedType, updatedPrice, updatedMileage, userId);
        updatedEntry.setId(entryId); // Сохраняем прежний ID

        // Обновляем запись в базе данных
        otherDatabaseReference.child(entryId).setValue(updatedEntry);

        // Возвращаем результат и закрываем активити
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void deleteOtherEntry() {
        otherDatabaseReference.child(entryId).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditOtherEntryActivity.this, "Запись удалена", Toast.LENGTH_SHORT).show();
                        finish(); // Закрываем активность после успешного удаления
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditOtherEntryActivity.this, "Ошибка при удалении записи", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(EditOtherEntryActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        dateEditText.setText(String.format("%02d.%02d.%d", dayOfMonth, monthOfYear + 1, year));
                    }
                }, year, month, day);
        datePickerDialog.show();
    }
}
