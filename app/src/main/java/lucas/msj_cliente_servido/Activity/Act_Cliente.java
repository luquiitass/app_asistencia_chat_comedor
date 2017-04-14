package lucas.msj_cliente_servido.Activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Observable;
import java.util.Observer;

import lucas.msj_cliente_servido.Class.ChatConnection;
import lucas.msj_cliente_servido.Class.Cliente;
import lucas.msj_cliente_servido.Class.Constants;
import lucas.msj_cliente_servido.Class.NsdHelper;
import lucas.msj_cliente_servido.R;


public class Act_Cliente extends AppCompatActivity {


    ProgressDialog progress;

    private NsdHelper mNsdHelper;
    private ChatConnection mConnection;

    public static final String TAG = Act_Cliente.class.getSimpleName();
    private Handler mUpdateHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act__cliente);

        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                try {
                    JSONObject json = new JSONObject(chatLine);
                    if (json.has(Constants.asistencia)){
                        if (json.has(Constants.accion)){

                            String accion = json.getString(Constants.accion);

                            if (accion.equals(Constants.op_presencia_registrada)){
                                Toast.makeText(Act_Cliente.this,getString(R.string.asistencia_registrada),Toast.LENGTH_LONG).show();
                            }else if (accion.equals(Constants.desconectado)){
                                Toast.makeText(Act_Cliente.this,getString(R.string.error_al_conectar),Toast.LENGTH_LONG).show();
                            }
                        }else{
                            Log.e(TAG,"Falta pasar accion");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(Act_Cliente.this,e.getMessage(),Toast.LENGTH_LONG).show();
                }
                progress.dismiss();
            }
        };

        mConnection = new ChatConnection(mUpdateHandler);

        mNsdHelper = new NsdHelper(this);

    }


    public void onClickRegistrarAsistencia(View view) {
        progress = ProgressDialog.show(this, getString(R.string.conectando),getString(R.string.registrando_presencia), true);
        final Cliente cliente = new Cliente("Legajo",mUpdateHandler);
        final Thread cli = new Thread(cliente);
        cli.start();

        Runnable progressRunnable = new Runnable() {

            @Override
            public void run() {
                progress.cancel();
                cliente.finalizar();
            }
        };

        Handler pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable, 2000);

    }


//    public void onClickSend(View view) {
//        try {
//            EditText et = (EditText) findViewById(R.id.EditText01);
//            String str = et.getText().toString();
//            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
//            out.println(str);
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void onClickConectar(View v){
//        new Thread(new ClientThread()).start();
//    }
//
//    private class ClientThread implements Runnable {
//
//        @Override
//        public void run() {
//
//            try {
//                Log.e("Cli","Conectando al Servidor");
//
//                String ip = ET_ip.getText().toString().trim();
//                int port = Integer.parseInt(ET_puerto.getText().toString().trim());
//
//                //ET_estado.setText("Conectado");
//
//                InetAddress serverAddr = InetAddress.getByName(ip);
//
//                socket = new Socket(serverAddr, port);
//
//            } catch (UnknownHostException e1) {
//                e1.printStackTrace();
//                //ET_estado.setText("No Conectado");
//
//                Log.e("Cli","Error Conectando al Servidor");
//            } catch (IOException e1) {
//                e1.printStackTrace();
//                //ET_estado.setText("No Conectado");
//
//                Log.e("Cli","Error Conectando al Servidor");
//            }
//
//        }
//
//    }


}
