package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 *
 * @author Stefano Reksten
 */
public class AlbaSacra  implements Incantesimo {

    private final int livello;

    public AlbaSacra(int livello) {
        this.livello = livello;
    }

    /**
     * @return true se la classe sa lanciare l'alba sacra senza pergamene (Sacerdote e Sacerdotessa)
     */
    public static boolean conosciutaDa(TipoPersonaggio classe) {
        return classe == TipoPersonaggio.SACERDOTE || classe == TipoPersonaggio.SACERDOTESSA;
    }

    /**
     * @return true se il personaggio è vivo, la conosce per natura e ha abbastanza MAGIA per lanciarla: in tal caso non
     * le servono pergamene, e se ne ha il lancio non ne consuma
     */
    public static boolean puoLanciarlaInnata(Personaggio personaggio) {
        return personaggio.isVivo() && conosciutaDa(personaggio.getClasse())
                && personaggio.getMagia() >= Costanti.INCANTESIMO_ALBA_SACRA_COSTO_LANCIO
                && !personaggio.hasEffettoDiStato(TipoEffettoDiStato.SILENZIATO);
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
