package com.example.diariopersonal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.HashMap;
import java.util.Map;

public class newUser extends AppCompatActivity {

    // Inicializar la base de datos firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseAuth mAuth;
    private EditText correoEditText, contraseñaEditText, usuarioEditText;
    private TextView errorTxv;
    private Button registrarButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        // Inicializar la instancia de Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Conectar las vistas
        correoEditText = findViewById(R.id.correoReEditText);
        contraseñaEditText = findViewById(R.id.contraseñaReEditText);
        registrarButton = findViewById(R.id.registrarButton);
        errorTxv = findViewById(R.id.errortextView);
        usuarioEditText = findViewById(R.id.usuarioReEditText);

        // ocultar el mensaje de error
        errorTxv.setVisibility(View.GONE);


        // Configurar el listener del botón de registro
        registrarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String correo = correoEditText.getText().toString();
                String contraseña = contraseñaEditText.getText().toString();
                String usuario = usuarioEditText.getText().toString();
                if (correo.isEmpty() || contraseña.isEmpty() || usuario.isEmpty()){
                    errorTxv.setText("Correo o contraseña vacíos");
                    errorTxv.setVisibility(View.VISIBLE);
                } else if (contraseña.length() < 6){
                    errorTxv.setText("Contraseña debe tener al menos 6 caracteres");
                    errorTxv.setVisibility(View.VISIBLE);
                } else if (!correo.contains("@") || !correo.contains(".")){
                    errorTxv.setText("Correo inválido");
                    errorTxv.setVisibility(View.VISIBLE);
                } else if (usuario.length() < 4){
                    errorTxv.setText("Usuario debe tener al menos 4 caracteres");
                    errorTxv.setVisibility(View.VISIBLE);
                } else{
                    // Crear cuenta
                    createAccount(correo, contraseña, usuario);
                    errorTxv.setVisibility(View.GONE);
                }
            }
        });
}

    private void createAccount(String email, String password, String username){
        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Crear un nuevo usuario en la base de datos
                            Map<String, Object> usuario = new HashMap<>();
                            usuario.put("usuario", username);
                            usuario.put("correo", email);

                            // Añadir el usuario a la base de datos
                            db.collection("usuarios").document(user.getUid())
                                    .set(usuario)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                // Si se registra correctamente mostrar mensaje
                                                Toast.makeText(newUser.this, "Usuario Registrado en la Base de Datos",
                                                        Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Si falla mostrar mensaje de error
                                                Toast.makeText(newUser.this, "Error al registrar usuario en la base de datos",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                            updateUI(user);
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(newUser.this, "El correo electrónico ya está en uso.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(newUser.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            updateUI(null);
                        }
                    }
                });
        // [END create_user_with_email]
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Navegar a la actividad Main del diario
            Intent intent = new Intent(newUser.this, MainActivity.class);
            startActivity(intent);
        } else {
            // Mostrar un mensaje de error
            errorTxv.setText("Error interno al Registrar Usuario");
            errorTxv.setVisibility(View.VISIBLE);
        }
    }
}