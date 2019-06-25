package com.ksy.vision.portfolio

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.upload_chooser.*

class UploadChooser : BottomSheetDialogFragment(){

    interface UploadCooserNotifierInterface {
        fun cameraOnClick()
        fun galleryOnClick()
    }

    var uploadChooserNotifierInterface : UploadCooserNotifierInterface? = null

    fun addNotifier(listner:UploadCooserNotifierInterface){
        uploadChooserNotifierInterface = listner
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.upload_chooser, container,false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupListener()
    }

    private fun setupListener(){
        upload_camera.setOnClickListener {
            uploadChooserNotifierInterface?.cameraOnClick() // null이 아니라면 cameraonClick
            // 권한 작업을 mainActivity에서 진행할수 있게 interface 로 구현
            dismiss()
        }

        upload_gallery.setOnClickListener {
            uploadChooserNotifierInterface?.galleryOnClick()
            dismiss()
        }
    }
}