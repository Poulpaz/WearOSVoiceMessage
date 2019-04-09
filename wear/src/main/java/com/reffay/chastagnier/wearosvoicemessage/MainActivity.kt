package com.reffay.chastagnier.wearosvoicemessage

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.speech.RecognizerIntent
import android.support.v4.app.ActivityCompat
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.util.Rational
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.wearable.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : WearableActivity(), DataClient.OnDataChangedListener {
        private var mTextView: TextView? = null
        private lateinit var messageButton: Button
        private lateinit var voiceButton: Button
        private var dataPath = "/data_path"
        private var message: String = ""

        companion object {
            private const val TAG = "Wear MainActivity"
            private const val SPEECH_REQUEST_CODE = 0
            const val REQUEST_PERMISSION_STORAGE = 1001
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            mTextView = findViewById(R.id.text)

            messageButton = findViewById(R.id.wrbutton)
            messageButton.setOnClickListener {
                sendData(message)
            }

            voiceButton = findViewById(R.id.voiceButton)
            voiceButton.setOnClickListener {
                displaySpeechRecognizer()
            }

            requestStoragePermission()

            setAmbientEnabled()

            iv_displayer.setOnClickListener {
                iv_displayer.setImageDrawable(null)
            }
        }

        //add listener.
        public override fun onResume() {
            super.onResume()
            Wearable.getDataClient(this).addListener(this)
        }

        //remove listener
        public override fun onPause() {
            super.onPause()
            Wearable.getDataClient(this).removeListener(this)
        }

        //receive data from the path.
        override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
            Log.d(TAG, "onDataChanged: $dataEventBuffer")
            for (event in dataEventBuffer) {
                if (event.type == DataEvent.TYPE_CHANGED) {
                    val path = event.dataItem.uri.path
                    if (dataPath == path) {
                        val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                        val message = dataMapItem.dataMap.getString("message")
                        Log.v(TAG, "Wear activity received message: $message")
                        val media = File(
                            Environment.getExternalStorageDirectory().absolutePath + "/Pictures/",
                            "$message.png"
                        )
                        if(media.exists()) {
                            Picasso.get().load(media).resize(200, 200).centerCrop().into(iv_displayer)
                            mTextView!!.text = ""
                        } else {
                            mTextView!!.text = message
                        }
                    } else { Log.e(TAG, "Unrecognized path: " + path!!) }
                } else if (event.type == DataEvent.TYPE_DELETED) { Log.v(TAG, "Data deleted : " + event.dataItem.toString()) }
                else { Log.e(TAG, "Unknown data event Type = " + event.type) }
            }
        }


        /* Voice Region */
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
            if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val spokenText = results[0]
                mTextView!!.text = spokenText
                message = spokenText
            }
            super.onActivityResult(requestCode, resultCode, data)
        }

        private fun displaySpeechRecognizer() {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
        }
        /* End Region */

    fun requestStoragePermission(): Boolean {
        return requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_PERMISSION_STORAGE, this)
    }

    private fun requestPermission(permission: String, code: Int, activity: Activity): Boolean {
        return requestPermissions(arrayOf(permission), code, activity)
    }

    private fun requestPermissions(permissions: Array<String>, code: Int, activity: Activity): Boolean {
        return if (!checkPermissions(permissions)) {
            ActivityCompat.requestPermissions(activity, permissions, code)
            false
        } else {
            true
        }
    }


    private fun checkPermissions(permissions: Array<out String>): Boolean {
        var permissionGranted = false
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                permissionGranted = true
            }
        }
        return permissionGranted
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_STORAGE && grantResults[permissions.indexOf(Manifest.permission.READ_EXTERNAL_STORAGE)] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Vous pouvez utiliser l'application", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Vous avez refusez la permission, l'application n'affichera pas d'images", Toast.LENGTH_LONG).show()
        }
    }


        private fun sendData(message: String) {
            val dataMap = PutDataMapRequest.create(dataPath)
            dataMap.dataMap.putString("message", message)
            val request = dataMap.asPutDataRequest()
            request.setUrgent()
            val dataItemTask = Wearable.getDataClient(this).putDataItem(request)
            dataItemTask
                .addOnSuccessListener { dataItem -> Log.d(TAG, "Sending message was successful: $dataItem") }
                .addOnFailureListener { e -> Log.e(TAG, "Sending message failed: $e") }
        }
    }

