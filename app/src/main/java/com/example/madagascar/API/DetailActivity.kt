package com.example.madagascar.API

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.madagascar.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailActivity : AppCompatActivity() {
    private lateinit var titleView: TextView
    private lateinit var addressView: TextView
    private lateinit var overviewView: TextView
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        titleView = findViewById(R.id.title)
        addressView = findViewById(R.id.address)
        overviewView = findViewById(R.id.overview)
        imageView = findViewById(R.id.image)

        val contentId = intent.getStringExtra("contentId")
        if (contentId != null) {
            fetchCommonData(contentId)
        }
    }

    private fun fetchCommonData(contentId: String) {
        val call = RetrofitClient.instance.getCommon(contentId)
        call.enqueue(object : Callback<CommonResponse> {
            override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                val item = response.body()?.response?.body?.items?.item?.firstOrNull()
                if (item != null) {
                    titleView.text = item.title
                    addressView.text = item.addr1
                    overviewView.text = item.overview

                    Glide.with(this@DetailActivity)
                        .load(item.firstimage)
                        .into(imageView)
                }
            }

            override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                Toast.makeText(this@DetailActivity, "상세 정보 조회 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }
}