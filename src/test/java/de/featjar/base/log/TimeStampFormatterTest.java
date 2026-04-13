/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-base.
 *
 * base is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with base. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-base> for further information.
 */
package de.featjar.base.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TimeStampFormatterTest {
    TimeStampFormatter timeStampFormatter;

    @BeforeEach
    public void setup() {
        timeStampFormatter = Mockito.spy(TimeStampFormatter.class);
        doReturn(Instant.ofEpochSecond(1659344400)).when(timeStampFormatter).getInstant();
    }

    @Test
    void mockInstant() {
        assertEquals("2022-08-01T09:00:00Z", timeStampFormatter.getInstant().toString());
    }

    @Test
    void getDefaultPrefix() {
        assertEquals("[01/08/2022, 11:00] ", timeStampFormatter.getPrefix(null, null));
    }

    @Test
    void getCustomPrefix() {
        timeStampFormatter.setFormatter(
                DateTimeFormatter.ofPattern("yyyy/MM/dd-HH:mm:ss").withZone(ZoneId.systemDefault()));
        assertEquals("[2022/08/01-11:00:00] ", timeStampFormatter.getPrefix(null, null));
    }
}
