package com.example.mcctest

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.gms.cloudmessaging.CloudMessage
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_signup.*
import java.util.UUID

class MainActivity : AppCompatActivity() {
    var img:Uri? = null
    val db = Firebase.firestore
    private lateinit var analytics: FirebaseAnalytics
    var player: SimpleExoPlayer? = null
    var videoURL = "https://firebasestorage.googleapis.com/v0/b/mccproject-51b7f.appspot.com/o/video.mp4?alt=media&token=07508262-a2dc-488e-b3a3-5b323377a73c"
    var playWhenReady = true
    var currentWindow = 0
    var playBackPosition: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        Log.e("asmaa", "test")
//        analytics = Firebase.analytics
//        onNewToken()



    }
    fun initVideo(){
        player = SimpleExoPlayer.Builder(this).build()
     //   video_view.player = player
        val mediaItem = MediaItem.Builder()
            .setUri(videoURL)
            .setMimeType(MimeTypes.APPLICATION_MP4)
            .build()
        val mediaSource = ProgressiveMediaSource.Factory(
            DefaultDataSource.Factory(this)
        )
            .createMediaSource(mediaItem)
        player!!.playWhenReady = playWhenReady
        player!!.seekTo(currentWindow,playBackPosition)
        player!!.prepare(mediaSource,false,false)
    }
    fun releaseVideo(){
        if(player != null){
            playWhenReady = player!!.playWhenReady
            playBackPosition = player!!.contentPosition
            currentWindow = player!!.currentWindowIndex
            player!!.release()
            player = null
        }
    }

    override fun onStart() {
        super.onStart()
        initVideo()
    }

    override fun onStop() {
        super.onStop()
        releaseVideo()
    }
    override fun onPause() {
        super.onPause()
        releaseVideo()
    }
     fun onNewToken() {
         FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
             if (!task.isSuccessful){
                 Log.d(TAG, "fail")
                 Log.e("asmaa", "test",task.exception)
                 return@addOnCompleteListener
             }
             val token = task.result
             Log.d("Token:", token.toString())

         }


        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        ///sendRegistrationToServer(token)
    }
    fun selectContent(id:String,name:String,contentType:String){
        analytics.logEvent(
            FirebaseAnalytics.Event.SELECT_CONTENT) {
            param(FirebaseAnalytics.Param.ITEM_ID, id);
            param(FirebaseAnalytics.Param.ITEM_NAME, name);
            param(FirebaseAnalytics.Param.CONTENT_TYPE, contentType);
        }
    }
    fun screenView(screenClass:String,screenName:String){
        analytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_CLASS,screenClass)
            param(FirebaseAnalytics.Param.SCREEN_NAME,screenName)
        }
    }
    fun customEvent(){
        analytics.logEvent("imageShare"){
            param("imageName","asmaa.png")
            param("shareFrom","MainActivity")
        }
    }
    fun  storage(){

        btn_choose.setOnClickListener {
            val i = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(i,100)
        }

        btn_uploade.setOnClickListener {
            val randomNumber = UUID.randomUUID().toString()
            val storageRef = FirebaseStorage.getInstance().getReference("images")
            val imgRef = storageRef.child("image "+ randomNumber)
            imgRef.putFile(img!!)
                .addOnSuccessListener {
                    Log.d("TAG","SUCCESS : ")
                }
                .addOnFailureListener {
                    Log.d("TAG","FAILED : ")
                }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 100){
            img = data!!.data!!
            imageView.setImageURI(img)

        }
    }
    fun fireStoreLec(){
        val subject = hashMapOf(
            "name" to "MCC",
            "number" to "MOBC 4312",
            "hours" to 3
        )

        db.collection("subjects")
            .add(subject)
            .addOnSuccessListener { documentReference ->
                Log.d("DONE", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("NOT DONE", "Error adding document", e)
            }

        db.collection("subjects")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

}