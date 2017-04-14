package lucas.msj_cliente_servido.Activity;

import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import lucas.msj_cliente_servido.Class.ChatConnection;
import lucas.msj_cliente_servido.Class.Cliente;
import lucas.msj_cliente_servido.Class.Constants;
import lucas.msj_cliente_servido.Class.NsdHelper;
import lucas.msj_cliente_servido.Class.Servidor;
import lucas.msj_cliente_servido.R;

public class Act_Servidor extends AppCompatActivity {

    ImageButton IB_pleyServer;
    ImageButton IB_stopServer;

    TextView TV_conectados;
    TextView TV_serverState;

    public static final String TAG = "SERVIDOR";
    private Handler mUpdateHandler;
    private Servidor servidor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act__servidor);

        IB_stopServer = (ImageButton) findViewById(R.id.IB_stopServer);
        IB_pleyServer = (ImageButton) findViewById(R.id.IB_pleyServer);

        TV_conectados =(TextView) findViewById(R.id.TV_conectados);
        TV_serverState =(TextView) findViewById(R.id.TV_serverState);

        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                try {
                    JSONObject json= new JSONObject(chatLine);
                    if (json.has(Constants.asistencia)){
                        addChatLine(json.getString("mensaje"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

    }

    public void onClickPleyServer(View v){
        servidor = new Servidor("nada",mUpdateHandler);
        Thread serv= new Thread(servidor);
        serv.start();
        inPley(true);

    }

    public void onClickStopServer(View v){
        servidor.finalizar();
        inPley(false);
    }


    public void inPley(boolean estado){
        TV_serverState.setText(estado?"Servidor Iniciado":"Servidor Apagado");
        IB_pleyServer.setVisibility(estado?View.VISIBLE:View.GONE);
        IB_stopServer.setVisibility(!estado?View.VISIBLE:View.GONE);
    }

    public void addChatLine(String line) {
        TV_conectados.append("\n" + line);
    }

}
