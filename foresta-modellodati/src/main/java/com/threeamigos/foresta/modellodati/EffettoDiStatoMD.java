package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.TipoEffettoDiStato;

/**
 *
 * @author Stefano Reksten
 */
public class EffettoDiStatoMD {

    private final TipoEffettoDiStato tipoEffettoDiStato;
    private int valore;
    private int danniNelTempo;
    private double intensita;

    public EffettoDiStatoMD(TipoEffettoDiStato tipoEffettoDiStato, int valore, int danniNelTempo) {
        this(tipoEffettoDiStato, valore, danniNelTempo, 0.0d);
    }

    public EffettoDiStatoMD(TipoEffettoDiStato tipoEffettoDiStato, int valore, int danniNelTempo, double intensita) {
        this.tipoEffettoDiStato = tipoEffettoDiStato;
        this.valore = valore;
        this.danniNelTempo = danniNelTempo;
        this.intensita = intensita;
    }

    public TipoEffettoDiStato getTipoEffettoDiStato() {
        return tipoEffettoDiStato;
    }

    public int getDurata() {
        return valore;
    }

    public void setDurata(int valore) {
    	this.valore = valore;
    }

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
