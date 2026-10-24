package com.threeamigos.foresta.artefatti;

import com.threeamigos.foresta.artefatti.tipi.GradoIncantamento;

/**
 * I valori di bilanciamento degli artefatti: come nascono (probabilità di essere incantati, di avere un nome proprio o di venire
 * dalla grammatica, danni delle armi, effetti degli ingredienti), da che livello una pergamena è di grado medio o maggiore, e quanto
 * valgono e costano le pergamene. Quelli degli altri oggetti del gioco (cofani, anelli...) restano in {@code Costanti}.
 *
 * @author Stefano Reksten
 */
public final class CostantiArtefatti {

    // Il prezzo di un modificatore a tempo è quello del modificatore permanente per questo coefficiente per ogni
    // turno di durata (vedi ListinoPergamene.prezzo), fino a un tetto. Una locazione vale tanti turni
    // "Porta a 0" (QUANTITA_ASSOLUTA con 0) non vale 5 × 0: costa questo, fisso (prima del moltiplicatore del tempo)
    public static final double PERGAMENA_PREZZO_PORTA_A_ZERO = 50;

    public static final int PERGAMENA_TURNI_PER_LOCAZIONE = 5;

    public static final double PERGAMENA_COEFFICIENTE_PREZZO_PER_TURNO = 0.5;

    public static final double PERGAMENA_MOLTIPLICATORE_PREZZO_TEMPO_MASSIMO = 2.0;

    // Prezzi degli ingredienti magici (vedi ListinoPergamene): un incantamento per punto di danno grezzo che aggiunge,
    // un modificatore per ogni 1% di cui aumenta il valore tipico dell'attributo
    public static final double PERGAMENA_PREZZO_PER_PUNTO_DI_DANNO = 0.5;

    // Come sopra per il libro che riprogramma un artefatto (non si compra né si vende)
    public static final int RIPROGRAMMAZIONE_VALORE_PER_LIVELLO = 200;

    // Gradi delle pergamene (vedi GradoIncantamento): fino al livello 3 minore, da 4 a 7 medio, da 8 maggiore
    public static final int GRADO_INCANTAMENTO_MEDIO_DAL_LIVELLO = 4;

    public static final double PERGAMENA_PREZZO_PER_PERCENTO_DI_ATTRIBUTO = 0.5;

    public static final int PERGAMENA_PREZZO_PER_PUNTO_ASSOLUTO = 5;

    public static final int GRADO_INCANTAMENTO_MAGGIORE_DAL_LIVELLO = 8;

    // Un ingrediente non è mai gratis: una gemma minore con la sola parte percentuale vale meno di una moneta
    public static final int PERGAMENA_PREZZO_MINIMO = 1;

    public static final double PERGAMENA_MAGGIORAZIONE_EFFETTI_DI_STATO = 0.25;

    // Generatore di artefatti (vedi GeneratoreArtefattiTabelle)
    public static final double ARTEFATTO_PROBABILITA_NOME_PROPRIO = 0.05;

    // Probabilità che un artefatto incantabile generato sia raro (i leggendari non si generano)
    public static final double ARTEFATTO_PROBABILITA_RARO = 0.1;

    // Probabilità che un artefatto incantabile generato nasca già incantato: 5% per ogni livello oltre il primo, fino al 60%
    public static final double ARTEFATTO_PROBABILITA_INCANTATO_PER_LIVELLO = 0.05;

    // Quota degli artefatti che prendono nome ed effetti dalla grammatica (artefatti2.txt), per i tipi che conosce.
    // Metà per provarla accanto alle tabelle: da rivedere quando sarà bilanciata
    public static final double ARTEFATTO_PROBABILITA_DA_GRAMMATICA = 0.5;

    public static final double ARTEFATTO_PROBABILITA_INCANTATO_MASSIMA = 0.6;

    public static final double ARTEFATTO_MOLTIPLICATORE_DUE_MANI = 1.5;

    // Grado degli effetti di un ingrediente magico: minore fino al livello 2, normale (MEDIO) fino al 4, maggiore dal 5
    public static final int INGREDIENTE_GRADO_MEDIO_DAL_LIVELLO = 3;

    // Un ingrediente magico ha tanti effetti quanto il suo livello, fino a questo
    public static final int INGREDIENTE_EFFETTI_MASSIMI = 3;

    // Danno medio di un'arma generata: 4 + 2 × livello, ma almeno 16 + livello, così ai livelli bassi un'arma
    // fa ben più delle mani nude di un PG (4 + 1,5 × √FORZA, cioè 9-10). Le due curve si incontrano al livello 12.
    public static final int ARMA_DANNI_BASE = 4;

    public static final int ARMA_DANNI_PER_LIVELLO = 2;

    public static final int ARMA_DANNI_MINIMI_BASE = 16;

    public static final int ARMA_DANNI_MINIMI_PER_LIVELLO = 1;

    public static final int INGREDIENTE_GRADO_MAGGIORE_DAL_LIVELLO = 5;

    // Incantamenti al massimo con cui un artefatto nasce; ne resta sempre almeno un posto libero per la fusione
    public static final int ARTEFATTO_MASSIMO_INCANTAMENTI_IN_NASCITA = 3;

    // Quanto vale, per livello, un oggetto che fa salire di livello un artefatto (non si compra né si vende)
    public static final int AGGIORNAMENTO_VALORE_PER_LIVELLO = 100;

	private CostantiArtefatti() {
	}
}
