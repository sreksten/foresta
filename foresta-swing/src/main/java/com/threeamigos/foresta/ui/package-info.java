/**
 * L'interfaccia grafica in Swing.
 * <p>
 * {@link com.threeamigos.foresta.ui.ForestaUI} è la classe radice: crea la finestra, si iscrive agli eventi del bus e traduce ciascuno in una chiamata sul
 * {@link com.threeamigos.foresta.ui.DisplayableCanvas}, il canvas principale che disegna la schermata corrente (un riquadro di gioco o una schermata a tutto schermo). Lo stato della
 * partita la UI lo legge da {@code VistaPartita}, in sola lettura, e verso il motore manda soltanto eventi: non conosce né il motore né il dominio. Vedi motore_grafico.md.
 * <p>
 * Il resto è diviso in sottopacchetti: {@code schermate} (le schermate a tutto schermo), {@code riquadri} (i riquadri della schermata di gioco), {@code barra} (la barra
 * delle icone), {@code sprite}, {@code testo} (i font bitmap), {@code immagini}, {@code tattiche} (la schermata delle tattiche), {@code componenti} (pezzi riusabili)
 * e {@code sfx} (logo e nuvole).
 */
package com.threeamigos.foresta.ui;
