package com.example.diariopersonal;

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

import java.util.HashMap;
import java.util.Map;

public class newUser extends AppCompatActivity {

    // Inicializar la base de datos firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseAuth mAuth;
    private EditText correorTxt, contraseñarTxt, usuariorTxt;
    private TextView errorrLbl;
    private Button guardarBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        // Inicializar la instancia de Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Conectar las vistas
        correorTxt = findViewById(R.id.txtCorreor);
        contraseñarTxt = findViewById(R.id.txtContraseñar);
        guardarBtn = findViewById(R.id.btnGuardar);
        errorrLbl = findViewById(R.id.lblErrorr);
        usuariorTxt = findViewById(R.id.txtUsuarior);

        // ocultar el mensaje de error
        errorrLbl.setVisibility(View.GONE);


        // Configurar el listener del botón de registro
       guardarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String correo = correorTxt.getText().toString();
                String contraseña = contraseñarTxt.getText().toString();
                String usuario = usuariorTxt.getText().toString();
                if (correo.isEmpty() || contraseña.isEmpty() || usuario.isEmpty()){
                    errorrLbl.setText("Correo o contraseña vacíos");
                    errorrLbl.setVisibility(View.VISIBLE);
                } else if (contraseña.length() < 6){
                    errorrLbl.setText("Contraseña debe tener al menos 6 caracteres");
                    errorrLbl.setVisibility(View.VISIBLE);
                } else if (!correo.contains("@") || !correo.contains(".")){
                    errorrLbl.setText("Correo inválido");
                    errorrLbl.setVisibility(View.VISIBLE);
                } else if (usuario.length() < 4){
                    errorrLbl.setText("Usuario debe tener al menos 4 caracteres");
                    errorrLbl.setVisibility(View.VISIBLE);
                } else{
                    // Crear cuenta
                    createAccount(correo, contraseña, usuario);
                    errorrLbl.setVisibility(View.GONE);
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
            errorrLbl.setText("Error interno al Registrar Usuario");
            errorrLbl.setVisibility(View.VISIBLE);
        }
    }
}