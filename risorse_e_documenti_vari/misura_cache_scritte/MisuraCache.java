import com.threeamigos.foresta.ui.testo.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Misura quanto lavoro fa una cache di scritte a 60 fotogrammi al secondo con tre tipi di riferimento.
 * Uso: MisuraCache <weak|soft|strong> <MB di spazzatura al secondo> <secondi> <scritte per fotogramma>
 */
public class MisuraCache {

    static final DoomdarkFont FONT = DoomdarkFontMedium.getInstance();
    static Map<String, Object> cache = new ConcurrentHashMap<>();
    static String tipo;
    static volatile Object SINK;
    static long ricostruzioni = 0;
    static long nanoRicostruzione = 0;

    static Image get(String testo, BufferedImage tela) {
        Object o = cache.get(testo);
        Image img = null;
        if (o instanceof Reference) {
            img = (Image) ((Reference<?>) o).get();
        } else if (o != null) {
            img = (Image) o;
        }
        if (img == null) {
            long t0 = System.nanoTime();
            img = DoomdarkTextProducer.getImage(testo, FONT, DoomdarkColorModel.Color.WHITE);
            // il costo vero sta nella prima conversione, quando si disegna
            Graphics2D g = tela.createGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();
            nanoRicostruzione += System.nanoTime() - t0;
            ricostruzioni++;
            switch (tipo) {
                case "weak": cache.put(testo, new WeakReference<>(img)); break;
                case "soft": cache.put(testo, new SoftReference<>(img)); break;
                case "none": break;
                default: cache.put(testo, img);
            }
        }
        return img;
    }

    public static void main(String[] args) throws Exception {
        tipo = args[0];
        double mbSecondo = Double.parseDouble(args[1]);
        int secondi = Integer.parseInt(args[2]);
        int scritte = Integer.parseInt(args[3]);
        int fps = 60;
        // insieme di scritte che cambiano poco, come in un riquadro: una parte fissa e una parte con numeri che cambiano
        List<String> fisse = new ArrayList<>();
        for (int i = 0; i < scritte; i++) {
            fisse.add(i % 4 == 0 ? "Salute " + i : "Voce di prova numero " + i);
        }
        BufferedImage tela = new BufferedImage(1000, 800, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gt = tela.createGraphics();
        int byteFotogramma = (int) (mbSecondo * 1024 * 1024 / fps);
        Object sink = null;
        long gc0 = gcCount();
        long fine = System.nanoTime() + secondi * 1_000_000_000L;
        long prossimo = System.nanoTime();
        long fotogrammi = 0;
        long nanoDisegno = 0;
        while (System.nanoTime() < fine) {
            long t0 = System.nanoTime();
            for (int i = 0; i < fisse.size(); i++) {
                // ogni tanto un testo che varia (un contatore), come "120/300"
                String testo = fisse.get(i);
                if (i % 10 == 0) {
                    testo = testo + " " + (fotogrammi / 120) % 50;
                }
                Image img = get(testo, tela);
                gt.drawImage(img, (i % 10) * 90, (i / 10) * 20, null);
            }
            // l'altra spazzatura di un fotogramma (contesti grafici, rettangoli, liste...), a pezzi piccoli
            int fatti = 0;
            while (fatti < byteFotogramma) {
                SINK = new byte[2048];
                fatti += 2048;
            }
            nanoDisegno += System.nanoTime() - t0;
            fotogrammi++;
            prossimo += 1_000_000_000L / fps;
            long attesa = prossimo - System.nanoTime();
            if (attesa > 0) {
                Thread.sleep(attesa / 1_000_000L, (int) (attesa % 1_000_000L));
            } else {
                prossimo = System.nanoTime();
            }
        }
        long gcFatti = gcCount() - gc0;
        long richieste = fotogrammi * scritte;
        System.out.printf("%-6s spazz=%5.1f MB/s  scritte=%3d  fotogrammi=%d  richieste=%d  ricostruite=%d (%.2f%%)  %.1f ricostr/s  tempo ricostr.=%.1f ms/s (%.2f%% del tempo)  GC=%d (%.1f/s)  voci in cache=%d%n",
                tipo, mbSecondo, scritte, fotogrammi, richieste, ricostruzioni, 100.0 * ricostruzioni / richieste,
                ricostruzioni / (double) secondi, nanoRicostruzione / 1e6 / secondi, 100.0 * nanoRicostruzione / (secondi * 1e9),
                gcFatti, gcFatti / (double) secondi, cache.size());
    }

    static long gcCount() {
        long n = 0;
        for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) {
            n += Math.max(0, b.getCollectionCount());
        }
        return n;
    }
}
