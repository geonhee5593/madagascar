package com.example.madagascar

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.TableLayout
import com.google.android.material.tabs.TabLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager.widget.ViewPager

class MainActivity : AppCompatActivity() {
    lateinit var  temp : ArrayList<Drawable>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val favoritesBtn = findViewById<ImageView>(R.id.star1) //즐겨찾기 이동

        favoritesBtn.setOnClickListener {
            val intent = Intent(this, FavoritesActivity::class.java)
            startActivity(intent)
                }

        val fieldBtn = findViewById<ImageView>(R.id.btn_field)

        fieldBtn.setOnClickListener {
            val intent = Intent(this, fieldActivity::class.java)
            startActivity(intent)
                }
        val happguyBtn = findViewById<ImageView>(R.id.btn_happguy)

        happguyBtn.setOnClickListener {
            val intent = Intent(this, happguyActivity::class.java)
            startActivity(intent)
        }
                var a = adapter(this)
                var pager = findViewById<ViewPager>(R.id.viewPager2)
                pager.setAdapter(a)

                var tabLayout = findViewById<TableLayout>(R.id.tablayout)
                tabLayout.setupWithViewPager(pager, true)
            }
                }



