package com.example.diariopersonal;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Settings extends AppCompatActivity {

    //instanciar firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public TextView usuarioLbl, correoLbl;
    public Button cerrarBtn, eliminarBtn;
    public ImageView avatarImgv;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_settings);
        usuarioLbl = findViewById(R.id.lblUsuario);
        correoLbl = findViewById(R.id.lblCorreo);
        cerrarBtn = findViewById(R.id.btnCerrar);
        eliminarBtn = findViewById(R.id.btnEliminar);
        avatarImgv = findViewById(R.id.imgvAvatar);

        //obtener el usuario actual
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //referencia al documento del usuario
        DocumentReference docRef = db.collection("usuarios").document(user.getUid());
        //obtener la primera letra del usuario

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                //obtener la primera letra del usuario si se registro con correo
                if (documentSnapshot.exists()) {
                    String usuario = documentSnapshot.getString("usuario");
                    char letra = usuario.charAt(0);
                    generarImagenConLetra(letra);
                }
                //obtener la primera letra del usuario si se registro con google
                else {
                    String usuario = user.getDisplayName();
                    char letra = usuario.charAt(0);
                    generarImagenConLetra(letra);
                }
            }
        });

        //llenar los campos con la informacion del usuario
        if (user != null) {
            if (user.getDisplayName() != null) {
                usuarioLbl.setText(user.getDisplayName());
            } else {
                docRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        usuarioLbl.setText(documentSnapshot.getString("usuario"));
                    }
                });
            }
            correoLbl.setText(user.getEmail());
        }

        //llamar al metodo para cerrar sesion
        cerrarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoConfirmacion("Cerrar sesión", "¿Estás seguro que deseas cerrar sesión?", () -> cerrarSesion());
            }
        });

        //llamar al metodo para eliminar cuenta
        eliminarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoConfirmacion("Eliminar cuenta", "¿Estás seguro que deseas eliminar tu cuenta?", () -> eliminarCuenta());
            }
        });

    }

    // Método genérico para mostrar el diálogo de confirmación
    private void mostrarDialogoConfirmacion(String titulo, String mensaje, Runnable accionConfirmar) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Si el usuario confirma, se ejecuta la acción pasada como parámetro
                        accionConfirmar.run();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }



    //método para cerrar sesion
    private void cerrarSesion() {
        FirebaseAuth.getInstance().signOut();

        SharedPreferences preferences = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        Intent intent = new Intent(Settings.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    //método para eliminar cuenta
    private void eliminarCuenta() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("usuarios").document(user.getUid()).delete();
            user.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    eliminarCuenta();
                }
            });
        }
    }

    //método para generar imagen con la primera letra del usuario
    private void generarImagenConLetra(char letra) {
        Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#FF4081"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(100);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(String.valueOf(letra), 100, 100, paint);
        avatarImgv.setImageBitmap(bitmap);
    }
}
