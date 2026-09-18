package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoGiocatore;

/**
 * TestataSalvataggio contiene informazioni sul gruppo del giocatore. Serve per identificare
 * quale salvataggio si voglia caricare.
 *
 * @author Stefano Reksten
 */
public class TestataSalvataggio {

    private final Comando id;
    private final String descrizione;
    private final GruppoGiocatore gruppoGiocatore;

    public TestataSalvataggio(Comando id, String descrizione, GruppoGiocatore gruppoGiocatore) {
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

    public GruppoGiocatore getGruppoGiocatore() {
        return gruppoGiocatore;
    }
}
