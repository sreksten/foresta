/**
 * Il temporizzatore che manda un impulso a intervalli ({@link com.threeamigos.foresta.strumenti.temporizzatore.Temporizzatore} e
 * {@link com.threeamigos.foresta.strumenti.temporizzatore.Temporizzabile}), la sua implementazione
 * ({@link com.threeamigos.foresta.strumenti.temporizzatore.TemporizzatoreJ2SE}) e il modo di eseguire qualcosa sul thread principale del toolkit
 * grafico ({@link com.threeamigos.foresta.strumenti.temporizzatore.ThreadPrincipale}). Non conosce né il dominio né il bus degli eventi:
 * gli impulsi falliti si segnalano a chi li ha chiesti.
 */
package com.threeamigos.foresta.strumenti.temporizzatore;
