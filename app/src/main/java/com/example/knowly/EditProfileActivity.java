package com.example.knowly;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.knowly.databinding.ActivityEditProfileBinding;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. 初始化 View Binding
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. 设置 "Interests Edit" 点击跳转逻辑 (新增部分)
        // 注意：如果你还没有创建 InterestsActivity.java，这行代码会报红
        // 请确保按照下面的步骤创建该文件
        binding.tvEditInterests.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, InterestsActivity.class);
            startActivity(intent);
        });

        // 3. 取消按钮：点击直接返回上一页
        binding.btnCancel.setOnClickListener(v -> finish());

        // 4. 保存按钮：获取输入框内容并回传数据
        binding.btnSave.setOnClickListener(v -> {
            String username = binding.etUsernameEdit.getText().toString().trim();
            String bio = binding.etBioEdit.getText().toString().trim();
            String credentials = binding.etCredentialsEdit.getText().toString().trim();

            // ✅ 安全检查：如果用户名为空，不允许保存
            if (username.isEmpty()) {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // 创建 Intent 携带数据
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updated_username", username);
            resultIntent.putExtra("updated_bio", bio);
            resultIntent.putExtra("updated_credentials", credentials);

            // 设置结果并关闭
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}