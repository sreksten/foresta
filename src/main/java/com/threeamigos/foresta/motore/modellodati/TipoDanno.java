package com.threeamigos.foresta.motore.modellodati;

import java.util.Random;

/**
 *
 * @author Stefano Reksten
 */
public enum TipoDanno {

    // Impatti da armi smussate (martelli, mazze) o cadute. Efficace contro scheletri e armature pesanti.
    CONTUNDENTE(SupertipoDanno.FISICO, TipoEffettoDiStato.STORDITO, TipoEffettoDiStato.ATTERRATO), // Bludgeoning
    // Attacchi di punta (frecce, lance, morsi). Penetra i punti deboli delle corazze.
    PERFORANTE(SupertipoDanno.FISICO), // Piercing
    // Tagli netti (spade, asce, artigli). Causa ferite esposte e sanguinamenti (DoT).
    TAGLIENTE(SupertipoDanno.FISICO, TipoEffettoDiStato.SANGUINAMENTO), // Slashing

    // Spinte d'aria, lacerazioni da vuoto, tempeste e sbalzi di pressione. Spesso sposta o destabilizza i bersagli.
    ARIA(SupertipoDanno.ELEMENTALE, TipoEffettoDiStato.ATTERRATO), // Wind/Air
    // Impatti idrici, annegamento, pressione idrostatica o getti corrosivi. Spesso spegne il fuoco o spinge i nemici.
    ACQUA(SupertipoDanno.ELEMENTALE, TipoEffettoDiStato.BAGNATO), // Water
    // Impatti da rocce, terremoti o sabbia. Perfetto per logorare le difese o accecare il bersaglio.
    TERRA(SupertipoDanno.ELEMENTALE, TipoEffettoDiStato.IMMOBILIZZATO, TipoEffettoDiStato.ACCECATO), // Earth
    // Brucia i bersagli, infiamma gli oggetti e spesso infligge danni nel tempo (DoT).
    FUOCO(SupertipoDanno.ELEMENTALE, TipoEffettoDiStato.BRUCIATO), // Fire
    // Danno rapido che spesso si propaga tra bersagli vicini, disattiva reazioni o stordisce.
    FULMINE(SupertipoDanno.ELEMENTALE, TipoEffettoDiStato.STORDITO), // Lightning
    // Congela, congela i fluidi corporei, rallenta i movimenti e riduce la velocità d'attacco.
    GELO(SupertipoDanno.ELEMENTALE, TipoEffettoDiStato.RALLENTATO, TipoEffettoDiStato.CONGELATO), // Cold/Ice
    // Corrode armature, scioglie la carne e riduce permanentemente le difese della vittima.
    ACIDO(SupertipoDanno.ELEMENTALE), // Acid
    // Onde d'urto invisibili, vibrazioni e boati che spingono indietro i nemici o li assordano.
    SONICO(SupertipoDanno.ELEMENTALE, TipoEffettoDiStato.ASSORDATO), // Thunder/Sonic
    // Tossine che debilitano il bersaglio, riducendo le statistiche e consumando la salute lentamente (DoT).
    VELENO(SupertipoDanno.ELEMENTALE, TipoEffettoDiStato.AVVELENATO), // Poison

    // Energia della morte e del decadimento. Dissangua la forza vitale e impedisce la cura.
    NECROTICO(SupertipoDanno.MAGICO, TipoEffettoDiStato.INFETTATO), // Necrotic
    // Energia di luce divina e radiazioni celestiali. Devastante contro non-morti, demoni e oscurità.
    SACRO(SupertipoDanno.MAGICO, TipoEffettoDiStato.ACCECATO), // Holy/Radiant
    // Attacchi mentali diretti (illusioni, telepatia). Ignora le armature fisiche e causa confusione o shock.
    PSICHICO(SupertipoDanno.MAGICO, TipoEffettoDiStato.CONFUSO, TipoEffettoDiStato.SPAVENTATO), // Psychic
    // Energia magica pura, grezza e manipolata (es. Dardo Incantato). Il tipo di danno meno resistito.
    ARCANO(SupertipoDanno.MAGICO, TipoEffettoDiStato.SILENZIATO), // Arcane
    // L'assenza di tutto, la gravità o l'energia delle stelle. Astrazione spaziale che disintegra la materia.
    VUOTO(SupertipoDanno.MAGICO), // Void/Cosmic
    // Corruzione e influssi maligni. Può ridurre i punti vita o intaccare le statistiche massime dei personaggi.
    MALEDIZIONE(SupertipoDanno.MAGICO), // Curse
    ;

    private final SupertipoDanno superTipo;
    private final TipoEffettoDiStato[] effetti;

    TipoDanno(SupertipoDanno superTipo, TipoEffettoDiStato... effetti) {
        this.superTipo = superTipo;
        this.effetti = effetti;
    }

    public SupertipoDanno getSuperTipo() {
        return superTipo;
    }

    public TipoEffettoDiStato[] getEffetti() {
        return effetti;
    }

    public boolean hasEffettiDiStato() {
        return effetti.length > 0;
    }

    public TipoEffettoDiStato getTipoEffettoDiStatoCasuale() {
        if (effetti.length == 0) {
            return null;
        }

        return effetti[new Random().nextInt(effetti.length) - 1];
    }

}
