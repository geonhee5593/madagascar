    package com.example.madagascar.freeborad

    import android.content.Context
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.BaseAdapter
    import android.widget.TextView
    import com.example.madagascar.R

    class FreeBoardAdapter(private val context: Context, private val items: List<FreeBoardItem>) :
        BaseAdapter() {

        override fun getCount(): Int = items.size
        override fun getItem(position: Int): Any = items[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_freeboard, parent, false)
            val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
            val viewsTextView = view.findViewById<TextView>(R.id.viewsTextView)
            val dateTextView = view.findViewById<TextView>(R.id.dateTextView)
            val userIdTextView = view.findViewById<TextView>(R.id.usernameTextView)

            val item = items[position]
            titleTextView.text = item.title
            viewsTextView.text = "조회수: ${item.views}"
            dateTextView.text = "등록일: ${item.date}"
            userIdTextView.text = "작성자 ID: ${item.userId}" // Firestore에서 가져온 id 필드 값 표시

            return view
        }

    }
