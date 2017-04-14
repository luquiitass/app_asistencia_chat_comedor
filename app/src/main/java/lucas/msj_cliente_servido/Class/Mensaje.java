package lucas.msj_cliente_servido.Class;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

/**
 * Created by lucas on 02/04/17.
 */

public class Mensaje {
    private String ip;
    private String nick;
    private String mensaje;
    private String hora;
    private String fecha;

    public Mensaje(String msj) throws JSONException {
        JSONObject mensaje = new JSONObject(msj);
        this.ip = mensaje.getString("ip");
        this.nick = mensaje.getString("nick");
        this.mensaje = mensaje.getString("mensaje");
        this.hora = mensaje.getString("hora");
        this.fecha = mensaje.getString("fecha");
    }

    public Mensaje(String ip, String nick, String mensaje) {
        this.ip = ip;
        this.nick = nick;
        this.mensaje = mensaje;
        Date date = new Date();
        this.hora = new SimpleDateFormat("HH:mm:ss").format(date);
        this.fecha = new SimpleDateFormat("dd/MM/yyyy").format(date);
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }



    public String toJSON() {
        return TGson.newInstance(Constants.op_nuevo_mensaje,"mensaje",this).getJosn();
    }
}
