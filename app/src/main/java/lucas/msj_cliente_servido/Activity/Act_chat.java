package lucas.msj_cliente_servido.Activity;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import lucas.msj_cliente_servido.Adapter.AdaptadorMensajes;
import lucas.msj_cliente_servido.Class.Cliente;
import lucas.msj_cliente_servido.Class.Constants;
import lucas.msj_cliente_servido.Class.Mensaje;
import lucas.msj_cliente_servido.Class.Servidor;
import lucas.msj_cliente_servido.R;

public class Act_chat extends AppCompatActivity {

    ProgressDialog progress;

    private static final String TAG = Act_chat.class.getSimpleName();
    String nick;

    Cliente cliente;
    Servidor servidor;

    EditText ET_mensaje;
    TextView TV_estado;
    TextView TV_conectados;


    RecyclerView RV_recycler;
    private LinearLayoutManager linearLayout;

    AdaptadorMensajes adapterMensaje;

    Handler handlerServer;
    Handler handlerCliente;
    private TextView TV_mensaje;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_chat);

        ET_mensaje =(EditText)findViewById(R.id.ET_cuerpoComentario);
        TV_mensaje =(TextView)findViewById(R.id.TV_mensajes);
        TV_estado = (TextView)findViewById(R.id.TV_chat_estado);
        TV_conectados = (TextView)findViewById(R.id.TV_chat_conectados);

        linearLayout = new LinearLayoutManager(this);
        RV_recycler = (RecyclerView) findViewById(R.id.RV_recycler);
        RV_recycler.setLayoutManager(linearLayout);

        this.nick = getIntent().getExtras().getString("nick");

        setTitle("nick: "+this.nick);

        progress = ProgressDialog.show(this, getString(R.string.chact),getString(R.string.conectando), false);

        initRecycler();

        initHandler();

        init();
    }

    private void initRecycler() {
        adapterMensaje = new AdaptadorMensajes();
        RV_recycler.setAdapter(adapterMensaje);
    }

    private void initHandler() {
        handlerServer = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                procesarDstosServidor(chatLine);
                //addChatLine(chatLine);
                //progress.dismiss();
            }
        };

        handlerCliente = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                procesarDstosCliente(chatLine);
                //addChatLine(chatLine);
                //progress.dismiss();
            }
        };

    }

    private void init() {
        iniciarCliente();
    }


    public void onClickenviarMensaje(View view){
        String mensaje = ET_mensaje.getText().toString().trim();
        if (!mensaje.equals("")){
            if (cliente != null){
                cliente.enviarMensaje(mensaje);
            }else{
                servidor.mEnviarMensaje(mensaje);
            }
            ET_mensaje.setText("");
        }
    }

    public void iniciarCliente() {
        progress.show();
        cliente = new Cliente(this.nick,handlerCliente,true);
        final Thread cli = new Thread(cliente);
        cli.start();
        Runnable progressRunnable = new Runnable() {

            @Override
            public void run() {
                if (cliente.getInetAdressServer() == null){
                    cliente.finalizar();
                    cliente = null;
                    iniciarServidor();
                }
                inPley(true);
                progress.cancel();
            }
        };

        Handler pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable,1000);

    }

    public void reconectar(){
        progress.show();
        final Thread cli = new Thread(cliente);
        cli.start();
        Runnable progressRunnable = new Runnable() {

            @Override
            public void run() {
                if (cliente!= null && cliente.getInetAdressServer() == null){
                    reconectar();
                    inPley(true);
                }else {
                    servidor = null;
                    inPley(false);
                }
                progress.cancel();
            }
        };

        Handler pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable,1000);
    }

    public void iniciarServidor(){
        cliente = null;
        servidor = new Servidor(this.nick,handlerServer);
        Thread serv= new Thread(servidor);
        serv.start();
        inPley(true);
    }

    public void finalizarConexion(){
        if (servidor!=null){
            servidor.finalizar();
        }
        if (cliente!=null){
            cliente.finalizar();
        }
    }

    private void inPley(boolean b) {
        TV_estado.setText(b?"Conectado":"Desconectado");
    }

    public void procesarDstosCliente(String data){
        try {
            trabajarJson(data);
        } catch (JSONException e) {
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
        //TV_mensaje.append("Cliente: " +data+"\n--------------------------------\n");

    }
    public void procesarDstosServidor(String data){
        try {
            trabajarJson(data);
        } catch (JSONException e) {
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finalizarConexion();
    }

    @Override
    protected void onRestart() {
        if (cliente ==null && servidor == null){
            iniciarCliente();
        }
        super.onRestart();
    }

    @Override
    protected void onPause() {
        finalizarConexion();
        super.onPause();
    }

    private void trabajarJson(String response) throws JSONException {
        if (!response.equals("")) {
            JSONObject json = new JSONObject(response);

            if (json.has("accion")) {
                String op = json.getString("accion");
                if (op.equals(Constants.op_nuevo_mensaje)) {
                    nuevoMensaje(response);
                } else if (op.equals(Constants.op_aceptado)) {
                    aceptado(response);
                } else if (op.equals(Constants.op_nuevo_cliente)) {
                    nuevoClliente(response);
                } else if (op.equals(Constants.reconectar)) {
                    if (cliente != null){
                        reconectar();
                    }
                }else if (op.equals(Constants.nuevo_server)){
                    iniciarServidor();
                }else if (op.equals(Constants.cantidad_clientes_conectados)){
                    mostrarCantidadConectados(response);
                }

            } else {
                Log.e(TAG, "Json sin accion");
            }
        }
    }

    private void mostrarCantidadConectados(String response) throws JSONException {
        JSONObject json = new JSONObject(response);
        TV_conectados.setText(json.getString(Constants.numeroConectados));
    }

    private void nuevoClliente(String response) throws JSONException {
        JSONObject json = new JSONObject(response);
        Gson gson = new Gson();
        Cliente unCliente = gson.fromJson(json.getString("cliente"),Cliente.class);
        String mensaje = unCliente.getNick() + " ";

        adapterMensaje.addObject(mensaje);
    }

    private void aceptado(String response) throws JSONException {

    }

    private void nuevoMensaje(String response) throws JSONException {
        JSONObject json = new JSONObject(response);
        Gson gson= new GsonBuilder().disableHtmlEscaping().create();
        Mensaje unMensaje = gson.fromJson(json.getString("mensaje"),Mensaje.class);
        adapterMensaje.addObject(unMensaje);
    }
}
