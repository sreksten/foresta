package com.threeamigos.foresta.tipi;

import java.util.EnumMap;
import java.util.Map;

/**
 * Le costanti di bilanciamento dei personaggi: per ogni classe di personaggio i caratteristiche, i valori di partenza e i moltiplicatori, in una
 * tabella con una riga per classe (vedi {@link #valoriDi(TipoPersonaggio)} e {@link ValoriDiClasse}), e quelli del combattimento,
 * dell'equipaggiamento, del riposo e dei gruppi. Si possono cambiare qui per variare la difficoltà senza toccare le regole a giro per il codice.
 * <p>
 * Le note testuali che spiegano ogni moltiplicatore (perché il goblin ha quel valore di critico) stanno nel file
 * {@code note_personaggi_it.properties} e si leggono con {@link NoteDeiPersonaggi}. Le costanti che non sono dei personaggi (gli altri
 * sottoprogetti ne hanno di proprie) stanno nel modulo del loro sottoprogetto.
 *
 * @author Stefano Reksten
 */
public final class CostantiPersonaggi {

    private CostantiPersonaggi() {
    }

    // Amicizia
    // Il tentativo di amicizia riesce se il carisma di chi lo fa è maggiore del tiro di un dado con queste facce; nel tiro
    // il carisma conta al massimo quanto le facce, così con un tiro di 12 il tentativo fallisce sempre (vedi LocazioneBase).
    // Il dado e la soglia della corruzione stanno nel motore (CostantiMotore).
    public static final int FACCE_DEL_DADO_DELL_AMICIZIA = 12;

    // Combattimento (vedi artefatti_e_incantamenti.md, §2 "Combattimento")
    // PARATA intrinseca di scudo, elmo, maschera, armatura e schinieri: una parte fissa, perché servano anche ai
    // livelli bassi, più una per ogni livello del pezzo. La veste dà lo stesso minimo in RESISTENZA_MAGICA.
    public static final int SCUDO_PARATA_MINIMA = 6;
    public static final int SCUDO_PARATA_PER_LIVELLO = 2;
    public static final int ELMO_PARATA_MINIMA = 2;
    public static final int ELMO_PARATA_PER_LIVELLO = 1;
    public static final int MASCHERA_PARATA_MINIMA = 1;
    public static final int MASCHERA_PARATA_PER_LIVELLO = 1;
    public static final int ARMATURA_PARATA_MINIMA = 3;
    public static final int ARMATURA_PARATA_PER_LIVELLO = 1;
    public static final int SCHINIERI_PARATA_MINIMA = 2;
    public static final int SCHINIERI_PARATA_PER_LIVELLO = 1;
    // FORZA minima per indossare un'armatura, per chi la può portare (il Ladro, il Mago, il Sacerdote e il Druido no: vedi
    // RegoleEquipaggiamento): il Guerriero ce l'ha sempre (17 a livello 1), Elfo e Bardo solo se forzuti ai livelli
    // alti (11-14 a livello 1, fino a 19 a livello 10)
    public static final int ARMATURA_FORZA_MINIMA = 16;
    // RESISTENZA_MAGICA intrinseca dello scudo per ogni suo livello: di più se è raro (o leggendario)
    public static final int SCUDO_RESISTENZA_MAGICA_PER_LIVELLO = 1;
    public static final int SCUDO_RARO_RESISTENZA_MAGICA_PER_LIVELLO = 2;
    // "Guardia aperta": chi impugna due armi o un'arma a due mani perde un quarto della PARATA
    public static final double GUARDIA_APERTA_FATTORE_PARATA = 0.75;
    // La seconda fase di attacco, con l'arma nella mano secondaria, fa il 40% (danno base e incantamenti)
    public static final double DOPPIA_ARMA_FATTORE_SECONDA_ARMA = 0.4;
    // Su elmo, scudo, armatura e schinieri la parte percentuale di un incantamento vale la metà, perché moltiplica
    // una difesa che cresce già con il livello
    public static final double RESISTENZA_FATTORE_PERCENTUALE = 0.5;
    // Il libro magico dà al danno degli incantesimi un bonus come un incantamento del suo grado, più il 25%
    public static final double LIBRO_MAGICO_MAGGIORAZIONE = 0.25;
    // L'elfo sa usare il libro magico, ma non bene come il mago: del bonus agli incantesimi gliene resta questa quota
    public static final double LIBRO_MAGICO_FATTORE_ELFO = 0.5;

    // Un'arma fino a tanti livelli sotto quello di chi la impugna rende ancora tutta la parte dell'eroe; oltre, la parte
    // dell'eroe cala in proporzione (vedi CalcolatoreCombattimento.calcolaDannoRisultante)
    public static final int TOLLERANZA_LIVELLO_ARMA = 2;

    // Un'arma e un'armatura di livello crescente fanno rendere al gruppo molto più di quanto cresca la salute (che segue la radice del
    // livello): i mostri comuni (TipoPersonaggio.isMostroComune) hanno quindi per ogni livello oltre il primo questa quota di salute in più,
    // perché chi non rinnova l'equipaggiamento se ne accorga (vedi PersonaggioBase.calcolaSaluteMassima)
    public static final double MOSTRI_COMUNI_CRESCITA_SALUTE_PER_LIVELLO = 0.09;

    // Il raffreddore (RAFFREDDATO, messo dall'ARIA su un BAGNATO): quanti turni dura, sempre, e la probabilità (percentuale) che a ogni turno chi lo ha starnutisca
    // invece di agire, perdendo il turno
    public static final int RAFFREDDATO_TURNI = 3;
    public static final int RAFFREDDATO_PROBABILITA_STARNUTO = 50;

