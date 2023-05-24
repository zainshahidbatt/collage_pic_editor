package com.example.piceditor

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.ViewTreeObserver
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.piceditor.adapters.BackgroundAdapter
import com.example.piceditor.adapters.FrameAdapter
import com.example.piceditor.frame.FramePhotoLayout
import com.example.piceditor.model.TemplateItem
import com.example.piceditor.multitouch.PhotoView
import com.example.piceditor.utils.AndroidUtils
import com.example.piceditor.utils.FrameImageUtils
import com.example.piceditor.utils.ImageUtils
import kotlinx.android.synthetic.main.activity_collage.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

open class CollageActivity : AppCompatActivity(), View.OnClickListener,
    FrameAdapter.OnFrameClickListener, BackgroundAdapter.OnBGClickListener {



    var mFramePhotoLayout: FramePhotoLayout? = null
    var DEFAULT_SPACE: Float = 0.0f
    var MAX_SPACE: Float = 0.0f
    var MAX_CORNER: Float = 0.0f

    private val RATIO_SQUARE = 0
    private val RATIO_GOLDEN = 2

    private var mSpace = DEFAULT_SPACE
    private var mCorner = 0f
    val MAX_SPACE_PROGRESS = 300.0f
    val MAX_CORNER_PROGRESS = 200.0f
    private var mBackgroundColor = Color.WHITE
    private var mBackgroundImage: Bitmap? = null
    private var mBackgroundUri: Uri? = null
    private var mSavedInstanceState: Bundle? = null
    private var mLayoutRatio = RATIO_SQUARE
    private lateinit var mPhotoView: PhotoView
    protected var mOutputScale = 1f
    protected var mSelectedTemplateItem: TemplateItem? = null
    private var mImageInTemplateCount = 0
    private var mTemplateItemList: ArrayList<TemplateItem>? = ArrayList()
    private var mSelectedPhotoPaths: MutableList<String> = java.util.ArrayList()

    lateinit var frameAdapter: FrameAdapter
    lateinit var img_background: ImageView

    private var mLastClickTime: Long = 0

    private fun checkClick() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
    }

    override fun onBGClick(drawable: Drawable) {

        val bmp = mFramePhotoLayout!!.createImage()
        val bitmap = (drawable as BitmapDrawable).bitmap
        mBackgroundImage = AndroidUtils.resizeImageToNewSize(bitmap, bmp.width, bmp.height)

//        img_background.background = BitmapDrawable(resources, mBackgroundImage)
        img_background.setImageBitmap(mBackgroundImage)

    }

    override fun onFrameClick(templateItem: TemplateItem) {

        mSelectedTemplateItem!!.isSelected = false

        for (idx in 0 until mSelectedTemplateItem!!.photoItemList.size) {
            val photoItem = mSelectedTemplateItem!!.photoItemList[idx]
            if (photoItem.imagePath != null && photoItem.imagePath!!.length > 0) {
                if (idx < mSelectedPhotoPaths.size) {
                    mSelectedPhotoPaths.add(idx, photoItem.imagePath!!)
                } else {
                    mSelectedPhotoPaths.add(photoItem.imagePath!!)
                }
            }
        }

        val size = Math.min(mSelectedPhotoPaths.size, templateItem.photoItemList.size)
        for (idx in 0 until size) {
            val photoItem = templateItem.photoItemList.get(idx)
            if (photoItem.imagePath == null || photoItem.imagePath!!.length < 1) {
                photoItem.imagePath = mSelectedPhotoPaths[idx]
            }
        }

        mSelectedTemplateItem = templateItem
        mSelectedTemplateItem!!.isSelected = true
        frameAdapter.notifyDataSetChanged()
        buildLayout(templateItem)
    }

    inner class space_listener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            mSpace = MAX_SPACE * seekBar!!.getProgress() / MAX_SPACE_PROGRESS
            if (mFramePhotoLayout != null)
                mFramePhotoLayout!!.setSpace(mSpace, mCorner)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }
    }

    inner class corner_listener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            mCorner = MAX_CORNER * seekBar!!.getProgress() / MAX_CORNER_PROGRESS
            if (mFramePhotoLayout != null)
                mFramePhotoLayout!!.setSpace(mSpace, mCorner)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }
    }

    override fun onClick(v: View?) {

        val tab_layout = findViewById<LinearLayout>(R.id.tab_layout)
        val tab_border = findViewById<LinearLayout>(R.id.tab_border)
        val tab_bg = findViewById<LinearLayout>(R.id.tab_bg)

        val ll_frame = findViewById<LinearLayout>(R.id.ll_frame)
        val ll_border = findViewById<LinearLayout>(R.id.ll_border)
        val ll_bg = findViewById<LinearLayout>(R.id.ll_bg)

        when (v!!.id) {

            R.id.tab_layout -> {
                tab_layout.setBackgroundColor(resources.getColor(R.color.darkBrown))
                tab_border.setBackgroundColor(resources.getColor(R.color.woodsmoke))
                tab_bg.setBackgroundColor(resources.getColor(R.color.woodsmoke))

                ll_frame.visibility = View.VISIBLE
                ll_border.visibility = View.GONE
                ll_bg.visibility = View.GONE
            }

            R.id.tab_border -> {
                tab_layout.setBackgroundColor(resources.getColor(R.color.woodsmoke))
                tab_border.setBackgroundColor(resources.getColor(R.color.darkBrown))
                tab_bg.setBackgroundColor(resources.getColor(R.color.woodsmoke))

                ll_frame.visibility = View.GONE
                ll_border.visibility = View.VISIBLE
                ll_bg.visibility = View.GONE
            }

            R.id.tab_bg -> {
                tab_layout.setBackgroundColor(resources.getColor(R.color.woodsmoke))
                tab_border.setBackgroundColor(resources.getColor(R.color.woodsmoke))
                tab_bg.setBackgroundColor(resources.getColor(R.color.darkBrown))

                ll_frame.visibility = View.GONE
                ll_border.visibility = View.GONE
                ll_bg.visibility = View.VISIBLE

            }

            R.id.btn_next -> {

                checkClick()

                var outStream: FileOutputStream? = null
                try {
                    val collageBitmap = createOutputImage()
                    outStream = FileOutputStream(File(cacheDir, "tempBMP"))
                    collageBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outStream)
                    outStream.close()
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                val intent = Intent(this, FilterCollageActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collage)

        val list_bg = findViewById<RecyclerView>(R.id.list_bg)

        val tab_layout = findViewById<LinearLayout>(R.id.tab_layout)
        val tab_border = findViewById<LinearLayout>(R.id.tab_border)
        val tab_bg = findViewById<LinearLayout>(R.id.tab_bg)

        val seekbar_space = findViewById<SeekBar>(R.id.seekbar_space)
        val seekbar_corner = findViewById<SeekBar>(R.id.seekbar_corner)

        val rl_container = findViewById<RelativeLayout>(R.id.rl_container)

        val list_frames = findViewById<RecyclerView>(R.id.list_frames)

        val back = findViewById<ImageView>(R.id.btn_back)
        back.setOnClickListener() {
            finish()
        }

        DEFAULT_SPACE = ImageUtils.pxFromDp(this, 2F)
        MAX_SPACE = ImageUtils.pxFromDp(this, 30F)
        MAX_CORNER = ImageUtils.pxFromDp(this, 60F)
        mSpace = DEFAULT_SPACE

        if (savedInstanceState != null) {
            mSpace = savedInstanceState.getFloat("mSpace")
            mCorner = savedInstanceState.getFloat("mCorner")
            mSavedInstanceState = savedInstanceState
        }

        mImageInTemplateCount = intent.getIntExtra("imagesinTemplate", 0)
        val extraImagePaths = intent.getStringArrayListExtra("selectedImages")

        list_bg.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        list_bg.adapter = BackgroundAdapter(this, this)

        tab_layout.setOnClickListener(this)
        tab_border.setOnClickListener(this)
        tab_bg.setOnClickListener(this)

        seekbar_space.setOnSeekBarChangeListener(space_listener())
        seekbar_corner.setOnSeekBarChangeListener(corner_listener())

        mPhotoView = PhotoView(this)
        rl_container.viewTreeObserver
            .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    mOutputScale = ImageUtils.calculateOutputScaleFactor(
                        rl_container.width,
                        rl_container.height
                    )
                    buildLayout(mSelectedTemplateItem!!)
                    // remove listener
                    rl_container.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })

        img_background = findViewById<ImageView>(R.id.img_background)

        loadFrameImages()
        list_frames.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        frameAdapter = FrameAdapter(this, mTemplateItemList!!, this)
        list_frames.adapter = frameAdapter


        mSelectedTemplateItem = mTemplateItemList!!.get(0)
        mSelectedTemplateItem!!.isSelected = true

        if (extraImagePaths != null) {
            val size =
                Math.min(extraImagePaths.size, mSelectedTemplateItem!!.photoItemList.size)
            for (i in 0 until size)
                mSelectedTemplateItem!!.photoItemList[i].imagePath = extraImagePaths[i]
        }

        btn_next.setOnClickListener(this)
    }

    private fun loadFrameImages() {
        val mAllTemplateItemList = java.util.ArrayList<TemplateItem>()

        mAllTemplateItemList.addAll(FrameImageUtils.loadFrameImages(this))

        mTemplateItemList = java.util.ArrayList<TemplateItem>()
        if (mImageInTemplateCount > 0) {
            for (item in mAllTemplateItemList)
                if (item.photoItemList.size === mImageInTemplateCount) {
                    mTemplateItemList!!.add(item)
                }
        } else {
            mTemplateItemList!!.addAll(mAllTemplateItemList)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putFloat("mSpace", mSpace)
        outState.putFloat("mCornerBar", mCorner)
        if (mFramePhotoLayout != null) {
            mFramePhotoLayout!!.saveInstanceState(outState)
        }

    }

    fun buildLayout(item: TemplateItem) {

        val rl_container = findViewById<RelativeLayout>(R.id.rl_container)

        val seekbar_space = findViewById<SeekBar>(R.id.seekbar_space)
        val seekbar_corner = findViewById<SeekBar>(R.id.seekbar_corner)

        mFramePhotoLayout = FramePhotoLayout(this, item.photoItemList)

//        if (mBackgroundImage != null && !mBackgroundImage!!.isRecycled()) {
//            if (Build.VERSION.SDK_INT >= 16)
//                rl_container.setBackground(BitmapDrawable(resources, mBackgroundImage))
//            else
//                rl_container.setBackgroundDrawable(BitmapDrawable(resources, mBackgroundImage))
//        } else {
//            rl_container.setBackgroundColor(mBackgroundColor)
//        }

        var viewWidth = rl_container.getWidth()
        var viewHeight = rl_container.getHeight()
        if (mLayoutRatio === RATIO_SQUARE) {
            if (viewWidth > viewHeight) {
                viewWidth = viewHeight
            } else {
                viewHeight = viewWidth
            }
        } else if (mLayoutRatio === RATIO_GOLDEN) {
            val goldenRatio = 1.61803398875
            if (viewWidth <= viewHeight) {
                if (viewWidth * goldenRatio >= viewHeight) {
                    viewWidth = (viewHeight / goldenRatio).toInt()
                } else {
                    viewHeight = (viewWidth * goldenRatio).toInt()
                }
            } else if (viewHeight <= viewWidth) {
                if (viewHeight * goldenRatio >= viewWidth) {
                    viewHeight = (viewWidth / goldenRatio).toInt()
                } else {
                    viewWidth = (viewHeight * goldenRatio).toInt()
                }
            }
        }

        mOutputScale = ImageUtils.calculateOutputScaleFactor(viewWidth, viewHeight)
        mFramePhotoLayout!!.build(viewWidth, viewHeight, mOutputScale, mSpace, mCorner)
        if (mSavedInstanceState != null) {
            mFramePhotoLayout!!.restoreInstanceState(mSavedInstanceState!!)
            mSavedInstanceState = null
        }
        val params = RelativeLayout.LayoutParams(viewWidth, viewHeight)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        rl_container.removeAllViews()

        rl_container.removeView(img_background)
        rl_container.addView(img_background, params)

        rl_container.addView(mFramePhotoLayout, params)
        //add sticker view
        rl_container.removeView(mPhotoView)
        rl_container.addView(mPhotoView, params)
        //reset space and corner seek bars

        seekbar_space.progress = (MAX_SPACE_PROGRESS * mSpace / MAX_SPACE).toInt()
        seekbar_corner.progress = (MAX_CORNER_PROGRESS * mCorner / MAX_CORNER).toInt()
    }

    @Throws(OutOfMemoryError::class)
    fun createOutputImage(): Bitmap {
        try {
            val template = mFramePhotoLayout!!.createImage()
            val result =
                Bitmap.createBitmap(template.width, template.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            if (mBackgroundImage != null && !mBackgroundImage!!.isRecycled()) {
                canvas.drawBitmap(
                    mBackgroundImage!!,
                    Rect(0, 0, mBackgroundImage!!.getWidth(), mBackgroundImage!!.getHeight()),
                    Rect(0, 0, result.width, result.height),
                    paint
                )
            } else {
                canvas.drawColor(mBackgroundColor)
            }

            canvas.drawBitmap(template, 0f, 0f, paint)
            template.recycle()
            var stickers = mPhotoView.getImage(mOutputScale)
            canvas.drawBitmap(stickers!!, 0f, 0f, paint)
            stickers.recycle()
            stickers = null
            System.gc()
            return result
        } catch (error: OutOfMemoryError) {
            throw error
        }
    }
}
