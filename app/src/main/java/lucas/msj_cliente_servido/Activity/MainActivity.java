package lucas.msj_cliente_servido.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import lucas.msj_cliente_servido.Class.Cliente;
import lucas.msj_cliente_servido.Class.Servidor;
import lucas.msj_cliente_servido.R;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void OnClickIniciarServisor(View v){
        startActivity(new Intent(this,Act_Servidor.class));
    }


    public void OnClickIniciarCliente(View v){
       startActivity(new Intent(this,Act_Cliente.class));
    }


    public void OnClickIniciarClass(View v){
        EditText ET_nick = (EditText)findViewById(R.id.ET_nick);
        String nick = ET_nick.getText().toString().trim();
        if (!nick.equals("")){
            Intent intent= new Intent(this,Act_classes.class);
            intent.putExtra("nick",nick);

            startActivity(intent);

        }else {
            Toast.makeText(this,"Nick obligatorio",Toast.LENGTH_SHORT).show();
        }

    }


    public void onClickIrServer(View view) {
        startActivity(new Intent(this,Act_Servidor.class));
    }

    public void onClickIrCliente(View view) {
        startActivity(new Intent(this,Act_Cliente.class));
    }

    public void onClickIrChat(View view) {
        EditText ET_nick = (EditText)findViewById(R.id.ET_nick2);
        String nick = ET_nick.getText().toString().trim();
        if (!nick.equals("")){
            Intent intent= new Intent(this,Act_chat.class);
            intent.putExtra("nick",nick);

            startActivity(intent);

        }else {
            Toast.makeText(this,"Nick obligatorio",Toast.LENGTH_SHORT).show();
        }


    }
}
