package lucas.msj_cliente_servido.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import lucas.msj_cliente_servido.Class.Cliente;
import lucas.msj_cliente_servido.Class.Servidor;
import lucas.msj_cliente_servido.R;

public class Act_classes extends AppCompatActivity implements Observer {


    public TextView TV_conectado;
    public TextView TV_ipServ;
    public TextView TV_desconectado;
    public TextView TV_mensajes;
    public EditText ET_mensaje;

    public String mNick;


    private Cliente cliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_classes);

        TV_ipServ = (TextView) findViewById(R.id.TV_ipServ);
        TV_conectado = (TextView) findViewById(R.id.TV_conectado);
        TV_desconectado = (TextView) findViewById(R.id.TV_desconectado);
        TV_mensajes = (TextView)findViewById(R.id.TV_mensajes);
        ET_mensaje = (EditText)findViewById(R.id.ET_cuerpoComentario);

        mNick =getIntent().getExtras().getString("nick");
        iniciarCliente();

    }



    public void iniciarCliente(){
        //cliente = new Cliente(mNick);
        //cliente.addObserver(this);
        //Thread c = new Thread(cliente);
        //c.start();
    }


    public void enviarMensaje(View v){
        String mensaje = ET_mensaje.getText().toString().trim();
        if (!mensaje.equals("")){
            cliente.enviarMensaje(mensaje);
            ET_mensaje.setText("");

        }

    }

    public void onLine(final boolean online){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {;
                TV_ipServ.setText(cliente.getInetAdressServer()!=null?cliente.getInetAdressServer().getHostAddress():"Sin Serv");
                TV_conectado.setVisibility(online?View.VISIBLE:View.GONE);
                TV_desconectado.setVisibility(online?View.GONE:View.VISIBLE);
            }
        });
    }

    public void añadirMensaje(final String mensaje){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {;
                TV_mensajes.append(mensaje+"\n");
            }
        });
    }


    @Override
    public void update(Observable o, final Object arg) {
        if (arg != null){
            switch (arg.toString()){
                case "aceptado":
                    onLine(true);
                    añadirMensaje(cliente.getNick() + " conectado");
                    break;
                case "cerrarConexion":
                    onLine(false);
                    break;
                case "nickExistente":
                    //Toast.makeText(this,"El nick ya existe pruebe con otro ",Toast.LENGTH_SHORT).show();
                    //finish();
                    break;
                default:
                    añadirMensaje(arg.toString().trim());
            }
        }
    }
}
