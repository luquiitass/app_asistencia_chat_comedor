package lucas.msj_cliente_servido.Class;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by lucas on 29/03/17.
 */

public class Servidor implements Runnable{

    private static String TAG = "SERVIDOR";

    private Handler mHandler;
    private String nick;
    private TreeMap<String ,Cliente> clientes;

    private DatagramSocket mSocket;
    private int mPort;

    private boolean estado;



    public Servidor(int port,Handler handler) {
        this.mPort = port;
        this.mHandler=handler;
    }

    public Servidor(Handler mUpdateHandler) {
        this.mHandler =mUpdateHandler;
        this.clientes = new TreeMap<String, Cliente>();
    }

    public Servidor(String nick,Handler mUpdateHandler) {
        this.nick = nick;
        this.mHandler =mUpdateHandler;
        this.clientes = new TreeMap<String, Cliente>();
    }

    public DatagramSocket getmSocket() {
        return mSocket;
    }

    public void setmSocket(DatagramSocket mSocket) {
        this.mSocket = mSocket;
    }

    private void aceptarCliente(String response, DatagramPacket packet) throws JSONException {
        JSONObject json = new JSONObject(response);

        //JSONObject jsonCliente = json.getJSONObject("cliente");

        Cliente cliente = new Cliente(json.getString("nick"),packet.getAddress(),packet.getPort());

        String ipCliente = cliente.getInetAdressCliente().getHostAddress();

        String mensaje="";

        if (clientes.containsKey(ipCliente)){
            Cliente clienteEnLista = clientes.get(ipCliente);
            if (cliente.getNick().equals(clienteEnLista.getNick())  && cliente.getInetAdressCliente().equals(clienteEnLista.getInetAdressCliente())) {
                Log.i(TAG,"Cliente reconectado");
                mensaje = TGson.newInstance(Constants.op_aceptado).getJosn();

            }else if(existeNick(cliente.getNick())){
                Log.i(TAG,"Nick ya existe");
                mensaje = TGson.newInstance(Constants.op_nick_ya_existe).getJosn();

            }else{
                clientes.remove(ipCliente);
                clientes.put(ipCliente,cliente);
                mensaje = TGson.newInstance(Constants.op_aceptado).getJosn();
                Log.i(TAG,"Cliente modificado");
            }
            enviarMensaje(cliente,mensaje);
        }else if(existeNick(cliente.getNick())){
            Log.i(TAG,"Nick ya existe");
            mensaje = TGson.newInstance(Constants.op_nick_ya_existe).getJosn();
            enviarMensaje(cliente,mensaje);
        }else{
            clientes.put(ipCliente,cliente);
            mensaje = TGson.newInstance(Constants.op_aceptado).getJosn();
            enviarMensaje(cliente,mensaje);
            Log.i(TAG,"Cliente mensaje de aceptacion enviado");
        }

        String msj = TGson.newInstance(Constants.op_nuevo_cliente,"cliente",cliente).getJosn();
        new EnviarMensajesClientes(msj).start();

        //informarClientesConectados();


    }

    private boolean existeNick(String nick) {
        Set set2 = clientes.entrySet();
        Iterator iterator2 = set2.iterator();
        while(iterator2.hasNext()) {
            Map.Entry mentry2 = (Map.Entry)iterator2.next();

            Cliente unCliente = (Cliente)mentry2.getValue();

            if (unCliente.getNick().equals(nick)){
                return true;
            }

        }
        return false;
    }

    private void registrarPresenciaServer(String response,DatagramPacket packet) throws JSONException {

        JSONObject json = new JSONObject(response);

        String legajo = json.getString("legajo");

        String mensajePantalla = TGson.newInstance().add(Constants.asistencia,true).add("mensaje",legajo + " confirmo asistencia").getJosn();

        String mensajeComensal = TGson.newInstance(Constants.op_presencia_registrada).add(Constants.asistencia,true).getJosn();

        Cliente unCliente = new Cliente(packet.getAddress(),packet.getPort());

        enviarMensaje(unCliente,mensajeComensal);

        notificarUI(mensajePantalla);

    }

