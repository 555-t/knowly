package com.example.knowly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;

public class InterestsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);

        // 初始化控件
        ChipGroup chipGroup = findViewById(R.id.chip_group_selection);
        View btnConfirm = findViewById(R.id.btn_confirm_interests);
        View btnCancel = findViewById(R.id.btn_cancel);

        // 1. 取消按钮：直接关闭
        btnCancel.setOnClickListener(v -> finish());

        // 2. 确认按钮：收集选中的标签
        btnConfirm.setOnClickListener(v -> {
            ArrayList<String> selectedInterests = new ArrayList<>();

            // 遍历 ChipGroup 里的所有 Chip
            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                Chip chip = (Chip) chipGroup.getChildAt(i);
                // 如果标签被选中了，就把它的文字加到列表里
                if (chip.isChecked()) {
                    selectedInterests.add(chip.getText().toString());
                }
            }

            // 把结果传回 EditProfileActivity
            Intent resultIntent = new Intent();
            resultIntent.putStringArrayListExtra("selected_interests", selectedInterests);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}