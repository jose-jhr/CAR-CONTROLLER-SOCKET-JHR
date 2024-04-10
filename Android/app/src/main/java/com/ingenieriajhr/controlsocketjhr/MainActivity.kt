package com.ingenieriajhr.controlsocketjhr

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ingenieriajhr.controlsocketjhr.databinding.ActivityMainBinding
import taimoor.sultani.sweetalert2.Sweetalert


class MainActivity : AppCompatActivity() {

    lateinit var vb :ActivityMainBinding

    private var widthBtnBreak = 0f
    private var heightBtnBreak = 0f
    private var getSize = false
    private var playAndPauseBool = true

    private var maxValue = 0f
    private val maxValueFinal = 9.87f

    //array save send
    private val arrayCharsTx = ArrayList<ModelSend>()

    //acelerometro
    private lateinit var acelerometro: Acelerometro

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)

        //hiden bar
        window.statusBarColor = ContextCompat.getColor(this,R.color.white)
        //init object acelerometro
        acelerometro = Acelerometro(this)
        vb.btnVolante.setOnClickListener {

        }


        //get up and down button
        vb.btnBrake.setOnTouchListener { view, motionEvent ->
            when(motionEvent.action){
                MotionEvent.ACTION_DOWN->{
                    if(!playAndPauseBool){
                        if (!getSize){
                            //change state for get size
                            getSize = true
                        }
                        // get dimens
                        widthBtnBreak = vb.btnBrake.layoutParams.width.toFloat()
                        heightBtnBreak = vb.btnBrake.layoutParams.height.toFloat()
                        //change dimens button
                        setSizeButton(0.8f)
                    }else{
                        changeSettingView(vb.btnBrake)
                    }
                }
                MotionEvent.ACTION_UP->{
                    if (!playAndPauseBool) setSizeButton(1f)
                }
            }
            true
        }


        //Play and pause game
        vb.btnplaPause.setOnClickListener {
            if (!playAndPauseBool) {
                vb.btnplaPause.setBackgroundResource(R.drawable.baseline_play_circle_filled_24)
                playAndPauseBool = !playAndPauseBool
                //stop acelerometro
                stopAcelerometro()
            }else {
                vb.btnplaPause.setBackgroundResource(R.drawable.baseline_pause_circle_24)
                playAndPauseBool = !playAndPauseBool
                //change acelerometro
                startAcelerometro()
            }
        }


    }

    /**
     * Stop Acelerometro
     */
    private fun stopAcelerometro() {
        acelerometro.stopListening()
    }

    /**
     * Start acelerometro Data
     */
    private fun startAcelerometro() {
        //start acelerometro
        acelerometro.startListening()
        //get acelerometro data
        acelerometro.initAcelerometroRetur(object :InterfaceAcelemetro{
            override fun getData(x: Float, y: Float, z: Float) {
                if (maxValue<y && y>0)maxValue = y
                val Quadrant = evulueQuadrant(x,y)
                val returnDegrees = evalueDegree(Quadrant,y)
                vb.txtConsole.text = "X: $x ---  Y: $y --- Z: $z \n $maxValue\n$Quadrant\n$returnDegrees"
                //rotate image
                vb.btnVolante.rotation = -returnDegrees+90
                //change rotat
                Log.d("acelerometro","X: $x ---  Y: $y --- Z: $z")
            }
        })
    }

    /**
     * Evalue Degree with quadran and y
     */
    private fun evalueDegree(quadrant: Int, y: Float): Float {
        var returnDegrees = 0f
        //0 to 90
        if (quadrant == 1 || quadrant == 2){ returnDegrees = 180-(((y+maxValueFinal)*90f)/9.87f)
        }
        if (quadrant == 4 || quadrant == 3){ returnDegrees = 180+(((y+maxValueFinal)*90f)/9.87f)
        }
        //return
        return returnDegrees
    }

    /**
     * Evalue Quadrant with x and y
     */
    private fun evulueQuadrant(x: Float, y: Float): Int {
        var returnQuadrant = 0
        if (x>0 && y>0) returnQuadrant = 1
        if (x>0 && y<0) returnQuadrant = 2
        if (x<0 && y<0) returnQuadrant = 3
        if (x<0 && y>0) returnQuadrant = 4
        return returnQuadrant
    }

    /**
     * Change view configure Send View
     */
    private fun changeSettingView(btnCommand: View) {
        val editText = EditText(this)
        //find character if exist
        val modelExist = arrayCharsTx.find { it.id == btnCommand.id }
        //if model exist so show character in editText
        if (modelExist!=null){
            //show character in edittext
            editText.setText(modelExist.txChar)
        }
        //create sweeet alert
        val sweet = Sweetalert(this, Sweetalert.NORMAL_TYPE)
            .setTitleText("Digita la letra a enviar")
        sweet.cancelText = "Ok"
        sweet.setOnDismissListener {
            //in dismiss evalue if commando exist for set in array list
            val existingId = arrayCharsTx.find { it.id == btnCommand.id}
            if (existingId!=null){
                existingId.txChar = editText.text.toString()
            }else{
                val command = editText.text.toString()
                arrayCharsTx.add(ModelSend(btnCommand.id,command))
            }
            //exit dismiss
            Toast.makeText(this, "Comando a enviar guardado", Toast.LENGTH_SHORT).show()
        }
        sweet.setCustomView(editText)
        sweet.show()
    }

    /**
     * Change size button
     */
    private fun setSizeButton(dimen: Float) {
        val layoutParams = vb.btnBrake.layoutParams
        layoutParams.width = (widthBtnBreak*dimen).toInt()
        layoutParams.height = (heightBtnBreak*dimen).toInt()
        vb.btnBrake.layoutParams = layoutParams
    }

    override fun onDestroy() {
        super.onDestroy()
        acelerometro.stopListening()
    }

}