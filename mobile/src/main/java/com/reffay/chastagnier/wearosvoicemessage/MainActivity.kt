package com.reffay.chastagnier.wearosvoicemessage

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.wearable.*

class MainActivity : AppCompatActivity(), DataClient.OnDataChangedListener {

    private var datapath = "/data_path"
    private lateinit var sendButton: Button
    private lateinit var voiceButton: Button
    private lateinit var logger: TextView
    private var message: String = ""

    companion object {
        private const val TAG = "Mobile MainActivity"
        private const val SPEECH_REQUEST_CODE = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sendButton = findViewById(R.id.sendbtn)
        sendButton.setOnClickListener { sendData(message) }
        logger = findViewById(R.id.logger)

        voiceButton = findViewById(R.id.voiceButton)
        voiceButton.setOnClickListener { displaySpeechRecognizer() }
    }

    // add data listener
    public override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(this)
    }

    //remove data listener
    public override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(this)
    }

    private fun logThis(newInfo: String) {
        if (newInfo.compareTo("") != 0) {
            logger.append("\n" + newInfo)
        }
    }

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        Log.d(TAG, "onDataChanged: $dataEventBuffer")
        for (event in dataEventBuffer) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                if (datapath == path) {
                    val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                    val message = dataMapItem.dataMap.getString("message")
                    Log.v(TAG, "Wear activity received message: $message")
                    logThis(message)
                } else {
                    Log.e(TAG, "Unrecognized path: " + path!!)
                }
            } else if (event.type == DataEvent.TYPE_DELETED) {
                Log.v(TAG, "Data deleted : " + event.dataItem.toString())
            } else {
                Log.e(TAG, "Unknown data event Type = " + event.type)
            }
        }
    }

    /* Voice Region */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0)
            logger.text = spokenText
            message = spokenText.toString()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun displaySpeechRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        startActivityForResult(intent, SPEECH_REQUEST_CODE)
    }
    /* End Region */

    private fun sendData(message: String) {
        val dataMap = PutDataMapRequest.create(datapath)
        dataMap.dataMap.putString("message", message)
        val request = dataMap.asPutDataRequest()
        request.setUrgent()
        val dataItemTask = Wearable.getDataClient(this).putDataItem(request)
        dataItemTask
            .addOnSuccessListener { dataItem -> Log.d(TAG, "Sending message was successful: $dataItem") }
            .addOnFailureListener { e -> Log.e(TAG, "Sending message failed: $e") }
    }
}
