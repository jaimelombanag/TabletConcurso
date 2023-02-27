package com.tablet.concurso.viewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import com.tablet.concurso.Clases.DatosTransferDTO;
import com.tablet.concurso.Servicios.ConnexionTCP;

public class SocketViewModel extends AndroidViewModel {
    ConnexionTCP sSocketOpen = new ConnexionTCP(this.getApplication());
    public SocketViewModel(@NonNull Application application) {
        super(application);
    }
    public LiveData<DatosTransferDTO> getRespuesta() {
        return sSocketOpen.RespuestaDatos();
    }


}