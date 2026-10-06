package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.EffettoDiStatoMD;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoInterazioneConEffettiDiStato;

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
    private final Collection<EffettoDiStatoMD> effettiDiStatoDaAggiungere = new ArrayList<>();
    private final Collection<TipoEffettoDiStato> effettiDiStatoDaRimuovere = new ArrayList<>();
    private boolean colpoDiGrazia = false;
    private int curaAdArea = 0;
    private int sifoneVitale = 0;
    private boolean diluizioneEmaticaAttiva = false;
    private boolean collassoEntropicoAttivo = false;
    private boolean mietituraAttiva = false;

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
        effettiDiStatoDaAggiungere.add(new EffettoDiStatoMD(tipoEffettoDiStato, durata, danniNelTempo));
    }

    public Collection<EffettoDiStatoMD> getEffettiDiStatoDaAggiungere() {
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

    public int getCuraAdArea() {
        return curaAdArea;
    }

    public void setCuraAdArea(int curaAdArea) {
        this.curaAdArea = curaAdArea;
    }

    public int getSifoneVitale() {
        return sifoneVitale;
    }

    public void setSifoneVitale(int sifoneVitale) {
        this.sifoneVitale = sifoneVitale;
    }

    public boolean isDiluizioneEmaticaAttiva() {
        return diluizioneEmaticaAttiva;
    }

    public void setDiluizioneEmaticaAttiva(boolean diluizioneEmaticaAttiva) {
        this.diluizioneEmaticaAttiva = diluizioneEmaticaAttiva;
    }

    public boolean isCollassoEntropicoAttivo() {
        return collassoEntropicoAttivo;
    }

    public void setCollassoEntropicoAttivo(boolean collassoEntropicoAttivo) {
        this.collassoEntropicoAttivo = collassoEntropicoAttivo;
    }

    public boolean isMietituraAttiva() {
        return mietituraAttiva;
    }

    public void setMietituraAttiva(boolean mietituraAttiva) {
        this.mietituraAttiva = mietituraAttiva;
    }

}
