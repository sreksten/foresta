package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.TipoEffettoDiStato;

/**
 * Uno stato alterato in corso su un personaggio (per esempio un veleno o l'ispirazione): di che tipo è, quanto dura
 * ancora, quanti danni infligge a ogni scatto del tempo (per gli stati che ne fanno) e, per quelli che hanno una forza,
 * quanto vale.
 * <p>
 * La durata e i danni sono modificabili, perché il motore li fa scendere col passare del tempo; il tipo no.
 *
 * @author Stefano Reksten
 */
public class EffettoDiStatoMD {

    private final TipoEffettoDiStato tipoEffettoDiStato;
    private int valore;
    private int danniNelTempo;
    private double intensita;

    /**
     * Un effetto senza intensità.
     */
    public EffettoDiStatoMD(TipoEffettoDiStato tipoEffettoDiStato, int valore, int danniNelTempo) {
        this(tipoEffettoDiStato, valore, danniNelTempo, 0.0d);
    }

    /**
     * @param valore la durata rimasta
     * @param danniNelTempo i danni a ogni scatto del tempo, 0 per gli stati che non ne fanno
     * @param intensita la forza dell'effetto, 0 per gli stati che non ne hanno
     */
    public EffettoDiStatoMD(TipoEffettoDiStato tipoEffettoDiStato, int valore, int danniNelTempo, double intensita) {
        this.tipoEffettoDiStato = tipoEffettoDiStato;
        this.valore = valore;
        this.danniNelTempo = danniNelTempo;
        this.intensita = intensita;
    }

    /**
     * Il tipo di stato alterato.
     */
    public TipoEffettoDiStato getTipoEffettoDiStato() {
        return tipoEffettoDiStato;
    }

    /**
     * La durata rimasta.
     */
    public int getDurata() {
        return valore;
    }

    public void setDurata(int valore) {
    	this.valore = valore;
    }

    /**
     * I danni che l'effetto infligge a ogni scatto del tempo; 0 per gli stati che non ne fanno.
     */
    public int getDanniNelTempo() {
    	return danniNelTempo;
    }

    public void setDanniNelTempo(int dannoNelTempo) {
    	this.danniNelTempo = dannoNelTempo;
    }

    /**
     * Di quanto vale l'effetto per gli stati che hanno una forza (per ISPIRATO la percentuale di coraggio e di
     * precisione in più); 0 per gli altri
     */
    public double getIntensita() {
        return intensita;
    }

    public void setIntensita(double intensita) {
        this.intensita = intensita;
    }
}