    private void enviarMensaje(Cliente cliente, String mensaje){
        byte[] sendData = mensaje.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, cliente.getInetAdressCliente(), cliente.getPort());
        try {
            mSocket.send(sendPacket);
            Log.i(TAG,mensaje+" >> enviado a >>"+cliente.getNick()+" ip:"+cliente.getInetAdressCliente().getHostAddress());
        } catch (IOException e) {
            Log.i(TAG,"no se pudo enviar mensaje  a >> "+cliente.getNick());
            e.printStackTrace();
        }
    }

    private void notificarUI(String data){
        Bundle messageBundle = new Bundle();
        messageBundle.putString("msg",data );

        Message message = new Message();
        message.setData(messageBundle);
        Log.i(TAG,"mostrando en pantalla el usuario precente");
        mHandler.sendMessage(message);
    }

    private void trabajarJSON(String respoonse,DatagramPacket packet) throws JSONException {
        Log.i(TAG,"Trabajando json >>"+respoonse);
        JSONObject json = new JSONObject(respoonse);
        if (json.has("accion")){
            String op = json.getString("accion");
            Log.i(TAG,"Accion "+op);
            if (op.equals(Constants.op_nuevo_cliente)){
                aceptarCliente(respoonse,packet);
            }
            else if (op.equals(Constants.op_nuevo_mensaje)){
                nuevoMensaje(respoonse,packet);
            }
            else if (op.equals(Constants.op_registrar_presencia)){
                registrarPresenciaServer(respoonse,packet);
            }else if (op.equals(Constants.cliente_desconecta)){
                deconectarCliente(packet);
            }

        }else{
            Log.e(TAG,"Sin Accion");

        }
    }

    private void desconectarServer(){
        if (clientes.size()>0){
                Cliente unCliente = clientes.firstEntry().getValue();
                new EnviarMensaje(unCliente,TGson.newInstance(Constants.nuevo_server).getJosn()).start();
                new EnviarMensajesClientes(TGson.newInstance(Constants.reconectar).getJosn()).start();;
        }
    }

    private void deconectarCliente(DatagramPacket packet) {
        String ipCliente = packet.getAddress().getHostAddress();
        if (clientes.containsKey(ipCliente)){
            clientes.remove(ipCliente);
            //informarClientesConectados();
        }
    }

    private void informarClientesConectados(){
        String mensaje = TGson.newInstance(Constants.cantidad_clientes_conectados).add(Constants.numeroConectados,clientes.size()).getJosn();
        new EnviarMensajesClientes(mensaje).start();
    }

    private void nuevoMensaje(String mensaje,DatagramPacket packet) throws JSONException {
        new EnviarMensajesClientes(mensaje).start();

    }

    //metodos publicos
    public void mEnviarMensaje(String msj){
        Gson gson =new Gson();
        Mensaje unMensaje= new Mensaje("",this.nick,msj);
        String mensaje =TGson.newInstance(Constants.op_nuevo_mensaje,"mensaje",unMensaje).getJosn();
        new EnviarMensajesClientes(mensaje).start();
        //notificarUI(mensaje);
    }

    public void finalizar(){
        desconectarServer();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        estado = false;
        mSocket.close();
    }

    @Override
    public void run() {
        try {
            Log.i(TAG,"Creando el servidor");

            mSocket = new DatagramSocket(this.mPort | 8888, InetAddress.getByName("0.0.0.0"));
            mSocket.setBroadcast(true);

            estado = true;
            while (estado) {

                //Esperando un paquete;
                byte[] recvBuf = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                try {
                    Log.i(TAG,"Esperando clientes");

                    mSocket.receive(packet);

                    Log.i(TAG,"Cliente conectado");

                }catch (Exception e){
                    Log.e(TAG,"Servidor Finalizado");
                    estado=false;
                }


                if (estado){

                    Log.i(TAG,"Leyendo mensaje");

                    String message = new String(packet.getData()).trim();

                    Log.i(TAG,"mensaje recibido >>> "+message);

                    try {
                        trabajarJSON(message,packet);
                    } catch (JSONException e) {

                       notificarUI(TGson.newInstance("toast","mensaje",e.getMessage()).getJosn());

                       e.printStackTrace();
                    }

                }

            }
        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private class EnviarMensajesClientes extends Thread {

        String mensaje;

        private EnviarMensajesClientes(String mensaje) {
            this.mensaje=mensaje;

        }

        @Override
        public void run() {
            Set set2 = clientes.entrySet();
            Iterator iterator2 = set2.iterator();
            while(iterator2.hasNext()) {
                Map.Entry mentry2 = (Map.Entry)iterator2.next();

                Cliente unCliente = (Cliente)mentry2.getValue();

                enviarMensaje(unCliente,mensaje);


                Log.i(TAG,"mensaje enviado a >> "+unCliente.getNick());

            }
            notificarUI(mensaje);
            Log.i(TAG,"mensaje al server");

        }
    }

    public class EnviarMensaje extends Thread{

        private String mensaje;
        private Cliente receptor;


        public EnviarMensaje(Cliente receptor,String mensaje) {
            this.mensaje=mensaje;
            this.receptor=receptor;
        }

        @Override
        public void run() {
            enviarMensaje(receptor,mensaje);
        }
    }
}
