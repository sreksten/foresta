package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.tipi.tattiche.TipoConfronto;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tutte le enum del modulo dei personaggi, trovate fra le classi compilate (anche quelle annidate e dei sottopacchetti): ognuna ha delle
 * costanti, e ogni costante si ritrova dal suo nome, come fanno i salvataggi (LettoreCampi.enumerato).
 */
class EnumDelModuloDeiPersonaggiTest {

    private static List<Class<?>> enumDelModulo() throws IOException, URISyntaxException, ClassNotFoundException {
        Path radice = Paths.get(TipoPersonaggio.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        List<String> nomi;
        try (Stream<Path> file = Files.walk(radice)) {
            nomi = file.map(p -> radice.relativize(p).toString())
                    .filter(nome -> nome.endsWith(".class"))
                    .map(nome -> nome.substring(0, nome.length() - ".class".length()).replace('/', '.').replace('\\', '.'))
                    .collect(Collectors.toList());
        }
        List<Class<?>> enumerazioni = new ArrayList<>();
        for (String nome : nomi) {
            Class<?> classe = Class.forName(nome);
            if (classe.isEnum()) {
                enumerazioni.add(classe);
            }
        }
        return enumerazioni;
    }

    @Test
    void ogniCostanteSiRitrovaDalSuoNome() throws Exception {
        List<Class<?>> enumerazioni = enumDelModulo();
        assertTrue(enumerazioni.size() > 5, "troppo poche enum trovate: " + enumerazioni);
        assertTrue(enumerazioni.contains(TipoPersonaggio.class));
        assertTrue(enumerazioni.contains(PersonaggioNoto.Ambito.class), "anche le enum annidate");
        for (Class<?> classe : enumerazioni) {
            Object[] costanti = classe.getEnumConstants();
            assertTrue(costanti.length > 0, classe + " non ha costanti");
            for (Object costante : costanti) {
                Enum<?> valore = (Enum<?>) costante;
                assertSame(valore, valueOf(classe, valore.name()), classe.getSimpleName() + "." + valore.name());
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Enum<?> valueOf(Class<?> classe, String nome) {
        return Enum.valueOf((Class) classe, nome);
    }
}
