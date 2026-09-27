package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.tipi.TipoDanno;
import com.threeamigos.foresta.motore.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.motore.tipi.TipoInterazioneConEffettiDiStato;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Stefano Reksten
 */
public class DannoRisultante {

    private final Personaggio attaccante;
    private final Personaggio difensore;
    private TipoDanno tipoDanno;
    private int danno;
    private final Collection<TipoInterazioneConEffettiDiStato> interazioni = new ArrayList<>();
    private final Collection<EffettoDiStato> effettiDiStatoDaAggiungere = new ArrayList<>();
    private final Collection<TipoEffettoDiStato> effettiDiStatoDaRimuovere = new ArrayList<>();
    private boolean colpoDiGrazia = false;

    public DannoRisultante(Personaggio attaccante, Personaggio bersaglio) {
        this.attaccante = attaccante;
        this.difensore = bersaglio;
    }

    public Personaggio getAttaccante() {
        return attaccante;
    }

    public Personaggio getDifensore() {
        return difensore;
    }

    public TipoDanno getTipoDanno() {
        return tipoDanno;
    }

    public void setTipoDanno(TipoDanno tipoDanno) {
        this.tipoDanno = tipoDanno;
    }

    public void setDanno(int danno) {
        this.danno = danno;
    }

    public int getDanno() {
        return danno;
    }

    public void addInterazione(TipoInterazioneConEffettiDiStato tipoInterazione) {
        interazioni.add(tipoInterazione);
    }

    public Collection<TipoInterazioneConEffettiDiStato> getInterazioni() {
        return interazioni;
    }

    public void addEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato, int durata, int danniNelTempo) {
        effettiDiStatoDaAggiungere.add(new EffettoDiStato(tipoEffettoDiStato, durata, danniNelTempo));
    }

    public Collection<EffettoDiStato> getEffettiDiStatoDaAggiungere() {
        return effettiDiStatoDaAggiungere;
    }

    public void rimuoviEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato) {
        effettiDiStatoDaRimuovere.add(tipoEffettoDiStato);
    }

    public Collection<TipoEffettoDiStato> getEffettiDiStatoDaRimuovere() {
        return effettiDiStatoDaRimuovere;
    }

    public boolean isColpoDiGrazia() {
        return colpoDiGrazia;
    }

    public void setColpoDiGrazia(boolean colpoDiGrazia) {
        this.colpoDiGrazia = colpoDiGrazia;
    }

}
