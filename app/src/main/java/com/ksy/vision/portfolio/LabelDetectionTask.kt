package com.ksy.vision.portfolio

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.VisionRequest
import com.google.api.services.vision.v1.VisionRequestInitializer
import com.google.api.services.vision.v1.model.*
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.lang.StringBuilder
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

// 비트맵을 받아 해당 비트맵으로 req 보내고 res 받아오는 클래스
class LabelDetectionTask(
    private val packageName: String ,
    private val packageManager:PackageManager,
    private val activity:MainActivity
){
    private val CLOUD_VISION_API_KEY = activity.getString(R.string.google_cloud_key)
    private val ANDROID_PACKAGE_HEADER = "X-Android-Package"
    private val ANDROID_CERT_HEADER = "X-Android-Cert"
    private val MAX_RESULTS_LABEL = 10
    private val MAX_RESULTS_LANDMARK = 1

    private var labelDetectionNotifierInterface: LabelDetectionNotifierInterface? =null
    private var requestType : Int? = null


    interface LabelDetectionNotifierInterface{
        fun notifiyResult(result: String)
    }

    fun requestCloudVisionApi(bitmap: Bitmap,
                              labelDetectionNotifierInterface:LabelDetectionNotifierInterface,
                              requestType:Int){
        this.labelDetectionNotifierInterface = labelDetectionNotifierInterface
        this.requestType = requestType
        val visionTask = ImageRequestTask(prepareImageRequest(bitmap))
        visionTask.execute()
    }

    inner class ImageRequestTask constructor(
        val request : Vision.Images.Annotate
    ): AsyncTask<Any, Void, String>(){

        private val weakReference : WeakReference<MainActivity>

        init {
            weakReference = WeakReference(activity)
        }

        override fun doInBackground(vararg params: Any?): String {
            try{
                val response = request.execute()
                return findProperResponsType(response)
            }catch (e: Exception){
                e.printStackTrace()
            }
            return "fail"
        }

        override fun onPostExecute(result: String?) {
            val activity = weakReference.get()
            if(activity != null && !activity.isFinishing){ // 응답이 오기전에 현재 액티비티를 벗어놔도 오류가 나지않게 하는 처리
                result?.let { labelDetectionNotifierInterface?.notifiyResult(it) }
            }
        }
    }

    private fun prepareImageRequest(bitmap: Bitmap): Vision.Images.Annotate{
        val httpTransport = AndroidHttp.newCompatibleTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()


        // 요청 헤더 값 작성
        val requestInitializer = object : VisionRequestInitializer(CLOUD_VISION_API_KEY){
            override fun initializeVisionRequest(request: VisionRequest<*>?) {
                super.initializeVisionRequest(request)
                val packageName = packageName
                request?.requestHeaders?.set(ANDROID_PACKAGE_HEADER,packageName)
                val sig = PackageManagerUtil().getSignature(packageManager,packageName)
                request?.requestHeaders?.set(ANDROID_CERT_HEADER, sig)
            }
        }
        val builder = Vision.Builder(httpTransport, jsonFactory, null)
        builder.setVisionRequestInitializer(requestInitializer)
        val vision = builder.build()

        val batchAnnotateImagesRequest = BatchAnnotateImagesRequest()
        batchAnnotateImagesRequest.requests = object : ArrayList<AnnotateImageRequest>(){
            init {
                // 이미지를 퀄리티 레벨을 90으로 통일하고 바이트배열 포멧으로 이미지를 전송한다.
                val annotateImageRequest = AnnotateImageRequest()
                val base64EncodedImage = Image()
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG,90,byteArrayOutputStream)
                val imageBytes = byteArrayOutputStream.toByteArray()
                base64EncodedImage.encodeContent(imageBytes)
                annotateImageRequest.image = base64EncodedImage

                // google cloud vision api에 요청할 이미지의 특징들을 요청
                annotateImageRequest.features = object : ArrayList<Feature>(){
                    init {
                        val labelDetection = Feature()
                        when(requestType){
                            activity.LABEL_DETECTION_REQ_NUM -> {
                                labelDetection.type = "LABEL_DETECTION"
                                labelDetection.maxResults = MAX_RESULTS_LABEL
                            }
                            activity.LANDMARK_DETECTION_REQ_NUM -> {
                                labelDetection.type = "LANDMARK_DETECTION"
                                labelDetection.maxResults = MAX_RESULTS_LANDMARK
                            }
                        }
                          // 최대 결과값 10개만 받기
                        add(labelDetection)
                    }
                }
                add(annotateImageRequest)
            }
        }
        val annotateRequest = vision.images().annotate(batchAnnotateImagesRequest)
        annotateRequest.setDisableGZipContent(true)
        return annotateRequest
    }

    // 구글 비전 api 의 응답을 String 형태로 변경하기 위한 함수
    private fun findProperResponsType(response: BatchAnnotateImagesResponse) : String{
        when(requestType){
            activity.LABEL_DETECTION_REQ_NUM -> {
                return convertResponseToString(response.responses[0].labelAnnotations)
            }
            activity.LANDMARK_DETECTION_REQ_NUM -> {
                return convertResponseToStringLand(response.responses[0].landmarkAnnotations)
            }
        }
        return "fail"
    }

    private fun convertResponseToString(labels: List<EntityAnnotation>):String{
        Log.d("TEEEAA",labels.toString())
        val message = StringBuilder()
            labels.forEach {
                Log.d("TAG",it.score.toString()+" : " + it.description)
                message.append("/")
                message.append(String.format(Locale.US,"%.1f:%s", it.score*100, it.description))
            }
        // 데이터를 정제하기 위해서 한 줄마다 "/" 추가 (더 좋은 방법이 생기면 업데이트 예정)
            return message.toString()
    }

    private fun convertResponseToStringLand(labels: List<EntityAnnotation>):String{
        val message = StringBuilder()
        labels.forEach {
            message.append(String.format(Locale.US,"%.1f:%s", it.score*100, it.description))
            it.locations.forEach {
                message.append(String.format(Locale.US,":%s:%s", it.latLng.latitude,it.latLng.longitude))
            }
        }
        return message.toString()
    }
}