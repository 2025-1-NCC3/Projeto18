package br.fecap.pi.ubershield.network.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import br.fecap.pi.ubershield.R;

public class AppConfigActivity extends AppCompatActivity {

    private LinearLayout homeButton, activityButton, activityLayout, accountButton, userProfileLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_config);

        // Referencia os Botões
        homeButton = findViewById(R.id.homeButton);
        activityButton = findViewById(R.id.activityButton);
        activityLayout = findViewById(R.id.activityLayout);
        accountButton = findViewById(R.id.accountButton);
        userProfileLayout = findViewById(R.id.userProfileLayout);

        // Trocando Telas
        homeButton.setOnClickListener(v -> {
            startActivity(new Intent(AppConfigActivity.this, MainActivity.class));
        });

        activityButton.setOnClickListener(v -> {
            startActivity(new Intent(AppConfigActivity.this, HistoryActivity.class));
        });

        activityLayout.setOnClickListener(v -> {
            startActivity(new Intent(AppConfigActivity.this, HistoryActivity.class));
        });

        accountButton.setOnClickListener(v -> {
            startActivity(new Intent(AppConfigActivity.this, UserInfoActivity.class));
        });

        userProfileLayout.setOnClickListener(v -> {
            startActivity(new Intent(AppConfigActivity.this, UserInfoActivity.class));
        });
    }
}
