package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.artefatti.Arma;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.Collection;
import java.util.Collections;

/**
 * L'arma di chi non ne impugna una: gli artigli, il morso, il tocco gelido... delle creature, e i pugni di un personaggio rimasto senza
 * arma in mano. PersonaggioBase.getArmaEquipaggiata ne crea una ogni volta che la mano principale è vuota, e il combattimento la usa come
 * un'arma qualsiasi (con {@link #controDifensore} per la Fiamma viva). Non è un artefatto: non si salva, non si trova e non si incanta;
 * il suo livello è quello del proprietario.
 */
public class ArmaNaturale implements Arma {

    private final Personaggio proprietario;
    private final TipoAttaccoNaturale tipoAttacco;

    /**
     * La potenza base dell'attacco innato di ogni creatura: i danni iniziali, il tipo di danno e l'attributo su cui scala (la Forza per
     * gli attacchi fisici, l'Intelligenza per quelli magici).
     */
    public enum TipoAttaccoNaturale {
        /** Gli artigli di Arpia, Folletto, Goblin, Hobgoblin e Scheletro; anche i pugni di chiunque altro sia senza arma */
        ARTIGLI_LEGGERI(4, true, TipoDanno.TAGLIENTE),
        /** Il morso di Minotauro, Troll, Chimera e Idra */
        MORSO_BRUTO(8, true, TipoDanno.PERFORANTE),
        /** La zampata di Gigante, Minotauro Gigante, Titano, Gargoyle e Centauro */
        ZAMPATA_COLOSSALE(12, true, TipoDanno.CONTUNDENTE),
        /** Le zanne di Drago, Viverna e Chimera Drago */
        ZANNE_DRACONICHE(16, true, TipoDanno.VELENO),
        /** Il tocco di Spettro e Fantasma */
        TOCCO_GELIDO(5, false, TipoDanno.GELO),
        /** La dissipazione dell'anima di Ombra Nera, Spirito, Lich e Necromante */
        DISSIPAZIONE_ANIMA(10, false, TipoDanno.NECROTICO),
        /** La fiamma viva dell'Ombrafiamma */
        FIAMMA_VIVA(14, false, TipoDanno.FUOCO);

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

    private ArmaNaturale(Personaggio proprietario, TipoAttaccoNaturale tipoAttacco) {
        this.proprietario = proprietario;
        this.tipoAttacco = tipoAttacco;
    }

    /**
     * L'arma naturale con cui il proprietario attacca questo difensore: la Fiamma viva dell'Ombrafiamma non fa
     * niente a chi è immune al fuoco, e contro di lui si ripiega sugli artigli, altrimenti lo scontro non finirebbe mai.
     * Per tutte le altre combinazioni è l'arma stessa.
     */
    public ArmaNaturale controDifensore(Personaggio difensore) {
        if (tipoAttacco == TipoAttaccoNaturale.FIAMMA_VIVA && difensore.isImmuneATipoDanno(tipoAttacco.tipoDanno)) {
            return new ArmaNaturale(proprietario, TipoAttaccoNaturale.ARTIGLI_LEGGERI);
        }
        return this;
    }

    /**
     * L'attacco innato di ogni classe; chi non è elencato (i giocanti, i non combattenti, l'Eremita, la Strega...) ha gli artigli leggeri.
     */
    private TipoAttaccoNaturale assegnaTipoAttacco(TipoPersonaggio classe) {
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
            case NECROMANTE:
                return TipoAttaccoNaturale.DISSIPAZIONE_ANIMA;
            case OMBRAFIAMMA:
                return TipoAttaccoNaturale.FIAMMA_VIVA;
            // Tutti gli altri, giocanti compresi quando non hanno un'arma nella mano principale
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
                proprietario.get(TipoAttributo.FORZA) : proprietario.get(TipoAttributo.INTELLIGENZA);

        // Il bonus cresce con la radice quadrata dell'attributo (rendimenti decrescenti): Forza 16 dà +6, Forza 100 dà +15, così un
        // mostro fortissimo non uccide in un colpo. Un attributo portato sotto zero da un modificatore conta 0: la radice di un negativo
        // darebbe NaN, e il danno 0 invece del minimo 1
        double bonusStatistica = Math.sqrt(Math.max(0, statRiferimento)) * 1.5;

        // Il danno fisso del tipo di attacco più il bonus, troncato, e mai meno di 1. Il livello non entra qui: conta attraverso l'attributo
        double dannoCalcolato = tipoAttacco.dannoInizialeFisso + bonusStatistica;

        return (int) Math.max(1, Math.floor(dannoCalcolato));
    }

    @Override
    public TipoDanno getTipoDanno() {
        return tipoAttacco.tipoDanno;
    }

    @Override
    public boolean isIncantata() {
        return false;
    }

    @Override
    public Collection<IncantamentoMD> getIncantamenti() {
        return Collections.emptyList();
    }
}
