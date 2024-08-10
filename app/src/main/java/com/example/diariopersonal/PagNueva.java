package com.example.diariopersonal;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PagNueva extends AppCompatActivity {
    private TextView fechaLbl, estadoLbl;
    private EditText tituloTxt;
    private TextInputEditText contenidoTxt;
    private Button guardarBtn, agregarImagenBtn;
    private DocumentReference notaRef; //Referencia al documento de la nota en Firestore


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pag_nueva);

        //inicializar componentes
        fechaLbl = findViewById(R.id.lblFecha);
        estadoLbl = findViewById(R.id.lblEstado);
        tituloTxt = findViewById(R.id.txtTItulo);
        contenidoTxt = findViewById(R.id.txtContenido);
        guardarBtn = findViewById(R.id.btnGuardar);
        agregarImagenBtn = findViewById(R.id.btnAgregarImagen);

        // Configurar la fecha actual
        fechaLbl.setText(getCurrentDate());

        // Detectar cambios en los campos de texto
        tituloTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                estadoLbl.setText("Cambios no guardados");
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        contenidoTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                estadoLbl.setText("Cambios no guardados");
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Configurar los listeners
        guardarBtn.setOnClickListener(v -> saveNote());
        agregarImagenBtn.setOnClickListener(v -> addImage());
    }

    // Método para obtener la fecha actual
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Método para guardar la nota en la base de datos firestore
    private void saveNote() {
        String titulo = tituloTxt.getText().toString();
        String contenido = contenidoTxt.getText().toString();
        String fecha = getCurrentDate();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (titulo.isEmpty() || contenido.isEmpty()) {
            Toast.makeText(this, "El título y el contenido no pueden estar vacíos", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (notaRef == null) {
            // Crear una nueva nota
            notaRef = db.collection("notas").document();
        }

        // Crear o actualizar la nota en Firestore
        Nota nota = new Nota(titulo, contenido, fecha, user != null ? user.getUid() : null);
        notaRef.set(nota)
                .addOnSuccessListener(aVoid -> {
                    estadoLbl.setText("Cambios guardados");
                    Toast.makeText(PagNueva.this, "Nota guardada/actualizada con éxito", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PagNueva.this, "Error al guardar la nota", Toast.LENGTH_SHORT).show();
                });
    }

    private void addImage() {

    }
}