/**
 * Le abilità innate del Chierico, del Druido, del Sacerdote, del Mago e dell'Elfo (e delle loro forme femminili): capacità che costano magia e non consumano pergamene, come i canti del
 * Bardo (vedi il pacchetto {@code canti}), e per questo non sono incantesimi.
 * <p>
 * {@link com.threeamigos.foresta.abilita.Abilita} è l'interfaccia; {@link com.threeamigos.foresta.abilita.AbilitaInnate} decide chi può usarle e quando, e
 * {@link com.threeamigos.foresta.abilita.FabbricaAbilita} costruisce l'abilità di ogni {@link com.threeamigos.foresta.tipi.TipoAbilita}: scacciare i non
 * morti e imposizione delle mani (chierico); radici, sciame, pelle di corteccia e rigenerazione (druido); alba sacra e luce divina (sacerdote); dardo arcano (mago ed elfo).
 * Quelle che fanno danno estendono {@link com.threeamigos.foresta.abilita.AbilitaDiDanno}, che è essa stessa l'attacco magico.
 */
package com.threeamigos.foresta.abilita;
