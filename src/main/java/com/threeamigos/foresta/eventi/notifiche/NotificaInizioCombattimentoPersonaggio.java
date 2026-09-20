package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoSuPersonaggio;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.motore.DannoRisultante;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.stream.Collectors;

/**
 * I risultati di un round di combattimento.
 *
 * @author Stefano Reksten
 */
public class NotificaInizioCombattimentoPersonaggio extends EventoSuPersonaggio {

    /**
     * IL bersaglio dell'azione di combattimento
     */
    private final Personaggio bersaglio;
    /**
     * Il risultato dell'azione di combattimento
     */
    private final DannoRisultante risultato;

    public NotificaInizioCombattimentoPersonaggio(Personaggio personaggio, Personaggio bersaglio, DannoRisultante risultato) {
        super(TipoEvento.NOTIFICA_INIZIO_COMBATTIMENTO_PERSONAGGIO, personaggio);
        this.bersaglio = bersaglio;
        this.risultato = risultato;
    }

    public DannoRisultante getRisultato() {
        return risultato;
    }

    public Personaggio getBersaglio() {
        return bersaglio;
    }

    public String formattaRisultatoCombattimento() {
        if (risultato == null) {
            return "MISS";
        }
        return "TipoDanno: " + risultato.getTipoDanno() +
        ", Danno: " + risultato.getDanno() +
        ", Fatale: " + risultato.isColpoDiGrazia() +
        ", Interazioni elementali: " +
        risultato.getInterazioniElementali().stream().map(e -> "+" + e).collect(Collectors.joining(", ")) +
        ", Effetti di stato: " +
        risultato.getEffettiDiStatoDaAggiungere().stream().map(e -> "+" + e.getTipoEffettoDiStato()
                + ":" + e.getDurata()).collect(Collectors.joining(", ")) +
        risultato.getEffettiDiStatoDaRimuovere().stream().map(e -> "-" + e.name()).collect(Collectors.joining(", "));
    }
}
