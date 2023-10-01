package mica.part1.checkMate.Home.BloodPressure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import mica.part1.checkMate.databinding.ActivityBloodPressureHomeBinding

class BloodPressureHomeActivity : AppCompatActivity() {
    val binding by lazy{ ActivityBloodPressureHomeBinding.inflate(layoutInflater)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 버튼 클릭 받기
        binding.bloodPressureButton.setOnClickListener {
            binding.bloodPressureTextView.text = "혈압 측정중..\n 손목의 움직임을 자제해주세요."
            // isVisible 사용시 gone역할(완전 빈공간 만듬 즉 걍 없어진다고 보면 됨) isInvisible 사용시 그 공백은 유지해준체 빈공간 만듬
            binding.bloodPressureCircleProgressBar.isInvisible = true // 안보이게 하는것.
            binding.bloodPressureProgressBar.isVisible = true // 보이게 하는것.
            it.setEnabled(false)
            // 다음 액티비티로 보내기
            val nextIntent = Intent(this, BloodPressureActivity::class.java)
            startActivity(nextIntent)
        }

    }
}