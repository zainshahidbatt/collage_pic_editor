package com.example.piceditor.multitouch

import com.example.piceditor.multitouch.MultiTouchEntity
import com.example.piceditor.multitouch.PhotoView

interface OnDoubleClickListener {
    fun onPhotoViewDoubleClick(view: PhotoView, entity: MultiTouchEntity)
    fun onBackgroundDoubleClick()
}
