package com.threeamigos.foresta.tipi;


/**
 * I tipi di danno, divisi in fisici, elementali e magici ({@link CategoriaDanno}). Ogni tipo ha un nome da mostrare
 * e gli {@link TipoEffettoDiStato stati alterati} che può causare.
 * <p>
 * I commenti su a ciascun tipo descrivono come ci si aspetta che si comporti. Le debolezze e le resistenze delle classi
 * di personaggio ai vari tipi stanno in {@link TipoPersonaggio} e in {@link CategoriaPersonaggio}.
 * <p>
 * I testi di ogni costante (NOME) non stanno qui ma nel file {@code tipo_danno_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link Testi}).
 *
 * @author Stefano Reksten
 */
public enum TipoDanno {

    /** Impatti da armi smussate (martelli, mazze) o cadute. Efficace contro scheletri e armature pesanti. */
    CONTUNDENTE(CategoriaDanno.FISICO, TipoEffettoDiStato.STORDITO, TipoEffettoDiStato.ATTERRATO), // Bludgeoning
    /** Attacchi di punta (frecce, lance, morsi). Penetra i punti deboli delle corazze. */
    PERFORANTE(CategoriaDanno.FISICO), // Piercing
    /** Tagli netti (spade, asce, artigli). Causa ferite esposte e sanguinamenti (DoT). */
    TAGLIENTE(CategoriaDanno.FISICO, TipoEffettoDiStato.SANGUINAMENTO), // Slashing

    /** Spinte d'aria, lacerazioni da vuoto, tempeste e sbalzi di pressione. Spesso sposta o destabilizza i bersagli. */
    ARIA(CategoriaDanno.ELEMENTALE, TipoEffettoDiStato.ATTERRATO), // Wind
    /** Impatti idrici, annegamento, pressione idrostatica o getti corrosivi. Spesso spegne il fuoco o spinge i nemici. */
    ACQUA(CategoriaDanno.ELEMENTALE, TipoEffettoDiStato.BAGNATO), // Water
    /** Impatti da rocce, terremoti o sabbia. Perfetto per logorare le difese o accecare il bersaglio. */
    TERRA(CategoriaDanno.ELEMENTALE, TipoEffettoDiStato.IMMOBILIZZATO, TipoEffettoDiStato.ACCECATO), // Earth
    /** Brucia i bersagli, infiamma gli oggetti e spesso infligge danni nel tempo (DoT). */
    FUOCO(CategoriaDanno.ELEMENTALE, TipoEffettoDiStato.BRUCIATO), // Fire
    /** Danno rapido che spesso si propaga tra bersagli vicini, disattiva reazioni o stordisce. */
    FULMINE(CategoriaDanno.ELEMENTALE, TipoEffettoDiStato.STORDITO), // Lightning
    /** Congela i fluidi corporei, rallenta i movimenti e riduce la velocità d'attacco. */
    GELO(CategoriaDanno.ELEMENTALE, TipoEffettoDiStato.RALLENTATO, TipoEffettoDiStato.CONGELATO), // Cold/Ice
    /** Corrode armature, scioglie la carne e riduce permanentemente le difese della vittima. */
    ACIDO(CategoriaDanno.ELEMENTALE), // Acid
    /** Onde d'urto invisibili, vibrazioni e boati che spingono indietro i nemici o li assordano. */
    SONICO(CategoriaDanno.ELEMENTALE, TipoEffettoDiStato.ASSORDATO), // Sonic
    /** Tossine che debilitano il bersaglio, riducendo le statistiche e consumando la salute lentamente (DoT). */
    VELENO(CategoriaDanno.ELEMENTALE, TipoEffettoDiStato.AVVELENATO), // Poison
    /** Energia della morte e del decadimento. Dissangua la forza vitale e impedisce la cura. */
    NECROTICO(CategoriaDanno.MAGICO, TipoEffettoDiStato.INFETTATO), // Necrotic
    /** Energia di luce divina e radiazioni celestiali. Devastante contro non-morti, demoni e oscurità. */
    SACRO(CategoriaDanno.MAGICO, TipoEffettoDiStato.ACCECATO), // Holy
    /** Attacchi mentali diretti (illusioni, telepatia). Ignora le armature fisiche e causa confusione o shock. */
    PSICHICO(CategoriaDanno.MAGICO, TipoEffettoDiStato.CONFUSO, TipoEffettoDiStato.SPAVENTATO), // Psychic
    /** Energia magica pura, grezza e manipolata (es. Dardo Incantato). Il tipo di danno meno resistito. */
    ARCANO(CategoriaDanno.MAGICO, TipoEffettoDiStato.SILENZIATO), // Arcane
    /** L'assenza di tutto, la gravità o l'energia delle stelle. Astrazione spaziale che disintegra la materia. */
    VUOTO(CategoriaDanno.MAGICO), // Void
    /** Corruzione e influssi maligni. Può ridurre i punti vita o intaccare le statistiche massime dei personaggi. */
    MALEDIZIONE(CategoriaDanno.MAGICO, TipoEffettoDiStato.MALEDETTO); // Curse

    private final CategoriaDanno categoriaDanno;
    private final TipoEffettoDiStato[] effetti;

    TipoDanno(CategoriaDanno categoriaDanno, TipoEffettoDiStato... effetti) {
        this.categoriaDanno = categoriaDanno;
        this.effetti = effetti;
    }

    public CategoriaDanno getCategoriaDanno() {
        return categoriaDanno;
    }

    /**
     * Gli stati alterati che questo tipo di danno può causare: una copia, perché l'array è di un'enum e chi lo modificasse lo cambierebbe per tutti.
     */
    public TipoEffettoDiStato[] getEffetti() {
        return effetti.clone();
    }

    public String getNome() {
        return Testi.testoOCostante(this, "NOME");
    }

    /**
     * Dice se il tipo di danno può causare almeno uno stato alterato.
     */
    public boolean hasEffettiDiStato() {
        return effetti.length > 0;
    }

}
