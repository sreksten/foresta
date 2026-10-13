package com.threeamigos.foresta.strumenti;

import com.threeamigos.foresta.interfacce.VistaGruppoGiocatore;
import com.threeamigos.foresta.tipi.Comando;

/**
 * TestataSalvataggio contiene informazioni sul gruppo del giocatore. Serve per identificare
 * quale salvataggio si voglia caricare.
 *
 * @author Stefano Reksten
 */
public class TestataSalvataggio {

    private final Comando id;
    private final String descrizione;
    private final VistaGruppoGiocatore gruppoGiocatore;

    public TestataSalvataggio(Comando id, String descrizione, VistaGruppoGiocatore gruppoGiocatore) {
        this.id = id;
        this.descrizione = descrizione;
        this.gruppoGiocatore = gruppoGiocatore;
    }

    public Comando getId() {
        return id;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public VistaGruppoGiocatore getGruppoGiocatore() {
        return gruppoGiocatore;
    }
}
