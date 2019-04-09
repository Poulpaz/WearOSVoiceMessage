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
import kotlinx.android.synthetic.main.activity_main.*
import android.support.v4.content.ContextCompat.startActivity
import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import android.os.Environment.getExternalStorageDirectory
import com.squareup.picasso.Picasso
import java.io.File
import java.io.File.separator
import android.provider.MediaStore








class MainActivity : AppCompatActivity(), DataClient.OnDataChangedListener {

    private var datapath = "/data_path"
    private var message: String = ""

    companion object {
        private const val TAG = "Mobile MainActivity"
        private const val SPEECH_REQUEST_CODE = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sendbtn.setOnClickListener { sendData(message) }
        button_microphone.setOnClickListener { displaySpeechRecognizer() }
        imageView.setOnClickListener {
            imageView.setImageDrawable(null)
        }
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
                    if(message?.toLowerCase().equals("ouvrir youtube")){
                        watchYoutubeVideo()
                    } else if(message?.toLowerCase().equals("ouvrir message")){
                        watchMessage()
                    } else if (message?.toLowerCase().equals("ouvrir musique")){
                        playMusic()
                    } else {
                        displayPicture(message.toLowerCase())
                    }
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
            message = spokenText.toString()
            textview_message.setText(message)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun displayPicture(word : String){
        val externalStorage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath
        val outputFile = File(externalStorage + separator + "Watch" + separator + word + ".png")
        if(outputFile.exists()) {
            Picasso.get().load(outputFile).into(imageView)
        }
    }

    fun watchYoutubeVideo() {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube"))
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.youtube.com/")
        )
        try {
            this.startActivity(appIntent)
        } catch (ex: ActivityNotFoundException) {
            this.startActivity(webIntent)
        }
    }

    fun playMusic(){
        val intent = Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER)
        startActivity(intent)
    }

    fun watchMessage(){
        val sendIntent = Intent(Intent.ACTION_VIEW)
        sendIntent.data = Uri.parse("sms:")
        startActivity(sendIntent);
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
            .addOnSuccessListener { dataItem ->
                Toast.makeText(this, "Message envoyé : $message", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erreur lors de l'envoi du message", Toast.LENGTH_SHORT).show()
            }
    }
}
