package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.DannoRisultante;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.stream.Collectors;

/**
 *
 * @author Stefano Reksten
 */
public class EventoCombattimento extends EventoPersonaggio {

    private final Personaggio bersaglio;
    private final DannoRisultante risultato;

    public EventoCombattimento(Personaggio personaggio, Personaggio bersaglio, DannoRisultante risultato) {
        super(TipoEvento.PERSONAGGIO_COMBATTIMENTO, personaggio);
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