    // La fuga (vedi SaluteEMorte)
    // Fuggire costa a ognuno una quota della sua salute massima, tirata fra queste due percentuali: la fuga non uccide, lascia almeno un punto
    // (a fine locazione ognuno recupera dal 16 al 35% della salute massima, secondo la classe: per questo la fuga deve costare di più)
    public static final int FUGA_DANNO_MINIMO_PERCENTUALE = 30;
    public static final int FUGA_DANNO_MASSIMO_PERCENTUALE = 50;
    // Il gruppo che fugge perde una percentuale, tirata fra queste due, di ognuna delle sue scorte: un tiro per le monete e i preziosi
    // e uno per gli incantesimi, le pergamene e le pozioni (non gli artefatti)
    public static final int FUGA_PERDITA_MINIMA_PERCENTUALE = 1;
    public static final int FUGA_PERDITA_MASSIMA_PERCENTUALE = 20;
    // L'ignominia della fuga: coraggio, valore e carisma calano di questa percentuale per questo numero di locazioni dopo quella
    // della fuga. La nota del modificatore è una chiave: il testo da mostrare, in ogni lingua, sta in note_modificatori_<lingua>.properties
    // (vedi TestoDeiModificatori.nota)
    public static final int FUGA_IGNOMINIA_PERCENTUALE = 20;
    public static final int FUGA_IGNOMINIA_LOCAZIONI = 3;
    public static final String FUGA_IGNOMINIA_NOTA = "Ignominia della fuga";

    // A fine locazione ogni personaggio vivo recupera getRigenerazioneSalute(): la parte fissa più questa quota della salute
    // massima, aumentati dalla COSTITUZIONE (circa il 25% della salute massima a inizio partita)
    // Allo stesso modo ognuno recupera getRigenerazioneMagia(): la parte fissa più questa quota della magia massima, per il
    // moltiplicatore di recupero magico della classe (a inizio partita circa 12 al Mago, 9 al Bardo, 3 al Guerriero)
    public static final double RIGENERAZIONE_PARTE_FISSA = 5.0;
    public static final double RIGENERAZIONE_SALUTE_QUOTA_SALUTE_MASSIMA = 0.15;
    public static final double RIGENERAZIONE_MAGIA_QUOTA_MAGIA_MASSIMA = 0.05;

    // Il riposo (vedi CalcolatoreRiposo): per ogni ora pesata (la radice delle ore per il moltiplicatore di TipoRiposo) si recuperano
    // una parte fissa più una quota del massimo di salute e di magia, e si toglie un po' di stanchezza
    public static final double RIPOSO_SALUTE_PARTE_FISSA = 5.0;
    public static final double RIPOSO_SALUTE_QUOTA_SALUTE_MASSIMA = 0.05;
    public static final double RIPOSO_MAGIA_PARTE_FISSA = 4.0;
    public static final double RIPOSO_MAGIA_QUOTA_MAGIA_MASSIMA = 0.05;
    public static final double RIPOSO_RIDUZIONE_STANCHEZZA = 2.0;

    /**
     * Il livello massimo di un personaggio e del mondo: oltre, i punti esperienza non fanno più salire di livello (vedi GestoreProgressione)
     */
    public static final int MASSIMO_LIVELLO = 50;

    // Le pozioni: quanta salute o magia ridanno, e di quanto le grandi alzano anche il massimo (vedi Gruppo).
    // Con cosa comincia una partita sta altrove: le monete e i preziosi in CostantiEconomia, incantesimi e pozioni in CostantiMotore
    // (li dà l'eremita); le pergamene e le pozioni che portano gli avversari in CostantiBottino.
    public static final int RECUPERO_DA_POZIONE_SALUTE = 100;
    public static final int RECUPERO_DA_POZIONE_SALUTE_GRANDE = 150;
    public static final int AUMENTO_SALUTE_DA_POZIONE_SALUTE_GRANDE = 10;
    public static final int RECUPERO_DA_POZIONE_MAGIA = 10;
    public static final int RECUPERO_DA_POZIONE_MAGIA_GRANDE = 20;
    public static final int AUMENTO_MAGIA_DA_POZIONE_MAGIA_GRANDE = 10;

    // Quante volte, al massimo, un personaggio cambia arma in un combattimento (vedi CambioDArma e
    // tattiche_di_combattimento_automatiche.md §5)
    public static final int MAX_CAMBI_ARMA_PER_COMBATTIMENTO = 2;

    // Gruppo
    // Oltre ai 5 permanenti, fino a 3 slot aggiuntivi per personaggi temporanei
    // (AiutoGratuito, AiutoMercenario, lo Scheletro da MIETITURA)
    public static final int MAX_PERSONAGGI_GRUPPO_GIOCATORE = 5;
    public static final int MAX_PERSONAGGI_GRUPPO_TOTALE = 8;
    // Per quanti turni uno Scheletro nato dalla MIETITURA accompagna il gruppo
    public static final int MIETITURA_DURATA_TURNI = 4;

    // Combattimento
    // La quota del danno grezzo di un colpo che una reazione ad area (esplosione di gas, rigetto, elettrocuzione) porta ai
    // vicini del bersaglio, prima della loro difesa
    public static final double DANNO_AD_AREA_QUOTA = 0.5d;

    // Personaggio
    // La stanchezza più alta che un personaggio può avere
    public static final int MAX_STANCHEZZA = 9;

    // Il berserk: la parte della salute massima sotto la quale scatta, e di quanto cresce il danno fisico per ogni parte di salute persa.
    // Il vichingo scatta prima (a metà) e con il bonus doppio.
    public static final double BERSERK_SOGLIA = 1.0d / 3.0d;
    public static final double BERSERK_BONUS = 0.5d;
    public static final double VICHINGO_BERSERK_SOGLIA = 1.0d / 2.0d;
    public static final double VICHINGO_BERSERK_BONUS = 2 * BERSERK_BONUS;

