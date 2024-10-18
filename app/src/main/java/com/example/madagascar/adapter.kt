package com.example.madagascar

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter

class adapter(var context: Context): PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): Any {

    var view : View ?= null;
        var inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.pager_adapter,container,false);
        var imageView = view.findViewById<ImageView>(R.id.imageView);
        var textView : TextView = view.findViewById(R.id.txt)

        if(position==0){
            imageView.setBackgroundColor((Color.parseColor("#bdbdbd")))
            textView.text = "첫번째"
        }else if(position==1){
            imageView.setBackgroundColor((Color.parseColor("#FF0000")))
            textView.text = "두번째"
        }else if (position == 2) {
            imageView.setBackgroundColor((Color.parseColor("#1DDB16")))
            textView.text = "세번째"
        }else if (position == 3) {
            imageView.setBackgroundColor((Color.parseColor("#F361DC")))
            textView.text = "네번째"
                }
                container.addView(view);
                return view;
            }
            override fun destroyItem(container: ViewGroup, position: Int,`object`:Any) {
                container.removeView(`object` as View)
            }
            override fun isViewFromObject(view: View, `object`:Any): Boolean {
            return view==`object`
            }
            override fun getCount(): Int {
                return 4

            }
        }