/**
 * I contratti fra il motore di gioco e chi lo usa o lo completa: tutto ciò che la UI deve poter chiedere senza conoscere le classi del
 * dominio, e ciò che il motore chiede a chi sta fuori di lui.
 * <p>
 * <b>Viste in sola lettura.</b> {@link com.threeamigos.foresta.interfacce.VistaPartita} è il punto da cui la UI legge lo stato della
 * partita; le altre viste ({@code VistaPersonaggio}, {@code VistaGruppo}, {@code VistaMappa}, {@code VistaMissione},
 * {@code VistaScambio}...) ne mostrano le parti. Hanno solo metodi che leggono: per cambiare qualcosa la UI manda un evento
 * al motore.
 * <p>
 * <b>Servizi.</b> {@link com.threeamigos.foresta.interfacce.ControlloreDiGioco} è il motore visto da chi lo avvia;
 * {@link com.threeamigos.foresta.interfacce.GestoreSalvataggi}, {@link com.threeamigos.foresta.interfacce.GestorePunteggi}
 * e {@link com.threeamigos.foresta.interfacce.GestoreTrofei} dicono dove e come si conservano salvataggi, classifica e trofei,
 * e hanno implementazioni su file.
 * <p>
 * <b>Caratteristiche di un oggetto.</b> {@link com.threeamigos.foresta.artefatti.Arma},
 * {@link com.threeamigos.foresta.artefatti.OggettoConCosto}, {@link com.threeamigos.foresta.artefatti.OggettoConPeso} e
 * {@link com.threeamigos.foresta.interfacce.OggettoConArticoli} permettono di trattare oggetti diversi nello stesso modo.
 */
package com.threeamigos.foresta.interfacce;
