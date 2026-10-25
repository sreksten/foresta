package com.threeamigos.foresta.combattimento;

import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoInterazioneConEffettiDiStato;
import com.threeamigos.foresta.modellodati.EffettoDiStatoMD;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.*;

/**
 * Il risultato di un colpo, prima che il difensore lo subisca: il danno e il suo tipo, le interazioni fra tipo di danno e stati scattate, le
 * debolezze e resistenze di classe che hanno contato, gli stati alterati e i modificatori da aggiungere o togliere, l'effetto dell'equipaggiamento
 * difensivo e gli effetti speciali (colpo di grazia, cura e danno ad area, sifone vitale, mietitura...).
 * <p>
 * Lo costruisce {@link CalcolatoreCombattimento} e lo applica il difensore con {@code applicaRisultatoCombattimento}.
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
    // Le reazioni che investono anche i vicini del difensore (vedi TipoInterazioneConEffettiDiStato.getDannoAdArea)
    private final List<DannoAdArea> danniAdArea = new ArrayList<>();
    // Se questo è il colpo che una reazione ad area porta a un vicino, e non un colpo scelto da chi attacca
    private boolean colpoAdArea = false;

    /**
     * Il danno che una reazione porta ai vicini del difensore: il tipo di danno e la quota del danno grezzo del colpo, prima
     * della difesa di ciascun vicino.
     */
    public static final class DannoAdArea {

        private final TipoInterazioneConEffettiDiStato interazione;
        private final double dannoGrezzo;

        public DannoAdArea(TipoInterazioneConEffettiDiStato interazione, double dannoGrezzo) {
            this.interazione = interazione;
            this.dannoGrezzo = dannoGrezzo;
        }

        public TipoInterazioneConEffettiDiStato getInterazione() {
            return interazione;
        }

        public TipoDanno getTipoDanno() {
            return interazione.getDannoAdArea();
        }

        public double getDannoGrezzo() {
            return dannoGrezzo;
        }
    }

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

    /**
     * Registra un'interazione fra tipo di danno e stato del difensore che è scattata con questo colpo.
     */
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

    /**
     * Aggiunge uno stato alterato da applicare al difensore.
     */
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

    /**
     * Segna uno stato alterato da togliere al difensore.
     */
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

    public void addDannoAdArea(DannoAdArea dannoAdArea) {
        danniAdArea.add(dannoAdArea);
    }

    /**
     * Toglie le reazioni ad area: il colpo investe solo il difensore.
     */
    public void togliDanniAdArea() {
        danniAdArea.clear();
    }

    public List<DannoAdArea> getDanniAdArea() {
        return Collections.unmodifiableList(danniAdArea);
    }

    public boolean isColpoAdArea() {
        return colpoAdArea;
    }

    public void setColpoAdArea(boolean colpoAdArea) {
        this.colpoAdArea = colpoAdArea;
    }

}
