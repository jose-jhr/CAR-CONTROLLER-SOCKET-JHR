package com.ingenieriajhr.controlsocketjhr

import ClientSocket
import android.annotation.SuppressLint
import android.graphics.ColorSpace.Model
import android.os.Bundle
import android.util.Log
import android.view.Display.Mode
import android.view.MotionEvent
import android.view.View
import android.widget.Button
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
    //socket
    private lateinit var socketClient: ClientSocket
    //change ip and port
    private var dataIpPort = ""
    //dir volante current
    private var volanteCurrent = ""
    //default volante
    private val VOLANTEIZQUIERDA = Pair<String,Float>("a",115f)
    private val VOLANTEDERECHA = Pair<String,Float>("d",70f)
    private val VOLANTECENTRO = Pair<String,Float>("q",VOLANTEIZQUIERDA.second)

    private val DEFAULT_BREAK = "s"
    private val DEFAULT_VEL = "w"
    private val DEFAULT_IP_POR = "192.168.1.100,8051"


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)

        //hiden bar
        window.statusBarColor = ContextCompat.getColor(this,R.color.white)
        //init object acelerometro
        acelerometro = Acelerometro(this)
        //init socketClient
        socketClient = ClientSocket(this)
        //Default values
        arrayCharsTx.add(ModelSend(vb.btnBrake.id,DEFAULT_BREAK))
        arrayCharsTx.add(ModelSend(vb.btnVel.id,DEFAULT_VEL))
        dataIpPort = DEFAULT_IP_POR

        vb.btnVolante.setOnClickListener {

        }

        vb.btnIpPort.setOnClickListener {
            changeIpPort()
        }
        //add method onTouchListener
        vb.btnBrake.setOnTouchListener(setOnTouchListenerForButton(vb.btnBrake))
        vb.btnVel.setOnTouchListener(setOnTouchListenerForButton(vb.btnVel))




        //TODO Play and pause game
        vb.btnplaPause.setOnClickListener {
            if (!playAndPauseBool) {
                vb.btnplaPause.setBackgroundResource(R.drawable.baseline_play_circle_filled_24)
                playAndPauseBool = !playAndPauseBool
                //stop acelerometro
                stopAcelerometro()
                //stopSocket
                socketClient.stopSocket()

            }else {
                vb.btnplaPause.setBackgroundResource(R.drawable.baseline_pause_circle_24)
                playAndPauseBool = !playAndPauseBool
                //change acelerometro
                startAcelerometro()

            }
        }


    }

    /**
     * OnTouchForButton
     */
    @SuppressLint("ClickableViewAccessibility")
    fun setOnTouchListenerForButton(button: View) = View.OnTouchListener { view, motionEvent ->
        when(motionEvent.action){
            MotionEvent.ACTION_DOWN -> {
                if(!playAndPauseBool){
                    if (!getSize){
                        //change state for get size
                        getSize = true
                    }
                    // get dimens
                    widthBtnBreak = button.layoutParams.width.toFloat()
                    heightBtnBreak = button.layoutParams.height.toFloat()
                    //change dimens button
                    setSizeButton(0.8f, button)
                    //send command
                    sendMessageSocket("", button.id)
                } else {
                    //animation button release
                    changeSettingView(button)
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!playAndPauseBool) {
                    setSizeButton(1f, button)
                    //send release
                    sendMessageSocket("r", button.id)
                }
            }
        }
        true
    }


    /**
     * Send character message socket
     */
    private fun sendMessageVolanteSocket(message:String){
        socketClient.enviarMensaje("$message",dataIpPort)
    }

    /**
     * Send character with view click id
     */
    private fun sendMessageSocket(aditional:String,id: Int) {
        val commandSend = arrayCharsTx.find { it.id == id }
        socketClient.enviarMensaje("$aditional${commandSend?.txChar}",dataIpPort)
    }

    /**
     * Chage ip and port
     */
    private fun changeIpPort() {
        val editText = EditText(this)
        if (dataIpPort.isNotEmpty())editText.setText(dataIpPort)
        val sweet = Sweetalert(this, Sweetalert.NORMAL_TYPE)
            .setTitleText("Digita la ip,puerto ejemplo (192.168.12.1,8050)")
        sweet.cancelText = "Ok"
        sweet.setOnDismissListener {
           dataIpPort = editText.text.toString()
            Toast.makeText(this, "Datos se enviaran a \nip:${dataIpPort[0]} \nport:${dataIpPort[1]}", Toast.LENGTH_SHORT).show()
        }
        sweet.setCustomView(editText)
        sweet.show()
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
                //rotate image
                vb.btnVolante.rotation = -returnDegrees+90
                //send character with rotate volante
                if (returnDegrees>VOLANTEIZQUIERDA.second && volanteCurrent!=VOLANTEIZQUIERDA.first){
                    volanteCurrent = VOLANTEIZQUIERDA.first
                    sendMessageVolanteSocket(VOLANTEIZQUIERDA.first)
                }
                if (returnDegrees<VOLANTEDERECHA.second && volanteCurrent!=VOLANTEDERECHA.first){
                    volanteCurrent = VOLANTEDERECHA.first
                    sendMessageVolanteSocket(VOLANTEDERECHA.first)
                }d
                if (returnDegrees in VOLANTEDERECHA.second..VOLANTEIZQUIERDA.second && volanteCurrent != VOLANTECENTRO.first){
                    volanteCurrent = VOLANTECENTRO.first
                    sendMessageVolanteSocket(VOLANTECENTRO.first)
                }
                vb.txtConsole.text = "X: $x ---  Y: $y --- Z: $z \n $maxValue\n$Quadrant\n$returnDegrees\n$volanteCurrent"

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
    private fun setSizeButton(dimen: Float,view: View) {
        val layoutParams = view.layoutParams
        layoutParams.width = (widthBtnBreak*dimen).toInt()
        layoutParams.height = (heightBtnBreak*dimen).toInt()
        view.layoutParams = layoutParams
    }

    override fun onDestroy() {
        super.onDestroy()
        socketClient.stopSocket()
        acelerometro.stopListening()
    }

}