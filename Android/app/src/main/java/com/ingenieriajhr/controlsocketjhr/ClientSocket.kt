import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.PrintWriter
import java.net.Socket
import kotlin.concurrent.thread

class ClientSocket(private val context:Context) {

    private var socket: Socket? = null
    private var out: PrintWriter? = null
    private lateinit var ip :String
    private var port = 0




    fun enviarMensaje(mensaje: String, dataConn: String) {
        val data = dataConn.split(",")
        //change ip and port
        val ip = data[0]
        val port = data[1].toInt()
        //socket conn
        thread(start = true){
            if (socket==null) socket = Socket(ip,port)
            socket?.getOutputStream()?.write(mensaje.toByteArray())
        }
    }

    fun stopSocket(){
        socket?.close()
        socket = null
    }

}
