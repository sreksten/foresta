/**
 * Le regole del combattimento.
 * <p>
 * {@link com.threeamigos.foresta.combattimento.CalcolatoreCombattimento} decide se un colpo va a segno, quanto danno fa e quali stati alterati
 * provoca, e dà anche le stime che le tattiche usano per scegliere; il risultato è un
 * {@link com.threeamigos.foresta.combattimento.DannoRisultante}. {@link com.threeamigos.foresta.combattimento.StatoDelCombattimento}
 * ricorda ciò che serve durante uno scontro (chi ha agito nel round, chi ha attaccato chi, le voci di tattica già usate), e le
 * {@link com.threeamigos.foresta.combattimento.Ondata ondate} sono i gruppi di mostri che si presentano uno dopo l'altro.
 */
package com.threeamigos.foresta.combattimento;
