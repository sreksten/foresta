package com.threeamigos.foresta.ui.sfx;

import com.threeamigos.foresta.ui.sfx.TracciatoreLogo.Verso;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Prova {@link TracciatoreLogo} da riga di comando: legge le opzioni, imposta l'effetto con il suo Costruttore e
 * lo mostra in una finestra Swing a ciclo continuo, oppure ne salva alcuni fotogrammi su PNG. Sta tra i sorgenti
 * dei test perche' serve solo a chi sviluppa: il gioco usa direttamente TracciatoreLogo.
 *
 * Uso, dalla radice del progetto dopo {@code mvn test-compile}:
 *   java -cp target/test-classes:target/classes com.threeamigos.foresta.ui.sfx.TracciatoreLogoDaRigaDiComando logo.png [opzioni]
 *
 * Opzioni (tutte facoltative):
 *   --alpha=N            soglia alpha 0-255 per il criterio di trasparenza
 *                         (predefinito 128; usata solo se --bgcolor non è dato)
 *   --bgcolor=RRGGBB      invece dell'alpha, scarta i pixel simili a questo
 *                         colore di sfondo (scontorno per colore), es. --bgcolor=FFFFFF
 *   --tol=N                tolleranza per --bgcolor, per canale RGB (predefinito 30)
 *   --region=x,y,w,h         limita la ricerca del logo a questo rettangolo
 *                         dell'immagine (vedi il punto 1bis in TracciatoreLogo); il rendering usa
 *                         comunque sempre l'immagine originale per intero
 *   --start=x,y[;x,y...]   uno o più punti di partenza (pixel), uno per corridore
 *   --end=x,y[;x,y...]     punti di arrivo corrispondenti, stesso ordine dei
 *                         --start (se un corridore non ha un end associato, fa
 *                         un giro completo)
 *   --delay=d[;d...]       ritardo iniziale di ciascun corridore (vedi il punto 6bis in TracciatoreLogo),
 *                         stesso ordine dei --start (o delle forme rilevate se
 *                         --start è omesso); un numero puro è in fotogrammi
 *                         (es. "30"), con suffisso "s" è in secondi
 *                         (es. "1.5s"); predefinito 0 (nessun ritardo)
 *   --dir=cw|ccw[;cw|ccw...] verso di percorrenza (vedi il punto 4 in TracciatoreLogo): un solo
 *                         valore si applica a TUTTI i corridori (predefinito cw);
 *                         più valori separati da ";" si applicano uno per
 *                         corridore, stesso ordine dei --start (o delle forme
 *                         rilevate se --start è omesso) — se i valori sono
 *                         meno dei corridori, agli ultimi si applica il primo
 *                         valore della lista
 *   --lap=ms                 durata di riferimento di un giro completo (predefinito 4000)
 *   --sync=independent|matched  velocità dei corridori multipli (predefinito independent,
 *                         vedi il punto 6 in TracciatoreLogo); in entrambi i casi si aspetta sempre
 *                         che TUTTI abbiano finito prima di procedere
 *   --reveal=spotlight|full     modalità di rivelazione del logo (predefinito spotlight,
 *                         vedi il punto 7 in TracciatoreLogo)
 *   --spot=N              raggio in pixel della torcia (predefinito ~10% del lato più corto dell'immagine)
 *   --fade=N              durata in fotogrammi della dissolvenza finale (predefinito 40)
 *   --dump=<dir> [--frames=N] [--scale=N]  anteprima senza schermo su PNG, senza finestra
 *
 * Se non si passa --start, si ottiene il comportamento "semplice": un
 * corridore a giro completo per ogni forma rilevata (contorni esterni e buchi),
 * nel verso richiesto da --dir.
 *
 * Esempi (nei quali "java TracciatoreLogo" sta per il comando completo qui sopra), utili anche come
 * checklist per riprovare le varie funzionalita' una per volta:
 *
 *   // comportamento base: un corridore a giro completo per ogni lettera/buco
 *   // rilevati automaticamente da un PNG con canale alpha (fase 1, 2, 2bis)
 *   java TracciatoreLogo logo.png
 *
 *   // stessa cosa, ma mostrando la finestra Swing a piena visibilita' invece
 *   // che con la torcia (utile per "vedere" subito tutti i contorni trovati)
 *   java TracciatoreLogo logo.png --reveal=full
 *
 *   // logo su sfondo opaco a tinta unita (scontorno per colore) invece che trasparente (fase 1)
 *   java TracciatoreLogo scena.png --bgcolor=DEB887 --tol=15
 *
 *   // limita la ricerca del logo a un dettaglio dentro un disegno piu' grande (fase 1bis)
 *   java TracciatoreLogo scena.png --bgcolor=DEB887 --region=100,200,260,140
 *
 *   // punti di partenza/arrivo espliciti per due corridori, ognuno su un arco parziale (fase 5)
 *   java TracciatoreLogo logo.png --start=10,50;150,10 --end=60,90;160,80
 *
 *   // stessi due corridori, ma con velocità calibrata perché arrivino insieme (punto 6)
 *   java TracciatoreLogo logo.png --start=10,50;150,10 --end=60,90;160,80 --sync=matched
 *
 *   // partenze scaglionate: un corridore ogni mezzo secondo (fase 6bis; attenzione
 *   // a quotare l'argomento nella shell, ";" è un separatore di comandi)
 *   java TracciatoreLogo logo.png "--delay=0;0.5s;1s"
 *
 *   // verso diverso per ciascun corridore (fase 4): "3" orario, "A" antiorario, "M" orario
 *   java TracciatoreLogo logo.png "--dir=cw;ccw;cw"
 *
 *   // raggio della torcia più stretto/largo e dissolvenza finale più lenta (fase 7)
 *   java TracciatoreLogo logo.png --spot=15 --fade=80 --lap=6000
 *
 *   // anteprima senza schermo su PNG (nessuna finestra, utile via SSH senza display):
 *   // salva 8 fotogrammi in ./anteprima, ingranditi 4x
 *   java -Djava.awt.headless=true TracciatoreLogo logo.png --dump=./anteprima --frames=8 --scale=4
 */
public final class TracciatoreLogoDaRigaDiComando {

    private static final int TOLLERANZA_COLORE_PREDEFINITA = 30;    // tolleranza per-canale predefinita per --bgcolor

    private TracciatoreLogoDaRigaDiComando() {
    }

    public static void main(String[] argomenti) {
        // argomenti[0] e' obbligatorio: il percorso del file immagine. Tutto il resto
        // sono opzioni "--chiave=valore" facoltative, interpretate da leggiOpzioni.
        if (argomenti.length < 1) {
            stampaUso();
            System.exit(1);
        }

        Map<String, String> opzioni = leggiOpzioni(argomenti, 1);

        // --- caricamento immagine -------------------------------------------
        BufferedImage logo;
        try {
            logo = ImageIO.read(new File(argomenti[0]));
        } catch (Exception e) {
            System.err.println("Impossibile leggere l'immagine: " + e.getMessage());
            return;
        }
        if (logo == null) {
            // ImageIO.read torna null (invece di lanciare) se il formato non è riconosciuto
            System.err.println("Formato immagine non riconosciuto.");
            return;
        }

        TracciatoreLogo.Costruttore costruttore = TracciatoreLogo.costruttore(logo).ripeti(true);

        // --- scelta del criterio "pixel di sfondo" (fase 1 della pipeline) -----
        if (opzioni.containsKey("bgcolor")) {
            // modalita' scontorno per colore: interpreta "RRGGBB" come intero esadecimale e
            // lo usa direttamente come colore RGB (il costruttore Color(int) legge
            // i bit 16-23/8-15/0-7 come R/G/B, esattamente il formato "RRGGBB")
            Color sfondo = new Color(Integer.parseInt(opzioni.get("bgcolor"), 16));
            int tolleranzaColore = Integer.parseInt(opzioni.getOrDefault("tol", String.valueOf(TOLLERANZA_COLORE_PREDEFINITA)));
            costruttore.sfondoPerColore(sfondo, tolleranzaColore);
            System.out.println("Modalita' sfondo a colore: " + coloreInEsadecimale(sfondo) + " (tolleranza " + tolleranzaColore + ")");
        } else {
            // modalita' predefinita: trasparenza (canale alpha)
            int soglia = Integer.parseInt(opzioni.getOrDefault("alpha", String.valueOf(TracciatoreLogo.SOGLIA_ALPHA_PREDEFINITA)));
            costruttore.sfondoPerAlpha(soglia);
            System.out.println("Modalita' alpha: soglia " + soglia);
        }

        // --- area di scansione opzionale (fase 1bis) ----------------------------
        try {
            costruttore.regione(leggiRegione(opzioni.get("region")));
        } catch (IllegalArgumentException e) {
            System.err.println("Regione non valida: " + e.getMessage());
            return;
        }

        // --- corridori (fase 5) -------------------------------------------------
        // Le liste --start/--end/--delay/--dir sono tutte "parallele": l'i-esimo
        // valore di ciascuna si applica all'i-esimo corridore. Senza --start ci pensa
        // il Costruttore, con un corridore per forma e i ritardi e i versi dati qui.
        List<Point2D.Double> partenze = leggiPunti(opzioni.get("start"));
        List<Point2D.Double> arrivi = leggiPunti(opzioni.get("end"));
        List<Integer> ritardi = leggiRitardi(opzioni.get("delay"));
        List<Verso> versi = leggiVersi(opzioni.get("dir"));
        if (partenze.isEmpty()) {
            costruttore.ritardi(ritardi).versi(versi);
        } else {
            // se non è stato specificato nessun --dir, o è stato dato un solo valore,
            // quel valore (o il verso orario predefinito) fa da "riserva" per i corridori in eccesso
            Verso versoDiRiserva = versi.isEmpty() ? Verso.ORARIO : versi.get(0);
            for (int i = 0; i < partenze.size(); i++) {
                Point2D.Double arrivo = i < arrivi.size() ? arrivi.get(i) : null;
                int ritardo = i < ritardi.size() ? ritardi.get(i) : 0;
                Verso verso = i < versi.size() ? versi.get(i) : versoDiRiserva;
                costruttore.corridore(partenze.get(i), arrivo, verso, ritardo);
            }
        }

        // --- velocita' e resa grafica (fasi 6 e 7) ------------------------------
        costruttore.durataGiroMs(Integer.parseInt(opzioni.getOrDefault("lap", String.valueOf(TracciatoreLogo.DURATA_GIRO_MS_PREDEFINITA))));
        costruttore.velocitaAllineata("matched".equalsIgnoreCase(opzioni.get("sync")));
        costruttore.rivelaConTorcia(!"full".equalsIgnoreCase(opzioni.get("reveal")));
        if (opzioni.containsKey("spot")) {
            costruttore.raggioTorcia(Integer.parseInt(opzioni.get("spot")));
        }
        costruttore.fotogrammiDissolvenza(Integer.parseInt(opzioni.getOrDefault("fade", String.valueOf(TracciatoreLogo.FOTOGRAMMI_DISSOLVENZA_PREDEFINITI))));

        TracciatoreLogo effetto;
        try {
            effetto = costruttore.costruisci();
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println(e.getMessage());
            return;
        }
        // diagnostica a console: utile per calibrare le coordinate di --start/--end
        effetto.getDiagnostica().forEach(System.out::println);

        if (opzioni.containsKey("dump")) {
            // modalità senza schermo: nessuna finestra, salva alcuni fotogrammi come PNG
            // (utile per generare un'anteprima quando non c'è un display disponibile,
            // es. una sessione SSH senza server X).
            int numeroFotogrammi = Integer.parseInt(opzioni.getOrDefault("frames", "8"));
            int scala = Integer.parseInt(opzioni.getOrDefault("scale", "1"));
            try {
                salvaFotogrammi(effetto, opzioni.get("dump"), numeroFotogrammi, scala);
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
            JFrame finestra = new JFrame("Tracciatore del logo");
            PannelloAnimazione pannello = new PannelloAnimazione(effetto);
            finestra.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            finestra.setContentPane(pannello);
            finestra.pack(); // dimensiona la finestra in base alla preferredSize del pannello
            finestra.setLocationRelativeTo(null); // centra la finestra sullo schermo
            finestra.setVisible(true);
            pannello.avvia(); // avvia il Timer dell'animazione solo ora che la finestra è visibile
        });
    }

    /**
     * Disegna {@code numeroFotogrammi} istantanee equidistanti di un intero ciclo
     * dell'animazione (tracciamento + dissolvenza + pausa), senza aprire alcuna
     * finestra, e le salva come PNG in {@code nomeCartella}. Comodo per verificare
     * l'effetto quando non e' disponibile un display (es. server senza schermo).
     */
    private static void salvaFotogrammi(TracciatoreLogo effetto, String nomeCartella, int numeroFotogrammi, int scala)
            throws IOException {
        File cartella = new File(nomeCartella);
        cartella.mkdirs();

        // calcola in anticipo A QUALE fotogramma va salvata ciascuna delle istantanee,
        // distribuendole in modo uniforme lungo tutta la durata stimata di un ciclo
        int fotogrammiTotali = effetto.stimaFotogrammiCiclo();
        int[] fotogrammiIstantanee = new int[numeroFotogrammi];
        for (int i = 0; i < numeroFotogrammi; i++) {
            fotogrammiIstantanee[i] = numeroFotogrammi == 1 ? 0 : (int) Math.round((double) i * fotogrammiTotali / (numeroFotogrammi - 1));
        }

        int w = effetto.getLarghezza() * scala, h = effetto.getAltezza() * scala;
        int indiceIstantanea = 0;
        for (int fotogramma = 0; fotogramma <= fotogrammiTotali && indiceIstantanea < numeroFotogrammi; fotogramma++) {
            if (fotogramma == fotogrammiIstantanee[indiceIstantanea]) {
                // ogni istantanea e' un'immagine ARGB indipendente: la creiamo,
                // disegniamo un fotogramma completo e la salviamo
                BufferedImage istantanea = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = istantanea.createGraphics();
                // scale() applica una trasformazione affine al contesto grafico: tutto
                // cio' che viene disegnato DOPO (incluse le larghezze dei tratti) viene
                // automaticamente ingrandito, senza dover moltiplicare ogni coordinata a mano
                g2.scale(scala, scala);
                effetto.disegna(g2);
                g2.dispose();

                File file = new File(cartella, String.format("fotogramma_%02d.png", indiceIstantanea));
                ImageIO.write(istantanea, "png", file);
                System.out.println("Salvato " + file.getPath() + "  [fase=" + effetto.getNomeFase() + "]");
                indiceIstantanea++;
            }
            effetto.avanza(); // fa avanzare di un fotogramma lo stato dell'animazione
        }
    }

    private static void stampaUso() {
        System.err.println("Uso: java TracciatoreLogoDaRigaDiComando <logo.png> [opzioni]");
        System.err.println("Opzioni: --alpha=N | --bgcolor=RRGGBB --tol=N | --region=x,y,w,h | --start=x,y[;x,y...]");
        System.err.println("         --end=x,y[;x,y...] | --delay=d[;d...] (fotogrammi o \"1.5s\") | --dir=cw|ccw[;cw|ccw...]");
        System.err.println("         --lap=ms | --sync=independent|matched | --reveal=spotlight|full | --spot=N | --fade=N");
        System.err.println("         --dump=<dir> [--frames=N] [--scale=N]  (anteprima headless su PNG, senza finestra)");
    }

    /** Formatta un colore come stringa esadecimale "#RRGGBB", per i messaggi diagnostici. */
    private static String coloreInEsadecimale(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    /**
     * Interpreta gli argomenti della riga di comando dopo il primo (il percorso
     * dell'immagine) come coppie "--chiave=valore". Un argomento senza "=" (es.
     * "--foo") viene registrato con valore "true" (utile per flag booleani, anche
     * se qui non ne usiamo nessuno). Argomenti che non iniziano con "--" vengono
     * ignorati silenziosamente.
     */
    private static Map<String, String> leggiOpzioni(String[] argomenti, int daIndice) {
        // LinkedHashMap mantiene l'ordine di inserimento: non e' strettamente
        // necessario qui, ma rende piu' prevedibile un eventuale debug
        Map<String, String> mappa = new LinkedHashMap<>();
        for (int i = daIndice; i < argomenti.length; i++) {
            String a = argomenti[i];
            if (a.startsWith("--")) {
                int posizioneUguale = a.indexOf('=');
                if (posizioneUguale > 0) mappa.put(a.substring(2, posizioneUguale), a.substring(posizioneUguale + 1));
                else mappa.put(a.substring(2), "true");
            }
        }
        return mappa;
    }

    /** Interpreta "x1,y1;x2,y2;..." in una lista di punti; lista vuota se spec e' null/vuota. */
    private static List<Point2D.Double> leggiPunti(String specifica) {
        List<Point2D.Double> punti = new ArrayList<>();
        if (specifica == null || specifica.isEmpty()) return punti;
        for (String elemento : specifica.split(";")) {
            String[] xy = elemento.split(",");
            punti.add(new Point2D.Double(Double.parseDouble(xy[0].trim()), Double.parseDouble(xy[1].trim())));
        }
        return punti;
    }

    /** Interpreta "d1;d2;..." (vedi {@link #leggiRitardo}) in una lista di ritardi in fotogrammi. */
    private static List<Integer> leggiRitardi(String specifica) {
        List<Integer> ritardi = new ArrayList<>();
        if (specifica == null || specifica.isEmpty()) return ritardi;
        for (String elemento : specifica.split(";")) {
            ritardi.add(leggiRitardo(elemento.trim()));
        }
        return ritardi;
    }

    /** Un numero puro e' in fotogrammi (es. "30"); con suffisso "s" e' in secondi (es. "1.5s"). */
    private static int leggiRitardo(String elemento) {
        if (elemento.isEmpty()) return 0;
        int fotogrammi;
        if (elemento.endsWith("s") || elemento.endsWith("S")) {
            // rimuove la "s" finale, interpreta il resto come numero di secondi
            // (con la virgola/punto decimale) e lo converte in fotogrammi
            double secondi = Double.parseDouble(elemento.substring(0, elemento.length() - 1));
            fotogrammi = (int) Math.round(secondi * 1000.0 / TracciatoreLogo.INTERVALLO_FOTOGRAMMA_MS);
        } else {
            fotogrammi = Integer.parseInt(elemento);
        }
        return Math.max(0, fotogrammi); // un ritardo negativo non ha senso: lo azzeriamo
    }

    /**
     * Interpreta "cw" / "ccw" o una lista "cw;ccw;cw;..." (un verso per corridore).
     * Lista vuota se spec e' null/vuota: in quel caso il chiamante usa il verso
     * orario predefinito per tutti i corridori (vedi Costruttore.versi in TracciatoreLogo).
     */
    private static List<Verso> leggiVersi(String specifica) {
        List<Verso> versi = new ArrayList<>();
        if (specifica == null || specifica.isEmpty()) return versi;
        for (String elemento : specifica.split(";")) {
            versi.add("ccw".equalsIgnoreCase(elemento.trim()) ? Verso.ANTIORARIO : Verso.ORARIO);
        }
        return versi;
    }

    /** Interpreta "--region=x,y,w,h"; restituisce null se non specificata. I limiti li controlla il Costruttore. */
    private static Rectangle leggiRegione(String specifica) {
        if (specifica == null || specifica.isEmpty()) return null;
        String[] parti = specifica.split(",");
        if (parti.length != 4) {
            throw new IllegalArgumentException("atteso il formato x,y,w,h, ricevuto \"" + specifica + "\"");
        }
        return new Rectangle(Integer.parseInt(parti[0].trim()), Integer.parseInt(parti[1].trim()),
                Integer.parseInt(parti[2].trim()), Integer.parseInt(parti[3].trim()));
    }

    // =========================================================================
    //  Finestra Swing
    // =========================================================================

    /**
     * Pannello Swing che mostra l'animazione dal vivo. Un {@link Timer} Swing
     * (NON un java.util.Timer: quello Swing consegna i suoi eventi sull'Event
     * Dispatch Thread, l'unico su cui è sicuro toccare componenti grafici)
     * scandisce i fotogrammi a {@link TracciatoreLogo#INTERVALLO_FOTOGRAMMA_MS} di distanza: ad ogni impulso fa
     * avanzare l'effetto e chiede un ridisegno ({@code repaint()}), che Swing effettuerà chiamando
     * {@link #paintComponent} il prima possibile.
     */
    private static class PannelloAnimazione extends JPanel implements ActionListener {
        private final TracciatoreLogo effetto;

        PannelloAnimazione(TracciatoreLogo effetto) {
            this.effetto = effetto;
            // la finestra (tramite finestra.pack()) si dimensionerà esattamente
            // quanto il logo, senza margini né ridimensionamenti indesiderati
            setPreferredSize(new Dimension(effetto.getLarghezza(), effetto.getAltezza()));
            setBackground(TracciatoreLogo.COLORE_SFONDO_PREDEFINITO);
        }

        /** Avvia il Timer dell'animazione; va chiamato solo dopo che la finestra è visibile. */
        void avvia() {
            new Timer(TracciatoreLogo.INTERVALLO_FOTOGRAMMA_MS, this).start();
        }

        /** Richiamata dal Timer: un impulso dell'animazione. */
        @Override
        public void actionPerformed(ActionEvent e) {
            effetto.avanza();
            repaint(); // richiede un nuovo disegno; Swing lo pianifica sull'EDT
        }

        /** Disegna un singolo fotogramma; chiamato da Swing ogni volta che il pannello va ridisegnato. */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // pulisce lo sfondo secondo setBackground()
            effetto.disegna((Graphics2D) g);
        }
    }
}
