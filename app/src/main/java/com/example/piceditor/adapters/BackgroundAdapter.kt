package com.example.piceditor.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.piceditor.R

class BackgroundAdapter(
    context: Context,
    bgClickListener: OnBGClickListener
) : RecyclerView.Adapter<BackgroundAdapter.BackgroundHolder>() {


    var mImages: Array<String>
    var mContext = context
    var bgListener: OnBGClickListener = bgClickListener
    var selectedindex = 0

    init {
        mImages = mContext.assets.list("background") as Array<String>
    }

    interface OnBGClickListener {
        fun onBGClick(drawable: Drawable)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackgroundHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_frame, parent, false)

        return BackgroundHolder(view)
    }

    override fun getItemCount(): Int {
        return mImages.size
    }

    override fun onBindViewHolder(holder: BackgroundHolder, @SuppressLint("RecyclerView") position: Int) {

        val inputStream = mContext.assets.open("background/" + mImages[position])
        val drawable = Drawable.createFromStream(inputStream, null)
        holder.imgFrame.setImageDrawable(drawable)

        if (selectedindex == position) {
            holder.llItemFrame.setBackgroundColor(mContext.resources.getColor(R.color.colorAccent))
        } else {
            holder.llItemFrame.setBackgroundColor(mContext.resources.getColor(R.color.transparent))
        }

        holder.imgFrame.setOnClickListener {
            selectedindex = position

            if (drawable != null) {
                bgListener.onBGClick(drawable)
            }
            notifyDataSetChanged()
        }
    }

    class BackgroundHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgFrame: ImageView = itemView.findViewById(R.id.img_frame)
        var llItemFrame: LinearLayout = itemView.findViewById(R.id.ll_itemframe)
    }
}