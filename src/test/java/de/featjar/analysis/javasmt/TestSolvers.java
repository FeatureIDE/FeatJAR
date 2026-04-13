/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-javasmt.
 *
 * formula-analysis-javasmt is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-javasmt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-javasmt. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-javasmt> for further information.
 */
package de.featjar.analysis.javasmt;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import de.featjar.Common;
import de.featjar.base.FeatJAR;
import de.featjar.base.env.HostEnvironment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.SolverContext;

public class TestSolvers extends Common { // TODO

    @BeforeAll
    public static void begin() {
        FeatJAR.testConfiguration().initialize();
    }

    @AfterAll
    public static void end() {
        FeatJAR.deinitialize();
    }

    private void solversWindows() {
        testAvailability(Solvers.MATHSAT5);
        testAvailability(Solvers.PRINCESS);
        testAvailability(Solvers.SMTINTERPOL);
        // testAvailability(Solvers.Z3);
    }

    private void solversUnix() {
        // testAvailability(Solvers.BOOLECTOR);
        // testAvailability(Solvers.CVC4);
        testAvailability(Solvers.MATHSAT5);
        testAvailability(Solvers.PRINCESS);
        testAvailability(Solvers.SMTINTERPOL);
        // testAvailability(Solvers.Z3);
    }

    private void solversMac() {
        testAvailability(Solvers.PRINCESS);
        testAvailability(Solvers.SMTINTERPOL);
        // testAvailability(Solvers.Z3);
    }

    @Test
    public void solvers() {
        try {
            switch (HostEnvironment.OPERATING_SYSTEM) {
                case LINUX:
                    solversUnix();
                    break;
                case MAC_OS:
                    solversMac();
                    break;
                case WINDOWS:
                    solversWindows();
                    break;
                case UNKNOWN:
                    fail();
                    break;
                default:
                    fail();
                    break;
            }
        } catch (final Exception e) {
            FeatJAR.log().error(e);
            fail();
        }
    }

    public void testAvailability(Solvers solver) {
        final Configuration config = Configuration.defaultConfiguration();
        final LogManager logger = LogManager.createNullLogManager();
        final ShutdownNotifier notifier = ShutdownNotifier.createDummy();

        try (SolverContext context = SolverContextFactory.createSolverContext(config, logger, notifier, solver)) {
            assertNotNull(context.getVersion());
        } catch (final InvalidConfigurationException e) {
            fail(solver + " not available!");
        }
    }
}
