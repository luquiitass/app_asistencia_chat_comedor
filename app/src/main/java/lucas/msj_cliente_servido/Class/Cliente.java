package lucas.msj_cliente_servido.Class;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by lucas on 29/03/17.
 */

public class Cliente extends Observable implements Runnable{

    private String TAG = "Cliente";

    private String nick;

    private InetAddress inetAdressCliente;

    private int port;

    private InetAddress inetAdressServer;

    private Handler handler;

    private DatagramSocket c;

    private boolean chat;

    public Cliente(String nick, InetAddress inetAddressCliente, int port) {
        this.nick = nick;
        this.inetAdressCliente = inetAddressCliente;
        this.port = port;
    }

    public Cliente(InetAddress inetAddressCliente, int port) {
        this.inetAdressCliente = inetAddressCliente;
        this.port = port;
    }


    public Cliente(String nick, Handler handlerCliente,boolean chat) {
        this.chat=chat;
        this.nick=nick;
        this.handler=handlerCliente;
    }

    public Cliente(String nick, Handler handlerCliente) {
        this.chat=false;
        this.nick=nick;
        this.handler=handlerCliente;
    }



    //GET  and SET
    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public InetAddress getInetAdressCliente() {
        return inetAdressCliente;
    }

    public void setInetAdressCliente(InetAddress inetAdressCliente) {
        this.inetAdressCliente = inetAdressCliente;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public InetAddress getInetAdressServer() {
        return inetAdressServer;
    }

    public void setInetAdressServer(InetAddress inetAdressServer) {
        this.inetAdressServer = inetAdressServer;
    }

    public boolean isActivo(){
        return c.isConnected();
    }

    //metodos privados

    private String datosParaConectarAlServidor(){
        String retorno = "";
        JSONObject data = new JSONObject();
        try {
            if (chat){
                retorno = TGson.newInstance(Constants.op_nuevo_cliente).add("nick",this.nick).getJosn();
            }else {
                retorno = TGson.newInstance(Constants.op_registrar_presencia).add("legajo",nick).getJosn();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"error al crear json para enviar al servidor");
        }

        return retorno;
    }

    public void enviarMensaje(String mensaje) {

        Mensaje unMensaje = new Mensaje("",this.nick,mensaje);

        new EnciarMensaje(unMensaje.toJSON()).start();
    }

    public void notificarUI(String json){
        Bundle messageBundle = new Bundle();
        Gson gson = new Gson();

        messageBundle.putSerializable("msg",json);

        Message message = new Message();
        message.setData(messageBundle);
        Log.i(TAG,"mostrando en pantalla el usuario precente");
        handler.sendMessage(message);
    }

    @Override
    public void notifyObservers(Object arg) {
        super.notifyObservers(arg);
    }

    public void finalizar(){
        operacionesAlFinalizar();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (c != null){
            c.close();
        }
    }

    public void operacionesAlFinalizar(){
        if (this.getInetAdressServer() != null){
            new EnciarMensaje(TGson.newInstance(Constants.cliente_desconecta).getJosn()).start();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public void run() {
        try {
            //Open a random port to send the package
            Log.i(TAG,"iniciando conexion del cliemte");
            c = new DatagramSocket();
            c.setBroadcast(true);

            String data = datosParaConectarAlServidor();

            byte[] sendData = data.getBytes();

            //Try the 255.255.255.255 first
            try {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 8888);
                //c.send(sendPacket);
                Log.e("Cliente","SEND to : "+sendPacket.getAddress().getHostAddress());
            } catch (Exception e) {
                Log.e("CLiente","Error en SEND a 255.255.255.255");
            }

            // Broadcast the message over all the network interfaces
            Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue; // Don't want to broadcast to the loopback interface
                }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }

                    if (! (broadcast instanceof Inet4Address)) {

                        continue;
                    }

                    // Send the broadcast package!
                    try {
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8888);
                        c.send(sendPacket);
                        Log.e("Cliente","SEND to : "+sendPacket.getAddress().getHostAddress());
                    } catch (Exception e) {
                    }

                    //System.out.println(getClass().getName() + ">>> Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
                }
            }

            new EscuchandoMensajes(c).start();

//            byte[] recvBuf = new byte[15000];
//            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
//
//            boolean conMensaje;
//            try {
//                c.receive(receivePacket);
//                conMensaje=true;
//            }catch (Exception e){
//                conMensaje=false;
//                setChanged();
//                notifyObservers("nada");
//            }
//
//            if (conMensaje){
//
//                System.out.println(getClass().getName() + ">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());
//
//                String message = new String(receivePacket.getData()).trim();
//
//                setChanged();
//                notifyObservers(message);
//            }

        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
            //cerrarConexion();
        }
    }

    public class EscuchandoMensajes extends Thread{

        private DatagramSocket c;

        public EscuchandoMensajes(DatagramSocket c) {
            this.c = c;
        }

        @Override
        public void run() {
            boolean esperandoMensaje=true;
            while (esperandoMensaje){
                byte[] recvBuf = new byte[15000];
                DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                try {
                    Log.i(TAG,"Esperando mensaje de server");

                    c.receive(receivePacket);

                    //We have a response
                    System.out.println(getClass().getName() + ">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());

                    String message = new String(receivePacket.getData()).trim();

                    try {
                        trabajarJson(message,receivePacket);
                        //trabajarJson(message,receivePacket);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

//                    if (message.equals(Constants.op_aceptado)){
//                        inetAdressServer = receivePacket.getAddress();
//                        Log.e("Cliente","conectado a "+inetAdressServer.getHostAddress());
//                        //conectado();
//                    }else if(message.equals("nickExistente")){
//                        setChanged();
//                        notifyObservers("nickExistente");
//                    }else{
//                        setChanged();
//                        notifyObservers(message);
//                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    esperandoMensaje=false;
                    //cerrarConexion();
                }


            }
        }
    }

    private void trabajarJson(String message, DatagramPacket receivePacket) throws JSONException {
        Log.i(TAG,"trabajamdo json");
        JSONObject json = new JSONObject(message);
        if (json.has("accion")) {
            String op = json.getString("accion");
            if (op.equals(Constants.op_aceptado)) {
                Log.i(TAG, "accion :" + op);
                aceptacionServer(receivePacket);
            } else {
                notificarUI(message);
            }
        }
    }

    private void nuevoCliente(String message) {
        JsonObject json = new JsonParser().parse(message).getAsJsonObject();
        Gson gson = new Gson();
        Cliente unCliente = gson.fromJson(json.get("cliente"),Cliente.class);
        //notificarUI(json.getAsJsonObject("cliente"));
    }

    private void mostrarMensaje(String mensaje) throws JSONException {
        JsonObject json = new JsonParser().parse(mensaje).getAsJsonObject();
        //json.addProperty("mensaje",mensaje);
        //notificarUI(json);
    }

    private void aceptacionServer(DatagramPacket receivePacket) throws JSONException {
        this.setInetAdressServer(receivePacket.getAddress());
        //JSONObject json = new JSONObject();
        //json.put("pantalla","estas conectado");
        //notificarUI(json);
    }

    public class EnciarMensaje extends Thread{

        private String mensaje;


        public EnciarMensaje(String mensaje) {
            this.mensaje=mensaje;
        }

        @Override
        public void run() {
            Log.i(TAG,"mensaje >>" +mensaje);
            byte[] sendData = mensaje.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, inetAdressServer, 8888);
            try {
                Log.i(TAG,"enviando mensaje al servidor "+sendPacket.getAddress().getHostName());
                c.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG,"error aal enviar mensaje al servidor");
            }
        }
    }

}
