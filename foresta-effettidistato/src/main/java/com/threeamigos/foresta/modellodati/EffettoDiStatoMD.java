package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.serializzazione.LettoreCampi;
import com.threeamigos.foresta.serializzazione.MappaProprieta;

import java.io.IOException;

import static com.threeamigos.foresta.serializzazione.Serializzabile.composto;

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

    // Quale stato è
    private final TipoEffettoDiStato tipoEffettoDiStato;
    // La durata rimasta
    private int valore;
    // I danni a ogni scatto del tempo
    private int danniNelTempo;
    // La forza dell'effetto, per gli stati che ne hanno una
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

    /**
     * Cambia la durata rimasta (a ogni turno scende, e a zero l'effetto finisce).
     */
    public void setDurata(int valore) {
    	this.valore = valore;
    }

    /**
     * I danni che l'effetto infligge a ogni scatto del tempo; 0 per gli stati che non ne fanno.
     */
    public int getDanniNelTempo() {
    	return danniNelTempo;
    }

    /**
     * Cambia i danni che l'effetto infligge a ogni scatto del tempo.
     */
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

    /**
     * Cambia la forza dell'effetto (vedi {@link #getIntensita()}).
     */
    public void setIntensita(double intensita) {
        this.intensita = intensita;
    }

    /**
     * L'effetto come campo di un salvataggio: tipo, durata, danni nel tempo e intensità, separati da
     * {@value MappaProprieta#SEPARATORE}. Lo rilegge {@link #daCampo(LettoreCampi)}.
     */
    public String comeCampo() {
        return composto(
                tipoEffettoDiStato,
                valore,
                danniNelTempo,
                intensita);
    }

    /**
     * Rilegge il prossimo campo scritto da {@link #comeCampo()}.
     */
    public static EffettoDiStatoMD daCampo(LettoreCampi campi) throws IOException {
        LettoreCampi sottocampi = campi.sottocampi(4, 4);
        return new EffettoDiStatoMD(
                sottocampi.enumerato(TipoEffettoDiStato.class),
                sottocampi.intero(),
                sottocampi.intero(),
                sottocampi.decimale());
    }

}
