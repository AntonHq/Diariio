package com.example.diariopersonal;

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
    public TextView usuarioLbl, correoLbl, contraseñaLbl;
    public Button cerrarsesionLbl;

    public ImageView avatarImgv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_settings);
        usuarioLbl = findViewById(R.id.lblUsuario);
        correoLbl = findViewById(R.id.lblCorreo);
        contraseñaLbl = findViewById(R.id.lblContraseña);
        cerrarsesionLbl = findViewById(R.id.btnCerrar);
        avatarImgv = findViewById(R.id.imgvAvatar);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DocumentReference docRef = db.collection("usuarios").document(user.getUid());

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String usuario = documentSnapshot.getString("usuario");
                    if (usuario != null && !usuario.isEmpty()) {
                        char primeraLetra = usuario.charAt(0);
                        generarImagenConLetra(primeraLetra);
                    }
                }
            }
        });

        if (user != null) {
            //recuperar datos del usuario desde firestore
            db.collection("usuarios").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    usuarioLbl.setText(documentSnapshot.getString("usuario"));
                    correoLbl.setText(documentSnapshot.getString("correo"));
                    contraseñaLbl.setText("********");
                }
            });
        }

        cerrarsesionLbl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    public void generarImagenConLetra(char letra) {
        int size = 200; // Tamaño de la imagen
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(100);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);

        // Dibujar el fondo de la imagen random pero colores claros
        canvas.drawRGB((int) (Math.random() * 128) + 128, (int) (Math.random() * 128) + 128, (int) (Math.random() * 128) + 128);

        // Dibujar la letra en el centro de la imagen
        canvas.drawText(String.valueOf(letra), size / 2, size / 2 - ((paint.descent() + paint.ascent()) / 2), paint);

        //hacer imagen circular
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, size, size);

        // Asignar el Bitmap al ImageView
        avatarImgv.setImageBitmap(bitmap);
    }


    private void logout() {
        FirebaseAuth.getInstance().signOut();

        SharedPreferences preferences = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        Intent intent = new Intent(Settings.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
