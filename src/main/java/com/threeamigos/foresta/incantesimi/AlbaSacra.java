package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 *
 * @author Stefano Reksten
 */
public class AlbaSacra  implements Incantesimo {

    private final int livello;

    public AlbaSacra(int livello) {
        this.livello = livello;
    }

    public ClasseIncantesimo getClasse() {
        return ClasseIncantesimo.ALBA_SACRA;
    }

    public int getLivello() {
        return livello;
    }

    public int getCostoLancio() {
        return Costanti.INCANTESIMO_ALBA_SACRA_COSTO_LANCIO;
    }

    public void formula(Personaggio formulante, Personaggio personaggioBersaglio, Gruppo gruppoBersaglio) {

        BusEventi.pubblica(new NotificaTestoFrase(formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
                Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " formula un " + getClasse().getNomeSingolare() + "."));

        for (Personaggio personaggio : gruppoBersaglio.getPersonaggi()) {
            personaggio.rimuoviTuttiGliEffettiDiStato();
        }
        formulante.subMagia(getCostoLancio());
    }
}
