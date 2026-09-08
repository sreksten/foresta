package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.TipoDanno;
import com.threeamigos.foresta.personaggi.ClassePersonaggio; // Presumo sia la tua Enum delle 30 classi
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Rappresenta gli attacchi biologici, magici o spettrali innati delle creature
 * che non utilizzano armi tradizionali nell'inventario.
 */
public class ArmaNaturale implements Arma {

    private final Personaggio proprietario;
    private final TipoAttaccoNaturale tipoAttacco;

    // Enum interno per mappare la potenza biologica base di ogni creatura
    public enum TipoAttaccoNaturale {
        ARTIGLI_LEGGERI(4, true, TipoDanno.TAGLIENTE),   // Arpia, Folletto, Goblin
        MORSO_BRUTO(8, true, TipoDanno.PERFORANTE),       // Minotauro, Troll, Chimera
        ZAMPATA_COLOSSALE(12, true, TipoDanno.CONTUNDENTE),// Gigante, Minotauro Gigante
        ZANNE_DRACONICHE(16, true, TipoDanno.VELENO), // Drago, Viverna, Chimera Drago
        TOCCO_GELIDO(5, false, TipoDanno.GELO),     // Spettro, Fantasma
        DISSIPAZIONE_ANIMA(10, false, TipoDanno.NECROTICO),// Ombra Nera, Spirito
        FIAMMA_VIVA(14, false, TipoDanno.FUOCO);     // Ombrafiamma

        private final int dannoInizialeFisso;
        private final boolean scalaSuForza; // true = Forza (Fisico), false = Intelligenza (Magico)
        private final TipoDanno tipoDanno;

        TipoAttaccoNaturale(int dannoInizialeFisso, boolean scalaSuForza, TipoDanno tipoDanno) {
            this.dannoInizialeFisso = dannoInizialeFisso;
            this.scalaSuForza = scalaSuForza;
            this.tipoDanno = tipoDanno;
        }
    }

    public ArmaNaturale(Personaggio proprietario) {
        this.proprietario = proprietario;
        this.tipoAttacco = assegnaTipoAttacco(proprietario.getClasse());
    }

    /**
     * Associa automaticamente ogni classe/mostro al suo corretto stile di attacco innato.
     */
    private TipoAttaccoNaturale assegnaTipoAttacco(ClassePersonaggio classe) {
        switch (classe) {
            case ARPIA:
            case FOLLETTO:
            case GOBLIN:
            case HOBGOBLIN:
            case SCHELETRO:
                return TipoAttaccoNaturale.ARTIGLI_LEGGERI;
            case MINOTAURO:
            case TROLL:
            case CHIMERA:
            case IDRA:
                return TipoAttaccoNaturale.MORSO_BRUTO;
            case GIGANTE:
            case MINOTAURO_GIGANTE:
            case TITANO:
            case GARGOYLE:
            case CENTAURO:
                return TipoAttaccoNaturale.ZAMPATA_COLOSSALE;
            case DRAGO:
            case CHIMERA_DRAGO:
            case VIVERNA:
                return TipoAttaccoNaturale.ZANNE_DRACONICHE;
            case FANTASMA:
            case SPETTRO:
                return TipoAttaccoNaturale.TOCCO_GELIDO;
            case SPIRITO:
            case OMBRA_NERA:
            case LICH:
                return TipoAttaccoNaturale.DISSIPAZIONE_ANIMA;
            case OMBRAFIAMMA:
                return TipoAttaccoNaturale.FIAMMA_VIVA;
            // Classi umane giocabili (se rimangono disarmate per errore)
            default:
                return TipoAttaccoNaturale.ARTIGLI_LEGGERI;
        }
    }

    @Override
    public int getLivello() {
        // L'arma naturale livella automaticamente insieme al mostro!
        return this.proprietario.getLivello();
    }

    @Override
    public int getDanni() {
        // Estraiamo la statistica corretta (Forza o Intelligenza) per calcolare il bonus
        int statRiferimento = tipoAttacco.scalaSuForza ?
                proprietario.getForza() : proprietario.getIntelligenza();

        // Applichiamo i Diminishing Returns tramite radice quadrata sulla statistica.
        // Evita che i mostri con 100 di Forza facciano istantaneamente kill in un colpo.
        double bonusStatistica = Math.sqrt(statRiferimento) * 1.5;

        // Calcolo del danno dell'arma a livello 1
        double dannoCalcolato = tipoAttacco.dannoInizialeFisso + bonusStatistica;

        return (int) Math.max(1, Math.floor(dannoCalcolato));
    }

    @Override
    public TipoDanno getTipoDanno() {
        return tipoAttacco.tipoDanno;
    }
}