    // La tabella dei valori di ogni classe (vedi ValoriDiClasse): le caratteristiche (femmina, corrompibile, amichevole, boss), salute e
    // magia di base con il livellamento della magia, l'esperienza che dà, quanti se ne incontrano al massimo (quanti), l'intelligenza
    // massima che regola l'astuzia delle tattiche, e i moltiplicatori: 1 è la media, di più un punto di forza, di meno un punto debole;
    // il perché di ognuno sta in note_personaggi_<lingua>.properties (vedi NoteDeiPersonaggi), e il bestiario li mostra. Chi non dà la
    // contrattazione non tratta con i mercanti. Ogni TipoPersonaggio deve avere la sua riga: lo controlla ValoriDiClasseTest.
    private static final Map<TipoPersonaggio, ValoriDiClasse> VALORI_DI_CLASSE = new EnumMap<>(TipoPersonaggio.class);

    static {
        metti(TipoPersonaggio.BARDO, ValoriDiClasse.nuovi().corrompibile().amichevole()
                .salute(105).magia(40, 18).esperienza(25).quanti(1).intelligenza(35)
                .carico(0.9).critico(1.1).precisione(1.0).velocita(1.1).furtivita(1.1).parata(1.0)
                .resistenzaMagica(1.1).percezione(1.1).soggezione(1.2).furia(1.1).coraggio(1.1).valore(1.1)
                .contrattazione(1.2).numeroBersagli(1.1).stanchezza(1.0).danniMagici(1.1).danniFisici(0.9)
                .recuperoFisico(1.1).recuperoMagico(1.2));
        // Il bardo delle locande ha i valori del bardo
        comeIl(TipoPersonaggio.BARDO_DELLE_LOCANDE, TipoPersonaggio.BARDO);
        metti(TipoPersonaggio.CANTASTORIE, ValoriDiClasse.nuovi().femmina().corrompibile().amichevole()
                .salute(105).magia(40, 18).esperienza(25).quanti(1).intelligenza(35)
                .carico(0.85).critico(1.15).precisione(1.0).velocita(1.1).furtivita(1.1).parata(1.0)
                .resistenzaMagica(1.1).percezione(1.1).soggezione(1.2).furia(1.1).coraggio(1.1).valore(1.1)
                .contrattazione(1.2).numeroBersagli(1.1).stanchezza(1.0).danniMagici(1.1).danniFisici(0.9)
                .recuperoFisico(1.1).recuperoMagico(1.2));
        metti(TipoPersonaggio.ELFA, ValoriDiClasse.nuovi().femmina().corrompibile().amichevole()
                .salute(105).magia(45, 22).esperienza(25).quanti(1).intelligenza(45)
                .carico(0.85).critico(1.2).precisione(1.3).velocita(1.2).furtivita(1.3).parata(1.1)
                .resistenzaMagica(1.4).percezione(1.4).soggezione(1.1).furia(1.2).coraggio(1.1).valore(1.2)
                .contrattazione(1.0).numeroBersagli(1.0).stanchezza(0.8).danniMagici(1.4).danniFisici(0.9)
                .recuperoFisico(1.1).recuperoMagico(1.3));
        metti(TipoPersonaggio.ELFO, ValoriDiClasse.nuovi().corrompibile().amichevole()
                .salute(105).magia(45, 22).esperienza(25).quanti(1).intelligenza(45)
                .carico(0.9).critico(1.2).precisione(1.3).velocita(1.2).furtivita(1.3).parata(1.1)
                .resistenzaMagica(1.4).percezione(1.4).soggezione(1.1).furia(1.2).coraggio(1.1).valore(1.2)
                .contrattazione(1.0).numeroBersagli(1.0).stanchezza(0.8).danniMagici(1.4).danniFisici(0.9)
                .recuperoFisico(1.1).recuperoMagico(1.3));
        metti(TipoPersonaggio.GUERRIERA, ValoriDiClasse.nuovi().femmina().corrompibile().amichevole()
                .salute(150).magia(20, 6).esperienza(25).quanti(1).intelligenza(15)
                .carico(1.2).critico(1.0).precisione(1.1).velocita(1.0).furtivita(1.0).parata(1.5)
                .resistenzaMagica(1.0).percezione(1.0).soggezione(1.0).furia(1.5).coraggio(1.3).valore(1.5)
                .contrattazione(0.9).numeroBersagli(1.1).stanchezza(1.0).danniMagici(0.5).danniFisici(1.3)
                .recuperoFisico(1.4).recuperoMagico(0.5));
        metti(TipoPersonaggio.GUERRIERO, ValoriDiClasse.nuovi().corrompibile().amichevole()
                .salute(150).magia(20, 6).esperienza(25).quanti(1).intelligenza(15)
                .carico(1.25).critico(1.0).precisione(1.1).velocita(1.0).furtivita(1.0).parata(1.5)
                .resistenzaMagica(1.0).percezione(1.0).soggezione(1.0).furia(1.5).coraggio(1.3).valore(1.5)
                .contrattazione(0.9).numeroBersagli(1.1).stanchezza(1.0).danniMagici(0.5).danniFisici(1.3)
                .recuperoFisico(1.4).recuperoMagico(0.5));
        // Il capitano delle guardie e il vichingo hanno i valori del guerriero
        comeIl(TipoPersonaggio.CAPITANO_DELLE_GUARDIE, TipoPersonaggio.GUERRIERO);
        comeIl(TipoPersonaggio.VICHINGO, TipoPersonaggio.GUERRIERO);
        metti(TipoPersonaggio.LADRA, ValoriDiClasse.nuovi().femmina().corrompibile().amichevole()
                .salute(100).magia(25, 8).esperienza(25).quanti(1).intelligenza(30)
                .carico(0.95).critico(1.5).precisione(1.3).velocita(1.4).furtivita(1.5).parata(1.0)
                .resistenzaMagica(1.3).percezione(1.3).soggezione(0.9).furia(1.0).coraggio(1.0).valore(1.0)
                .contrattazione(1.3).numeroBersagli(0.9).stanchezza(0.9).danniMagici(1.0).danniFisici(1.0)
                .recuperoFisico(1.1).recuperoMagico(0.9));
        metti(TipoPersonaggio.LADRO, ValoriDiClasse.nuovi().corrompibile().amichevole()
                .salute(100).magia(25, 8).esperienza(25).quanti(1).intelligenza(30)
                .carico(1.0).critico(1.5).precisione(1.3).velocita(1.4).furtivita(1.5).parata(1.0)
                .resistenzaMagica(1.3).percezione(1.3).soggezione(0.9).furia(1.0).coraggio(1.0).valore(1.0)
                .contrattazione(1.3).numeroBersagli(0.9).stanchezza(0.9).danniMagici(1.0).danniFisici(1.0)
                .recuperoFisico(1.1).recuperoMagico(0.9));
        metti(TipoPersonaggio.MAGA, ValoriDiClasse.nuovi().femmina().corrompibile().amichevole()
                .salute(80).magia(60, 34).esperienza(25).quanti(1).intelligenza(55)
                .carico(0.75).critico(0.8).precisione(1.0).velocita(0.9).furtivita(0.9).parata(0.6)
                .resistenzaMagica(1.1).percezione(1.1).soggezione(1.0).furia(1.0).coraggio(1.0).valore(1.0)
                .contrattazione(1.0).numeroBersagli(1.3).stanchezza(1.4).danniMagici(2.0).danniFisici(0.5)
                .recuperoFisico(0.7).recuperoMagico(1.5));
        metti(TipoPersonaggio.MAGO, ValoriDiClasse.nuovi().corrompibile().amichevole()
                .salute(80).magia(60, 34).esperienza(25).quanti(1).intelligenza(55)
                .carico(0.8).critico(0.8).precisione(1.0).velocita(0.9).furtivita(0.9).parata(0.6)
                .resistenzaMagica(1.1).percezione(1.1).soggezione(1.0).furia(1.0).coraggio(1.0).valore(1.0)
                .contrattazione(1.0).numeroBersagli(1.3).stanchezza(1.4).danniMagici(2.0).danniFisici(0.5)
                .recuperoFisico(0.7).recuperoMagico(1.5));
        // Sacerdote e Sacerdotessa: la magia dell'elfo con più Saggezza e più robustezza, senza doppia arma e senza dardo arcano
        metti(TipoPersonaggio.SACERDOTE, ValoriDiClasse.nuovi().corrompibile().amichevole()
                .salute(100).magia(45, 22).esperienza(25).quanti(1).intelligenza(40)
                .carico(0.85).critico(1.0).precisione(1.1).velocita(1.0).furtivita(0.9).parata(0.8)
                .resistenzaMagica(1.3).percezione(1.2).soggezione(1.1).furia(1.0).coraggio(1.2).valore(1.1)
                .contrattazione(1.0).numeroBersagli(1.0).stanchezza(1.0).danniMagici(1.4).danniFisici(0.8)
                .recuperoFisico(0.9).recuperoMagico(1.3));
        metti(TipoPersonaggio.SACERDOTESSA, ValoriDiClasse.nuovi().femmina().corrompibile().amichevole()
                .salute(100).magia(45, 22).esperienza(25).quanti(1).intelligenza(40)
                .carico(0.8).critico(1.0).precisione(1.1).velocita(1.0).furtivita(0.9).parata(0.8)
                .resistenzaMagica(1.3).percezione(1.2).soggezione(1.1).furia(1.0).coraggio(1.2).valore(1.1)
                .contrattazione(1.0).numeroBersagli(1.0).stanchezza(1.0).danniMagici(1.4).danniFisici(0.8)
                .recuperoFisico(0.9).recuperoMagico(1.3));
        // Chierico e Chierica: il sacerdote che combatte in prima linea (mazza, scudo, armatura): fra il Sacerdote e il Guerriero,
        // con più salute e parata del primo e più magia del secondo
        metti(TipoPersonaggio.CHIERICO, ValoriDiClasse.nuovi().corrompibile().amichevole()
                .salute(130).magia(30, 14).esperienza(25).quanti(1).intelligenza(25)
                .carico(1.1).critico(1.0).precisione(1.0).velocita(0.9).furtivita(0.8).parata(1.3)
                .resistenzaMagica(1.2).percezione(1.0).soggezione(1.1).furia(1.0).coraggio(1.2).valore(1.2)
                .contrattazione(1.0).numeroBersagli(1.0).stanchezza(1.0).danniMagici(1.0).danniFisici(1.0)
                .recuperoFisico(1.2).recuperoMagico(1.0));
        metti(TipoPersonaggio.CHIERICA, ValoriDiClasse.nuovi().femmina().corrompibile().amichevole()
                .salute(130).magia(30, 14).esperienza(25).quanti(1).intelligenza(25)
                .carico(1.1).critico(1.0).precisione(1.0).velocita(0.9).furtivita(0.8).parata(1.3)
                .resistenzaMagica(1.2).percezione(1.0).soggezione(1.1).furia(1.0).coraggio(1.2).valore(1.2)
                .contrattazione(1.0).numeroBersagli(1.0).stanchezza(1.0).danniMagici(1.0).danniFisici(1.0)
                .recuperoFisico(1.2).recuperoMagico(1.0));
        // Druido e Druida: l'incantatore della natura, con più Percezione, Saggezza e recupero magico del Mago ma meno magia
        // base, e senza armatura né scudo
        metti(TipoPersonaggio.DRUIDO, ValoriDiClasse.nuovi().corrompibile().amichevole()
                .salute(110).magia(40, 20).esperienza(25).quanti(1).intelligenza(40)
                .carico(0.85).critico(1.0).precisione(1.1).velocita(1.1).furtivita(1.1).parata(0.9)
                .resistenzaMagica(1.2).percezione(1.3).soggezione(1.1).furia(1.0).coraggio(1.1).valore(1.0)
                .contrattazione(1.0).numeroBersagli(1.0).stanchezza(0.9).danniMagici(1.2).danniFisici(0.9)
                .recuperoFisico(1.1).recuperoMagico(1.2));
        metti(TipoPersonaggio.DRUIDA, ValoriDiClasse.nuovi().femmina().corrompibile().amichevole()
                .salute(110).magia(40, 20).esperienza(25).quanti(1).intelligenza(40)
                .carico(0.85).critico(1.0).precisione(1.1).velocita(1.1).furtivita(1.1).parata(0.9)
                .resistenzaMagica(1.2).percezione(1.3).soggezione(1.1).furia(1.0).coraggio(1.1).valore(1.0)
                .contrattazione(1.0).numeroBersagli(1.0).stanchezza(0.9).danniMagici(1.2).danniFisici(0.9)
                .recuperoFisico(1.1).recuperoMagico(1.2));
        // I personaggi non combattenti (locandiere, armaiolo, alchimista, venditore di pergamene, incantatore, moglie del
        // bardo, borgomastro, uomo e donna qualunque): tutti con la stessa riga (vedi nonCombattente), al maschile o al femminile
        metti(TipoPersonaggio.LOCANDIERE, nonCombattente());
        metti(TipoPersonaggio.ARMAIOLO, nonCombattente());
        metti(TipoPersonaggio.ALCHIMISTA, nonCombattente());
        metti(TipoPersonaggio.VENDITORE_DI_PERGAMENE, nonCombattente());
        metti(TipoPersonaggio.INCANTATORE, nonCombattente());
        metti(TipoPersonaggio.MOGLIE_DEL_BARDO, nonCombattente().femmina());
        metti(TipoPersonaggio.UOMO, nonCombattente());
        metti(TipoPersonaggio.BORGOMASTRO, nonCombattente());
        metti(TipoPersonaggio.DONNA, nonCombattente().femmina());
        // Il Viandante: un ospite di missione, con valori simili a quelli del bardo
        metti(TipoPersonaggio.VIANDANTE, ValoriDiClasse.nuovi().corrompibile().amichevole()
                .salute(105).magia(40, 18).esperienza(25).quanti(1).intelligenza(35)
                .carico(0.9).critico(1.1).precisione(1.0).velocita(1.1).furtivita(1.1).parata(1.0)
                .resistenzaMagica(1.1).percezione(1.1).soggezione(1.2).furia(1.1).coraggio(1.1).valore(1.1)
                .contrattazione(1.2).numeroBersagli(1.1).stanchezza(1.0).danniMagici(1.1).danniFisici(0.9)
                .recuperoFisico(1.1).recuperoMagico(1.2));
        metti(TipoPersonaggio.OMBRAFIAMMA, ValoriDiClasse.nuovi().femmina().corrompibile().amichevole().boss()
                .salute(140).magia(50, 20).esperienza(100).quanti(1).intelligenza(50)
                .carico(90).critico(1.5).precisione(1.5).velocita(1.5).furtivita(1.5).parata(1.5)
                .resistenzaMagica(1.5).percezione(1.5).soggezione(1.5).furia(1.5).coraggio(1.5).valore(1.5)
                .contrattazione(1.0).numeroBersagli(3).stanchezza(0.1).danniMagici(2.0).danniFisici(2.0)
                .recuperoFisico(1.8).recuperoMagico(1.5));
        metti(TipoPersonaggio.ARPIA, ValoriDiClasse.nuovi().femmina()
                .salute(95).magia(35, 12).esperienza(15).quanti(4).intelligenza(8)
                .carico(0.7).critico(1.3).precisione(1.1).velocita(1.3).furtivita(0.7).parata(0.7)
                .resistenzaMagica(1.4).percezione(1.4).soggezione(0.9).furia(0.5).coraggio(0.7).valore(0.5)
                .numeroBersagli(0.8).stanchezza(1.1).danniMagici(0.8).danniFisici(0.9).recuperoFisico(1.0)
                .recuperoMagico(0.8));
        metti(TipoPersonaggio.CENTAURO, ValoriDiClasse.nuovi().corrompibile().amichevole()
                .salute(125).magia(15, 4).esperienza(40).quanti(4).intelligenza(12)
                .carico(2.0).critico(0.8).precisione(1.1).velocita(1.4).furtivita(0.5).parata(1.1)
                .resistenzaMagica(1.1).percezione(1.1).soggezione(1.1).furia(1.3).coraggio(1.1).valore(1.3)
                .contrattazione(0.9).numeroBersagli(1.2).stanchezza(0.8).danniMagici(0.8).danniFisici(1.3)
                .recuperoFisico(1.3).recuperoMagico(0.7));
        metti(TipoPersonaggio.CHIMERA, ValoriDiClasse.nuovi().femmina()
                .salute(120).magia(30, 8).esperienza(40).quanti(5).intelligenza(6)
                .carico(2.5).critico(1.1).precisione(1.0).velocita(1.2).furtivita(1.1).parata(0.9)
                .resistenzaMagica(1.3).percezione(1.3).soggezione(1.2).furia(0.6).coraggio(1.0).valore(0.6)
                .numeroBersagli(1.4).stanchezza(0.9).danniMagici(0.9).danniFisici(1.3).recuperoFisico(1.2)
                .recuperoMagico(0.8));
        metti(TipoPersonaggio.CHIMERA_DRAGO, ValoriDiClasse.nuovi().femmina()
                .salute(300).magia(60, 16).esperienza(70).quanti(2).intelligenza(28)
                .carico(6.0).critico(0.8).precisione(0.9).velocita(0.9).furtivita(0.6).parata(1.2)
                .resistenzaMagica(1.4).percezione(1.4).soggezione(1.4).furia(0.7).coraggio(1.2).valore(0.7)
                .numeroBersagli(1.6).stanchezza(1.1).danniMagici(1.2).danniFisici(2.0).recuperoFisico(1.3)
                .recuperoMagico(1.2));
        metti(TipoPersonaggio.EREMITA, ValoriDiClasse.nuovi().corrompibile().amichevole()
                .salute(120).magia(20, 10).esperienza(15).quanti(1).intelligenza(25)
                .carico(0.8).critico(0.8).precisione(0.9).velocita(0.8).furtivita(1.1).parata(0.8)
                .resistenzaMagica(1.2).percezione(1.2).soggezione(0.8).furia(1.0).coraggio(1.0).valore(1.0)
                .contrattazione(0.8).numeroBersagli(0.9).stanchezza(1.2).danniMagici(1.3).danniFisici(0.7)
                .recuperoFisico(0.9).recuperoMagico(1.4));
        metti(TipoPersonaggio.FANTASMA, ValoriDiClasse.nuovi()
                .salute(100).magia(30, 15).esperienza(15).quanti(3).intelligenza(15)
                .carico(0.0).critico(0.5).precisione(0.8).velocita(1.1).furtivita(1.3).parata(0.2)
                .resistenzaMagica(1.0).percezione(1.0).soggezione(1.1).furia(0.4).coraggio(1.2).valore(0.4)
                .numeroBersagli(0.9).stanchezza(0.0).danniMagici(1.2).danniFisici(0.2).recuperoFisico(0.0)
                .recuperoMagico(1.2));
        metti(TipoPersonaggio.FOLLETTO, ValoriDiClasse.nuovi().corrompibile().amichevole()
                .salute(45).magia(65, 28).esperienza(15).quanti(5).intelligenza(8)
                .carico(0.2).critico(1.2).precisione(1.1).velocita(1.5).furtivita(1.4).parata(0.3)
                .resistenzaMagica(1.1).percezione(1.1).soggezione(0.4).furia(0.5).coraggio(0.6).valore(0.5)
                .numeroBersagli(0.6).stanchezza(1.3).danniMagici(1.2).danniFisici(0.3).recuperoFisico(0.8)
                .recuperoMagico(1.3));
        metti(TipoPersonaggio.GARGOYLE, ValoriDiClasse.nuovi()
                .salute(135).magia(25, 6).esperienza(40).quanti(2).intelligenza(18)
                .carico(1.4).critico(0.8).precisione(0.9).velocita(0.7).furtivita(0.4).parata(1.4)
                .resistenzaMagica(0.9).percezione(0.9).soggezione(1.1).furia(1.2).coraggio(1.3).valore(1.2)
                .numeroBersagli(1.1).stanchezza(0.5).danniMagici(0.7).danniFisici(1.2).recuperoFisico(1.4)
                .recuperoMagico(1.0));
        metti(TipoPersonaggio.GIGANTE, ValoriDiClasse.nuovi().corrompibile().amichevole()
                .salute(250).magia(0, 0).esperienza(70).quanti(3).intelligenza(15)
                .carico(4.5).critico(0.6).precisione(0.7).velocita(0.6).furtivita(0.2).parata(1.4)
                .resistenzaMagica(0.8).percezione(0.8).soggezione(1.4).furia(1.1).coraggio(1.2).valore(1.1)
                .contrattazione(0.7).numeroBersagli(1.6).stanchezza(1.3).danniMagici(0.6).danniFisici(1.6)
                .recuperoFisico(1.3).recuperoMagico(0.5));
        metti(TipoPersonaggio.GOBLIN, ValoriDiClasse.nuovi().corrompibile()
                .salute(90).magia(0, 0).esperienza(15).quanti(5).intelligenza(4)
                .carico(0.6).critico(1.2).precisione(1.0).velocita(1.2).furtivita(1.3).parata(0.8)
                .resistenzaMagica(1.1).percezione(1.1).soggezione(0.5).furia(0.3).coraggio(0.4).valore(0.3)
                .contrattazione(1.2).numeroBersagli(0.8).stanchezza(1.1).danniMagici(0.8).danniFisici(1.4)
                .recuperoFisico(0.9).recuperoMagico(0.9));
        metti(TipoPersonaggio.HOBGOBLIN, ValoriDiClasse.nuovi().corrompibile()
                .salute(130).magia(0, 0).esperienza(15).quanti(3).intelligenza(12)
                .carico(1.1).critico(1.1).precisione(1.2).velocita(0.9).furtivita(0.9).parata(1.4)
                .resistenzaMagica(1.1).percezione(1.1).soggezione(1.1).furia(1.3).coraggio(1.3).valore(1.3)
                .contrattazione(1.1).numeroBersagli(1.1).stanchezza(1.0).danniMagici(0.7).danniFisici(1.8)
                .recuperoFisico(1.2).recuperoMagico(0.7));
        metti(TipoPersonaggio.MINOTAURO, ValoriDiClasse.nuovi().corrompibile()
                .salute(180).magia(0, 0).esperienza(40).quanti(3).intelligenza(10)
                .carico(2.2).critico(0.7).precisione(0.9).velocita(0.8).furtivita(0.5).parata(1.2)
                .resistenzaMagica(0.8).percezione(0.8).soggezione(1.3).furia(0.6).coraggio(1.2).valore(0.6)
                .contrattazione(0.6).numeroBersagli(1.3).stanchezza(1.2).danniMagici(0.5).danniFisici(2.0)
                .recuperoFisico(1.3).recuperoMagico(0.5));
        metti(TipoPersonaggio.OMBRA_NERA, ValoriDiClasse.nuovi().femmina()
                .salute(220).magia(40, 20).esperienza(40).quanti(2).intelligenza(31)
                .carico(0.0).critico(1.4).precisione(1.2).velocita(1.3).furtivita(1.5).parata(1.4)
                .resistenzaMagica(1.2).percezione(1.2).soggezione(1.4).furia(0.3).coraggio(1.4).valore(0.3)
                .numeroBersagli(1.1).stanchezza(0.0).danniMagici(1.4).danniFisici(1.0).recuperoFisico(0.0)
                .recuperoMagico(1.8));
        metti(TipoPersonaggio.SCHELETRO, ValoriDiClasse.nuovi()
                .salute(100).magia(0, 0).esperienza(15).quanti(4).intelligenza(6)
                .carico(1.1).critico(1.1).precisione(1.0).velocita(1.0).furtivita(0.9).parata(1.1)
                .resistenzaMagica(0.6).percezione(0.6).soggezione(0.8).furia(0.0).coraggio(1.5).valore(0.0)
                .numeroBersagli(0.9).stanchezza(0.0).danniMagici(0.5).danniFisici(1.4).recuperoFisico(0.0)
                .recuperoMagico(0.6));
        metti(TipoPersonaggio.SPETTRO, ValoriDiClasse.nuovi()
                .salute(130).magia(50, 25).esperienza(15).quanti(2).intelligenza(18)
                .carico(0.0).critico(0.5).precisione(0.8).velocita(1.2).furtivita(1.4).parata(0.3)
                .resistenzaMagica(1.1).percezione(1.1).soggezione(1.3).furia(0.2).coraggio(1.3).valore(0.2)
                .numeroBersagli(1.1).stanchezza(0.0).danniMagici(1.3).danniFisici(0.3).recuperoFisico(0.0)
                .recuperoMagico(1.4));
        metti(TipoPersonaggio.SPIRITO, ValoriDiClasse.nuovi().amichevole()
                .salute(110).magia(45, 20).esperienza(15).quanti(2).intelligenza(15)
                .carico(0.0).critico(0.5).precisione(0.8).velocita(1.1).furtivita(1.2).parata(0.3)
                .resistenzaMagica(1.2).percezione(1.2).soggezione(1.0).furia(0.9).coraggio(1.1).valore(0.9)
                .numeroBersagli(1.0).stanchezza(0.0).danniMagici(1.3).danniFisici(0.2).recuperoFisico(0.0)
                .recuperoMagico(1.4));
        metti(TipoPersonaggio.TITANO, ValoriDiClasse.nuovi().corrompibile().amichevole()
                .salute(300).magia(0, 0).esperienza(100).quanti(3).intelligenza(22)
                .carico(10.0).critico(0.4).precisione(0.5).velocita(0.4).furtivita(0.0).parata(1.6)
                .resistenzaMagica(1.0).percezione(1.0).soggezione(1.6).furia(1.4).coraggio(1.7).valore(1.4)
                .contrattazione(0.8).numeroBersagli(2.0).stanchezza(0.5).danniMagici(1.5).danniFisici(2.0)
                .recuperoFisico(1.5).recuperoMagico(1.4));
        metti(TipoPersonaggio.TROLL, ValoriDiClasse.nuovi().corrompibile()
                .salute(180).magia(0, 0).esperienza(40).quanti(3).intelligenza(10)
                .carico(2.8).critico(0.7).precisione(0.9).velocita(0.7).furtivita(0.5).parata(1.2)
                .resistenzaMagica(0.7).percezione(0.7).soggezione(1.1).furia(0.7).coraggio(0.9).valore(0.7)
                .numeroBersagli(1.2).stanchezza(0.8).danniMagici(0.5).danniFisici(2.0).recuperoFisico(1.5)
                .recuperoMagico(0.5));
        metti(TipoPersonaggio.VIVERNA, ValoriDiClasse.nuovi().femmina()
                .salute(170).magia(30, 10).esperienza(40).quanti(3).intelligenza(30)
                .carico(3.5).critico(1.2).precisione(1.1).velocita(1.2).furtivita(0.8).parata(0.9)
                .resistenzaMagica(1.3).percezione(1.3).soggezione(1.2).furia(0.8).coraggio(1.0).valore(0.8)
                .numeroBersagli(1.2).stanchezza(1.0).danniMagici(0.8).danniFisici(1.7).recuperoFisico(1.1)
                .recuperoMagico(0.8));
        metti(TipoPersonaggio.IDRA, ValoriDiClasse.nuovi().femmina().boss()
                .salute(850).magia(10, 4).esperienza(70).quanti(1).intelligenza(25)
                .carico(5.0).critico(0.6).precisione(1.2).velocita(0.8).furtivita(0.4).parata(1.0)
                .resistenzaMagica(1.3).percezione(1.3).soggezione(1.3).furia(0.5).coraggio(1.1).valore(0.5)
                .numeroBersagli(1.9).stanchezza(0.9).danniMagici(0.9).danniFisici(2.8).recuperoFisico(1.5)
                .recuperoMagico(0.8));
        metti(TipoPersonaggio.LICH, ValoriDiClasse.nuovi().boss()
                .salute(300).magia(80, 45).esperienza(100).quanti(1).intelligenza(70)
                .carico(1.0).critico(1.0).precisione(1.1).velocita(0.9).furtivita(0.8).parata(0.7)
                .resistenzaMagica(1.2).percezione(1.2).soggezione(1.5).furia(0.2).coraggio(1.6).valore(0.2)
                .numeroBersagli(5.0).stanchezza(0.0).danniMagici(3).danniFisici(0.5).recuperoFisico(0.0)
                .recuperoMagico(1.6));
        // Il Necromante: avversario umano oscuro, un mago che ha fatto dei morti il suo esercito. Come il Mago per forza e magia, ma più temibile a vedersi
        metti(TipoPersonaggio.NECROMANTE, ValoriDiClasse.nuovi()
                .salute(300).magia(60, 34).esperienza(40).quanti(1).intelligenza(55)
                .carico(0.8).critico(0.8).precisione(1.0).velocita(0.9).furtivita(0.9).parata(1.4)
                .resistenzaMagica(1.1).percezione(1.1).soggezione(1.3).furia(0.8).coraggio(1.2).valore(1.0)
                .contrattazione(1.0).numeroBersagli(1.3).stanchezza(1.4).danniMagici(2.6).danniFisici(0.5)
                .recuperoFisico(0.7).recuperoMagico(1.5));
        metti(TipoPersonaggio.MINOTAURO_GIGANTE, ValoriDiClasse.nuovi().boss()
                .salute(950).magia(0, 0).esperienza(70).quanti(1).intelligenza(20)
                .carico(4.0).critico(0.5).precisione(1.2).velocita(0.5).furtivita(0.2).parata(1.4)
                .resistenzaMagica(0.6).percezione(0.6).soggezione(1.5).furia(0.5).coraggio(1.3).valore(0.5)
                .numeroBersagli(1.6).stanchezza(1.4).danniMagici(0.4).danniFisici(2.8).recuperoFisico(1.4)
                .recuperoMagico(0.4));
        metti(TipoPersonaggio.STREGA, ValoriDiClasse.nuovi().femmina().boss()
                .salute(190).magia(55, 32).esperienza(100).quanti(1).intelligenza(65)
                .carico(0.95).critico(0.8).precisione(0.9).velocita(0.9).furtivita(1.0).parata(0.6)
                .resistenzaMagica(1.4).percezione(1.3).soggezione(1.2).furia(0.8).coraggio(1.1).valore(0.8)
                .numeroBersagli(5.0).stanchezza(1.4).danniMagici(3.0).danniFisici(0.6).recuperoFisico(0.8)
                .recuperoMagico(1.4));
        metti(TipoPersonaggio.DRAGO, ValoriDiClasse.nuovi().boss()
                .salute(600).magia(30, 8).esperienza(250).quanti(1).intelligenza(80)
                .carico(8.0).critico(1.0).precisione(1.2).velocita(1.1).furtivita(0.5).parata(1.4)
                .resistenzaMagica(1.5).percezione(1.5).soggezione(1.6).furia(1.1).coraggio(1.6).valore(1.1)
                .numeroBersagli(5.0).stanchezza(0.7).danniMagici(3.0).danniFisici(1.7).recuperoFisico(1.4)
                .recuperoMagico(1.5));
    }

