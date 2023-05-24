package com.example.piceditor.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import com.example.piceditor.R
import com.example.piceditor.utils.AndroidUtils


class GalleryAlbumImageAdapter(context: Context, list: List<String>) :
    ArrayAdapter<String>(context, R.layout.item_gallery_photo, list) {

    var mInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var view: View
        var viewHolder: ViewHolder
        if (convertView == null) {
            viewHolder = ViewHolder()
            view = mInflater.inflate(R.layout.item_gallery_photo, parent, false)
            viewHolder.imageView = view.findViewById(R.id.imageView)
            view.setTag(viewHolder)
        } else {
            view = convertView
            viewHolder = convertView.getTag() as ViewHolder
        }

        AndroidUtils.loadImageWithGlide(context, viewHolder.imageView, getItem(position))
        return view
    }

    inner class ViewHolder {
        lateinit var imageView: ImageView
    }
}