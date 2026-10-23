package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.ui.immagini.ImageCache;

import java.util.Locale;

/**
 * Una finestra è una parte di DisplayableCanvas.
 * DisplayableCanvas disegna i vari componenti nell'ordine in cui appaiono nello stack.
 * Usando lo stack e usando le coordinate di ogni componente grafico, siamo in grado di adattare
 * le coordinate del mouse alla finestra sottostante.
 */
public interface Finestra {

    int SPACING = ImageCache.SPACING;

    /**
     * In quanti piccoli passi si divide uno scatto della rotella per le liste che scorrono a pixel, fuori da macOS: lì
     * il trackpad manda già una raffica di eventi per ogni gesto, altrove ogni scatto è un solo evento e con pochi pixel
     * la lista sembrerebbe ferma (e con molti a scatti): così i passi si consegnano a uno o due per fotogramma.
     */
    int PASSI_PER_SCATTO_DELLE_LISTE = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("mac") ? 1 : 6;

    /**
     * Il verso in cui gira la rotella del mouse.
     */
    enum MovimentoRotella {
        SU,
        GIU
    }

    /**
     * Il tasto del mouse.
     */
    enum Tasto {
        SINISTRO,
        CENTRALE,
        DESTRO
    }

    default String getNome() {
        return getClass().getSimpleName();
    }

    /**
     * Una finestra che non sta disegnando nulla non deve ricevere gli eventi del mouse,
     * altrimenti li sottrae alle finestre che le stanno sotto.
     */
    default boolean isVisibile() {
        return true;
    }

    /**
     * Se true, un click viene ritardato di {@code intervalloDoppioClick} millisecondi
     * per poter distinguere un singolo click da un doppio click (necessario solo a chi
     * dà un significato diverso a {@link #processaDoppioClick}). Le finestre che non lo
     * fanno (default) ricevono {@link #processaClick} subito, senza attesa.
     */
    default boolean gestisceDoppioClick() {
        return false;
    }

    default void processaEntrata(int x, int y) {
//        System.out.println(getNome() + ": processaEntrata(" + x + ", " + y + ")");
    }
    default void processaUscita(int x, int y) {
//        System.out.println(getNome() + ": processaUscita(" + x + ", " + y + ")");
    }
    default void processaMovimento(int x, int y) {
//        System.out.println(getNome() + ": processaMovimento(" + x + ", " + y + ")");
    }
    default void processaTrascinamento(int x, int y) {
//        System.out.println(getNome() + ": processaTrascinamento(" + x + ", " + y + ")");
    }
    default void processaPressione(int x, int y, Tasto tasto) {
//        System.out.println(getNome() + ": processaPressione(" + x + ", " + y + ", " + tasto + ")");
    }
    default void processaClick(int x, int y, Tasto tasto) {
//        System.out.println(getNome() + ": processaClick(" + x + ", " + y + ", " + tasto + ")");
    }
    default void processaDoppioClick(int x, int y, Tasto tasto) {
//        System.out.println(getNome() + ": processaDoppioClick(" + x + ", " + y + ", " + tasto + ")");
    }
    default void processaRilascio(int x, int y, Tasto tasto) {
//        System.out.println(getNome() + ": processaRilascio(" + x + ", " + y + ", " + tasto + ")");
    }
    /**
     * Quanti passi di {@code processaRotella} fa uno scatto della rotella: 1 per chi scorre a righe, di più per chi
     * scorre a pixel con un passo piccolo (vedi PASSI_PER_SCATTO_DELLE_LISTE).
     */
    default int passiPerScattoDellaRotella() {
        return 1;
    }
    default void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
//        System.out.println(getNome() + ": processaRotella(" + x + ", " + y + ", " + numeroRotazioni + ", " + movimentoRotella + ")");
    }
}
