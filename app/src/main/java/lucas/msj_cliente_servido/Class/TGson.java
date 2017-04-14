package lucas.msj_cliente_servido.Class;

import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lucas on 03/04/17.
 */

public class TGson {

    //private static String TAG = TGson.class.getSimpleName();

    private Map<String,Object> datos;

    public TGson() {
        this.datos = new HashMap<String, Object>();
    }

    public TGson(String accion, String claveObjeto, Object objeto) {
        this.datos = new HashMap<String, Object>();
        datos.put("accion",accion);
        datos.put(claveObjeto,objeto);
    }

    public static TGson newInstance(String accion, String claveObjeto, Object objeto) {
        TGson tgson = new TGson( accion, claveObjeto, objeto);
        return tgson;
    }

    public static TGson newInstance(String accion) {
        TGson tgson = new TGson();
        tgson.datos.put("accion",accion);
        return tgson;
    }
    public static TGson newInstance(){
        return new TGson();
    }

    public TGson add(String clave, Object object){
        if (!this.datos.containsKey(clave)){
            this.datos.put(clave,object);
        }else{
            Log.e(TGson.class.getSimpleName(),"Ya existe un dato con la clave "+clave);
        }
        return this;
    }

    public String getJosn(){
        Gson json = new Gson();
        return json.toJson(this.datos);
    }
}
