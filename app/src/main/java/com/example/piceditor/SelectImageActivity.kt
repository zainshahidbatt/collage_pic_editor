package com.example.piceditor

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.piceditor.adapters.SelectedPhotoAdapter
import com.example.piceditor.uiFragments.GalleryAlbumFragment
import com.example.piceditor.uiFragments.GalleryAlbumImageFragment
import kotlinx.android.synthetic.main.activity_select_image.*
import java.io.File
import java.lang.Exception
import java.util.ArrayList

class SelectImageActivity : AppCompatActivity(), GalleryAlbumImageFragment.OnSelectImageListener,
    SelectedPhotoAdapter.OnDeleteButtonClickListener {


    override fun onDeleteButtonClick(str: String) {

        mSelectedImages.remove(str)
        mSelectedPhotoAdapter.notifyDataSetChanged()
        val textView = text_imgcount
        val str2 = "Select upto 10 photo(s)"
        val sb = StringBuilder()
        sb.append("(")
        sb.append(this.mSelectedImages.size)
        sb.append(")")
        textView.text = str2 + sb.toString()
    }

    private val mSelectedImages = ArrayList<String>()
    private var maxIamgeCount = 10
    private lateinit var mSelectedPhotoAdapter: SelectedPhotoAdapter
    private var mLastClickTime: Long = 0
    fun checkClick() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
    }


    override fun onSelectImage(str: String) {
        if (this.mSelectedImages.size == this.maxIamgeCount) {
            Toast.makeText(
                this,
                String.format("You only need %d photo(s)", maxIamgeCount),
                Toast.LENGTH_SHORT
            )
                .show()
        } else {
            var uri = Uri.fromFile(File(str))

            this.mSelectedImages.add(str)
            this.mSelectedPhotoAdapter.notifyDataSetChanged()
            val textView = text_imgcount
            val str2 = "Select upto 10 photo(s)"
            val sb = StringBuilder()
            sb.append("(")
            sb.append(this.mSelectedImages.size)
            sb.append(")")
            textView.text = str2 + sb.toString()
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_image)
        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener{
            onBackPressed()
        }

        mSelectedPhotoAdapter = SelectedPhotoAdapter(mSelectedImages, this)

        list_images.hasFixedSize()
        list_images.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        list_images.adapter = mSelectedPhotoAdapter

        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, GalleryAlbumFragment(this)).commit()

        btn_next.setOnClickListener {
            checkClick()
            createCollage()
        }
    }

    fun createCollage() {
        if (mSelectedImages.size == 0) {
            Toast.makeText(this, "Please select photo(s)", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val intent = Intent(this, CollageActivity::class.java)
            intent.putExtra("imageCount", mSelectedImages.size)
            intent.putExtra("selectedImages", mSelectedImages)
            intent.putExtra("imagesinTemplate", mSelectedImages.size)

            startActivityForResult(intent, 111)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == 111) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
