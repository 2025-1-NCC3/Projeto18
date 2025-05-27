package br.fecap.pi.ubershield.network.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import br.fecap.pi.ubershield.R;

public class UserInfoActivity extends AppCompatActivity {

    private LinearLayout configButton, activityButton, homeButton;
    private RelativeLayout item_nome, item_genero, item_telefone, item_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_info);

        // Referencia os Botões
        homeButton = findViewById(R.id.homeButton);
        activityButton = findViewById(R.id.activityButton);
        configButton = findViewById(R.id.configButton);
        item_nome = findViewById(R.id.item_nome);
        item_genero = findViewById(R.id.item_genero);
        item_telefone = findViewById(R.id.item_telefone);
        item_email = findViewById(R.id.item_email);

        // Trocando Telas
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserInfoActivity.this, MainActivity.class);
            startActivity(intent);
        });

        configButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserInfoActivity.this, AppConfigActivity.class);
            startActivity(intent);
        });

        activityButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserInfoActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        item_nome.setOnClickListener(v -> {
            Intent intent = new Intent(UserInfoActivity.this, UserNameActivity.class);
            startActivity(intent);
        });

        item_genero.setOnClickListener(v -> {
            Intent intent = new Intent(UserInfoActivity.this, UserGenerActivity.class);
            startActivity(intent);
        });

        item_telefone.setOnClickListener(v -> {
            Intent intent = new Intent(UserInfoActivity.this, UserPhoneActivity.class);
            startActivity(intent);
        });

        item_email.setOnClickListener(v -> {
            Intent intent = new Intent(UserInfoActivity.this, UserEmailActivity.class);
            startActivity(intent);
        });
    }
}