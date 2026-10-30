package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle;
import com.threeamigos.foresta.artefatti.OggettoConPeso;
import com.threeamigos.foresta.artefatti.VistaArtefatto;
import com.threeamigos.foresta.gruppi.DatiDiProva;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gli eventi del modulo sono dati con un tipo e qualche lettura: li si costruisce tutti con valori qualunque (il primo valore di ogni enumerazione,
 * un goblin al posto di ogni personaggio) e se ne chiamano le letture, per vedere che ognuno riporti quello che ha avuto e porti il suo tipo.
 */
class EventiDelModuloTest {

    @BeforeEach
    void preparaModello() {
        DatiDiProva.nuovaPartita();
    }

    private static Object valoreDi(Class<?> tipo) {
        if (tipo == int.class || tipo == Integer.class) {
            return 3;
        }
        if (tipo == double.class || tipo == Double.class) {
            return 1.5d;
        }
        if (tipo == boolean.class || tipo == Boolean.class) {
            return true;
        }
        if (tipo == String.class) {
            return "prova";
        }
        if (tipo == List.class || tipo == java.util.Collection.class) {
            return new ArrayList<>(Collections.singletonList(TipoPersonaggio.GOBLIN));
        }
        if (tipo.isEnum()) {
            return tipo.getEnumConstants()[0];
        }
        if (tipo == Enum.class) {
            return TipoPersonaggio.GOBLIN;
        }
        if (tipo == OggettoConPeso.class || tipo == VistaArtefatto.class) {
            return new GeneratoreArtefattiTabelle(new Random(5)).generaArtefatto(TipoArtefatto.SPADA, 1);
        }
        if (tipo == ModificatoreAttributoMD.class) {
            return new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 2);
        }
        if (tipo.isAssignableFrom(Personaggio.class) && tipo != Object.class) {
            return FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        }
        if (tipo.isInterface()) {
            // Un'interfaccia che l'evento si limita a portare in giro (per esempio la vista di uno scambio): basta una finta
            return Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[] {tipo}, (proxy, metodo, argomenti) -> null);
        }
        return null;
    }

    private static List<Class<?>> gliEventi() throws IOException, ClassNotFoundException {
        Path radice = Paths.get("target/classes");
        List<Class<?>> classi = new ArrayList<>();
        try (Stream<Path> files = Files.walk(radice.resolve("com/threeamigos/foresta/eventi"))) {
            for (Path file : files.filter(f -> f.toString().endsWith(".class") && !f.toString().contains("$")).collect(Collectors.toList())) {
                String nome = radice.relativize(file).toString().replace('/', '.').replace(".class", "");
                classi.add(Class.forName(nome));
            }
        }
        return classi;
    }

    @Test
    void ogniEventoSiCostruisceESiLegge() throws Exception {
        List<String> nonCostruiti = new ArrayList<>();
        int costruiti = 0;
        for (Class<?> classe : gliEventi()) {
            if (Modifier.isAbstract(classe.getModifiers()) || !EventoBase.class.isAssignableFrom(classe)) {
                continue;
            }
            Object evento = null;
            for (Constructor<?> costruttore : classe.getConstructors()) {
                Object[] argomenti = new Object[costruttore.getParameterCount()];
                boolean completo = true;
                for (int i = 0; i < argomenti.length; i++) {
                    argomenti[i] = valoreDi(costruttore.getParameterTypes()[i]);
                    completo &= argomenti[i] != null;
                }
                if (completo) {
                    try {
                        evento = costruttore.newInstance(argomenti);
                        break;
                    } catch (InvocationTargetException e) {
                        // valori qualunque che la classe non accetta: si prova con un altro costruttore
                    }
                }
            }
            if (evento == null) {
                nonCostruiti.add(classe.getSimpleName());
                continue;
            }
            costruiti++;
            assertNotNull(((EventoBase) evento).getTipoEvento(), classe.getSimpleName());
            for (Method metodo : classe.getMethods()) {
                if (metodo.getParameterCount() == 0 && !Modifier.isStatic(metodo.getModifiers()) && metodo.getDeclaringClass() != Object.class) {
                    metodo.invoke(evento);
                }
            }
        }
        assertTrue(costruiti > 20, "costruiti: " + costruiti);
        assertTrue(nonCostruiti.isEmpty(), "eventi non costruiti: " + nonCostruiti);
    }
}
