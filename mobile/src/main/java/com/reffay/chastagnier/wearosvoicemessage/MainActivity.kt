package com.reffay.chastagnier.wearosvoicemessage

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.wearable.*

class MainActivity : AppCompatActivity(), DataClient.OnDataChangedListener, View.OnClickListener {

    private var datapath = "/data_path"
    private lateinit var sendButton: Button
    private lateinit var logger: TextView

    companion object {
        private const val TAG = "Mobile MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sendButton = findViewById(R.id.sendbtn)
        sendButton.setOnClickListener(this)
        logger = findViewById(R.id.logger)
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

    //button listener
    override fun onClick(v: View) {
        val message = "Hello wearable"
        sendData(message)
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
