package by.econodrive.econodriveassistant;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class FuelCalculatorActivity extends AppCompatActivity {

    private EditText distanceEditText;
    private EditText fuelConsumptionEditText;
    private EditText fuelPriceEditText;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_calculator);

        distanceEditText = findViewById(R.id.distance_edit_text);
        fuelConsumptionEditText = findViewById(R.id.fuel_consumption_edit_text);
        fuelPriceEditText = findViewById(R.id.fuel_price_edit_text);
        resultTextView = findViewById(R.id.result_text_view);

        Button calculateButton = findViewById(R.id.calculate_button);
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateFuelCost();
            }
        });
    }

    private void calculateFuelCost() {
        String distanceStr = distanceEditText.getText().toString();
        String fuelConsumptionStr = fuelConsumptionEditText.getText().toString();
        String fuelPriceStr = fuelPriceEditText.getText().toString();

        if (TextUtils.isEmpty(distanceStr) || TextUtils.isEmpty(fuelConsumptionStr) || TextUtils.isEmpty(fuelPriceStr)) {
            resultTextView.setText("Пожалуйста, заполните все поля.");
            return;
        }

        double distance = Double.parseDouble(distanceStr);
        double fuelConsumption = Double.parseDouble(fuelConsumptionStr);
        double fuelPrice = Double.parseDouble(fuelPriceStr);

        double requiredFuel = (distance / 100) * fuelConsumption;
        double totalCost = requiredFuel * fuelPrice;

        resultTextView.setText(String.format(Locale.getDefault(), "Результат:\nНеобходимо топлива: %.2f л\nСтоимость: %.2f руб.", requiredFuel, totalCost));
        resultTextView.setVisibility(View.VISIBLE);
    }
}
