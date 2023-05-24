package com.example.piceditor.uiFragments


import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.example.piceditor.R
import com.example.piceditor.adapters.GalleryAlbumImageAdapter

import kotlinx.android.synthetic.main.fragment_gallery_album_image.view.*

class GalleryAlbumImageFragment : Fragment() {

    companion object {
        val ALBUM_IMAGE_EXTRA = "albumImage"
        val ALBUM_NAME_EXTRA = "albumName"
    }

    var mImages: ArrayList<String> = ArrayList()
    lateinit var names: String
    lateinit var mListener: OnSelectImageListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (activity is OnSelectImageListener) {
            mListener = activity as OnSelectImageListener
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_gallery_album_image, container, false)

        if (arguments != null) {
            mImages = requireArguments().getStringArrayList(ALBUM_IMAGE_EXTRA)!!
            names = requireArguments().getString(ALBUM_NAME_EXTRA)!!

            if (mImages != null) {

                view.gridView.adapter = GalleryAlbumImageAdapter(view.context, mImages)
                view.gridView.onItemClickListener = object : AdapterView.OnItemClickListener {
                    override fun onItemClick(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (mListener != null) {
                            mListener.onSelectImage(mImages[position])
                        }
                    }
                }
            }
        }
        return view
    }

    interface OnSelectImageListener {
        fun onSelectImage(str: String)
    }
}
