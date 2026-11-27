package br.com.ifrs.gastos.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class TagSumUnitTest {

    // Verifica se o construtor preserva a tag e o total.
    @Test
    void constructorPreservesTagAndTotal() {
        TagSum sum = new TagSum("mercado", 125.75);

        assertEquals("mercado", sum.getTag());
        assertEquals(125.75, sum.getTotal());
    }

    // Verifica se o construtor vazio inicializa os campos como nulos.
    @Test
    void noArgumentConstructorCanBeUsedBySerialization() {
        TagSum sum = new TagSum();

        assertNull(sum.getTag());
        assertNull(sum.getTotal());
    }
}
