package lucas.msj_cliente_servido.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.ArrayList;

import lucas.msj_cliente_servido.Class.Mensaje;
import lucas.msj_cliente_servido.R;

/**
 * Created by lucas on 27/12/16.
 */

public class AdaptadorMensajes extends RecyclerView.Adapter<AdaptadorMensajes.ViewHolder> implements View.OnClickListener{

    private ArrayList<Object> vistas = new ArrayList<>();

    //private Principal principal;

    private ViewHolder viewSinDatos = null;

    private View.OnClickListener listener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Campos respectivos de un item

        private LinearLayout LL_mensaje;
        //protected TextView ip;
        protected TextView nick;
        protected TextView mensaje;
        protected TextView fecha;
        protected TextView hora;


        protected LinearLayout LL_mensajeNuevoCliente;
        protected TextView mensajeNuevoCliente;


        public ViewHolder(View v) {
            super(v);
            //ip=(TextView)v.findViewById(R.id.TV_unEquipo_id);
            LL_mensaje=(LinearLayout) v.findViewById(R.id.LL_mensaje);
            nick=(TextView)v.findViewById(R.id.TV_mensaje_nick);
            mensaje = (TextView) v.findViewById(R.id.TV_mensaje_mensaje);
            fecha = (TextView) v.findViewById(R.id.TV_mensaje_fecha);
            hora = (TextView) v.findViewById(R.id.TV_mensaje_hora);

            LL_mensajeNuevoCliente=(LinearLayout) v.findViewById(R.id.LL_mensajeNuevoCliente);
            mensajeNuevoCliente = (TextView)v.findViewById(R.id.TV_mensajeNuevoCliente);
        }
    }


    public AdaptadorMensajes(){
        this.vistas = new ArrayList<>();
        //this.principal = principal;
        //this.vistas = principal.getEquipos();
    }
    public void addObject(Object object){
        this.vistas.add(object);
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return this.vistas.size();
    }

    public Object getObjetVistas(int pos)
    {
        return vistas.get(pos);
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.layout_mensaje_nuvo_cliente, viewGroup, false);

        //v.setOnClickListener(this);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Object item = vistas.get(i);
        if (item instanceof Mensaje){
            viewHolder.LL_mensaje.setVisibility(View.VISIBLE);
            viewHolder.LL_mensajeNuevoCliente.setVisibility(View.GONE);
            viewHolder.nick.setText(((Mensaje)item).getNick());
            viewHolder.mensaje.setText(((Mensaje)item).getMensaje());
            viewHolder.fecha.setText(((Mensaje)item).getFecha());
            viewHolder.hora.setText(((Mensaje)item).getHora());
        }else{
            viewHolder.LL_mensaje.setVisibility(View.GONE);
            viewHolder.LL_mensajeNuevoCliente.setVisibility(View.VISIBLE);
            viewHolder.mensajeNuevoCliente.setText(item.toString());
        }
    }

    private void cargarDatosViewHolder(){

    }


    public void actualizar(){
        //this.vistas = this.principal.getEquipos();
        //this.notifyDataSetChanged();
    }


    public void setClickListener(View.OnClickListener listener){
        this.listener= listener;
    }

    @Override
    public void onClick(View v) {
        if (listener!= null){
            listener.onClick(v);
        }
    }

}
