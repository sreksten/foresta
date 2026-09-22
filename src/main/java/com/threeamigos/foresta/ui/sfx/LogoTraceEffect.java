package com.threeamigos.foresta.ui.sfx;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * ============================================================================
 *  PRINCIPIO DI FUNZIONAMENTO
 * ============================================================================
 *
 * L'effetto si ottiene in una pipeline di fasi, eseguite in ordine:
 *
 * 1) MASCHERA (quali pixel sono "logo" e quali sono "sfondo da scartare")
 *    -----------------------------------------------------------------------
 *    Non decidiamo staticamente "alpha basso = sfondo": deleghiamo la
 *    decisione a un {@link BackgroundPredicate}, una semplice funzione
 *    (x, y, colore-ARGB) -> booleano. Questo permette di riusare lo stesso
 *    algoritmo sia su PNG con canale alpha (predicato basato sulla
 *    trasparenza, vedi {@link BackgroundPredicate#byAlpha}) sia su immagini
 *    opache con uno sfondo a tinta unita (chroma-key per colore, vedi
 *    {@link BackgroundPredicate#byColor}), sia su qualunque altro criterio
 *    che si voglia scrivere come lambda. Il risultato e' una matrice
 *    booleana mask[y][x] = true se il pixel appartiene al logo (foreground),
 *    false se va scartato (background).
 *
 * 1bis) AREA DI SCANSIONE (opzionale, --region=x,y,w,h)
 *    -----------------------------------------------------------------------
 *    Se il logo e' solo un dettaglio dentro un disegno molto piu' grande
 *    (es. la scritta "3AM" nera dentro un'illustrazione con sfondo marrone
 *    chiaro), si puo' limitare la ricerca a un rettangolo con
 *    {@code --region=x,y,w,h}: maschera, tracciamento e buchi vengono
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
 * 2) TRACCIAMENTO DEL CONTORNO ESTERNO (Moore-Neighbor boundary tracing)
 *    -----------------------------------------------------------------------
 *    Un logo puo' contenere piu' forme non collegate tra loro (es. lettere
 *    separate come "3", "A", "M"): la maschera viene quindi scandita riga
 *    per riga cercando OGNI pixel di foreground non ancora "consumato". Il
 *    primo trovato di ciascuna zona e' garantito essere un pixel di bordo
 *    (i pixel sopra di lui e a sinistra nella sua riga sono background). Da
 *    li' si "cammina" lungo il perimetro: ad ogni passo si esaminano gli 8
 *    vicini in senso orario partendo dalla direzione opposta a quella di
 *    arrivo (backtrack), e si salta al primo vicino di foreground trovato.
 *    Si ripete finche' non si torna al pixel di partenza: il risultato e'
 *    una sequenza ORDINATA di pixel di bordo che percorre l'intero contorno
 *    di quella forma una sola volta. Dopo aver tracciato una forma, tutti i
 *    suoi pixel (bordo e interno) vengono marcati come "consumati" con un
 *    flood-fill, cosi' la scansione prosegue e trova la forma successiva.
 *
 * 2bis) BUCHI INTERNI (es. il triangolo dentro la "A")
 *    -----------------------------------------------------------------------
 *    Una lettera come la "A" racchiude un'area di sfondo che NON tocca mai
 *    il bordo dell'immagine: e' un "buco". Per tracciarlo si applica un
 *    trucco: si costruisce una seconda maschera booleana contenente SOLO i
 *    pixel di sfondo che appartengono a una componente connessa (8-conn.)
 *    che non tocca mai il bordo dell'immagine (vedi
 *    {@link #extractEnclosedHoles}), e si passa questa maschera allo STESSO
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
 * 4) VERSO DI PERCORRENZA (orario / antiorario), PER-RUNNER
 *    -----------------------------------------------------------------------
 *    L'algoritmo di tracciamento produce un verso "naturale" che dipende
 *    dalla forma (non e' prevedibile a priori, e vale sia per i contorni
 *    esterni sia per i buchi). Per sapere se quel verso naturale sia,
 *    visivamente, orario o antiorario, si calcola l'area con segno del
 *    poligono tracciato (formula del shoelace) sulle coordinate immagine
 *    (asse Y verso il basso): un'area con segno positivo corrisponde a un
 *    percorso orario sullo schermo, una negativa a un percorso antiorario.
 *    Da qui si costruiscono due liste di punti per ciascuna forma: quella
 *    naturale e la sua inversa (stesso contorno, verso opposto), così ogni
 *    singolo runner puo' scegliere il proprio verso (vedi
 *    {@link ShapeContour} e {@code --dir}, che accetta un valore per
 *    runner, es. {@code --dir=cw;ccw;cw}).
 *
 * 5) PUNTI DI PARTENZA / ARRIVO E "RUNNER" MULTIPLI
 *    -----------------------------------------------------------------------
 *    Un {@link RunnerSpec} descrive un singolo punto luminoso indipendente:
 *    un punto di partenza (coordinate pixel), un punto di arrivo opzionale
 *    (se assente, il runner fa un giro completo tornando al punto di
 *    partenza), un verso (vedi punto 4) e un ritardo (vedi punto 6bis). Il
 *    punto di partenza viene "agganciato" prima alla forma (contorno
 *    esterno o buco) il cui contorno ha il punto piu' vicino, poi, dentro
 *    quella forma, al punto del contorno piu' vicino. Se non viene passato
 *    nessun --start esplicito, viene creato automaticamente un runner a
 *    giro completo per OGNI forma rilevata (contorni esterni e buchi
 *    inclusi).
 *
 * 6) SINCRONIZZAZIONE DI PIU' RUNNER (quando --start ne definisce piu' di uno)
 *    -----------------------------------------------------------------------
 *    Con più runner di lunghezza diversa (lettere/buchi diversi, o archi
 *    parziali via --end) c'è un'unica domanda che conta: "quando si
 *    considera completato il giro nel suo insieme?". La risposta è sempre
 *    la stessa in questo programma — SOLO quando OGNI runner ha raggiunto
 *    la fine del proprio percorso (vedi {@link #advance}, fase TRACING: un
 *    runner che finisce prima si ferma lì e aspetta gli altri). Quello che
 *    cambia con {@code --sync=matched} è SOLO la velocita' di ciascun
 *    runner:
 *      - default (indipendente): tutti i runner avanzano alla stessa
 *        velocità "di riferimento", quindi un percorso più corto finisce
 *        prima e resta fermo ad aspettare gli altri;
 *      - matched: la velocità di ogni runner è ricalcolata in base alla
 *        lunghezza del proprio percorso, così tutti raggiungono la fine
 *        esattamente nello stesso fotogramma.
 *
 * 6bis) PARTENZA POSTICIPATA (--delay)
 *    -----------------------------------------------------------------------
 *    Ogni {@link RunnerSpec} può avere un ritardo iniziale, in fotogrammi o
 *    in secondi (vedi {@code --delay}). Finché il ritardo non è scaduto il
 *    runner è semplicemente invisibile: {@link #advance} lo tiene fermo a
 *    inizio percorso e ne decrementa il contatore invece di farlo avanzare,
 *    e {@link #renderFrame}/{@link #buildSpotlightRevealedLogo} saltano del
 *    tutto i runner non ancora partiti (niente torcia, niente scia). Questo
 *    si somma naturalmente alla regola del punto 6: il gruppo non è
 *    "completato" finché anche l'ultimo runner ritardato non ha concluso
 *    il proprio percorso. Il ritardo residuo viene ripristinato al valore
 *    iniziale ad ogni nuovo ciclo (fase HOLD -> TRACING), così la partenza
 *    sfalsata si ripete identica ad ogni giro.
 *
 * 7) RESA GRAFICA: TORCIA (SPOTLIGHT) + FADE-IN FINALE
 *    -----------------------------------------------------------------------
 *    L'intera animazione e' una piccola macchina a stati ({@link Phase}):
 *      TRACING    -> il logo è nascosto; ogni runner porta con se una
 *                    "torcia" (un cerchio con gradiente radiale morbido)
 *                    che rivela il logo SOLO nel proprio raggio d'azione,
 *                    ricreata da zero ad ogni fotogramma (non si accumula:
 *                    appena la luce si allontana da un punto, quel punto
 *                    torna nascosto). Tecnicamente si disegna il logo su un
 *                    buffer offscreen e si applica come "maschera" la torcia
 *                    con {@code AlphaComposite.DstIn}, che moltiplica
 *                    l'alpha del logo per l'alpha della maschera pixel per
 *                    pixel. La scia bianca e il punto luminoso restano
 *                    sempre disegnati sopra, indipendentemente dal logo.
 *      FADING_IN  -> una volta che TUTTI i runner hanno completato (vedi
 *                    punto 6), il logo intero viene dissolto in trasparenza
 *                    da invisibile a completamente visibile in
 *                    {@code fadeFrames} fotogrammi (un semplice
 *                    AlphaComposite globale, non più localizzato).
 *      HOLD       -> il logo resta visibile a piena opacità per una pausa.
 *      (torna a TRACING, azzerando i runner, per un ciclo continuo).
 *    La modalità "torcia" è quella di default; con {@code --reveal=full}
 *    si va al comportamento più semplice (logo sempre visibile).
 *
 * Uso da riga di comando:
 *   javac LogoTraceEffect.java
 *   java LogoTraceEffect logo.png [opzioni]
 *
 * Opzioni (tutte facoltative):
 *   --alpha=N            soglia alpha 0-255 per il criterio di trasparenza
 *                         (default 128; usata solo se --bgcolor non è dato)
 *   --bgcolor=RRGGBB      invece dell'alpha, scarta i pixel simili a questo
 *                         colore di sfondo (chroma-key), es. --bgcolor=FFFFFF
 *   --tol=N                tolleranza per --bgcolor, per canale RGB (default 30)
 *   --region=x,y,w,h         limita la ricerca del logo a questo rettangolo
 *                         dell'immagine (vedi punto 1bis); il rendering usa
 *                         comunque sempre l'immagine originale per intero
 *   --start=x,y[;x,y...]   uno o più punti di partenza (pixel), uno per runner
 *   --end=x,y[;x,y...]     punti di arrivo corrispondenti, stesso ordine dei
 *                         --start (se un runner non ha un end associato, fa
 *                         un giro completo)
 *   --delay=d[;d...]       ritardo iniziale di ciascun runner (vedi punto 6bis),
 *                         stesso ordine dei --start (o delle forme rilevate se
 *                         --start è omesso); un numero puro è in fotogrammi
 *                         (es. "30"), con suffisso "s" è in secondi
 *                         (es. "1.5s"); default 0 (nessun ritardo)
 *   --dir=cw|ccw[;cw|ccw...] verso di percorrenza (vedi punto 4): un solo
 *                         valore si applica a TUTTI i runner (default cw);
 *                         più valori separati da ";" si applicano uno per
 *                         runner, stesso ordine dei --start (o delle forme
 *                         rilevate se --start è omesso) — se i valori sono
 *                         meno dei runner, agli ultimi si applica il primo
 *                         valore della lista
 *   --lap=ms                 durata di riferimento di un giro completo (default 4000)
 *   --sync=independent|matched  velocità dei runner multipli (default independent,
 *                         vedi punto 6); in entrambi i casi si aspetta sempre
 *                         che TUTTI abbiano finito prima di procedere
 *   --reveal=spotlight|full     modalità di rivelazione del logo (default spotlight,
 *                         vedi punto 7)
 *   --spot=N              raggio in pixel della torcia (default ~10% del lato più corto dell'immagine)
 *   --fade=N              durata in fotogrammi del fade-in finale (default 40)
 *   --dump=<dir> [--frames=N] [--scale=N]  anteprima headless su PNG, senza finestra
 *
 * Se non si passa --start, si ottiene il comportamento "semplice": un
 * runner a giro completo per ogni forma rilevata (contorni esterni e buchi),
 * nel verso richiesto da --dir.
 *
 * Esempi (da eseguire dopo "javac LogoTraceEffect.java"), utili anche come
 * checklist per riprovare le varie funzionalita' una per volta:
 *
 *   // comportamento base: un runner a giro completo per ogni lettera/buco
 *   // rilevati automaticamente da un PNG con canale alpha (fase 1, 2, 2bis)
 *   java LogoTraceEffect logo.png
 *
 *   // stessa cosa, ma mostrando la finestra Swing a piena visibilita' invece
 *   // che con la torcia (utile per "vedere" subito tutti i contorni trovati)
 *   java LogoTraceEffect logo.png --reveal=full
 *
 *   // logo su sfondo opaco a tinta unita (chroma-key) invece che trasparente (fase 1)
 *   java LogoTraceEffect scena.png --bgcolor=DEB887 --tol=15
 *
 *   // limita la ricerca del logo a un dettaglio dentro un disegno piu' grande (fase 1bis)
 *   java LogoTraceEffect scena.png --bgcolor=DEB887 --region=100,200,260,140
 *
 *   // punti di partenza/arrivo espliciti per due runner, ognuno su un arco parziale (fase 5)
 *   java LogoTraceEffect logo.png --start=10,50;150,10 --end=60,90;160,80
 *
 *   // stessi due runner, ma con velocità calibrata perché arrivino insieme (punto 6)
 *   java LogoTraceEffect logo.png --start=10,50;150,10 --end=60,90;160,80 --sync=matched
 *
 *   // partenze scaglionate: un runner ogni mezzo secondo (fase 6bis; attenzione
 *   // a quotare l'argomento nella shell, ";" è un separatore di comandi)
 *   java LogoTraceEffect logo.png "--delay=0;0.5s;1s"
 *
 *   // verso diverso per ciascun runner (fase 4): "3" orario, "A" antiorario, "M" orario
 *   java LogoTraceEffect logo.png "--dir=cw;ccw;cw"
 *
 *   // raggio della torcia più stretto/largo e dissolvenza finale più lenta (fase 7)
 *   java LogoTraceEffect logo.png --spot=15 --fade=80 --lap=6000
 *
 *   // anteprima headless su PNG (nessuna finestra, utile via SSH senza display):
 *   // salva 8 fotogrammi in ./anteprima, ingranditi 4x
 *   java -Djava.awt.headless=true LogoTraceEffect logo.png --dump=./anteprima --frames=8 --scale=4
 */
public class LogoTraceEffect {

    // ---- parametri regolabili ----------------------------------------------
    // Costanti globali usate in piu' punti del programma: raggruppate qui in
    // cima per essere facili da ritrovare e modificare senza dover cercare
    // "numeri magici" sparsi nel codice.
    private static final int ALPHA_THRESHOLD_DEFAULT = 128;   // soglia alpha di default (0-255)
    private static final int COLOR_TOLERANCE_DEFAULT = 30;    // tolleranza per-canale di default per --bgcolor
    private static final int LAP_DURATION_MS_DEFAULT = 4000;  // durata di riferimento di un giro completo
    private static final int TRAIL_RESAMPLE_POINTS = 900;     // punti equidistanti in cui viene ricampionato ogni contorno
    private static final int FRAME_DELAY_MS = 16;             // intervallo del Timer Swing: 16ms ~= 60 fotogrammi al secondo
    private static final int HOLD_FRAMES_AT_END = 45;         // quanti fotogrammi restare fermi in fase HOLD (logo completo acceso)
    private static final int SPOTLIGHT_PEEK_ALPHA = 200;      // 0-255: opacità massima del logo sotto la torcia ("si intravede" e basta)
    private static final int FADE_FRAMES_DEFAULT = 40;        // durata di default della dissolvenza finale, in fotogrammi
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 34); // colore di sfondo mostrato quando il logo è nascosto

    // =========================================================================
    //  Criterio "pixel da scartare"
    // =========================================================================

    /**
     * Decide se il pixel (x, y), di colore argb, va considerato sfondo
     * (quindi escluso dal logo e dal tracciamento del contorno).
     */
    @FunctionalInterface
    public interface BackgroundPredicate {
        boolean isBackground(int x, int y, int argb);

        /** Scarta i pixel il cui canale alpha e' minore o uguale alla soglia. */
        static BackgroundPredicate byAlpha(int threshold) {
            // argb e' un intero a 32 bit nel formato 0xAARRGGBB: >>> 24 sposta il
            // canale alpha nei bit bassi (shift SENZA segno, importante perche' il
            // bit piu' alto di un int Java è il segno), & 0xFF isola solo quel byte.
            return (x, y, argb) -> ((argb >>> 24) & 0xFF) <= threshold;
        }

        /**
         * Scarta i pixel il cui colore RGB e' vicino a {@code bg} entro
         * {@code tolerance} per canale (utile per immagini opache con uno
         * sfondo a tinta unita ben definito, es. verde/bianco/nero pieno).
         * Il canale alpha viene ignorato.
         */
        static BackgroundPredicate byColor(Color bg, int tolerance) {
            // i tre componenti del colore di sfondo vengono estratti UNA VOLTA sola,
            // fuori dalla lambda, cosi' non si ricalcolano ad ogni pixel controllato
            int br = bg.getRed(), bgc = bg.getGreen(), bb = bg.getBlue();
            return (x, y, argb) -> {
                // estrazione dei canali R, G, B del pixel corrente dall'intero ARGB
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                // "vicino" = ogni canale entro +/- tolerance dal colore di sfondo
                // (distanza "a cubo", non euclidea: piu' semplice e sufficiente qui)
                return Math.abs(r - br) <= tolerance
                        && Math.abs(g - bgc) <= tolerance
                        && Math.abs(b - bb) <= tolerance;
            };
        }
    }

    public enum Direction { CLOCKWISE, COUNTER_CLOCKWISE }

    /** Descrive un singolo punto luminoso indipendente: partenza, arrivo (opzionale), verso e ritardo. */
    public static class RunnerSpec {
        final Point2D.Double start;
        final Point2D.Double end; // null => giro completo
        final Direction direction;
        final int delayFrames; // ritardo iniziale, in fotogrammi, prima che il runner parta

        public RunnerSpec(Point2D.Double start, Point2D.Double end, Direction direction, int delayFrames) {
            this.start = start;
            this.end = end;
            this.direction = direction;
            this.delayFrames = delayFrames;
        }
    }

    // =========================================================================
    //  main / CLI
    // =========================================================================

    public static void main(String[] args) {
        // args[0] e' obbligatorio: il percorso del file immagine. Tutto il resto
        // sono opzioni "--chiave=valore" facoltative, interpretate da parseOptions.
        if (args.length < 1) {
            printUsage();
            System.exit(1);
        }

        Map<String, String> opts = parseOptions(args, 1);

        // --- caricamento immagine -------------------------------------------
        BufferedImage logo;
        try {
            logo = ImageIO.read(new File(args[0]));
        } catch (Exception e) {
            System.err.println("Impossibile leggere l'immagine: " + e.getMessage());
            return;
        }
        if (logo == null) {
            // ImageIO.read torna null (invece di lanciare) se il formato non è riconosciuto
            System.err.println("Formato immagine non riconosciuto.");
            return;
        }

        // --- scelta del criterio "pixel di sfondo" (fase 1 della pipeline) -----
        BackgroundPredicate bgPredicate;
        if (opts.containsKey("bgcolor")) {
            // modalita' chroma-key: interpreta "RRGGBB" come intero esadecimale e
            // lo usa direttamente come colore RGB (il costruttore Color(int) legge
            // i bit 16-23/8-15/0-7 come R/G/B, esattamente il formato "RRGGBB")
            Color bg = new Color(Integer.parseInt(opts.get("bgcolor"), 16));
            int tol = Integer.parseInt(opts.getOrDefault("tol", String.valueOf(COLOR_TOLERANCE_DEFAULT)));
            bgPredicate = BackgroundPredicate.byColor(bg, tol);
            System.out.println("Modalita' sfondo a colore: " + colorToHex(bg) + " (tolleranza " + tol + ")");
        } else {
            // modalita' di default: trasparenza (canale alpha)
            int threshold = Integer.parseInt(opts.getOrDefault("alpha", String.valueOf(ALPHA_THRESHOLD_DEFAULT)));
            if (!logo.getColorModel().hasAlpha()) {
                System.err.println("Attenzione: l'immagine non ha canale alpha; usa --bgcolor per tracciarla correttamente.");
            }
            bgPredicate = BackgroundPredicate.byAlpha(threshold);
            System.out.println("Modalita' alpha: soglia " + threshold);
        }

        // --- area di scansione opzionale (fase 1bis) ----------------------------
        Rectangle region;
        try {
            region = parseRegion(opts.get("region"), logo.getWidth(), logo.getHeight());
        } catch (IllegalArgumentException e) {
            System.err.println("Regione non valida: " + e.getMessage());
            return;
        }
        // getSubimage() NON copia i pixel: restituisce una "vista" che condivide
        // lo stesso array di dati dell'immagine originale, ma la cui coordinata
        // (0,0) corrisponde a (region.x, region.y) nell'immagine intera. Per
        // questo, dopo aver trovato i contorni su questa vista, dovremo poi
        // sommare (offsetX, offsetY) per riportarli nel sistema di coordinate
        // dell'immagine originale (usata invece per il rendering finale).
        BufferedImage scanImage = region != null ? logo.getSubimage(region.x, region.y, region.width, region.height) : logo;
        int offsetX = region != null ? region.x : 0;
        int offsetY = region != null ? region.y : 0;
        if (region != null) {
            System.out.println("Area di scansione: " + region.width + "x" + region.height
                    + " a partire da (" + region.x + "," + region.y + "); il rendering userà comunque l'immagine intera.");
        }

        // --- fase 1: maschera foreground/background, calcolata SOLO sull'area di scansione ---
        boolean[][] mask = buildMask(scanImage, bgPredicate);

        // --- fase 2 e 2bis: tracciamento dei contorni esterni e dei buchi interni ---
        List<List<Point>> outerContours = traceAllContours(mask);
        boolean[][] holeMask = extractEnclosedHoles(mask);
        List<List<Point>> holeContours = traceAllContours(holeMask);

        // uniamo i due elenchi: da qui in poi un contorno esterno e un buco sono
        // trattati esattamente allo stesso modo (entrambi diventano una "forma")
        List<List<Point>> allContours = new ArrayList<>(outerContours);
        allContours.addAll(holeContours);
        if (allContours.isEmpty()) {
            System.err.println("Nessun contorno trovato: controlla la soglia/il colore di sfondo, l'immagine o la regione.");
            return;
        }

        // --- fase 3 e 4: ricampionamento a distanza costante + verso naturale ---
        List<ShapeContour> shapes = new ArrayList<>();
        for (List<Point> contour : allContours) {
            List<Point2D.Double> natural = resample(contour, TRAIL_RESAMPLE_POINTS);
            // se si stava usando --region, i punti sono ancora in coordinate LOCALI
            // al ritaglio: li riportiamo nel sistema di coordinate dell'immagine intera
            if (offsetX != 0 || offsetY != 0) {
                for (Point2D.Double p : natural) {
                    p.x += offsetX;
                    p.y += offsetY;
                }
            }
            // il verso naturale (orario/antiorario) è invariante per traslazione,
            // quindi non importa se lo calcoliamo prima o dopo aver sommato l'offset
            boolean cw = signedArea(natural) > 0;
            List<Point2D.Double> reversed = new ArrayList<>(natural);
            Collections.reverse(reversed); // stesso contorno, ma percorso in verso opposto
            shapes.add(new ShapeContour(natural, reversed, cw));
        }

        // diagnostica a console: utile per calibrare le coordinate di --start/--end
        // a tentativi, senza dover indovinare "a occhio" dove cade ogni forma
        System.out.println(shapes.size() + " forma/e rilevate (" + outerContours.size()
                + " contorni esterni, " + holeContours.size() + " buchi interni):");
        for (int i = 0; i < shapes.size(); i++) {
            ShapeContour sc = shapes.get(i);
            String kind = i < outerContours.size() ? "esterno" : "buco";
            System.out.println("  forma " + i + " (" + kind + "): verso naturale "
                    + (sc.naturalIsClockwise ? "orario" : "antiorario")
                    + ", partenza naturale " + point(sc.naturalPath.get(0)) + ", " + sc.naturalPath.size() + " punti.");
        }

        // --- fase 5: costruzione delle "specifiche" di ciascun runner -----------
        // Le liste --start/--end/--delay/--dir sono tutte "parallele": l'i-esimo
        // valore di ciascuna si applica all'i-esimo runner. parseDirections gestisce
        // anche il caso "un solo valore per tutti".
        List<Point2D.Double> starts = parsePoints(opts.get("start"));
        List<Point2D.Double> ends = parsePoints(opts.get("end"));
        List<Integer> delays = parseDelays(opts.get("delay"));
        List<Direction> dirs = parseDirections(opts.get("dir"));
        // se non è stato specificato nessun --dir, o è stato dato un solo valore,
        // quel valore (o il default orario) fa da "riserva" per i runner in eccesso
        Direction fallbackDir = dirs.isEmpty() ? Direction.CLOCKWISE : dirs.get(0);

        List<RunnerSpec> specs = new ArrayList<>();
        if (starts.isEmpty()) {
            // nessun punto esplicito: un runner a giro completo per ogni forma rilevata,
            // ciascuno con il proprio ritardo/verso se --delay/--dir ne definiscono uno
            for (int i = 0; i < shapes.size(); i++) {
                int delay = i < delays.size() ? delays.get(i) : 0;
                Direction dir = i < dirs.size() ? dirs.get(i) : fallbackDir;
                specs.add(new RunnerSpec(shapes.get(i).naturalPath.get(0), null, dir, delay));
            }
        } else {
            for (int i = 0; i < starts.size(); i++) {
                Point2D.Double end = i < ends.size() ? ends.get(i) : null;
                int delay = i < delays.size() ? delays.get(i) : 0;
                Direction dir = i < dirs.size() ? dirs.get(i) : fallbackDir;
                specs.add(new RunnerSpec(starts.get(i), end, dir, delay));
            }
        }

        // --- calcolo della velocita' di riferimento e costruzione dei Runner veri e propri ---
        int lapDurationMs = Integer.parseInt(opts.getOrDefault("lap", String.valueOf(LAP_DURATION_MS_DEFAULT)));
        // framesForFullLap = quanti "tick" del Timer servono per completare un giro
        // di riferimento (un giro completo alla velocita' "di riferimento" impiega
        // circa questo numero di fotogrammi)
        int framesForFullLap = Math.max(1, lapDurationMs / FRAME_DELAY_MS);
        boolean matchedSpeed = "matched".equalsIgnoreCase(opts.get("sync"));

        List<Runner> runners = new ArrayList<>();
        for (RunnerSpec spec : specs) {
            // trasforma la "specifica astratta" (punti pixel) in un percorso concreto
            // di punti ricampionati, agganciato alla forma giusta (vedi punto 5)
            List<Point2D.Double> path = buildRunnerPath(shapes, spec);
            double pointsPerFrame = matchedSpeed
                    // velocità "su misura": lunghezza del percorso diviso il numero di
                    // fotogrammi disponibili, così ogni runner arriva in fondo esattamente
                    // allo stesso tick (vedi punto 6 della documentazione)
                    ? (double) path.size() / framesForFullLap
                    // velocità "di riferimento" uguale per tutti: un percorso più corto
                    // del giro di riferimento (TRAIL_RESAMPLE_POINTS) finisce prima e resta
                    // fermo ad aspettare gli altri (vedi advance())
                    : (double) TRAIL_RESAMPLE_POINTS / framesForFullLap;
            runners.add(new Runner(path, pointsPerFrame, spec.delayFrames));
            System.out.println("Runner: partenza agganciata a " + point(path.get(0))
                    + (spec.end != null ? ", arrivo agganciato a " + point(path.get(path.size() - 1)) : ", giro completo")
                    + ", verso " + (spec.direction == Direction.CLOCKWISE ? "orario" : "antiorario")
                    + ", " + path.size() + " punti" + (matchedSpeed ? " (velocita' sincronizzata)" : "")
                    + (spec.delayFrames > 0 ? String.format(", ritardo %d frame (~%.2fs)",
                            spec.delayFrames, spec.delayFrames * FRAME_DELAY_MS / 1000.0) : "") + ".");
        }

        // --- opzioni di resa grafica (fase 7) -----------------------------------
        boolean spotlightReveal = !"full".equalsIgnoreCase(opts.get("reveal"));
        // il raggio della torcia è proporzionato alle dimensioni dell'immagine:
        // un raggio fisso (pensato per loghi grandi) coprirebbe quasi tutto un
        // logo piccolo come "3AM" (100px di altezza), facendo sembrare la torcia
        // "sempre spenta" perché rivela l'intera lettera in un colpo solo.
        int defaultSpotlightRadius = Math.max(6, Math.min(logo.getWidth(), logo.getHeight()) / 10);
        int spotlightRadius = Integer.parseInt(opts.getOrDefault("spot", String.valueOf(defaultSpotlightRadius)));
        int fadeFrames = Integer.parseInt(opts.getOrDefault("fade", String.valueOf(FADE_FRAMES_DEFAULT)));

        if (opts.containsKey("dump")) {
            // modalità headless: nessuna finestra, salva alcuni fotogrammi come PNG
            // (utile per generare un'anteprima quando non c'è un display disponibile,
            // es. una sessione SSH senza server X).
            int frameCount = Integer.parseInt(opts.getOrDefault("frames", "8"));
            int scale = Integer.parseInt(opts.getOrDefault("scale", "1"));
            try {
                dumpFrames(logo, runners, framesForFullLap, fadeFrames, spotlightReveal, spotlightRadius,
                        opts.get("dump"), frameCount, scale);
            } catch (IOException e) {
                System.err.println("Errore salvando i fotogrammi: " + e.getMessage());
            }
            return;
        }

        // --- modalita' normale: apre una finestra Swing con l'animazione dal vivo ---
        // invokeLater è d'obbligo per qualunque cosa tocchi componenti Swing: li
        // fa creare/aggiornare sull'Event Dispatch Thread, il thread unico dedicato
        // al disegno e agli eventi dell'interfaccia grafica.
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Logo Trace Effect");
            AnimationPanel panel = new AnimationPanel(logo, runners, fadeFrames, spotlightReveal, spotlightRadius);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(panel);
            frame.pack(); // dimensiona la finestra in base alla preferredSize del pannello
            frame.setLocationRelativeTo(null); // centra la finestra sullo schermo
            frame.setVisible(true);
            panel.start(); // avvia il Timer dell'animazione solo ora che la finestra è visibile
        });
    }

    /**
     * Renderizza {@code frameCount} istantanee equidistanti di un intero ciclo
     * dell'animazione (tracciamento + fade-in + pausa), senza aprire alcuna
     * finestra, e le salva come PNG in {@code dir}. Comodo per verificare
     * l'effetto quando non e' disponibile un display (es. server headless).
     */
    private static void dumpFrames(BufferedImage logo, List<Runner> runners, int framesForFullLap, int fadeFrames,
                                    boolean spotlightReveal, int spotlightRadius,
                                    String dir, int frameCount, int scale) throws IOException {
        File outDir = new File(dir);
        outDir.mkdirs();

        // Stima (per eccesso) di quanti "tick" dura un intero ciclo dell'animazione,
        // per poter distribuire gli istantanei richiesti su tutto il ciclo:
        // fase TRACING (il runner piu' lento, in genere un giro completo, ci mette
        // circa framesForFullLap tick, PIU' l'eventuale ritardo iniziale del runner
        // piu' "tardivo") + fase FADING_IN (fadeFrames tick) + fase HOLD.
        int maxDelay = 0;
        for (Runner r : runners) maxDelay = Math.max(maxDelay, r.initialDelayFrames);
        int totalTicks = framesForFullLap + maxDelay + 3 + fadeFrames + HOLD_FRAMES_AT_END;

        // calcola in anticipo A QUALE tick va salvato ciascuno degli frameCount
        // istantanei, distribuendoli in modo uniforme lungo tutta la durata stimata
        int[] snapshotTicks = new int[frameCount];
        for (int i = 0; i < frameCount; i++) {
            snapshotTicks[i] = (int) Math.round((double) i * totalTicks / (frameCount - 1));
        }

        int w = logo.getWidth() * scale, h = logo.getHeight() * scale;
        // AnimState e' lo stesso oggetto usato dalla finestra Swing: qui lo si fa
        // avanzare "a mano", un tick alla volta, invece che tramite un Timer reale
        AnimState state = new AnimState();
        int snapIdx = 0;
        for (int tick = 0; tick <= totalTicks && snapIdx < frameCount; tick++) {
            if (tick == snapshotTicks[snapIdx]) {
                // ogni istantanea e' un'immagine ARGB indipendente: la creiamo,
                // disegniamo un fotogramma completo con renderFrame() e la salviamo
                BufferedImage frame = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = frame.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                // scale() applica una trasformazione affine al contesto grafico: tutto
                // cio' che viene disegnato DOPO (incluse le larghezze dei tratti) viene
                // automaticamente ingrandito, senza dover moltiplicare ogni coordinata a mano
                g2.scale(scale, scale);
                double fadeAlpha = !spotlightReveal ? 1.0 : fadeAlphaFor(state, fadeFrames);
                renderFrame(g2, logo, runners, state.phase, fadeAlpha, spotlightReveal, spotlightRadius);
                g2.dispose();

                File f = new File(outDir, String.format("frame_%02d.png", snapIdx));
                ImageIO.write(frame, "png", f);
                System.out.println("Salvato " + f.getPath() + "  [fase=" + state.phase + "]");
                snapIdx++;
            }
            advance(runners, state, fadeFrames); // fa avanzare di un tick lo stato dell'animazione
        }
    }

    private static void printUsage() {
        System.err.println("Uso: java LogoTraceEffect <logo.png> [opzioni]");
        System.err.println("Opzioni: --alpha=N | --bgcolor=RRGGBB --tol=N | --region=x,y,w,h | --start=x,y[;x,y...]");
        System.err.println("         --end=x,y[;x,y...] | --delay=d[;d...] (fotogrammi o \"1.5s\") | --dir=cw|ccw[;cw|ccw...]");
        System.err.println("         --lap=ms | --sync=independent|matched | --reveal=spotlight|full | --spot=N | --fade=N");
        System.err.println("         --dump=<dir> [--frames=N] [--scale=N]  (anteprima headless su PNG, senza finestra)");
    }

    /** Interpreta "--region=x,y,w,h"; restituisce null se non specificata. Lancia se fuori dai limiti dell'immagine. */
    private static Rectangle parseRegion(String spec, int imgW, int imgH) {
        if (spec == null || spec.isEmpty()) return null;
        String[] parts = spec.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("atteso il formato x,y,w,h, ricevuto \"" + spec + "\"");
        }
        int x = Integer.parseInt(parts[0].trim());
        int y = Integer.parseInt(parts[1].trim());
        int w = Integer.parseInt(parts[2].trim());
        int h = Integer.parseInt(parts[3].trim());
        // il rettangolo deve stare interamente dentro l'immagine: niente coordinate
        // negative e niente bordo destro/inferiore che esce dai limiti
        if (x < 0 || y < 0 || w <= 0 || h <= 0 || x + w > imgW || y + h > imgH) {
            throw new IllegalArgumentException("il rettangolo (" + x + "," + y + "," + w + "," + h
                    + ") esce dai limiti dell'immagine (" + imgW + "x" + imgH + ")");
        }
        return new Rectangle(x, y, w, h);
    }

    /** Formatta un punto per i messaggi diagnostici a console, arrotondato al pixel. */
    private static String point(Point2D.Double p) {
        return "(" + Math.round(p.x) + "," + Math.round(p.y) + ")";
    }

    /** Formatta un colore come stringa esadecimale "#RRGGBB", per i messaggi diagnostici. */
    private static String colorToHex(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    /**
     * Interpreta gli argomenti della riga di comando dopo il primo (il percorso
     * dell'immagine) come coppie "--chiave=valore". Un argomento senza "=" (es.
     * "--foo") viene registrato con valore "true" (utile per flag booleani, anche
     * se qui non ne usiamo nessuno). Argomenti che non iniziano con "--" vengono
     * ignorati silenziosamente.
     */
    private static Map<String, String> parseOptions(String[] args, int fromIndex) {
        // LinkedHashMap mantiene l'ordine di inserimento: non e' strettamente
        // necessario qui, ma rende piu' prevedibile un eventuale debug
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = fromIndex; i < args.length; i++) {
            String a = args[i];
            if (a.startsWith("--")) {
                int eq = a.indexOf('=');
                if (eq > 0) map.put(a.substring(2, eq), a.substring(eq + 1));
                else map.put(a.substring(2), "true");
            }
        }
        return map;
    }

    /** Interpreta "x1,y1;x2,y2;..." in una lista di punti; lista vuota se spec e' null/vuota. */
    private static List<Point2D.Double> parsePoints(String spec) {
        List<Point2D.Double> pts = new ArrayList<>();
        if (spec == null || spec.isEmpty()) return pts;
        for (String tok : spec.split(";")) {
            String[] xy = tok.split(",");
            pts.add(new Point2D.Double(Double.parseDouble(xy[0].trim()), Double.parseDouble(xy[1].trim())));
        }
        return pts;
    }

    /** Interpreta "d1;d2;..." (vedi {@link #parseDelayToken}) in una lista di ritardi in fotogrammi. */
    private static List<Integer> parseDelays(String spec) {
        List<Integer> delays = new ArrayList<>();
        if (spec == null || spec.isEmpty()) return delays;
        for (String tok : spec.split(";")) {
            delays.add(parseDelayToken(tok.trim()));
        }
        return delays;
    }

    /** Un numero puro e' in fotogrammi (es. "30"); con suffisso "s" e' in secondi (es. "1.5s"). */
    private static int parseDelayToken(String tok) {
        if (tok.isEmpty()) return 0;
        int frames;
        if (tok.endsWith("s") || tok.endsWith("S")) {
            // rimuove la "s" finale, interpreta il resto come numero di secondi
            // (con la virgola/punto decimale) e lo converte in fotogrammi
            double seconds = Double.parseDouble(tok.substring(0, tok.length() - 1));
            frames = (int) Math.round(seconds * 1000.0 / FRAME_DELAY_MS);
        } else {
            frames = Integer.parseInt(tok);
        }
        return Math.max(0, frames); // un ritardo negativo non ha senso: lo azzeriamo
    }

    /**
     * Interpreta "cw" / "ccw" o una lista "cw;ccw;cw;..." (un verso per runner).
     * Lista vuota se spec e' null/vuota: in quel caso il chiamante usa il verso
     * orario di default per tutti i runner (vedi {@code fallbackDir} in main()).
     */
    private static List<Direction> parseDirections(String spec) {
        List<Direction> dirs = new ArrayList<>();
        if (spec == null || spec.isEmpty()) return dirs;
        for (String tok : spec.split(";")) {
            dirs.add("ccw".equalsIgnoreCase(tok.trim()) ? Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE);
        }
        return dirs;
    }

    // =========================================================================
    //  1. Maschera foreground/background
    // =========================================================================

    /**
     * Scandisce ogni pixel dell'immagine (o del ritaglio, se è stata data una
     * regione) e applica il {@link BackgroundPredicate} per decidere se
     * appartiene al logo (true) o va scartato come sfondo (false).
     */
    private static boolean[][] buildMask(BufferedImage img, BackgroundPredicate bg) {
        int w = img.getWidth(), h = img.getHeight();
        // array indicizzato [riga][colonna] = [y][x], come si fa di solito per le
        // immagini: rende piu' naturale scorrere riga per riga (vedi i cicli sotto)
        boolean[][] mask = new boolean[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = img.getRGB(x, y); // colore del pixel, codificato 0xAARRGGBB
                mask[y][x] = !bg.isBackground(x, y, argb);
            }
        }
        return mask;
    }

    // =========================================================================
    //  2. Moore-Neighbor boundary tracing (contorni esterni)
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
     * maschera (scansione riga per riga; ogni pixel di foreground non ancora
     * consumato avvia il tracciamento di una nuova forma, che viene poi
     * marcata interamente come consumata con un flood-fill). Usata sia per i
     * contorni esterni del logo sia, riapplicata su {@link #extractEnclosedHoles},
     * per i buchi interni: l'algoritmo non fa distinzione tra i due casi.
     */
    private static List<List<Point>> traceAllContours(boolean[][] mask) {
        int h = mask.length, w = mask[0].length;
        // "consumed" tiene traccia di quali pixel appartengono a una forma gia'
        // trovata, per non ri-tracciarla (e non trovarla una seconda volta durante
        // la scansione) quando la scansione incontra un suo altro pixel piu' avanti
        boolean[][] consumed = new boolean[h][w];
        List<List<Point>> allContours = new ArrayList<>();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (mask[y][x] && !consumed[y][x]) {
                    // (x, y) è il primo pixel di foreground di una forma MAI VISTA
                    // prima: è garantito essere un pixel di bordo (vedi commento su
                    // traceContourFrom), quindi possiamo partire da qui per tracciare
                    List<Point> contour = traceContourFrom(mask, new Point(x, y));
                    if (contour.size() >= 4) {
                        // scarta forme degeneri (rumore di pochi pixel isolati)
                        allContours.add(contour);
                    }
                    // marca TUTTA la forma (non solo il bordo) come consumata, cosi'
                    // la scansione prosegue senza ritrovare altri pixel della stessa forma
                    markComponentConsumed(mask, consumed, x, y);
                }
            }
        }
        return allContours;
    }

    /** Flood-fill (8-connesso) che marca come "consumati" tutti i pixel della forma a cui appartiene (sx, sy). */
    private static void markComponentConsumed(boolean[][] mask, boolean[][] consumed, int sx, int sy) {
        int h = mask.length, w = mask[0].length;
        // flood-fill iterativo con uno stack esplicito (invece che ricorsivo):
        // evita il rischio di StackOverflowError su forme molto grandi
        Deque<Point> stack = new ArrayDeque<>();
        stack.push(new Point(sx, sy));
        consumed[sy][sx] = true;
        while (!stack.isEmpty()) {
            Point p = stack.pop();
            // esamina tutti gli 8 vicini: se sono foreground e non ancora visitati,
            // li marca e li aggiunge allo stack per essere esplorati a loro volta
            for (int i = 0; i < 8; i++) {
                int nx = p.x + DX[i], ny = p.y + DY[i];
                if (inBounds(nx, ny, w, h) && mask[ny][nx] && !consumed[ny][nx]) {
                    consumed[ny][nx] = true;
                    stack.push(new Point(nx, ny));
                }
            }
        }
    }

    /**
     * Traccia il contorno della forma a cui appartiene {@code start}, che deve
     * essere gia' un pixel di bordo (garantito quando lo si passa dalla
     * scansione riga-per-riga di {@link #traceAllContours}: essendo il primo
     * pixel di foreground incontrato in quella riga/forma, il pixel sopra di
     * lui e quello a sinistra sono sicuramente sfondo).
     *
     * Algoritmo (Moore-Neighbor boundary tracing): partendo dal pixel corrente
     * e dalla direzione da cui si è arrivati (backtrack), si esaminano gli 8
     * vicini in senso orario A PARTIRE da quella direzione, e si salta al primo
     * vicino di foreground trovato. Questo garantisce di "seguire" sempre il
     * bordo più esterno, senza tagliare angoli né saltare a componenti diverse.
     * Si ripete finché non si ritorna al pixel di partenza.
     */
    private static List<Point> traceContourFrom(boolean[][] mask, Point start) {
        List<Point> contour = new ArrayList<>();
        int h = mask.length, w = mask[0].length;

        Point current = start;
        // il pixel a Ovest di start è garantito sfondo (vedi Javadoc sopra):
        // è il punto di partenza "virtuale" da cui iniziare la ricerca in senso orario
        Point backtrack = new Point(start.x - 1, start.y);
        contour.add(start);

        // salvaguardia anti-loop-infinito: in condizioni normali l'algoritmo
        // termina tornando su "start" molto prima di questo limite, ma un bug o
        // una maschera patologica non devono poter bloccare il programma
        long maxSteps = (long) w * h * 2L;
        long steps = 0;

        while (steps++ < maxSteps) {
            // direzione (0-7) dal pixel corrente verso il pixel di backtrack:
            // la ricerca del prossimo pixel di bordo riparte da QUI, in senso orario
            int startDir = dirOf(current, backtrack);
            Point next = null;
            for (int i = 1; i <= 8; i++) {
                int dir = (startDir + i) % 8; // scorre gli 8 vicini in senso orario
                int nx = current.x + DX[dir];
                int ny = current.y + DY[dir];
                if (inBounds(nx, ny, w, h) && mask[ny][nx]) {
                    // trovato il prossimo pixel di bordo: il NUOVO backtrack è il
                    // vicino appena precedente (l'ultimo che era ancora sfondo),
                    // così al prossimo giro la ricerca riparte dalla direzione giusta
                    next = new Point(nx, ny);
                    int pdir = (dir - 1 + 8) % 8;
                    backtrack = new Point(current.x + DX[pdir], current.y + DY[pdir]);
                    break;
                }
            }
            if (next == null) break; // pixel isolato, nessun vicino di foreground: forma di un solo pixel

            current = next;
            if (current.equals(start)) break; // il giro si è richiuso: contorno completo

            contour.add(current);
        }
        return contour;
    }

    /** Vero se (x, y) cade dentro una griglia w x h (0-indicizzata). */
    private static boolean inBounds(int x, int y, int w, int h) {
        return x >= 0 && x < w && y >= 0 && y < h;
    }

    /** Trova l'indice (0-7, vedi DX/DY) della direzione che porta da "from" a "to" (devono essere vicini adiacenti). */
    private static int dirOf(Point from, Point to) {
        int dx = to.x - from.x, dy = to.y - from.y;
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
     * {@link #traceAllContours} esattamente come una maschera di foreground.
     */
    private static boolean[][] extractEnclosedHoles(boolean[][] mask) {
        int h = mask.length, w = mask[0].length;

        // "background" e' semplicemente la maschera invertita: qui vogliamo
        // ragionare sulle zone di SFONDO, per capire quali sono "isole" chiuse
        boolean[][] background = new boolean[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                background[y][x] = !mask[y][x];
            }
        }

        boolean[][] visited = new boolean[h][w];
        boolean[][] holeMask = new boolean[h][w];

        // scansione riga per riga di tutte le componenti connesse di SFONDO
        // (stesso schema della scansione dei contorni, ma qui raccogliamo l'intera
        // componente con un flood-fill, non solo il suo contorno)
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (background[y][x] && !visited[y][x]) {
                    List<Point> component = new ArrayList<>();
                    boolean touchesBorder = false;
                    Deque<Point> stack = new ArrayDeque<>();
                    stack.push(new Point(x, y));
                    visited[y][x] = true;
                    while (!stack.isEmpty()) {
                        Point p = stack.pop();
                        component.add(p);
                        // se anche un solo pixel della componente tocca il bordo
                        // dell'immagine, questa componente è "sfondo esterno" (non un
                        // buco chiuso): la scoperta scatta appena la incontriamo, ma il
                        // flood-fill prosegue comunque fino a raccogliere tutta la componente
                        if (p.x == 0 || p.x == w - 1 || p.y == 0 || p.y == h - 1) touchesBorder = true;
                        for (int i = 0; i < 8; i++) {
                            int nx = p.x + DX[i], ny = p.y + DY[i];
                            if (inBounds(nx, ny, w, h) && background[ny][nx] && !visited[ny][nx]) {
                                visited[ny][nx] = true;
                                stack.push(new Point(nx, ny));
                            }
                        }
                    }
                    // solo le componenti che NON toccano mai il bordo sono "buchi":
                    // aree di sfondo completamente racchiuse dal logo
                    if (!touchesBorder) {
                        for (Point p : component) holeMask[p.y][p.x] = true;
                    }
                }
            }
        }
        return holeMask;
    }

    // =========================================================================
    //  3. Ricampionamento a distanza costante
    // =========================================================================

    /**
     * Riduce un contorno pixel-per-pixel a {@code count} punti equidistanti
     * lungo il suo perimetro (parametrizzazione per lunghezza d'arco), così
     * la velocità del punto luminoso è indipendente dalla risoluzione o
     * dalla forma dei tratti (dritti, diagonali, curvi) del contorno originale.
     */
    private static List<Point2D.Double> resample(List<Point> contour, int count) {
        int n = contour.size();
        // cumLen[i] = lunghezza totale percorsa dal punto 0 fino al punto i
        // (compreso il tratto di chiusura contour[n-1] -> contour[0], che serve
        // perché il contorno è un anello chiuso, non una linea aperta)
        double[] cumLen = new double[n + 1];
        for (int i = 0; i < n; i++) {
            Point a = contour.get(i);
            Point b = contour.get((i + 1) % n); // % n fa "avvolgere" l'ultimo punto sul primo
            cumLen[i + 1] = cumLen[i] + a.distance(b);
        }
        double total = cumLen[n]; // perimetro totale del contorno

        List<Point2D.Double> result = new ArrayList<>(count);
        int seg = 0; // indice del segmento del contorno originale attualmente in esame
        for (int k = 0; k < count; k++) {
            // distanza (lungo il perimetro) del k-esimo punto equidistante che vogliamo generare
            double target = total * k / count;
            // avanza "seg" finché il segmento [seg, seg+1] non contiene "target"
            // (cumLen è crescente, quindi questo ciclo scorre in avanti senza mai tornare indietro)
            while (seg < n && cumLen[seg + 1] < target) seg++;
            Point a = contour.get(seg % n);
            Point b = contour.get((seg + 1) % n);
            double segLen = cumLen[seg + 1] - cumLen[seg];
            // t = quanto siamo avanti nel segmento corrente, da 0 (in a) a 1 (in b)
            double t = segLen > 0 ? (target - cumLen[seg]) / segLen : 0;
            // interpolazione lineare tra a e b, per ottenere un punto preciso
            // (non necessariamente su un pixel intero) a distanza "target" dall'inizio
            double x = a.x + (b.x - a.x) * t;
            double y = a.y + (b.y - a.y) * t;
            result.add(new Point2D.Double(x, y));
        }
        return result;
    }

    // =========================================================================
    //  4. Verso di percorrenza (shoelace) + 5. costruzione percorso per runner
    // =========================================================================

    /**
     * Area con segno del poligono (formula del shoelace), calcolata in
     * coordinate immagine (Y verso il basso): positiva => percorso orario
     * sullo schermo, negativa => antiorario.
     */
    private static double signedArea(List<Point2D.Double> path) {
        // formula del shoelace: somma di (x_i * y_(i+1) - x_(i+1) * y_i) su tutti
        // i lati del poligono, diviso 2. Il SEGNO del risultato dipende dal verso
        // in cui i vertici sono elencati; qui non ci interessa il valore assoluto
        // (l'area vera e propria), solo il segno.
        double sum = 0;
        int n = path.size();
        for (int i = 0; i < n; i++) {
            Point2D.Double a = path.get(i);
            Point2D.Double b = path.get((i + 1) % n);
            sum += a.x * b.y - b.x * a.y;
        }
        return sum / 2.0;
    }

    /** Indice del punto di {@code path} più vicino (distanza euclidea) a {@code target}. */
    private static int nearestIndex(List<Point2D.Double> path, Point2D.Double target) {
        int best = 0;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < path.size(); i++) {
            // distanceSq (distanza al quadrato) invece di distance: evita una
            // radice quadrata per ogni punto, inutile visto che ci interessa
            // solo QUALE punto è più vicino, non la distanza esatta
            double d = path.get(i).distanceSq(target);
            if (d < bestDist) {
                bestDist = d;
                best = i;
            }
        }
        return best;
    }

    /** Restituisce una copia di {@code path} "ruotata" in modo che l'elemento {@code startIdx} diventi il primo. */
    private static List<Point2D.Double> rotate(List<Point2D.Double> path, int startIdx) {
        int n = path.size();
        List<Point2D.Double> rotated = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            // % n fa "avvolgere" l'indice: dopo l'ultimo elemento si riparte dal primo
            rotated.add(path.get((startIdx + i) % n));
        }
        return rotated;
    }

    /** Il contorno ricampionato di una singola forma connessa (esterna o buco), nei due versi possibili. */
    private static class ShapeContour {
        final List<Point2D.Double> naturalPath;   // ordine così come prodotto dal tracciamento
        final List<Point2D.Double> reversedPath;  // stesso contorno, ordine invertito (verso opposto)
        final boolean naturalIsClockwise;         // vero se naturalPath è orario sullo schermo

        ShapeContour(List<Point2D.Double> naturalPath, List<Point2D.Double> reversedPath, boolean naturalIsClockwise) {
            this.naturalPath = naturalPath;
            this.reversedPath = reversedPath;
            this.naturalIsClockwise = naturalIsClockwise;
        }
    }

    /**
     * Costruisce il percorso concreto (ordinato) di un runner: individua a
     * quale forma appartiene il punto di partenza richiesto (quella con il
     * punto di contorno piu' vicino), sceglie in quella forma la lista con
     * il verso richiesto, la ruota per iniziare nel punto agganciato e, se
     * è dato un arrivo, la taglia nel punto più vicino a quello;
     * altrimenti richiude il giro completo.
     */
    private static List<Point2D.Double> buildRunnerPath(List<ShapeContour> shapes, RunnerSpec spec) {
        // passo 1: tra TUTTE le forme rilevate, trova quella il cui contorno
        // (verso naturale) ha il punto più vicino al punto di partenza richiesto
        int bestShape = 0, bestIdx = 0;
        double bestDist = Double.MAX_VALUE;
        for (int s = 0; s < shapes.size(); s++) {
            List<Point2D.Double> np = shapes.get(s).naturalPath;
            int idx = nearestIndex(np, spec.start);
            double d = np.get(idx).distanceSq(spec.start);
            if (d < bestDist) {
                bestDist = d;
                bestShape = s;
                bestIdx = idx;
            }
        }

        // passo 2: dentro quella forma, scegli la lista (naturale o invertita)
        // che corrisponde al verso richiesto dal runner
        ShapeContour sc = shapes.get(bestShape);
        boolean wantClockwise = spec.direction == Direction.CLOCKWISE;
        boolean useNatural = wantClockwise == sc.naturalIsClockwise;
        List<Point2D.Double> chosen = useNatural ? sc.naturalPath : sc.reversedPath;
        // se si usa la lista invertita, l'indice del punto agganciato va rispecchiato:
        // reversedPath[i] = naturalPath[n-1-i] (Collections.reverse inverte l'ordine
        // ma NON sposta gli elementi in una posizione "a specchio" banale da indovinare
        // senza questa formula)
        int startIdx = useNatural ? bestIdx : (chosen.size() - 1 - bestIdx);
        // passo 3: ruota la lista scelta così che inizi esattamente nel punto agganciato
        List<Point2D.Double> rotated = rotate(chosen, startIdx);

        if (spec.end == null) {
            // nessun arrivo richiesto: il runner fa un giro completo, quindi il
            // percorso si richiude tornando al punto di partenza (rotated.get(0))
            List<Point2D.Double> closed = new ArrayList<>(rotated);
            closed.add(rotated.get(0));
            return closed;
        }

        // arrivo richiesto: taglia il percorso al punto (nella lista GIA' ruotata,
        // quindi nel verso corretto) piu' vicino al punto di arrivo richiesto
        int endIdx = nearestIndex(rotated, spec.end);
        if (endIdx == 0) endIdx = rotated.size() - 1; // evita un arco degenere di lunghezza zero
        return new ArrayList<>(rotated.subList(0, endIdx + 1));
    }

    // =========================================================================
    //  6. Runner e sincronizzazione
    // =========================================================================

    /** Stato di un singolo punto luminoso animato lungo il proprio percorso. */
    private static class Runner {
        final List<Point2D.Double> path;      // percorso concreto, gia' orientato e tagliato (vedi buildRunnerPath)
        final double pointsPerFrame;          // "velocita'": di quanti punti del percorso avanzare ad ogni tick
        final int initialDelayFrames;         // ritardo di partenza originale, per poterlo ripristinare ad ogni ciclo
        int delayFrames;                      // countdown corrente: finche' > 0 il runner e' fermo e invisibile
        double progress = 0;                  // posizione corrente lungo "path", come indice frazionario

        Runner(List<Point2D.Double> path, double pointsPerFrame, int delayFrames) {
            this.path = path;
            this.pointsPerFrame = pointsPerFrame;
            this.initialDelayFrames = delayFrames;
            this.delayFrames = delayFrames;
        }

        /** Vero se il ritardo iniziale e' scaduto e il runner e' quindi "in pista". */
        boolean hasStarted() {
            return delayFrames <= 0;
        }
    }

    // =========================================================================
    //  7. Macchina a stati dell'animazione (tracciamento -> fade-in -> pausa)
    // =========================================================================

    private enum Phase { TRACING, FADING_IN, HOLD }

    /** Stato mutabile della macchina a stati: fase corrente + contatori delle fasi temporizzate. */
    private static class AnimState {
        Phase phase = Phase.TRACING;
        int fadeFrame = 0; // quanti fotogrammi sono trascorsi dall'inizio della fase FADING_IN
        int holdFrame = 0; // quanti fotogrammi sono trascorsi dall'inizio della fase HOLD
    }

    /**
     * Avanza di un fotogramma tutti i runner e la macchina a stati globale.
     * In fase TRACING un runner che raggiunge la fine del proprio percorso
     * si ferma e aspetta: si passa a FADING_IN solo quando TUTTI hanno
     * finito (vedi punto 6 della documentazione in testa alla classe).
     * Chiamata sia dal Timer Swing (un tick reale ogni FRAME_DELAY_MS ms)
     * sia da {@link #dumpFrames} (tanti tick "simulati" quanti servono).
     */
    private static void advance(List<Runner> runners, AnimState state, int fadeFrames) {
        switch (state.phase) {
            case TRACING: {
                // allDone diventa false appena un runner qualsiasi non ha ancora
                // finito (o non e' nemmeno partito): il passaggio alla fase
                // successiva scatta solo quando TUTTI i runner sono fermi in fondo
                boolean allDone = true;
                for (Runner r : runners) {
                    if (r.delayFrames > 0) {
                        // il runner e' ancora "in attesa": il suo countdown scende di
                        // uno, ma non si muove lungo il percorso (progress resta 0)
                        r.delayFrames--;
                        allDone = false;
                        continue;
                    }
                    int last = r.path.size() - 1;
                    if (r.progress < last) {
                        // avanza di "pointsPerFrame" punti, senza mai superare l'ultimo
                        // punto del percorso (Math.min evita un overshoot oltre la fine)
                        r.progress = Math.min(last, r.progress + r.pointsPerFrame);
                        allDone = false;
                    }
                    // se r.progress e' gia' == last, il runner resta semplicemente
                    // fermo li' (nessuna azione), aspettando gli altri
                }
                if (allDone) {
                    // tutti i runner hanno finito: si passa alla dissolvenza globale
                    state.phase = Phase.FADING_IN;
                    state.fadeFrame = 0;
                }
                break;
            }
            case FADING_IN: {
                state.fadeFrame++;
                if (state.fadeFrame >= fadeFrames) {
                    state.phase = Phase.HOLD;
                    state.holdFrame = 0;
                }
                break;
            }
            case HOLD: {
                state.holdFrame++;
                if (state.holdFrame >= HOLD_FRAMES_AT_END) {
                    // fine della pausa: si ricomincia un nuovo ciclo, riportando ogni
                    // runner all'inizio del proprio percorso E ripristinando il ritardo
                    // iniziale (altrimenti al secondo giro partirebbero tutti insieme)
                    for (Runner r : runners) {
                        r.progress = 0;
                        r.delayFrames = r.initialDelayFrames;
                    }
                    state.phase = Phase.TRACING;
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
    private static double fadeAlphaFor(AnimState state, int fadeFrames) {
        switch (state.phase) {
            case TRACING:
                return 0.0;
            case FADING_IN:
                // rapporto lineare tra 0 e 1 in base a quanti fotogrammi di
                // dissolvenza sono gia' trascorsi
                return Math.min(1.0, (double) state.fadeFrame / fadeFrames);
            default: // HOLD
                return 1.0;
        }
    }

    // =========================================================================
    //  Rendering condiviso (finestra Swing e dump headless)
    // =========================================================================

    /**
     * Disegna un fotogramma completo: sfondo, logo (a torcia oppure a piena
     * opacita' con dissolvenza, a seconda della fase/modalita'), scia e
     * punto luminoso di ogni runner. Chiamata sia da
     * {@link AnimationPanel#paintComponent} sia da {@link #dumpFrames}, cosi'
     * la logica di disegno e' scritta e mantenuta in un solo posto.
     */
    private static void renderFrame(Graphics2D g2, BufferedImage logo, List<Runner> runners,
                                     Phase phase, double fadeAlpha, boolean spotlightReveal, int spotlightRadius) {
        int w = logo.getWidth(), h = logo.getHeight();
        // riempie sempre lo sfondo per primo: qualunque cosa venga disegnata dopo
        // (logo, torcia, scia) vi si sovrappone
        g2.setColor(BACKGROUND_COLOR);
        g2.fillRect(0, 0, w, h);

        if (spotlightReveal && phase == Phase.TRACING) {
            // modalità torcia, e siamo ancora nella fase di tracciamento:
            // costruisce una versione del logo visibile solo vicino ai runner
            BufferedImage revealed = buildSpotlightRevealedLogo(logo, runners, spotlightRadius);
            g2.drawImage(revealed, 0, 0, null);
        } else {
            // fuori dal tracciamento (o modalita' "full"): il logo intero viene
            // disegnato con un'opacita' globale, gestita da un AlphaComposite
            // temporaneo (che viene ripristinato subito dopo, per non alterare
            // il composito con cui verranno disegnati scia e punto luminoso)
            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) fadeAlpha));
            g2.drawImage(logo, 0, 0, null);
            g2.setComposite(old);
        }

        // scia e punto luminoso vengono SEMPRE disegnati sopra al logo (rivelato o
        // meno), per ogni runner che ha già superato il proprio ritardo iniziale
        for (Runner r : runners) {
            if (!r.hasStarted()) continue; // ritardo non ancora scaduto: resta invisibile
            int upTo = (int) Math.min(r.progress, r.path.size() - 1);
            if (upTo > 0) drawGlowTrail(g2, r.path, upTo);
            drawCometHead(g2, r.path, upTo);
        }
    }

    /**
     * Costruisce una copia del logo visibile solo entro un piccolo raggio
     * attorno alla posizione corrente di ciascun runner ("torcia"), ricreata
     * da zero ogni volta (nessun accumulo: è una torcia che scorre, non una
     * rivelazione progressiva). Tecnica: disegna una maschera con un
     * gradiente radiale morbido per ogni runner, poi la applica al logo con
     * {@code AlphaComposite.DstIn}, che moltiplica l'alpha di destinazione
     * (il logo) per l'alpha sorgente (la maschera).
     */
    private static BufferedImage buildSpotlightRevealedLogo(BufferedImage logo, List<Runner> runners, int radius) {
        int w = logo.getWidth(), h = logo.getHeight();

        // --- passo 1: disegna la maschera (un'immagine ARGB inizialmente del
        // tutto trasparente, dato che una nuova BufferedImage parte a zero) con
        // un cerchio sfumato per ogni runner attivo
        BufferedImage mask = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gm = mask.createGraphics();
        gm.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (Runner r : runners) {
            if (!r.hasStarted()) continue; // ritardo non ancora scaduto: nessuna torcia
            int idx = (int) Math.min(r.progress, r.path.size() - 1);
            Point2D.Double p = r.path.get(idx); // posizione corrente del runner
            float rr = radius;
            // gradiente radiale: alpha SPOTLIGHT_PEEK_ALPHA al centro (non 255
            // pieno: è voluto, così il logo "si intravede" e basta sotto la
            // torcia, non appare a piena opacità), sfuma fino a 0 (trasparente)
            // al bordo del cerchio di raggio "radius"
            RadialGradientPaint glow = new RadialGradientPaint(
                    new Point2D.Float((float) p.x, (float) p.y), rr,
                    new float[] { 0f, 1f },
                    new Color[] {
                            new Color(255, 255, 255, SPOTLIGHT_PEEK_ALPHA),
                            new Color(255, 255, 255, 0)
                    });
            gm.setPaint(glow);
            gm.fill(new Ellipse2D.Double(p.x - rr, p.y - rr, 2 * rr, 2 * rr));
        }
        gm.dispose();

        // --- passo 2: disegna il logo su un secondo buffer, poi "ritaglialo"
        // attraverso la maschera appena costruita
        BufferedImage revealed = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = revealed.createGraphics();
        gr.drawImage(logo, 0, 0, null); // il buffer ora contiene il logo, alpha originale
        // DstIn (Porter-Duff "destination in source"): il risultato mantiene il
        // colore della DESTINAZIONE (il logo appena disegnato) ma con alpha =
        // alpha_destinazione * alpha_sorgente (la maschera appena disegnata sopra).
        // Dove la maschera è trasparente (lontano da ogni torcia), il logo sparisce;
        // dove è opaca (vicino a una torcia), il logo resta visibile (fino a
        // SPOTLIGHT_PEEK_ALPHA di opacità massima).
        gr.setComposite(AlphaComposite.DstIn);
        gr.drawImage(mask, 0, 0, null);
        gr.dispose();
        return revealed;
    }

    /**
     * Disegna la scia bianca dal punto 0 fino all'indice {@code upTo} del
     * percorso. L'effetto "bagliore" è ottenuto disegnando la STESSA linea
     * quattro volte, con tratti via via più sottili e più opachi: le
     * passate larghe e trasparenti danno l'alone sfumato, l'ultima
     * (sottile, bianco pieno) dà il "nucleo" luminoso netto al centro.
     * È un trucco economico per un blur senza dover convolvere l'immagine.
     */
    private static void drawGlowTrail(Graphics2D g2, List<Point2D.Double> path, int upTo) {
        // costruisce un'unica polilinea che collega tutti i punti già percorsi
        Path2D.Double trail = new Path2D.Double();
        Point2D.Double p0 = path.get(0);
        trail.moveTo(p0.x, p0.y);
        for (int i = 1; i <= upTo; i++) {
            Point2D.Double p = path.get(i);
            trail.lineTo(p.x, p.y);
        }

        // passata 1: tratto molto largo, molto trasparente -> alone esterno ampio
        g2.setStroke(new BasicStroke(14f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(255, 255, 255, 35));
        g2.draw(trail);

        // passata 2: più stretto, meno trasparente -> alone intermedio
        g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(255, 255, 255, 80));
        g2.draw(trail);

        // passata 3: ancora più stretto, quasi opaco -> bordo del nucleo
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(255, 255, 255, 210));
        g2.draw(trail);

        // passata 4: linea sottilissima, bianco pieno -> nucleo netto e definito
        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(Color.WHITE);
        g2.draw(trail);
    }

    /**
     * Disegna il punto luminoso ("testa della cometa") nella posizione
     * corrente del percorso: un alone radiale morbido più un piccolo
     * cerchio bianco pieno al centro, per un nucleo netto sopra il bagliore.
     */
    private static void drawCometHead(Graphics2D g2, List<Point2D.Double> path, int index) {
        Point2D.Double p = path.get(Math.min(index, path.size() - 1));
        float r = 16f;
        // gradiente radiale a tre tappe: bianco pieno al centro, bianco
        // semitrasparente a metà raggio, trasparente al bordo -> alone morbido
        RadialGradientPaint glow = new RadialGradientPaint(
                new Point2D.Float((float) p.x, (float) p.y), r,
                new float[] { 0f, 0.4f, 1f },
                new Color[] {
                        new Color(255, 255, 255, 255),
                        new Color(255, 255, 255, 110),
                        new Color(255, 255, 255, 0)
                });
        g2.setPaint(glow);
        g2.fill(new Ellipse2D.Double(p.x - r, p.y - r, 2 * r, 2 * r));

        // piccolo cerchio bianco pieno sopra l'alone: dà un "nucleo" netto e
        // riconoscibile, invece di un semplice sfumato senza centro definito
        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Double(p.x - 3, p.y - 3, 6, 6));
    }

    // =========================================================================
    //  Finestra Swing
    // =========================================================================

    /**
     * Pannello Swing che mostra l'animazione dal vivo. Un {@link Timer} Swing
     * (NON un java.util.Timer: quello Swing consegna i suoi eventi sull'Event
     * Dispatch Thread, l'unico su cui è sicuro toccare componenti grafici)
     * scandisce i fotogrammi a FRAME_DELAY_MS di distanza: ad ogni tick fa
     * avanzare lo stato dell'animazione ({@link #advance}) e chiede un
     * ridisegno ({@code repaint()}), che Swing effettuerà chiamando
     * {@link #paintComponent} il prima possibile.
     */
    private static class AnimationPanel extends JPanel implements ActionListener {
        private final BufferedImage logo;
        private final List<Runner> runners;
        private final int fadeFrames;
        private final boolean spotlightReveal;
        private final int spotlightRadius;
        private final AnimState state = new AnimState(); // stato della macchina a stati (vedi advance())
        private Timer timer;

        AnimationPanel(BufferedImage logo, List<Runner> runners, int fadeFrames,
                        boolean spotlightReveal, int spotlightRadius) {
            this.logo = logo;
            this.runners = runners;
            this.fadeFrames = fadeFrames;
            this.spotlightReveal = spotlightReveal;
            this.spotlightRadius = spotlightRadius;
            // la finestra (tramite frame.pack()) si dimensionerà esattamente
            // quanto il logo, senza margini né ridimensionamenti indesiderati
            setPreferredSize(new Dimension(logo.getWidth(), logo.getHeight()));
            setBackground(BACKGROUND_COLOR);
        }

        /** Avvia il Timer dell'animazione; va chiamato solo dopo che la finestra è visibile. */
        void start() {
            timer = new Timer(FRAME_DELAY_MS, this);
            timer.start();
        }

        /** Callback del Timer: un "tick" dell'animazione, invocato ogni FRAME_DELAY_MS millisecondi. */
        @Override
        public void actionPerformed(ActionEvent e) {
            advance(runners, state, fadeFrames);
            repaint(); // richiede un nuovo disegno; Swing lo pianifica sull'EDT
        }

        /** Disegna un singolo fotogramma; chiamato da Swing ogni volta che il pannello va ridisegnato. */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // pulisce lo sfondo secondo setBackground()
            // g.create() clona il contesto grafico: le modifiche fatte qui sotto
            // (rendering hints, stroke, colore...) non "sporcano" l'oggetto Graphics
            // originale ricevuto da Swing, buona norma quando si fanno molte modifiche
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            double fadeAlpha = !spotlightReveal ? 1.0 : fadeAlphaFor(state, fadeFrames);
            renderFrame(g2, logo, runners, state.phase, fadeAlpha, spotlightReveal, spotlightRadius);
            g2.dispose(); // libera le risorse del contesto clonato
        }
    }
}
