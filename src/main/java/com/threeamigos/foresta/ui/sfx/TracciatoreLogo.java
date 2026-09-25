package com.threeamigos.foresta.ui.sfx;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Traccia il contorno di un logo con uno o piu' punti luminosi ("corridori") che lo rivelano man mano, poi lo
 * fa apparire per intero con una dissolvenza. Si imposta con il {@link Costruttore}
 * ({@code TracciatoreLogo.costruttore(logo)...costruisci()}), poi si fa avanzare di un fotogramma alla volta con
 * {@link #avanza()} e si disegna con {@link #disegna(Graphics2D)}: chi lo usa decide dove e quando, per
 * esempio con un Timer ogni {@link #INTERVALLO_FOTOGRAMMA_MS} millisecondi.
 * <p>
 * Da riga di comando lo si prova con TracciatoreLogoDaRigaDiComando, tra i sorgenti dei test.
 *
 * ============================================================================
 *  PRINCIPIO DI FUNZIONAMENTO
 * ============================================================================
 *
 * L'effetto si ottiene in una pipeline di fasi, eseguite in ordine:
 *
 * 1) MASCHERA (quali pixel sono "logo" e quali sono "sfondo da scartare")
 *    -----------------------------------------------------------------------
 *    Non decidiamo staticamente "alpha basso = sfondo": deleghiamo la
 *    decisione a un {@link PredicatoSfondo}, una semplice funzione
 *    (x, y, colore-ARGB) -> booleano. Questo permette di riusare lo stesso
 *    algoritmo sia su PNG con canale alpha (predicato basato sulla
 *    trasparenza, vedi {@link PredicatoSfondo#perAlpha}) sia su immagini
 *    opache con uno sfondo a tinta unita (scontorno per colore, vedi
 *    {@link PredicatoSfondo#perColore}), sia su qualunque altro criterio
 *    che si voglia scrivere come lambda. Il risultato e' una matrice
 *    booleana maschera[y][x] = true se il pixel appartiene al logo (primo piano),
 *    false se va scartato (sfondo).
 *
 * 1bis) AREA DI SCANSIONE (opzionale, {@link Costruttore#regione})
 *    -----------------------------------------------------------------------
 *    Se il logo e' solo un dettaglio dentro un disegno molto piu' grande
 *    (es. la scritta "3AM" nera dentro un'illustrazione con sfondo marrone
 *    chiaro), si puo' limitare la ricerca a un rettangolo con
 *    {@link Costruttore#regione}: maschera, tracciamento e buchi vengono
 *    calcolati SOLO su {@code logo.getSubimage(x, y, w, h)} — una vista che
 *    condivide i pixel dell'immagine originale ma ha origine locale (0,0) —
 *    cosi' il resto del disegno (che magari contiene altri elementi con lo
 *    stesso colore/alpha) viene completamente ignorato. Dopo il
 *    ricampionamento (punto 3), a ogni punto dei contorni trovati si
 *    risomma l'offset (x, y) del rettangolo, riportandolo nel sistema di
 *    coordinate dell'immagine intera. Il rendering finale (punto 7) disegna
 *    sempre l'immagine ORIGINALE per intero, sfondo compreso: la regione
 *    serve solo a decidere DOVE cercare il logo, mai cosa disegnare.
 *
 * 2) TRACCIAMENTO DEL CONTORNO ESTERNO (tracciamento del bordo di Moore, "Moore-Neighbor")
 *    -----------------------------------------------------------------------
 *    Un logo puo' contenere piu' forme non collegate tra loro (es. lettere
 *    separate come "3", "A", "M"): la maschera viene quindi scandita riga
 *    per riga cercando OGNI pixel di primo piano non ancora "consumato". Il
 *    primo trovato di ciascuna zona e' garantito essere un pixel di bordo
 *    (i pixel sopra di lui e a sinistra nella sua riga sono sfondo). Da
 *    li' si "cammina" lungo il perimetro: ad ogni passo si esaminano gli 8
 *    vicini in senso orario partendo dalla direzione opposta a quella di
 *    arrivo (il "ritorno"), e si salta al primo vicino di primo piano trovato.
 *    Si ripete finche' non si torna al pixel di partenza: il risultato e'
 *    una sequenza ORDINATA di pixel di bordo che percorre l'intero contorno
 *    di quella forma una sola volta. Dopo aver tracciato una forma, tutti i
 *    suoi pixel (bordo e interno) vengono marcati come "consumati" con un
 *    riempimento, cosi' la scansione prosegue e trova la forma successiva.
 *
 * 2bis) BUCHI INTERNI (es. il triangolo dentro la "A")
 *    -----------------------------------------------------------------------
 *    Una lettera come la "A" racchiude un'area di sfondo che NON tocca mai
 *    il bordo dell'immagine: e' un "buco". Per tracciarlo si applica un
 *    trucco: si costruisce una seconda maschera booleana contenente SOLO i
 *    pixel di sfondo che appartengono a una componente connessa (8-conn.)
 *    che non tocca mai il bordo dell'immagine (vedi
 *    {@link #estraiBuchiRacchiusi}), e si passa questa maschera allo STESSO
 *    tracciatore usato al punto 2 — dal punto di vista dell'algoritmo, un
 *    buco non e' concettualmente diverso da una qualsiasi altra "forma": e'
 *    semplicemente una regione connessa di cui vogliamo il contorno. Il
 *    risultato e' un contorno aggiuntivo che corre lungo l'interno della
 *    lettera. Nota: usando la stessa connettivita' (8) sia per il logo sia
 *    per lo sfondo puo', in teoria, dare ambiguita' su un pixel di
 *    diagonale singola (paradosso della topologia digitale); per font e
 *    loghi normali, con tratti di almeno 2px, non e' un problema pratico.
 *
 * 3) RICAMPIONAMENTO A DISTANZA COSTANTE
 *    -----------------------------------------------------------------------
 *    Ogni contorno pixel-per-pixel (esterno o buco) viene ridotto a un
 *    numero fisso di punti equidistanti lungo il perimetro (parametrizzazione
 *    per lunghezza d'arco), cosi' la velocita' del punto luminoso e'
 *    uniforme indipendentemente dalla risoluzione dell'immagine.
 *
 * 4) VERSO DI PERCORRENZA (orario / antiorario), PER CORRIDORE
 *    -----------------------------------------------------------------------
 *    L'algoritmo di tracciamento produce un verso "naturale" che dipende
 *    dalla forma (non e' prevedibile a priori, e vale sia per i contorni
 *    esterni sia per i buchi). Per sapere se quel verso naturale sia,
 *    visivamente, orario o antiorario, si calcola l'area con segno del
 *    poligono tracciato (formula dell'area di Gauss, "shoelace") sulle coordinate immagine
 *    (asse Y verso il basso): un'area con segno positivo corrisponde a un
 *    percorso orario sullo schermo, una negativa a un percorso antiorario.
 *    Da qui si costruiscono due liste di punti per ciascuna forma: quella
 *    naturale e la sua inversa (stesso contorno, verso opposto), così ogni
 *    singolo corridore puo' scegliere il proprio verso (vedi
 *    {@link ContornoForma}, {@link Costruttore#corridore} e
 *    {@link Costruttore#versi}, che accetta un valore per corridore).
 *
 * 5) PUNTI DI PARTENZA / ARRIVO E "CORRIDORI" MULTIPLI
 *    -----------------------------------------------------------------------
 *    Un {@link SpecificaCorridore} descrive un singolo punto luminoso indipendente:
 *    un punto di partenza (coordinate pixel), un punto di arrivo opzionale
 *    (se assente, il corridore fa un giro completo tornando al punto di
 *    partenza), un verso (vedi punto 4) e un ritardo (vedi punto 6bis). Il
 *    punto di partenza viene "agganciato" prima alla forma (contorno
 *    esterno o buco) il cui contorno ha il punto piu' vicino, poi, dentro
 *    quella forma, al punto del contorno piu' vicino. Se non viene indicato
 *    nessun corridore esplicito ({@link Costruttore#corridore}), viene creato automaticamente un corridore a
 *    giro completo per OGNI forma rilevata (contorni esterni e buchi
 *    inclusi).
 *
 * 6) SINCRONIZZAZIONE DI PIU' CORRIDORI (quando ce n'e' piu' di uno)
 *    -----------------------------------------------------------------------
 *    Con più corridori di lunghezza diversa (lettere/buchi diversi, o archi
 *    parziali con un arrivo) c'è un'unica domanda che conta: "quando si
 *    considera completato il giro nel suo insieme?". La risposta è sempre
 *    la stessa in questo programma — SOLO quando OGNI corridore ha raggiunto
 *    la fine del proprio percorso (vedi {@link #avanza}, fase TRACCIAMENTO: un
 *    corridore che finisce prima si ferma lì e aspetta gli altri). Quello che
 *    cambia con {@link Costruttore#velocitaAllineata} è SOLO la velocita' di ciascun
 *    corridore:
 *      - predefinito (indipendente): tutti i corridori avanzano alla stessa
 *        velocità "di riferimento", quindi un percorso più corto finisce
 *        prima e resta fermo ad aspettare gli altri;
 *      - matched: la velocità di ogni corridore è ricalcolata in base alla
 *        lunghezza del proprio percorso, così tutti raggiungono la fine
 *        esattamente nello stesso fotogramma.
 *
 * 6bis) PARTENZA POSTICIPATA (ritardo)
 *    -----------------------------------------------------------------------
 *    Ogni {@link SpecificaCorridore} può avere un ritardo iniziale, in fotogrammi o
 *    in secondi (vedi {@link Costruttore#ritardi} e {@link Costruttore#corridore}). Finché il ritardo non è scaduto il
 *    corridore è semplicemente invisibile: {@link #avanza} lo tiene fermo a
 *    inizio percorso e ne decrementa il contatore invece di farlo avanzare,
 *    e {@link #disegnaFotogramma}/{@link #rivelaLogoConTorcia} saltano del
 *    tutto i corridori non ancora partiti (niente torcia, niente scia). Questo
 *    si somma naturalmente alla regola del punto 6: il gruppo non è
 *    "completato" finché anche l'ultimo corridore ritardato non ha concluso
 *    il proprio percorso. Il ritardo residuo viene ripristinato al valore
 *    iniziale ad ogni nuovo ciclo (fase PAUSA -> TRACCIAMENTO), così la partenza
 *    sfalsata si ripete identica ad ogni giro.
 *
 * 7) RESA GRAFICA: TORCIA + DISSOLVENZA FINALE
 *    -----------------------------------------------------------------------
 *    L'intera animazione e' una piccola macchina a stati ({@link Fase}):
 *      TRACCIAMENTO    -> il logo è nascosto; ogni corridore porta con se una
 *                    "torcia" (un cerchio con gradiente radiale morbido)
 *                    che rivela il logo SOLO nel proprio raggio d'azione,
 *                    ricreata da zero ad ogni fotogramma (non si accumula:
 *                    appena la luce si allontana da un punto, quel punto
 *                    torna nascosto). Tecnicamente si disegna il logo su
 *                    immagine di appoggio fuori schermo e si applica come "maschera" la torcia
 *                    con {@code AlphaComposite.DstIn}, che moltiplica
 *                    l'alpha del logo per l'alpha della maschera pixel per
 *                    pixel. La scia bianca e il punto luminoso restano
 *                    sempre disegnati sopra, indipendentemente dal logo.
 *      DISSOLVENZA  -> una volta che TUTTI i corridori hanno completato (vedi
 *                    punto 6), il logo intero viene dissolto in trasparenza
 *                    da invisibile a completamente visibile in
 *                    {@code fotogrammiDissolvenza} fotogrammi (un semplice
 *                    AlphaComposite globale, non più localizzato).
 *      PAUSA        -> il logo resta visibile a piena opacità per una pausa.
 *      Poi, se il Costruttore ha chiesto di ripetere ({@link Costruttore#ripeti}), si
 *      torna a TRACCIAMENTO azzerando i corridori; altrimenti l'effetto resta
 *      fermo in PAUSA e {@link #isFinito()} diventa vero.
 *    La modalità "torcia" è quella predefinita; con {@code rivelaConTorcia(false)}
 *    si va al comportamento più semplice (logo sempre visibile).
 *
 */
public final class TracciatoreLogo {

    // ---- parametri regolabili ----------------------------------------------
    // I valori predefiniti del Costruttore, raggruppati qui in cima per essere
    // facili da ritrovare e modificare senza dover cercare "numeri magici"
    // sparsi nel codice.
    static final int SOGLIA_ALPHA_PREDEFINITA = 128;              // soglia alpha predefinita (0-255)
    static final int DURATA_GIRO_MS_PREDEFINITA = 4000;           // durata di riferimento di un giro completo
    private static final int PUNTI_RICAMPIONAMENTO = 900;         // punti equidistanti in cui viene ricampionato ogni contorno
    /** L'intervallo tra due fotogrammi a cui l'effetto e' pensato: 16ms ~= 60 fotogrammi al secondo. */
    public static final int INTERVALLO_FOTOGRAMMA_MS = 16;
    static final int FOTOGRAMMI_PAUSA_PREDEFINITI = 45;           // quanti fotogrammi restare fermi in fase PAUSA (logo completo acceso)
    private static final int ALPHA_MASSIMA_TORCIA = 200;          // 0-255: opacità massima del logo sotto la torcia ("si intravede" e basta)
    static final int FOTOGRAMMI_DISSOLVENZA_PREDEFINITI = 40;     // durata predefinita della dissolvenza finale, in fotogrammi
    static final Color COLORE_SFONDO_PREDEFINITO = new Color(30, 30, 34); // colore di sfondo mostrato quando il logo è nascosto

    // =========================================================================
    //  Criterio "pixel da scartare"
    // =========================================================================

    /**
     * Decide se il pixel (x, y), di colore argb, va considerato sfondo
     * (quindi escluso dal logo e dal tracciamento del contorno).
     */
    @FunctionalInterface
    public interface PredicatoSfondo {
        boolean isSfondo(int x, int y, int argb);

        /** Scarta i pixel il cui canale alpha e' minore o uguale alla soglia. */
        static PredicatoSfondo perAlpha(int soglia) {
            // argb e' un intero a 32 bit nel formato 0xAARRGGBB: >>> 24 sposta il
            // canale alpha nei bit bassi (shift SENZA segno, importante perche' il
            // bit piu' alto di un int Java è il segno), & 0xFF isola solo quel byte.
            return (x, y, argb) -> ((argb >>> 24) & 0xFF) <= soglia;
        }

        /**
         * Scarta i pixel il cui colore RGB e' vicino a {@code sfondo} entro
         * {@code tolleranza} per canale (utile per immagini opache con uno
         * sfondo a tinta unita ben definito, es. verde/bianco/nero pieno).
         * Il canale alpha viene ignorato.
         */
        static PredicatoSfondo perColore(Color sfondo, int tolleranza) {
            // i tre componenti del colore di sfondo vengono estratti UNA VOLTA sola,
            // fuori dalla lambda, cosi' non si ricalcolano ad ogni pixel controllato
            int rossoSfondo = sfondo.getRed(), verdeSfondo = sfondo.getGreen(), bluSfondo = sfondo.getBlue();
            return (x, y, argb) -> {
                // estrazione dei canali R, G, B del pixel corrente dall'intero ARGB
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                // "vicino" = ogni canale entro +/- tolerance dal colore di sfondo
                // (distanza "a cubo", non euclidea: piu' semplice e sufficiente qui)
                return Math.abs(r - rossoSfondo) <= tolleranza
                        && Math.abs(g - verdeSfondo) <= tolleranza
                        && Math.abs(b - bluSfondo) <= tolleranza;
            };
        }
    }

    public enum Verso { ORARIO, ANTIORARIO }

    /** Descrive un singolo punto luminoso indipendente: partenza, arrivo (opzionale), verso e ritardo. */
    public static class SpecificaCorridore {
        final Point2D.Double partenza;
        final Point2D.Double arrivo; // null => giro completo
        final Verso verso;
        final int fotogrammiRitardo; // ritardo iniziale, in fotogrammi, prima che il corridore parta

        public SpecificaCorridore(Point2D.Double partenza, Point2D.Double arrivo, Verso verso, int fotogrammiRitardo) {
            this.partenza = partenza;
            this.arrivo = arrivo;
            this.verso = verso;
            this.fotogrammiRitardo = fotogrammiRitardo;
        }
    }

    // =========================================================================
    //  L'effetto
    // =========================================================================

    private final BufferedImage logo;
    private final List<Corridore> corridori;
    private final int fotogrammiPerGiro;
    private final int fotogrammiDissolvenza;
    private final int fotogrammiPausa;
    private final boolean rivelaConTorcia;
    private final int raggioTorcia;
    private final boolean ripeti;
    private final Color coloreSfondo;
    private final List<String> diagnostica;
    private final StatoAnimazione stato = new StatoAnimazione();

    private TracciatoreLogo(Costruttore c, List<Corridore> corridori, int fotogrammiPerGiro, int raggioTorcia,
                            List<String> diagnostica) {
        this.logo = c.logo;
        this.corridori = corridori;
        this.fotogrammiPerGiro = fotogrammiPerGiro;
        this.fotogrammiDissolvenza = c.fotogrammiDissolvenza;
        this.fotogrammiPausa = c.fotogrammiPausa;
        this.rivelaConTorcia = c.rivelaConTorcia;
        this.raggioTorcia = raggioTorcia;
        this.ripeti = c.ripeti;
        this.coloreSfondo = c.coloreSfondo;
        this.diagnostica = Collections.unmodifiableList(diagnostica);
    }

    /**
     * Comincia a impostare l'effetto per quel logo. Il logo viene sempre disegnato per intero; dove cercarne il
     * contorno lo decidono il criterio di sfondo e l'eventuale regione.
     */
    public static Costruttore costruttore(BufferedImage logo) {
        return new Costruttore(logo);
    }

    /** Fa avanzare l'animazione di un fotogramma. Finito l'effetto (vedi {@link #isFinito()}) non cambia piu' nulla. */
    public void avanza() {
        avanza(corridori, stato, fotogrammiDissolvenza, fotogrammiPausa, ripeti);
    }

    /**
     * Disegna il fotogramma corrente con l'angolo in alto a sinistra del logo in (0, 0) e alla sua dimensione:
     * per spostarlo o ingrandirlo si trasforma prima {@code g2} (translate, scale).
     */
    public void disegna(Graphics2D g2) {
        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        double opacita = !rivelaConTorcia ? 1.0 : opacitaDissolvenza(stato, fotogrammiDissolvenza);
        disegnaFotogramma(g, logo, corridori, stato.fase, opacita, rivelaConTorcia, raggioTorcia, coloreSfondo);
        g.dispose();
    }

    /** Vero quando, senza ripetizione, tracciamento, dissolvenza e pausa finale sono terminati. */
    public boolean isFinito() {
        return stato.finito;
    }

    /** Riporta l'effetto all'inizio, come appena costruito. */
    public void ricomincia() {
        for (Corridore c : corridori) {
            c.avanzamento = 0;
            c.fotogrammiRitardo = c.fotogrammiRitardoIniziale;
        }
        stato.fase = Fase.TRACCIAMENTO;
        stato.fotogrammaDissolvenza = 0;
        stato.fotogrammaPausa = 0;
        stato.finito = false;
    }

    public int getLarghezza() {
        return logo.getWidth();
    }

    public int getAltezza() {
        return logo.getHeight();
    }

    /** Il nome della fase corrente (TRACCIAMENTO, DISSOLVENZA, PAUSA), per la diagnostica. */
    public String getNomeFase() {
        return stato.fase.name();
    }

    /**
     * Quanti fotogrammi dura (per eccesso) un ciclo intero: il tracciamento del corridore piu' lento, in genere un
     * giro completo, piu' il ritardo del corridore piu' "tardivo", la dissolvenza e la pausa finale.
     */
    public int stimaFotogrammiCiclo() {
        int ritardoMassimo = 0;
        for (Corridore c : corridori) {
            ritardoMassimo = Math.max(ritardoMassimo, c.fotogrammiRitardoIniziale);
        }
        return fotogrammiPerGiro + ritardoMassimo + 3 + fotogrammiDissolvenza + fotogrammiPausa;
    }

    /** Cosa ha trovato e deciso il Costruttore (forme, corridori, avvertimenti): utile per calibrare i parametri. */
    public List<String> getDiagnostica() {
        return diagnostica;
    }

    // =========================================================================
    //  Costruttore
    // =========================================================================

    /**
     * Imposta l'effetto: tutti i parametri sono facoltativi, con i valori predefiniti in cima alla classe.
     * {@link #costruisci()} esegue le fasi 1-5 della pipeline (maschera, contorni, buchi, ricampionamento,
     * corridori).
     */
    public static final class Costruttore {

        private final BufferedImage logo;
        private PredicatoSfondo sfondo;
        private Rectangle regione;
        private final List<SpecificaCorridore> corridoriEspliciti = new ArrayList<>();
        private List<Verso> versi = Collections.emptyList();
        private List<Integer> ritardi = Collections.emptyList();
        private int durataGiroMs = DURATA_GIRO_MS_PREDEFINITA;
        private boolean velocitaAllineata;
        private boolean rivelaConTorcia = true;
        private Integer raggioTorcia;
        private int fotogrammiDissolvenza = FOTOGRAMMI_DISSOLVENZA_PREDEFINITI;
        private int fotogrammiPausa = FOTOGRAMMI_PAUSA_PREDEFINITI;
        private boolean ripeti;
        private Color coloreSfondo = COLORE_SFONDO_PREDEFINITO;

        private Costruttore(BufferedImage logo) {
            this.logo = Objects.requireNonNull(logo, "logo");
        }

        /** Sono sfondo i pixel con alpha minore o uguale alla soglia (predefinito: trasparenza con soglia 128). */
        public Costruttore sfondoPerAlpha(int soglia) {
            this.sfondo = PredicatoSfondo.perAlpha(soglia);
            return this;
        }

        /** Sono sfondo i pixel di colore vicino a questo, entro la tolleranza per canale (scontorno per colore). */
        public Costruttore sfondoPerColore(Color colore, int tolleranza) {
            this.sfondo = PredicatoSfondo.perColore(colore, tolleranza);
            return this;
        }

        /** Un criterio di sfondo qualsiasi. */
        public Costruttore sfondo(PredicatoSfondo sfondo) {
            this.sfondo = Objects.requireNonNull(sfondo, "sfondo");
            return this;
        }

        /** Cerca il logo solo in questo rettangolo dell'immagine (vedi punto 1bis); null per tutta l'immagine. */
        public Costruttore regione(Rectangle regione) {
            this.regione = regione;
            return this;
        }

        /**
         * Aggiunge un corridore esplicito: parte dal punto del contorno piu' vicino a {@code partenza} e arriva a
         * quello piu' vicino ad {@code arrivo}, oppure fa un giro completo se {@code arrivo} e' null. Se non se ne
         * aggiunge nessuno, c'e' un corridore a giro completo per ogni forma trovata.
         */
        public Costruttore corridore(Point2D.Double partenza, Point2D.Double arrivo, Verso verso, int fotogrammiRitardo) {
            corridoriEspliciti.add(new SpecificaCorridore(partenza, arrivo, verso, Math.max(0, fotogrammiRitardo)));
            return this;
        }

        /** Il verso di tutti i corridori automatici (predefinito orario). */
        public Costruttore verso(Verso verso) {
            return versi(Collections.singletonList(verso));
        }

        /**
         * Il verso dei corridori automatici, uno per forma nell'ordine in cui sono trovate; alle forme in piu' si
         * applica il primo.
         */
        public Costruttore versi(List<Verso> versi) {
            this.versi = new ArrayList<>(versi);
            return this;
        }

        /** Il ritardo di partenza dei corridori automatici, in fotogrammi, uno per forma (le altre partono subito). */
        public Costruttore ritardi(List<Integer> ritardi) {
            this.ritardi = new ArrayList<>(ritardi);
            return this;
        }

        /** Durata di riferimento di un giro completo, in millisecondi. */
        public Costruttore durataGiroMs(int durataGiroMs) {
            this.durataGiroMs = durataGiroMs;
            return this;
        }

        /** Vero perche' tutti i corridori arrivino in fondo insieme (vedi punto 6); predefinito falso. */
        public Costruttore velocitaAllineata(boolean velocitaAllineata) {
            this.velocitaAllineata = velocitaAllineata;
            return this;
        }

        /** Falso per mostrare subito il logo intero invece di rivelarlo con la torcia (vedi punto 7). */
        public Costruttore rivelaConTorcia(boolean rivelaConTorcia) {
            this.rivelaConTorcia = rivelaConTorcia;
            return this;
        }

        /** Raggio della torcia in pixel (predefinito circa il 10% del lato piu' corto del logo). */
        public Costruttore raggioTorcia(int raggioTorcia) {
            this.raggioTorcia = raggioTorcia;
            return this;
        }

        /** Durata della dissolvenza finale, in fotogrammi. */
        public Costruttore fotogrammiDissolvenza(int fotogrammiDissolvenza) {
            this.fotogrammiDissolvenza = Math.max(1, fotogrammiDissolvenza);
            return this;
        }

        /** Quanti fotogrammi resta acceso il logo intero alla fine. */
        public Costruttore fotogrammiPausa(int fotogrammiPausa) {
            this.fotogrammiPausa = Math.max(0, fotogrammiPausa);
            return this;
        }

        /** Vero per ricominciare da capo dopo la pausa, all'infinito; predefinito falso (una volta sola). */
        public Costruttore ripeti(boolean ripeti) {
            this.ripeti = ripeti;
            return this;
        }

        /** Il colore con cui riempire il rettangolo del logo prima di disegnarlo; null per non riempirlo. */
        public Costruttore coloreSfondo(Color coloreSfondo) {
            this.coloreSfondo = coloreSfondo;
            return this;
        }

        /**
         * Esegue la pipeline e restituisce l'effetto, fermo al primo fotogramma.
         *
         * @throws IllegalArgumentException se la regione esce dall'immagine
         * @throws IllegalStateException se non si trova nessun contorno
         */
        public TracciatoreLogo costruisci() {
            List<String> diagnostica = new ArrayList<>();
            PredicatoSfondo predicatoSfondo = sfondo;
            if (predicatoSfondo == null) {
                if (!logo.getColorModel().hasAlpha()) {
                    diagnostica.add("Attenzione: l'immagine non ha canale alpha; va indicato un colore di sfondo per tracciarla correttamente.");
                }
                predicatoSfondo = PredicatoSfondo.perAlpha(SOGLIA_ALPHA_PREDEFINITA);
            }

            // --- area di scansione opzionale (fase 1bis) ----------------------------
            if (regione != null && (regione.x < 0 || regione.y < 0 || regione.width <= 0 || regione.height <= 0
                    || regione.x + regione.width > logo.getWidth() || regione.y + regione.height > logo.getHeight())) {
                throw new IllegalArgumentException("il rettangolo (" + regione.x + "," + regione.y + "," + regione.width
                        + "," + regione.height + ") esce dai limiti dell'immagine (" + logo.getWidth() + "x" + logo.getHeight() + ")");
            }
            // getSubimage() NON copia i pixel: restituisce una "vista" che condivide
            // lo stesso array di dati dell'immagine originale, ma la cui coordinata
            // (0,0) corrisponde a (regione.x, regione.y) nell'immagine intera. Per
            // questo, dopo aver trovato i contorni su questa vista, dovremo poi
            // sommare (scostamentoX, scostamentoY) per riportarli nel sistema di coordinate
            // dell'immagine originale (usata invece per la resa finale).
            BufferedImage immagineScansione = regione != null ? logo.getSubimage(regione.x, regione.y, regione.width, regione.height) : logo;
            int scostamentoX = regione != null ? regione.x : 0;
            int scostamentoY = regione != null ? regione.y : 0;
            if (regione != null) {
                diagnostica.add("Area di scansione: " + regione.width + "x" + regione.height
                        + " a partire da (" + regione.x + "," + regione.y + "); la resa userà comunque l'immagine intera.");
            }

            // --- fase 1: maschera primo piano/sfondo, calcolata SOLO sull'area di scansione ---
            boolean[][] maschera = costruisciMaschera(immagineScansione, predicatoSfondo);

            // --- fase 2 e 2bis: tracciamento dei contorni esterni e dei buchi interni ---
            List<List<Point>> contorniEsterni = tracciaTuttiIContorni(maschera);
            boolean[][] mascheraBuchi = estraiBuchiRacchiusi(maschera);
            List<List<Point>> contorniBuchi = tracciaTuttiIContorni(mascheraBuchi);

            // uniamo i due elenchi: da qui in poi un contorno esterno e un buco sono
            // trattati esattamente allo stesso modo (entrambi diventano una "forma")
            List<List<Point>> tuttiIContorni = new ArrayList<>(contorniEsterni);
            tuttiIContorni.addAll(contorniBuchi);
            if (tuttiIContorni.isEmpty()) {
                throw new IllegalStateException("Nessun contorno trovato: controlla la soglia/il colore di sfondo, l'immagine o la regione.");
            }

            // --- fase 3 e 4: ricampionamento a distanza costante + verso naturale ---
            List<ContornoForma> forme = new ArrayList<>();
            for (List<Point> contorno : tuttiIContorni) {
                List<Point2D.Double> naturale = ricampiona(contorno, PUNTI_RICAMPIONAMENTO);
                // con una regione, i punti sono ancora in coordinate LOCALI al
                // ritaglio: li riportiamo nel sistema di coordinate dell'immagine intera
                if (scostamentoX != 0 || scostamentoY != 0) {
                    for (Point2D.Double p : naturale) {
                        p.x += scostamentoX;
                        p.y += scostamentoY;
                    }
                }
                // il verso naturale (orario/antiorario) è invariante per traslazione,
                // quindi non importa se lo calcoliamo prima o dopo aver sommato lo scostamento
                boolean orario = areaConSegno(naturale) > 0;
                List<Point2D.Double> invertito = new ArrayList<>(naturale);
                Collections.reverse(invertito); // stesso contorno, ma percorso in verso opposto
                forme.add(new ContornoForma(naturale, invertito, orario));
            }

            // diagnostica: utile per calibrare le coordinate di partenza e arrivo dei
            // corridori a tentativi, senza dover indovinare "a occhio" dove cade ogni forma
            diagnostica.add(forme.size() + " forma/e rilevate (" + contorniEsterni.size()
                    + " contorni esterni, " + contorniBuchi.size() + " buchi interni):");
            for (int i = 0; i < forme.size(); i++) {
                ContornoForma forma = forme.get(i);
                String tipo = i < contorniEsterni.size() ? "esterno" : "buco";
                diagnostica.add("  forma " + i + " (" + tipo + "): verso naturale "
                        + (forma.naturaleOrario ? "orario" : "antiorario")
                        + ", partenza naturale " + formattaPunto(forma.percorsoNaturale.get(0)) + ", " + forma.percorsoNaturale.size() + " punti.");
            }

            // --- fase 5: costruzione delle "specifiche" di ciascun corridore -----------
            // Senza corridori espliciti ce n'e' uno a giro completo per ogni forma rilevata,
            // ciascuno con il proprio ritardo/verso se ritardi/versi ne definiscono uno; il
            // primo verso (o quello orario) fa da "riserva" per i corridori in eccesso
            List<SpecificaCorridore> specifiche = new ArrayList<>(corridoriEspliciti);
            if (specifiche.isEmpty()) {
                Verso versoDiRiserva = versi.isEmpty() ? Verso.ORARIO : versi.get(0);
                for (int i = 0; i < forme.size(); i++) {
                    int ritardo = i < ritardi.size() ? Math.max(0, ritardi.get(i)) : 0;
                    Verso direzione = i < versi.size() ? versi.get(i) : versoDiRiserva;
                    specifiche.add(new SpecificaCorridore(forme.get(i).percorsoNaturale.get(0), null, direzione, ritardo));
                }
            }

            // --- calcolo della velocita' di riferimento e costruzione dei Corridore veri e propri ---
            // fotogrammiPerGiro = quanti fotogrammi servono per completare un giro di
            // riferimento alla velocita' "di riferimento"
            int fotogrammiPerGiro = Math.max(1, durataGiroMs / INTERVALLO_FOTOGRAMMA_MS);

            List<Corridore> corridori = new ArrayList<>();
            for (SpecificaCorridore specifica : specifiche) {
                // trasforma la "specifica astratta" (punti pixel) in un percorso concreto
                // di punti ricampionati, agganciato alla forma giusta (vedi punto 5)
                List<Point2D.Double> percorso = costruisciPercorsoCorridore(forme, specifica);
                double puntiPerFotogramma = velocitaAllineata
                        // velocità "su misura": lunghezza del percorso diviso il numero di
                        // fotogrammi disponibili, così ogni corridore arriva in fondo esattamente
                        // allo stesso fotogramma (vedi punto 6 della documentazione)
                        ? (double) percorso.size() / fotogrammiPerGiro
                        // velocità "di riferimento" uguale per tutti: un percorso più corto
                        // del giro di riferimento (PUNTI_RICAMPIONAMENTO) finisce prima e resta
                        // fermo ad aspettare gli altri (vedi avanza())
                        : (double) PUNTI_RICAMPIONAMENTO / fotogrammiPerGiro;
                corridori.add(new Corridore(percorso, puntiPerFotogramma, specifica.fotogrammiRitardo));
                diagnostica.add("Corridore: partenza agganciata a " + formattaPunto(percorso.get(0))
                        + (specifica.arrivo != null ? ", arrivo agganciato a " + formattaPunto(percorso.get(percorso.size() - 1)) : ", giro completo")
                        + ", verso " + (specifica.verso == Verso.ORARIO ? "orario" : "antiorario")
                        + ", " + percorso.size() + " punti" + (velocitaAllineata ? " (velocita' sincronizzata)" : "")
                        + (specifica.fotogrammiRitardo > 0 ? String.format(", ritardo %d fotogrammi (~%.2fs)",
                                specifica.fotogrammiRitardo, specifica.fotogrammiRitardo * INTERVALLO_FOTOGRAMMA_MS / 1000.0) : "") + ".");
            }

            // --- resa grafica (fase 7) -----------------------------------------------
            // il raggio della torcia è proporzionato alle dimensioni dell'immagine:
            // un raggio fisso (pensato per loghi grandi) coprirebbe quasi tutto un
            // logo piccolo come "3AM" (100px di altezza), facendo sembrare la torcia
            // "sempre spenta" perché rivela l'intera lettera in un colpo solo.
            int raggio = raggioTorcia != null ? raggioTorcia : Math.max(6, Math.min(logo.getWidth(), logo.getHeight()) / 10);

            return new TracciatoreLogo(this, corridori, fotogrammiPerGiro, raggio, diagnostica);
        }
    }

    /** Formatta un punto per i messaggi diagnostici a console, arrotondato al pixel. */
    private static String formattaPunto(Point2D.Double p) {
        return "(" + Math.round(p.x) + "," + Math.round(p.y) + ")";
    }

    // =========================================================================
    //  1. Maschera primo piano/sfondo
    // =========================================================================

    /**
     * Scandisce ogni pixel dell'immagine (o del ritaglio, se è stata data una
     * regione) e applica il {@link PredicatoSfondo} per decidere se
     * appartiene al logo (true) o va scartato come sfondo (false).
     */
    private static boolean[][] costruisciMaschera(BufferedImage immagine, PredicatoSfondo sfondo) {
        int w = immagine.getWidth(), h = immagine.getHeight();
        // array indicizzato [riga][colonna] = [y][x], come si fa di solito per le
        // immagini: rende piu' naturale scorrere riga per riga (vedi i cicli sotto)
        boolean[][] maschera = new boolean[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = immagine.getRGB(x, y); // colore del pixel, codificato 0xAARRGGBB
                maschera[y][x] = !sfondo.isSfondo(x, y, argb);
            }
        }
        return maschera;
    }

    // =========================================================================
    //  2. tracciamento del bordo di Moore, "Moore-Neighbor" (contorni esterni)
    // =========================================================================

    // Le due tabelle seguenti codificano gli 8 vicini di un pixel, IN SENSO
    // ORARIO, partendo da Ovest: DX[i]/DY[i] e' lo spostamento (dx, dy) per
    // arrivare al vicino i-esimo. Sono il cuore geometrico di tutto
    // l'algoritmo di tracciamento (Moore-Neighbor) usato sotto.
    //   i=0 Ovest, i=1 Nord-Ovest, i=2 Nord, i=3 Nord-Est,
    //   i=4 Est,   i=5 Sud-Est,    i=6 Sud,  i=7 Sud-Ovest
    private static final int[] DX = { -1, -1, 0, 1, 1, 1, 0, -1 };
    private static final int[] DY = { 0, -1, -1, -1, 0, 1, 1, 1 };

    /**
     * Trova e traccia il contorno di OGNI forma connessa presente nella
     * maschera (scansione riga per riga; ogni pixel di primo piano non ancora
     * consumato avvia il tracciamento di una nuova forma, che viene poi
     * marcata interamente come consumata con un riempimento). Usata sia per i
     * contorni esterni del logo sia, riapplicata su {@link #estraiBuchiRacchiusi},
     * per i buchi interni: l'algoritmo non fa distinzione tra i due casi.
     */
    private static List<List<Point>> tracciaTuttiIContorni(boolean[][] maschera) {
        int h = maschera.length, w = maschera[0].length;
        // "consumed" tiene traccia di quali pixel appartengono a una forma gia'
        // trovata, per non ri-tracciarla (e non trovarla una seconda volta durante
        // la scansione) quando la scansione incontra un suo altro pixel piu' avanti
        boolean[][] consumati = new boolean[h][w];
        List<List<Point>> tuttiIContorni = new ArrayList<>();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (maschera[y][x] && !consumati[y][x]) {
                    // (x, y) è il primo pixel di primo piano di una forma MAI VISTA
                    // prima: è garantito essere un pixel di bordo (vedi commento su
                    // tracciaContornoDa), quindi possiamo partire da qui per tracciare
                    List<Point> contorno = tracciaContornoDa(maschera, new Point(x, y));
                    if (contorno.size() >= 4) {
                        // scarta forme degeneri (rumore di pochi pixel isolati)
                        tuttiIContorni.add(contorno);
                    }
                    // marca TUTTA la forma (non solo il bordo) come consumata, cosi'
                    // la scansione prosegue senza ritrovare altri pixel della stessa forma
                    marcaComponenteConsumata(maschera, consumati, x, y);
                }
            }
        }
        return tuttiIContorni;
    }

    /** Riempimento (8-connesso) che marca come "consumati" tutti i pixel della forma a cui appartiene (sx, sy). */
    private static void marcaComponenteConsumata(boolean[][] maschera, boolean[][] consumati, int sx, int sy) {
        int h = maschera.length, w = maschera[0].length;
        // riempimento iterativo con una pila esplicita (invece che ricorsivo):
        // evita il rischio di StackOverflowError su forme molto grandi
        Deque<Point> pila = new ArrayDeque<>();
        pila.push(new Point(sx, sy));
        consumati[sy][sx] = true;
        while (!pila.isEmpty()) {
            Point p = pila.pop();
            // esamina tutti gli 8 vicini: se sono primo piano e non ancora visitati,
            // li marca e li aggiunge allo stack per essere esplorati a loro volta
            for (int i = 0; i < 8; i++) {
                int nx = p.x + DX[i], ny = p.y + DY[i];
                if (dentroILimiti(nx, ny, w, h) && maschera[ny][nx] && !consumati[ny][nx]) {
                    consumati[ny][nx] = true;
                    pila.push(new Point(nx, ny));
                }
            }
        }
    }

    /**
     * Traccia il contorno della forma a cui appartiene {@code start}, che deve
     * essere gia' un pixel di bordo (garantito quando lo si passa dalla
     * scansione riga-per-riga di {@link #tracciaTuttiIContorni}: essendo il primo
     * pixel di primo piano incontrato in quella riga/forma, il pixel sopra di
     * lui e quello a sinistra sono sicuramente sfondo).
     *
     * Algoritmo (tracciamento del bordo di Moore, "Moore-Neighbor"): partendo dal pixel corrente
     * e dalla direzione da cui si è arrivati (il "ritorno"), si esaminano gli 8
     * vicini in senso orario A PARTIRE da quella direzione, e si salta al primo
     * vicino di primo piano trovato. Questo garantisce di "seguire" sempre il
     * bordo più esterno, senza tagliare angoli né saltare a componenti diverse.
     * Si ripete finché non si ritorna al pixel di partenza.
     */
    private static List<Point> tracciaContornoDa(boolean[][] maschera, Point partenza) {
        List<Point> contorno = new ArrayList<>();
        int h = maschera.length, w = maschera[0].length;

        Point corrente = partenza;
        // il pixel a Ovest di start è garantito sfondo (vedi Javadoc sopra):
        // è il punto di partenza "virtuale" da cui iniziare la ricerca in senso orario
        Point ritorno = new Point(partenza.x - 1, partenza.y);
        contorno.add(partenza);

        // salvaguardia anti-loop-infinito: in condizioni normali l'algoritmo
        // termina tornando su "start" molto prima di questo limite, ma un bug o
        // una maschera patologica non devono poter bloccare il programma
        long passiMassimi = (long) w * h * 2L;
        long passi = 0;

        while (passi++ < passiMassimi) {
            // direzione (0-7) dal pixel corrente verso il pixel di ritorno:
            // la ricerca del prossimo pixel di bordo riparte da QUI, in senso orario
            int direzioneIniziale = direzioneVerso(corrente, ritorno);
            Point prossimo = null;
            for (int i = 1; i <= 8; i++) {
                int direzione = (direzioneIniziale + i) % 8; // scorre gli 8 vicini in senso orario
                int nx = corrente.x + DX[direzione];
                int ny = corrente.y + DY[direzione];
                if (dentroILimiti(nx, ny, w, h) && maschera[ny][nx]) {
                    // trovato il prossimo pixel di bordo: il NUOVO ritorno è il
                    // vicino appena precedente (l'ultimo che era ancora sfondo),
                    // così al prossimo giro la ricerca riparte dalla direzione giusta
                    prossimo = new Point(nx, ny);
                    int direzionePrecedente = (direzione - 1 + 8) % 8;
                    ritorno = new Point(corrente.x + DX[direzionePrecedente], corrente.y + DY[direzionePrecedente]);
                    break;
                }
            }
            if (prossimo == null) break; // pixel isolato, nessun vicino di primo piano: forma di un solo pixel

            corrente = prossimo;
            if (corrente.equals(partenza)) break; // il giro si è richiuso: contorno completo

            contorno.add(corrente);
        }
        return contorno;
    }

    /** Vero se (x, y) cade dentro una griglia w x h (0-indicizzata). */
    private static boolean dentroILimiti(int x, int y, int w, int h) {
        return x >= 0 && x < w && y >= 0 && y < h;
    }

    /** Trova l'indice (0-7, vedi DX/DY) della direzione che porta da "from" a "to" (devono essere vicini adiacenti). */
    private static int direzioneVerso(Point da, Point a) {
        int dx = a.x - da.x, dy = a.y - da.y;
        for (int i = 0; i < 8; i++) {
            if (DX[i] == dx && DY[i] == dy) return i;
        }
        return 0; // non dovrebbe mai accadere se from/to sono effettivamente vicini
    }

    // =========================================================================
    //  2bis. Buchi interni (aree di sfondo racchiuse dal logo)
    // =========================================================================

    /**
     * Restituisce una maschera booleana contenente solo i pixel di sfondo
     * che appartengono a una componente connessa (8-conn.) che non tocca mai
     * il bordo dell'immagine: sono le aree "racchiuse" dal logo (es. il
     * triangolo dentro la "A"). Il risultato puo' essere passato a
     * {@link #tracciaTuttiIContorni} esattamente come una maschera di primo piano.
     */
    private static boolean[][] estraiBuchiRacchiusi(boolean[][] maschera) {
        int h = maschera.length, w = maschera[0].length;

        // "sfondoInvertito" e' semplicemente la maschera invertita: qui vogliamo
        // ragionare sulle zone di SFONDO, per capire quali sono "isole" chiuse
        boolean[][] sfondoInvertito = new boolean[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                sfondoInvertito[y][x] = !maschera[y][x];
            }
        }

        boolean[][] visitati = new boolean[h][w];
        boolean[][] mascheraBuchi = new boolean[h][w];

        // scansione riga per riga di tutte le componenti connesse di SFONDO
        // (stesso schema della scansione dei contorni, ma qui raccogliamo l'intera
        // componente con un riempimento, non solo il suo contorno)
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (sfondoInvertito[y][x] && !visitati[y][x]) {
                    List<Point> componente = new ArrayList<>();
                    boolean toccaIlBordo = false;
                    Deque<Point> pila = new ArrayDeque<>();
                    pila.push(new Point(x, y));
                    visitati[y][x] = true;
                    while (!pila.isEmpty()) {
                        Point p = pila.pop();
                        componente.add(p);
                        // se anche un solo pixel della componente tocca il bordo
                        // dell'immagine, questa componente è "sfondo esterno" (non un
                        // buco chiuso): la scoperta scatta appena la incontriamo, ma il
                        // riempimento prosegue comunque fino a raccogliere tutta la componente
                        if (p.x == 0 || p.x == w - 1 || p.y == 0 || p.y == h - 1) toccaIlBordo = true;
                        for (int i = 0; i < 8; i++) {
                            int nx = p.x + DX[i], ny = p.y + DY[i];
                            if (dentroILimiti(nx, ny, w, h) && sfondoInvertito[ny][nx] && !visitati[ny][nx]) {
                                visitati[ny][nx] = true;
                                pila.push(new Point(nx, ny));
                            }
                        }
                    }
                    // solo le componenti che NON toccano mai il bordo sono "buchi":
                    // aree di sfondo completamente racchiuse dal logo
                    if (!toccaIlBordo) {
                        for (Point p : componente) mascheraBuchi[p.y][p.x] = true;
                    }
                }
            }
        }
        return mascheraBuchi;
    }

    // =========================================================================
    //  3. Ricampionamento a distanza costante
    // =========================================================================

    /**
     * Riduce un contorno pixel-per-pixel a {@code quanti} punti equidistanti
     * lungo il suo perimetro (parametrizzazione per lunghezza d'arco), così
     * la velocità del punto luminoso è indipendente dalla risoluzione o
     * dalla forma dei tratti (dritti, diagonali, curvi) del contorno originale.
     */
    private static List<Point2D.Double> ricampiona(List<Point> contorno, int quanti) {
        int n = contorno.size();
        // lunghezzaCumulata[i] = lunghezza totale percorsa dal punto 0 fino al punto i
        // (compreso il tratto di chiusura contour[n-1] -> contour[0], che serve
        // perché il contorno è un anello chiuso, non una linea aperta)
        double[] lunghezzaCumulata = new double[n + 1];
        for (int i = 0; i < n; i++) {
            Point a = contorno.get(i);
            Point b = contorno.get((i + 1) % n); // % n fa "avvolgere" l'ultimo punto sul primo
            lunghezzaCumulata[i + 1] = lunghezzaCumulata[i] + a.distance(b);
        }
        double totale = lunghezzaCumulata[n]; // perimetro totale del contorno

        List<Point2D.Double> risultato = new ArrayList<>(quanti);
        int segmento = 0; // indice del segmento del contorno originale attualmente in esame
        for (int k = 0; k < quanti; k++) {
            // distanza (lungo il perimetro) del k-esimo punto equidistante che vogliamo generare
            double obiettivo = totale * k / quanti;
            // avanza "seg" finché il segmento [seg, seg+1] non contiene "target"
            // (lunghezzaCumulata è crescente, quindi questo ciclo scorre in avanti senza mai tornare indietro)
            while (segmento < n && lunghezzaCumulata[segmento + 1] < obiettivo) segmento++;
            Point a = contorno.get(segmento % n);
            Point b = contorno.get((segmento + 1) % n);
            double lunghezzaSegmento = lunghezzaCumulata[segmento + 1] - lunghezzaCumulata[segmento];
            // t = quanto siamo avanti nel segmento corrente, da 0 (in a) a 1 (in b)
            double t = lunghezzaSegmento > 0 ? (obiettivo - lunghezzaCumulata[segmento]) / lunghezzaSegmento : 0;
            // interpolazione lineare tra a e b, per ottenere un punto preciso
            // (non necessariamente su un pixel intero) a distanza "target" dall'inizio
            double x = a.x + (b.x - a.x) * t;
            double y = a.y + (b.y - a.y) * t;
            risultato.add(new Point2D.Double(x, y));
        }
        return risultato;
    }

    // =========================================================================
    //  4. Verso di percorrenza (area di Gauss) + 5. costruzione percorso per corridore
    // =========================================================================

    /**
     * Area con segno del poligono (formula dell'area di Gauss, "shoelace"), calcolata in
     * coordinate immagine (Y verso il basso): positiva => percorso orario
     * sullo schermo, negativa => antiorario.
     */
    private static double areaConSegno(List<Point2D.Double> percorso) {
        // formula dell'area di Gauss, "shoelace": somma di (x_i * y_(i+1) - x_(i+1) * y_i) su tutti
        // i lati del poligono, diviso 2. Il SEGNO del risultato dipende dal verso
        // in cui i vertici sono elencati; qui non ci interessa il valore assoluto
        // (l'area vera e propria), solo il segno.
        double somma = 0;
        int n = percorso.size();
        for (int i = 0; i < n; i++) {
            Point2D.Double a = percorso.get(i);
            Point2D.Double b = percorso.get((i + 1) % n);
            somma += a.x * b.y - b.x * a.y;
        }
        return somma / 2.0;
    }

    /** Indice del punto di {@code percorso} più vicino (distanza euclidea) a {@code obiettivo}. */
    private static int indicePiuVicino(List<Point2D.Double> percorso, Point2D.Double obiettivo) {
        int migliore = 0;
        double distanzaMigliore = Double.MAX_VALUE;
        for (int i = 0; i < percorso.size(); i++) {
            // distanceSq (distanza al quadrato) invece di distance: evita una
            // radice quadrata per ogni punto, inutile visto che ci interessa
            // solo QUALE punto è più vicino, non la distanza esatta
            double d = percorso.get(i).distanceSq(obiettivo);
            if (d < distanzaMigliore) {
                distanzaMigliore = d;
                migliore = i;
            }
        }
        return migliore;
    }

    /** Restituisce una copia di {@code percorso} "ruotata" in modo che l'elemento {@code indiceIniziale} diventi il primo. */
    private static List<Point2D.Double> ruota(List<Point2D.Double> percorso, int indiceIniziale) {
        int n = percorso.size();
        List<Point2D.Double> ruotato = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            // % n fa "avvolgere" l'indice: dopo l'ultimo elemento si riparte dal primo
            ruotato.add(percorso.get((indiceIniziale + i) % n));
        }
        return ruotato;
    }

    /** Il contorno ricampionato di una singola forma connessa (esterna o buco), nei due versi possibili. */
    private static class ContornoForma {
        final List<Point2D.Double> percorsoNaturale;   // ordine così come prodotto dal tracciamento
        final List<Point2D.Double> percorsoInvertito;  // stesso contorno, ordine invertito (verso opposto)
        final boolean naturaleOrario;         // vero se percorsoNaturale è orario sullo schermo

        ContornoForma(List<Point2D.Double> percorsoNaturale, List<Point2D.Double> percorsoInvertito, boolean naturaleOrario) {
            this.percorsoNaturale = percorsoNaturale;
            this.percorsoInvertito = percorsoInvertito;
            this.naturaleOrario = naturaleOrario;
        }
    }

    /**
     * Costruisce il percorso concreto (ordinato) di un corridore: individua a
     * quale forma appartiene il punto di partenza richiesto (quella con il
     * punto di contorno piu' vicino), sceglie in quella forma la lista con
     * il verso richiesto, la ruota per iniziare nel punto agganciato e, se
     * è dato un arrivo, la taglia nel punto più vicino a quello;
     * altrimenti richiude il giro completo.
     */
    private static List<Point2D.Double> costruisciPercorsoCorridore(List<ContornoForma> forme, SpecificaCorridore specifica) {
        // passo 1: tra TUTTE le forme rilevate, trova quella il cui contorno
        // (verso naturale) ha il punto più vicino al punto di partenza richiesto
        int formaMigliore = 0, indiceMigliore = 0;
        double distanzaMigliore = Double.MAX_VALUE;
        for (int s = 0; s < forme.size(); s++) {
            List<Point2D.Double> np = forme.get(s).percorsoNaturale;
            int indice = indicePiuVicino(np, specifica.partenza);
            double d = np.get(indice).distanceSq(specifica.partenza);
            if (d < distanzaMigliore) {
                distanzaMigliore = d;
                formaMigliore = s;
                indiceMigliore = indice;
            }
        }

        // passo 2: dentro quella forma, scegli la lista (naturale o invertita)
        // che corrisponde al verso richiesto dal corridore
        ContornoForma forma = forme.get(formaMigliore);
        boolean vuoleOrario = specifica.verso == Verso.ORARIO;
        boolean usaNaturale = vuoleOrario == forma.naturaleOrario;
        List<Point2D.Double> scelto = usaNaturale ? forma.percorsoNaturale : forma.percorsoInvertito;
        // se si usa la lista invertita, l'indice del punto agganciato va rispecchiato:
        // percorsoInvertito[i] = percorsoNaturale[n-1-i] (Collections.reverse inverte l'ordine
        // ma NON sposta gli elementi in una posizione "a specchio" banale da indovinare
        // senza questa formula)
        int indiceIniziale = usaNaturale ? indiceMigliore : (scelto.size() - 1 - indiceMigliore);
        // passo 3: ruota la lista scelta così che inizi esattamente nel punto agganciato
        List<Point2D.Double> ruotato = ruota(scelto, indiceIniziale);

        if (specifica.arrivo == null) {
            // nessun arrivo richiesto: il corridore fa un giro completo, quindi il
            // percorso si richiude tornando al punto di partenza (rotated.get(0))
            List<Point2D.Double> chiuso = new ArrayList<>(ruotato);
            chiuso.add(ruotato.get(0));
            return chiuso;
        }

        // arrivo richiesto: taglia il percorso al punto (nella lista GIA' ruotata,
        // quindi nel verso corretto) piu' vicino al punto di arrivo richiesto
        int indiceFinale = indicePiuVicino(ruotato, specifica.arrivo);
        if (indiceFinale == 0) indiceFinale = ruotato.size() - 1; // evita un arco degenere di lunghezza zero
        return new ArrayList<>(ruotato.subList(0, indiceFinale + 1));
    }

    // =========================================================================
    //  6. Corridore e sincronizzazione
    // =========================================================================

    /** Stato di un singolo punto luminoso animato lungo il proprio percorso. */
    private static class Corridore {
        final List<Point2D.Double> percorso;      // percorso concreto, gia' orientato e tagliato (vedi costruisciPercorsoCorridore)
        final double puntiPerFotogramma;          // "velocita'": di quanti punti del percorso avanzare ad ogni impulso
        final int fotogrammiRitardoIniziale;         // ritardo di partenza originale, per poterlo ripristinare ad ogni ciclo
        int fotogrammiRitardo;                      // conto alla rovescia corrente: finche' > 0 il corridore e' fermo e invisibile
        double avanzamento = 0;                  // posizione corrente lungo "path", come indice frazionario

        Corridore(List<Point2D.Double> percorso, double puntiPerFotogramma, int fotogrammiRitardo) {
            this.percorso = percorso;
            this.puntiPerFotogramma = puntiPerFotogramma;
            this.fotogrammiRitardoIniziale = fotogrammiRitardo;
            this.fotogrammiRitardo = fotogrammiRitardo;
        }

        /** Vero se il ritardo iniziale e' scaduto e il corridore e' quindi "in pista". */
        boolean isPartito() {
            return fotogrammiRitardo <= 0;
        }
    }

    // =========================================================================
    //  7. Macchina a stati dell'animazione (tracciamento -> dissolvenza -> pausa)
    // =========================================================================

    private enum Fase { TRACCIAMENTO, DISSOLVENZA, PAUSA }

    /** Stato mutabile della macchina a stati: fase corrente + contatori delle fasi temporizzate. */
    private static class StatoAnimazione {
        Fase fase = Fase.TRACCIAMENTO;
        int fotogrammaDissolvenza = 0; // quanti fotogrammi sono trascorsi dall'inizio della fase DISSOLVENZA
        int fotogrammaPausa = 0; // quanti fotogrammi sono trascorsi dall'inizio della fase PAUSA
        boolean finito = false;  // vero quando, senza ripetizione, la pausa finale e' terminata
    }

    /**
     * Avanza di un fotogramma tutti i corridori e la macchina a stati globale.
     * In fase TRACCIAMENTO un corridore che raggiunge la fine del proprio percorso
     * si ferma e aspetta: si passa a DISSOLVENZA solo quando TUTTI hanno
     * finito (vedi punto 6 della documentazione in testa alla classe).
     * Senza ripetizione, alla fine della pausa l'effetto si ferma (vedi {@link #isFinito()}).
     */
    private static void avanza(List<Corridore> corridori, StatoAnimazione stato, int fotogrammiDissolvenza,
                               int fotogrammiPausa, boolean ripeti) {
        switch (stato.fase) {
            case TRACCIAMENTO: {
                // tuttiFiniti diventa false appena un corridore qualsiasi non ha ancora
                // finito (o non e' nemmeno partito): il passaggio alla fase
                // successiva scatta solo quando TUTTI i corridori sono fermi in fondo
                boolean tuttiFiniti = true;
                for (Corridore r : corridori) {
                    if (r.fotogrammiRitardo > 0) {
                        // il corridore e' ancora "in attesa": il suo conto alla rovescia scende di
                        // uno, ma non si muove lungo il percorso (avanzamento resta 0)
                        r.fotogrammiRitardo--;
                        tuttiFiniti = false;
                        continue;
                    }
                    int ultimo = r.percorso.size() - 1;
                    if (r.avanzamento < ultimo) {
                        // avanza di "puntiPerFotogramma" punti, senza mai superare l'ultimo
                        // punto del percorso (Math.min evita un sforamento oltre la fine)
                        r.avanzamento = Math.min(ultimo, r.avanzamento + r.puntiPerFotogramma);
                        tuttiFiniti = false;
                    }
                    // se r.avanzamento e' gia' == ultimo, il corridore resta semplicemente
                    // fermo li' (nessuna azione), aspettando gli altri
                }
                if (tuttiFiniti) {
                    // tutti i corridori hanno finito: si passa alla dissolvenza globale
                    stato.fase = Fase.DISSOLVENZA;
                    stato.fotogrammaDissolvenza = 0;
                }
                break;
            }
            case DISSOLVENZA: {
                stato.fotogrammaDissolvenza++;
                if (stato.fotogrammaDissolvenza >= fotogrammiDissolvenza) {
                    stato.fase = Fase.PAUSA;
                    stato.fotogrammaPausa = 0;
                }
                break;
            }
            case PAUSA: {
                stato.fotogrammaPausa++;
                if (stato.fotogrammaPausa >= fotogrammiPausa && !stato.finito) {
                    if (!ripeti) {
                        // una volta sola: il logo resta acceso e l'effetto e' finito
                        stato.finito = true;
                        break;
                    }
                    // fine della pausa: si ricomincia un nuovo ciclo, riportando ogni
                    // corridore all'inizio del proprio percorso E ripristinando il ritardo
                    // iniziale (altrimenti al secondo giro partirebbero tutti insieme)
                    for (Corridore r : corridori) {
                        r.avanzamento = 0;
                        r.fotogrammiRitardo = r.fotogrammiRitardoIniziale;
                    }
                    stato.fase = Fase.TRACCIAMENTO;
                }
                break;
            }
        }
    }

    /**
     * Calcola l'opacita' (0.0-1.0) a cui disegnare il logo intero, in base
     * alla fase corrente: invisibile durante il tracciamento (la torcia
     * pensa a rivelarlo localmente), in transizione lineare durante la
     * dissolvenza, piena durante la pausa finale.
     */
    private static double opacitaDissolvenza(StatoAnimazione stato, int fotogrammiDissolvenza) {
        switch (stato.fase) {
            case TRACCIAMENTO:
                return 0.0;
            case DISSOLVENZA:
                // rapporto lineare tra 0 e 1 in base a quanti fotogrammi di
                // dissolvenza sono gia' trascorsi
                return Math.min(1.0, (double) stato.fotogrammaDissolvenza / fotogrammiDissolvenza);
            default: // PAUSA
                return 1.0;
        }
    }

    // =========================================================================
    //  Resa grafica
    // =========================================================================

    /**
     * Disegna un fotogramma completo: sfondo, logo (a torcia oppure a piena
     * opacita' con dissolvenza, a seconda della fase/modalita'), scia e
     * punto luminoso di ogni corridore.
     */
    private static void disegnaFotogramma(Graphics2D g2, BufferedImage logo, List<Corridore> corridori, Fase fase,
                                          double opacita, boolean rivelaConTorcia, int raggioTorcia, Color coloreSfondo) {
        int w = logo.getWidth(), h = logo.getHeight();
        // riempie lo sfondo per primo (se ce n'e' uno): qualunque cosa venga
        // disegnata dopo (logo, torcia, scia) vi si sovrappone
        if (coloreSfondo != null) {
            g2.setColor(coloreSfondo);
            g2.fillRect(0, 0, w, h);
        }

        if (rivelaConTorcia && fase == Fase.TRACCIAMENTO) {
            // modalità torcia, e siamo ancora nella fase di tracciamento:
            // costruisce una versione del logo visibile solo vicino ai corridori
            BufferedImage rivelato = rivelaLogoConTorcia(logo, corridori, raggioTorcia);
            g2.drawImage(rivelato, 0, 0, null);
        } else {
            // fuori dal tracciamento (o modalita' "full"): il logo intero viene
            // disegnato con un'opacita' globale, gestita da un AlphaComposite
            // temporaneo (che viene ripristinato subito dopo, per non alterare
            // il composito con cui verranno disegnati scia e punto luminoso)
            Composite compositoPrecedente = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) opacita));
            g2.drawImage(logo, 0, 0, null);
            g2.setComposite(compositoPrecedente);
        }

        // scia e punto luminoso vengono SEMPRE disegnati sopra al logo (rivelato o
        // meno), per ogni corridore che ha già superato il proprio ritardo iniziale
        for (Corridore r : corridori) {
            if (!r.isPartito()) continue; // ritardo non ancora scaduto: resta invisibile
            int finoA = (int) Math.min(r.avanzamento, r.percorso.size() - 1);
            if (finoA > 0) disegnaScia(g2, r.percorso, finoA);
            disegnaTestaCometa(g2, r.percorso, finoA);
        }
    }

    /**
     * Costruisce una copia del logo visibile solo entro un piccolo raggio
     * attorno alla posizione corrente di ciascun corridore ("torcia"), ricreata
     * da zero ogni volta (nessun accumulo: è una torcia che scorre, non una
     * rivelazione progressiva). Tecnica: disegna una maschera con un
     * gradiente radiale morbido per ogni corridore, poi la applica al logo con
     * {@code AlphaComposite.DstIn}, che moltiplica l'alpha di destinazione
     * (il logo) per l'alpha sorgente (la maschera).
     */
    private static BufferedImage rivelaLogoConTorcia(BufferedImage logo, List<Corridore> corridori, int raggio) {
        int w = logo.getWidth(), h = logo.getHeight();

        // --- passo 1: disegna la maschera (un'immagine ARGB inizialmente del
        // tutto trasparente, dato che una nuova BufferedImage parte a zero) con
        // un cerchio sfumato per ogni corridore attivo
        BufferedImage maschera = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gm = maschera.createGraphics();
        gm.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (Corridore r : corridori) {
            if (!r.isPartito()) continue; // ritardo non ancora scaduto: nessuna torcia
            int indice = (int) Math.min(r.avanzamento, r.percorso.size() - 1);
            Point2D.Double p = r.percorso.get(indice); // posizione corrente del corridore
            float raggioF = raggio;
            // gradiente radiale: alpha ALPHA_MASSIMA_TORCIA al centro (non 255
            // pieno: è voluto, così il logo "si intravede" e basta sotto la
            // torcia, non appare a piena opacità), sfuma fino a 0 (trasparente)
            // al bordo del cerchio di raggio "radius"
            RadialGradientPaint bagliore = new RadialGradientPaint(
                    new Point2D.Float((float) p.x, (float) p.y), raggioF,
                    new float[] { 0f, 1f },
                    new Color[] {
                            new Color(255, 255, 255, ALPHA_MASSIMA_TORCIA),
                            new Color(255, 255, 255, 0)
                    });
            gm.setPaint(bagliore);
            gm.fill(new Ellipse2D.Double(p.x - raggioF, p.y - raggioF, 2 * raggioF, 2 * raggioF));
        }
        gm.dispose();

        // --- passo 2: disegna il logo su una seconda immagine di appoggio, poi "ritaglialo"
        // attraverso la maschera appena costruita
        BufferedImage rivelato = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = rivelato.createGraphics();
        gr.drawImage(logo, 0, 0, null); // l'immagine ora contiene il logo, alpha originale
        // DstIn (Porter-Duff "destination in source"): il risultato mantiene il
        // colore della DESTINAZIONE (il logo appena disegnato) ma con alpha =
        // alpha_destinazione * alpha_sorgente (la maschera appena disegnata sopra).
        // Dove la maschera è trasparente (lontano da ogni torcia), il logo sparisce;
        // dove è opaca (vicino a una torcia), il logo resta visibile (fino a
        // ALPHA_MASSIMA_TORCIA di opacità massima).
        gr.setComposite(AlphaComposite.DstIn);
        gr.drawImage(maschera, 0, 0, null);
        gr.dispose();
        return rivelato;
    }

    /**
     * Disegna la scia bianca dal punto 0 fino all'indice {@code finoA} del
     * percorso. L'effetto "bagliore" è ottenuto disegnando la STESSA linea
     * quattro volte, con tratti via via più sottili e più opachi: le
     * passate larghe e trasparenti danno l'alone sfumato, l'ultima
     * (sottile, bianco pieno) dà il "nucleo" luminoso netto al centro.
     * È un trucco economico per un blur senza dover convolvere l'immagine.
     */
    private static void disegnaScia(Graphics2D g2, List<Point2D.Double> percorso, int finoA) {
        // costruisce un'unica polilinea che collega tutti i punti già percorsi
        Path2D.Double scia = new Path2D.Double();
        Point2D.Double p0 = percorso.get(0);
        scia.moveTo(p0.x, p0.y);
        for (int i = 1; i <= finoA; i++) {
            Point2D.Double p = percorso.get(i);
            scia.lineTo(p.x, p.y);
        }

        // passata 1: tratto molto largo, molto trasparente -> alone esterno ampio
        g2.setStroke(new BasicStroke(14f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(255, 255, 255, 35));
        g2.draw(scia);

        // passata 2: più stretto, meno trasparente -> alone intermedio
        g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(255, 255, 255, 80));
        g2.draw(scia);

        // passata 3: ancora più stretto, quasi opaco -> bordo del nucleo
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(255, 255, 255, 210));
        g2.draw(scia);

        // passata 4: linea sottilissima, bianco pieno -> nucleo netto e definito
        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(Color.WHITE);
        g2.draw(scia);
    }

    /**
     * Disegna il punto luminoso ("testa della cometa") nella posizione
     * corrente del percorso: un alone radiale morbido più un piccolo
     * cerchio bianco pieno al centro, per un nucleo netto sopra il bagliore.
     */
    private static void disegnaTestaCometa(Graphics2D g2, List<Point2D.Double> percorso, int indice) {
        Point2D.Double p = percorso.get(Math.min(indice, percorso.size() - 1));
        float raggioTesta = 16f;
        // gradiente radiale a tre tappe: bianco pieno al centro, bianco
        // semitrasparente a metà raggio, trasparente al bordo -> alone morbido
        RadialGradientPaint bagliore = new RadialGradientPaint(
                new Point2D.Float((float) p.x, (float) p.y), raggioTesta,
                new float[] { 0f, 0.4f, 1f },
                new Color[] {
                        new Color(255, 255, 255, 255),
                        new Color(255, 255, 255, 110),
                        new Color(255, 255, 255, 0)
                });
        g2.setPaint(bagliore);
        g2.fill(new Ellipse2D.Double(p.x - raggioTesta, p.y - raggioTesta, 2 * raggioTesta, 2 * raggioTesta));

        // piccolo cerchio bianco pieno sopra l'alone: dà un "nucleo" netto e
        // riconoscibile, invece di un semplice sfumato senza centro definito
        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Double(p.x - 3, p.y - 3, 6, 6));
    }
}
