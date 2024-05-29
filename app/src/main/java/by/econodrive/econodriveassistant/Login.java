package by.econodrive.econodriveassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.mindrot.jbcrypt.BCrypt;

public class Login extends AppCompatActivity {

    private static final String EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private Button callSignUp, login_btn;
    private TextInputLayout username, password;
    private ProgressBar loadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        callSignUp = findViewById(R.id.signup_screen);
        login_btn = findViewById(R.id.btn_sign);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loadingSpinner = findViewById(R.id.loading_spinner);

        callSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
            }
        });

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser(view);
            }
        });
    }

    private boolean validateEmail() {
        String emailValue = username.getEditText().getText().toString();
        if (emailValue.isEmpty()) {
            username.setError("Поле не может быть пустым!");
            return false;
        } else if (emailValue.length() >= 64) {
            username.setError("Максимум 64 символа!");
            return false;
        } else if (!emailValue.matches(EMAIL_PATTERN)) {
            username.setError("Неверный формат!");
            return false;
        } else {
            username.setError(null);
            username.setErrorEnabled(false);
            return true;
        }
    }

    public void loginUser(View view) {
        if (!validateEmail()) {
            return;
        } else {
            // Показываем анимацию загрузки
            showLoadingAnimation();

            isUser();
        }
    }

    private void isUser() {
        String userEnteredEmail = username.getEditText().getText().toString().trim();
        String userEnteredPassword = password.getEditText().getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        reference.orderByChild("email").equalTo(userEnteredEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Прячем анимацию загрузки
                hideLoadingAnimation();

                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        String passwordFromDB = childSnapshot.child("password").getValue(String.class);
                        // Проверка хэшированного пароля
                        if (BCrypt.checkpw(userEnteredPassword, passwordFromDB)) {
                            String userId = childSnapshot.getKey();

                            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("userId", userId);
                            editor.apply();

                            Intent intent = new Intent(Login.this, Dashboard.class);
                            intent.putExtra("userId", userId);
                            startActivity(intent);
                            finish();
                        } else {
                            password.setError("Неверный пароль");
                        }
                    }
                } else {
                    username.setError("Пользователь не найден");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Прячем анимацию загрузки в случае ошибки
                hideLoadingAnimation();
            }
        });
    }

    private void showLoadingAnimation() {
        loadingSpinner.setVisibility(View.VISIBLE);
        RotateAnimation rotate = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(1000);
        rotate.setInterpolator(new LinearInterpolator());
        rotate.setRepeatCount(Animation.INFINITE);
        loadingSpinner.startAnimation(rotate);
    }

    private void hideLoadingAnimation() {
        loadingSpinner.clearAnimation();
        loadingSpinner.setVisibility(View.GONE);
    }
}