    /**
     * I valori fissi di quella classe (vedi ValoriDiClasse).
     *
     * @throws IllegalStateException se la classe non ha la sua riga nella tabella
     */
    public static ValoriDiClasse valoriDi(TipoPersonaggio classe) {
        ValoriDiClasse valori = VALORI_DI_CLASSE.get(classe);
        if (valori == null) {
            throw new IllegalStateException("La classe " + classe + " non ha i suoi valori in CostantiPersonaggi");
        }
        return valori;
    }

    // Una riga della tabella: i valori, con il controllo che non ne manchi nessuno
    private static void metti(TipoPersonaggio classe, ValoriDiClasse.Costruttore valori) {
        VALORI_DI_CLASSE.put(classe, valori.per(classe));
    }

    // Una classe che ha gli stessi valori di un'altra, già nella tabella (il capitano delle guardie quelli del guerriero)
    private static void comeIl(TipoPersonaggio classe, TipoPersonaggio altra) {
        VALORI_DI_CLASSE.put(classe, valoriDi(altra));
    }

    // I valori comuni a tutti i non combattenti (locandiere, armaiolo, alchimista...): le massime del bardo per 0,85 e un poco meno
    // salute e magia; nessuno di loro si corrompe né fa amicizia
    private static ValoriDiClasse.Costruttore nonCombattente() {
        return ValoriDiClasse.nuovi()
                .salute(90).magia(30, 14).esperienza(25).quanti(1).intelligenza(29.75)
                .carico(0.9).critico(1.1).precisione(1.0).velocita(1.1).furtivita(1.1).parata(1.0)
                .resistenzaMagica(1.1).percezione(1.1).soggezione(1.2).furia(1.1).coraggio(1.1).valore(1.1)
                .contrattazione(1.2).numeroBersagli(1.1).stanchezza(1.0).danniMagici(1.1).danniFisici(0.9)
                .recuperoFisico(1.1).recuperoMagico(1.2);
    }
}
