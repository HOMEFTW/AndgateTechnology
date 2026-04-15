package com.andgatech.AHTech.common.currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class CurrencyTypeTest {

    @Test
    void allTypesHaveExpectedMetaValues() {
        assertEquals(0, CurrencyType.COPPER.getMeta());
        assertEquals(1, CurrencyType.STEEL.getMeta());
        assertEquals(2, CurrencyType.TITANIUM.getMeta());
        assertEquals(3, CurrencyType.PLATINUM.getMeta());
        assertEquals(4, CurrencyType.NEUTRONIUM.getMeta());
        assertEquals(5, CurrencyType.INFINITY.getMeta());
    }

    @Test
    void getByMetaReturnsMatchingType() {
        for (CurrencyType type : CurrencyType.values()) {
            assertEquals(type, CurrencyType.getByMeta(type.getMeta()));
        }
    }

    @Test
    void getByMetaReturnsNullForUnknownValue() {
        assertNull(CurrencyType.getByMeta(-1));
        assertNull(CurrencyType.getByMeta(99));
    }
}
