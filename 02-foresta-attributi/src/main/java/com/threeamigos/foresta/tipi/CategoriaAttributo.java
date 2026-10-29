package com.threeamigos.foresta.tipi;

/**
 * La categoria di un {@link TipoAttributo}: <b>primari</b> sono le caratteristiche di base (forza, destrezza...),
 * <b>secondari</b> quelle che ne derivano o la completano (precisione, critico, parata...), <b>risorse dinamiche</b>
 * quelle che cambiano di continuo durante la partita (salute, magia, stanchezza, esperienza...).
 *
 * @author Stefano Reksten
 */
public enum CategoriaAttributo {

    /**
     * Categoria per attributi primari (forza, destrezza...)
     */
    PRIMARIO,
    /**
     * Categoria per attributi secondari (precisione, critico, parata...)
     */
    SECONDARIO,
    /**
     * Categoria per risorse dinamiche (salute, magia, stanchezza, esperienza...)
     */
    RISORSA_DINAMICA

}
