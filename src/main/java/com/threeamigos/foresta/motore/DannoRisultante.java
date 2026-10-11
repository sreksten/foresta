package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.modellodati.EffettoDiStatoMD;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoInterazioneConEffettiDiStato;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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
    // Le debolezze, resistenze e immunità di classe a un tipo di danno che hanno contato nel colpo (moltiplicatore diverso da 1)
    private final Map<TipoDanno, Double> reazioniDiClasse = new LinkedHashMap<>();
    private final Collection<EffettoDiStatoMD> effettiDiStatoDaAggiungere = new ArrayList<>();
    private final Collection<ModificatoreAttributoMD> modificatoriDaAggiungere = new ArrayList<>();
    private final Collection<TipoEffettoDiStato> effettiDiStatoDaRimuovere = new ArrayList<>();
    // Quanto il danno è cambiato per l'equipaggiamento difensivo del bersaglio (1 se non conta, meno di 1 se lo protegge)
    private double fattoreEquipaggiamento = 1.0d;
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

    /**
     * Registra che il difensore, per la sua classe, è debole, resiste o è immune a quel tipo di danno; non fa niente se il
     * moltiplicatore è 1, né se per quel tipo di danno l'aveva già registrato
     */
    public void addReazioneDiClasse(TipoDanno tipoDanno, double moltiplicatore) {
        if (moltiplicatore != 1.0d) {
            reazioniDiClasse.putIfAbsent(tipoDanno, moltiplicatore);
        }
    }

    public Map<TipoDanno, Double> getReazioniDiClasse() {
        return Collections.unmodifiableMap(reazioniDiClasse);
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

    /**
     * Un modificatore che l'arma mette sul bersaglio colpito (già con bersaglio PORTATORE, vedi suPortatore)
     */
    public void addModificatoreDaAggiungere(ModificatoreAttributoMD modificatore) {
        modificatoriDaAggiungere.add(modificatore);
    }

    public Collection<ModificatoreAttributoMD> getModificatoriDaAggiungere() {
        return modificatoriDaAggiungere;
    }

    public void rimuoviEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato) {
        effettiDiStatoDaRimuovere.add(tipoEffettoDiStato);
    }

    public Collection<TipoEffettoDiStato> getEffettiDiStatoDaRimuovere() {
        return effettiDiStatoDaRimuovere;
    }

    public double getFattoreEquipaggiamento() {
        return fattoreEquipaggiamento;
    }

    public void setFattoreEquipaggiamento(double fattoreEquipaggiamento) {
        this.fattoreEquipaggiamento = fattoreEquipaggiamento;
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
