package com.threeamigos.foresta.ui;

/**
 * Una finestra è una parte di DisplayableCanvas.
 * DisplayableCanvas disegna i vari componenti nell'ordine in cui appaiono nello stack.
 * Usando lo stack e usando le coordinate di ogni componente grafico, siamo in grado di adattare
 * le coordinate del mouse alla finestra sottostante.
 */
interface Finestra {

    int SPACING = ImageCache.SPACING;

    enum MovimentoRotella {
        SU,
        GIU
    }

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
    default void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
//        System.out.println(getNome() + ": processaRotella(" + x + ", " + y + ", " + numeroRotazioni + ", " + movimentoRotella + ")");
    }
}
