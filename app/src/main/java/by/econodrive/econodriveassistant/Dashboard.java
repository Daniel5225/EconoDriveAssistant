package by.econodrive.econodriveassistant;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Dashboard extends AppCompatActivity {

    private static final int BACK_PRESS_TIME_INTERVAL = 3000; // Интервал времени для двойного нажатия на кнопку "назад"
    private long backPressedTime;
    private RecyclerView recyclerView;
    private FuelEntryAdapter fuelAdapter;
    private OtherEntryAdapter otherAdapter;
    private List<FuelEntry> fuelEntryList;
    private List<OtherEntry> otherEntryList;
    private DatabaseReference fuelDatabaseReference, otherDatabaseReference;
    private String userId;
    private TextView mileageStatsTextView;
    private TextView fuelStatsTextView;
    private TextView otherExpensesTextView;
    private Switch entryTypeSwitch;

    private ActivityResultLauncher<Intent> editOtherEntryLauncher;
    private ActivityResultLauncher<Intent> editFuelEntryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Инициализация компонентов интерфейса
        recyclerView = findViewById(R.id.entries_recycler_view);
        mileageStatsTextView = findViewById(R.id.mileage_stats_text_view);
        fuelStatsTextView = findViewById(R.id.fuelStatsTextView);
        otherExpensesTextView = findViewById(R.id.other_expenses_text_view);
        entryTypeSwitch = findViewById(R.id.entry_type_switch);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fuelEntryList = new ArrayList<>();
        otherEntryList = new ArrayList<>();
        fuelAdapter = new FuelEntryAdapter(fuelEntryList, new FuelEntryAdapter.OnEditButtonClickListener() {
            @Override
            public void onEditButtonClick(FuelEntry fuelEntry) {
                editFuelEntry(fuelEntry);
            }
        });
        otherAdapter = new OtherEntryAdapter(otherEntryList, new OtherEntryAdapter.OnEditButtonClickListener() {
            @Override
            public void onEditButtonClick(OtherEntry otherEntry) {
                editOtherEntry(otherEntry);
            }
        });
        recyclerView.setAdapter(fuelAdapter);

        // Установка обработчика для переключателя
        entryTypeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                entryTypeSwitch.setText("Прочие расходы   ");
                recyclerView.setAdapter(otherAdapter);
                displayOtherExpenses();
            } else {
                entryTypeSwitch.setText("Заправки   ");
                recyclerView.setAdapter(fuelAdapter);
                sortAndDisplayEntries();
            }
        });

        // Инициализация кнопок и установка обработчиков нажатия
        Button addFuelEntryButton = findViewById(R.id.add_fuel_entry_button);
        addFuelEntryButton.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, AddFuelEntryActivity.class);
            startActivity(intent);
        });

        Button openFuelCalculatorButton = findViewById(R.id.open_fuel_calculator_button);
        openFuelCalculatorButton.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, FuelCalculatorActivity.class);
            startActivity(intent);
        });

        Button openMapButton = findViewById(R.id.open_map_button);
        openMapButton.setOnClickListener(v -> openMapWithNearbyGasStations());

        // Получение userId из SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");

        // Установка ссылки на базу данных Firebase
        if (!userId.isEmpty()) {
            fuelDatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("fuel_entries");
            otherDatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("other_entries");
        } else {
            // Логика для случая, если userId пустой
        }

        // Инициализация ActivityResultLauncher для редактирования записей
        editOtherEntryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        otherDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                otherEntryList.clear();
                                for (DataSnapshot entrySnapshot : dataSnapshot.getChildren()) {
                                    OtherEntry otherEntry = entrySnapshot.getValue(OtherEntry.class);
                                    if (otherEntry != null) {
                                        otherEntryList.add(otherEntry);
                                    }
                                }
                                displayOtherExpenses();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(Dashboard.this, "Ошибка обновления данных", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
        );

        editFuelEntryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        fuelDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                fuelEntryList.clear();
                                for (DataSnapshot entrySnapshot : dataSnapshot.getChildren()) {
                                    FuelEntry fuelEntry = entrySnapshot.getValue(FuelEntry.class);
                                    if (fuelEntry != null) {
                                        fuelEntryList.add(fuelEntry);
                                    }
                                }
                                sortAndDisplayEntries();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(Dashboard.this, "Ошибка обновления данных", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
        );
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Получение данных о заправках для конкретного пользователя
        fuelDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fuelEntryList.clear();
                for (DataSnapshot entrySnapshot : dataSnapshot.getChildren()) {
                    FuelEntry fuelEntry = entrySnapshot.getValue(FuelEntry.class);
                    if (fuelEntry != null) {
                        fuelEntryList.add(fuelEntry);
                    }
                }
                sortAndDisplayEntries();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Dashboard.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            }
        });

        // Получение других данных для конкретного пользователя
        otherDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                otherEntryList.clear();
                for (DataSnapshot entrySnapshot : dataSnapshot.getChildren()) {
                    OtherEntry otherEntry = entrySnapshot.getValue(OtherEntry.class);
                    if (otherEntry != null) {
                        otherEntryList.add(otherEntry);
                    }
                }
                displayOtherExpenses();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Dashboard.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sortAndDisplayEntries() {
        Collections.sort(fuelEntryList, new Comparator<FuelEntry>() {
            @Override
            public int compare(FuelEntry o1, FuelEntry o2) {
                SimpleDateFormat sdf
                        = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                try {
                    Date date1 = sdf.parse(o1.getDate());
                    Date date2 = sdf.parse(o2.getDate());
                    return date1.compareTo(date2);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });
        fuelAdapter.notifyDataSetChanged();
        calculateMileageStats();
        calculateFuelStats();
    }

    private void displayOtherExpenses() {
        double totalExpenses = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        Date now = new Date();
        List<OtherEntry> lastMonthEntries = new ArrayList<>();

        for (OtherEntry otherEntry : otherEntryList) {
            try {
                Date entryDate = dateFormat.parse(otherEntry.getDate());
                if (entryDate != null) {
                    long diffInMillies = Math.abs(now.getTime() - entryDate.getTime());
                    long diffInDays = diffInMillies / (1000 * 60 * 60 * 24);
                    if (diffInDays <= 30) {
                        lastMonthEntries.add(otherEntry);
                        totalExpenses += otherEntry.getPrice();
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        otherExpensesTextView.setText(String.format(Locale.getDefault(), "Прочие расходы: %.2f руб", totalExpenses));
        otherAdapter.notifyDataSetChanged();
    }

    private void calculateFuelStats() {
        double totalFuel = 0;

        for (FuelEntry entry : fuelEntryList) {
            totalFuel += entry.getVolume();
        }

        String fuelStats = "Заправлено: " + totalFuel + " л";
        fuelStatsTextView.setText(fuelStats);
    }

    private void calculateMileageStats() {
        if (fuelEntryList.isEmpty()) {
            mileageStatsTextView.setText("Нет данных для расчета пробега.");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        Date now = new Date();
        List<FuelEntry> lastMonthEntries = new ArrayList<>();

        for (FuelEntry entry : fuelEntryList) {
            try {
                Date entryDate = dateFormat.parse(entry.getDate());
                if (entryDate != null) {
                    long diffInMillies = Math.abs(now.getTime() - entryDate.getTime());
                    long diffInDays = diffInMillies / (1000 * 60 * 60 * 24);
                    if (diffInDays <= 30) {
                        lastMonthEntries.add(entry);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (lastMonthEntries.isEmpty()) {
            mileageStatsTextView.setText("Нет данных за последний месяц.");
            return;
        }

        Collections.sort(lastMonthEntries, new Comparator<FuelEntry>() {
            @Override
            public int compare(FuelEntry o1, FuelEntry o2) {
                try {
                    Date date1 = dateFormat.parse(o1.getDate());
                    Date date2 = dateFormat.parse(o2.getDate());
                    return date1 != null && date2 != null ? date1.compareTo(date2) : 0;
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });

        int startMileage = lastMonthEntries.get(0).getMileage();
        int endMileage = lastMonthEntries.get(lastMonthEntries.size() - 1).getMileage();
        int mileageDifference = endMileage - startMileage;

        mileageStatsTextView.setText("Пробег: " + mileageDifference + " км");
    }

    private void editFuelEntry(FuelEntry fuelEntry) {
        Intent intent = new Intent(Dashboard.this, EditFuelEntryActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("entryId", fuelEntry.getId());
        intent.putExtra("date", fuelEntry.getDate());
        intent.putExtra("volume", fuelEntry.getVolume());
        intent.putExtra("price", fuelEntry.getPrice());
        intent.putExtra("mileage", fuelEntry.getMileage());
        startActivity(intent);
    }

    private void editOtherEntry(OtherEntry otherEntry) {
        Intent intent = new Intent(Dashboard.this, EditOtherEntryActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("entryId", otherEntry.getId());
        intent.putExtra("date", otherEntry.getDate());
        intent.putExtra("type", otherEntry.getExpenseType());
        intent.putExtra("price", otherEntry.getPrice());
        intent.putExtra("mileage", otherEntry.getMileage());

        editOtherEntryLauncher.launch(intent);
    }

    private void openMapWithNearbyGasStations() {
        // Base URL for Google Maps search with nearby gas stations
        String mapQuery = "geo:0,0?q=gas+stations+near+me";

        // Create an intent to open Google Maps
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapQuery));

        // Set package to ensure the intent is handled by Google Maps
        mapIntent.setPackage("com.google.android.apps.maps");

        try {
            startActivity(mapIntent);
        } catch (ActivityNotFoundException e) {
            // Handle the case where Google Maps is not installed
            Toast.makeText(this, "Google Maps is not installed.", Toast.LENGTH_LONG).show();
        }
    }




    @Override
    public void onBackPressed() {
        if (backPressedTime + BACK_PRESS_TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            Toast.makeText(getBaseContext(), "Нажмите еще раз для выхода", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
    }
}
