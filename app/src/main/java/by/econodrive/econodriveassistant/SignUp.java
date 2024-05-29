package by.econodrive.econodriveassistant;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.mindrot.jbcrypt.BCrypt;

public class SignUp extends AppCompatActivity {

    private TextInputLayout regEmail, regPassword;
    private Button regBtn;
    private ProgressBar loadingSpinner;

    FirebaseDatabase rootNode;
    DatabaseReference reference;

    private Boolean validateEmail() {
        String val = regEmail.getEditText().getText().toString();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (val.isEmpty()) {
            regEmail.setError("Поле не может быть пустым!");
            return false;
        } else if(val.length()>=64) {
            regEmail.setError("Максимум 64 символа!");
            return false;
        } else if(!val.matches(emailPattern)) {
            regEmail.setError("Неверный формат!");
            return false;
        } else {
            regEmail.setError(null);
            regEmail.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePassword() {
        String val = regPassword.getEditText().getText().toString();
        String passwordVal = "^(?=.*[a-zA-Z])(?=.*\\d).{6,}$"; // Пароль должен содержать минимум 6 символов, как минимум одну букву и одну цифру

        if (val.isEmpty()) {
            regPassword.setError("Поле не может быть пустым!");
            return false;
        } else if (!val.matches(passwordVal)) {
            regPassword.setError("Пароль слишком слабый");
            return false;
        } else {
            regPassword.setError(null);
            regPassword.setErrorEnabled(false);
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        loadingSpinner = findViewById(R.id.loading_spinner);
        regEmail = findViewById(R.id.email);
        regPassword = findViewById(R.id.password);
        regBtn = findViewById(R.id.regbtn);

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!validateEmail() || !validatePassword()){
                    return;
                }

                showLoadingAnimation();
                String email = regEmail.getEditText().getText().toString();
                String password = regPassword.getEditText().getText().toString();

                // Проверка существует ли уже пользователь с такой почтой
                checkIfUserExists(email, password);
            }
        });
    }

    private void checkIfUserExists(final String email, final String password) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query query = reference.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    hideLoadingAnimation();
                    // Пользователь с такой почтой уже существует
                    Toast.makeText(SignUp.this, "Пользователь с такой почтой уже зарегистрирован", Toast.LENGTH_SHORT).show();
                } else {
                    // Регистрация нового пользователя
                    rootNode = FirebaseDatabase.getInstance();
                    final DatabaseReference reference = rootNode.getReference("users");

                    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                    UserHelperClass helperClass = new UserHelperClass(email, hashedPassword);
                    // Запись
                    reference.push().setValue(helperClass);
                    hideLoadingAnimation();

                    // Уведомление о успешной регистрации
                    Toast.makeText(SignUp.this, "Регистрация успешно завершена", Toast.LENGTH_SHORT).show();

                    // Переход обратно в окно авторизации
                    onBackPressed();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideLoadingAnimation();
                // Обработка ошибки
                Toast.makeText(SignUp.this, "Произошла ошибка при проверке существующего пользователя", Toast.LENGTH_SHORT).show();
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
