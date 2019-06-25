package com.ksy.vision.portfolio

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.FragmentManager
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_analyze_view.*
import java.io.File
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var LandMarkMap: GoogleMap
    private lateinit var backPressHolder: OnBackPressHolder
    private val CAMERA_PERMISSION_REQ_CODE = 1000
    private val GALLERY_PERMISSION_REQ_CODE = 1001
    private val FILE_NAME = "picture.jpg"
    private var uploadChooser : UploadChooser? = null
    private var labelDetectionTask : LabelDetectionTask? =null
    val LABEL_DETECTION_REQ_NUM = 0
    val LANDMARK_DETECTION_REQ_NUM = 1
    private var lat:Double = 0.0
    private var lon:Double = 0.0
    private lateinit var mapFragment:SupportMapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        labelDetectionTask = LabelDetectionTask(
            packageName= packageName,
            packageManager = packageManager,
            activity = this
        )
        backPressHolder = OnBackPressHolder()

        mapFragment = supportFragmentManager
            .findFragmentById(R.id.landmark_map) as SupportMapFragment
        setupListener()
    }

    override fun onBackPressed() {
        backPressHolder.onBackPressed()
    }

    private fun drawView(analysisData:String, analysisType:Int){
        // 데이터를 넘겨받아(LabelDetectionTask) 데이터를 정제 후 차트형식(horizontal bar) 으로 볼수 있게 ListView 에 보여주는 형식
        val analysisList = analysisData.split("/")
        if(analysisType == LABEL_DETECTION_REQ_NUM){
            labelDraw(analysisList)
        }else if(analysisType == LANDMARK_DETECTION_REQ_NUM){
            landMarkDraw(analysisList)
        }
    }

    private fun labelDraw(Analysislabel: List<String>){
        if(landmark_map_container.visibility == View.VISIBLE){
            landmark_map_container.visibility = View.GONE
        }
        val list:ArrayList<String> = Analysislabel as ArrayList<String>
        list.removeAt(0)
        val chartAdapter = ChartListAdapter(this@MainActivity,list)
        analysis_label.adapter = chartAdapter
        analysis_label.visibility = View.VISIBLE
    }

    private fun landMarkDraw(AnalysisLandMark: List<String>){
        mapFragment.getMapAsync(this)

        if(analysis_label.visibility == View.VISIBLE){
            landmark_map_container.visibility = View.GONE
        }

        val landMarkRefList = AnalysisLandMark[0].split(":")
        landMarkRefList.let {
            landmark_per.text = it[0] + "%"
            landmark_desc.text = it[1]
            lat = it[2].toDouble()
            lon = it[3].toDouble()
        }
        landmark_map_container.visibility = View.VISIBLE
    }

    override fun onMapReady(googleMap: GoogleMap) {
        LandMarkMap = googleMap
        val marker = LatLng(lat,lon)
            LandMarkMap.addMarker(MarkerOptions().position(marker))
            LandMarkMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker,15.0f))
    }

    private fun setupListener(){
        upload_image.setOnClickListener {
            uploadChooser = UploadChooser().apply {
                addNotifier(object : UploadChooser.UploadCooserNotifierInterface{
                    override fun cameraOnClick() {
                        checkCameraPermission()// 카메라 권한
                    }

                    override fun galleryOnClick() {
                        checkGalleryPermission()// 스토리지 권한
                    }
                })
            }
            uploadChooser!!.show(supportFragmentManager, "")
        }
    }

    private fun checkCameraPermission(){
        if(PermissionUtil().requestPermission(
            this, CAMERA_PERMISSION_REQ_CODE, Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
        )) openCamera()
    }

    private fun checkGalleryPermission(){
        if(PermissionUtil().requestPermission(
            this,
            GALLERY_PERMISSION_REQ_CODE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )) openGallery()
    }

    private fun openGallery(){
        val intent = Intent().apply {
            setType("image/*")
            setAction(Intent.ACTION_GET_CONTENT)
        }
        startActivityForResult(Intent.createChooser(intent,"Select a photo"),GALLERY_PERMISSION_REQ_CODE)
    }

    private fun openCamera(){
        val photoUri = FileProvider.getUriForFile(this,applicationContext.packageName + ".provider",
           createCameraFile()) // 카메라로 찍은 사진이 저장된 위치

        startActivityForResult(
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoUri)// 사진을 경로에 저장
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },CAMERA_PERMISSION_REQ_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            CAMERA_PERMISSION_REQ_CODE -> {
                if(resultCode != Activity.RESULT_OK) return
                val photoUri = FileProvider.getUriForFile(this,applicationContext.packageName + ".provider"
                ,createCameraFile())
                uploadImage(photoUri)
            }
            GALLERY_PERMISSION_REQ_CODE -> data?.let { uploadImage(it.data) }
        }
    }

    private fun resizeBitmapImg(source: Bitmap, maxResolution: Int): Bitmap {
        // 비트맵 파일의 크기로 인해 cloud vision api 의 속도가 많이 저하되어 리사이징해주는 함수
        val width = source.width
        val height = source.height
        var newWidth = width
        var newHeight = height
        var rate = 0.0f

        if (width > height) {
            if (maxResolution < width) {
                rate = maxResolution / width.toFloat()
                newHeight = (height * rate).toInt()
                newWidth = maxResolution
            }
        } else {
            if (maxResolution < height) {
                rate = maxResolution / height.toFloat()
                newWidth = (width * rate).toInt()
                newHeight = maxResolution
            }
        }
        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true)
    }

    private fun uploadImage(imageUri: Uri){
        val bitmap : Bitmap = resizeBitmapImg(MediaStore.Images.Media.getBitmap(contentResolver, imageUri),880)
        DetectionChooser().apply {
            addDetectionChooserNotifierInterface(object : DetectionChooser.DetectionChooserNotifierInterface{
                override fun detectLabel() {
//                   upload_image.setImageBitmap(bitmap)
                    findViewById<ImageView>(R.id.uploaded_image).setImageBitmap(bitmap)
                        requestCloudVisionApi(bitmap,LABEL_DETECTION_REQ_NUM)
                }
                override fun detectLandmark() {
                    findViewById<ImageView>(R.id.uploaded_image).setImageBitmap(bitmap)
                    requestCloudVisionApi(bitmap,LANDMARK_DETECTION_REQ_NUM)
                }
            })
        }.show(supportFragmentManager,"")
    }

    private fun requestCloudVisionApi(bitmap: Bitmap, requestType:Int){
        analysis_progress.visibility = View.VISIBLE
        labelDetectionTask?.requestCloudVisionApi(bitmap,object : LabelDetectionTask.LabelDetectionNotifierInterface{
            override fun notifiyResult(result: String) {
                if(result.equals("fail")){
                    uploaded_image_result.text = "분석 실패"
                    analysis_progress.visibility = View.GONE
                    analysis_label.visibility = View.INVISIBLE
                    landmark_map_container.visibility = View.INVISIBLE
                }else{
                    analysis_progress.visibility = View.GONE
                    uploaded_image_result.text = "분석 결과"
                    drawView(result,requestType)
                }
            }
        },requestType)
    }

    private fun createCameraFile(): File {
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(dir,FILE_NAME)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            GALLERY_PERMISSION_REQ_CODE -> {
                if(PermissionUtil().permissionGranted(requestCode,GALLERY_PERMISSION_REQ_CODE,grantResults)) openGallery()
            }
            CAMERA_PERMISSION_REQ_CODE -> {
                if(PermissionUtil().permissionGranted(requestCode,CAMERA_PERMISSION_REQ_CODE,grantResults)) openCamera()
            }
        }
    }

    inner class OnBackPressHolder(){
        private var backPressHolder : Long = 0

        fun onBackPressed(){
            if(System.currentTimeMillis() > backPressHolder + 2000 ){
                backPressHolder = System.currentTimeMillis()
                showBackToast()
                return
            }
            if(System.currentTimeMillis() <= backPressHolder + 2000){
                finishAffinity()
            }
        }

        fun showBackToast(){ // 사용자가 백버튼을 두 번 누르면 토스트메세지 출력
            Toast.makeText(this@MainActivity,"한번더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
