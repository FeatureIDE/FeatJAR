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
package de.featjar.base.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.log.Log;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArgumentParserTest {

    @BeforeEach
    public void init() {
        FeatJAR.initialize();
    }

    @AfterEach
    public void deinit() {
        FeatJAR.deinitialize();
    }

    OptionList parser(String... args) {
        OptionList optionList = new OptionList(Option.getAllOptions(FeatJAR.class), args);
        List<Problem> problems = optionList.parseArguments();
        assertTrue(problems.isEmpty(), Problem.printProblems(problems));
        return optionList;
    }

    @Test
    void parseCommand() {
        // TODO: needs mocking of extension points
    }

    @Test
    void getVerbosity() {
        // assertEquals(Log.Verbosity.DEBUG, parser("arg", "--log-info").getVerbosity()); TODO: mock System.exit
        OptionList parser = parser("--log-info", "debug");
        parser.parseArguments();
        assertEquals(Log.Verbosity.DEBUG, parser.get(FeatJAR.LOG_INFO_OPTION).get(0));
    }

    @Test
    void parseOption() {
        Option<Integer> option1 = new Option<>("x", Integer::valueOf);
        Option<Integer> option2 = new Option<>("y", Integer::valueOf);

        OptionList parser = new OptionList("--x", "42");
        parser.parseArguments();
        parser.addOptions(List.of(option1, option2)).parseArguments();

        assertEquals(Result.of(42), parser.getResult(option1));
        assertEquals(Result.empty(), parser.getResult(option2));
    }
}
