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
package de.featjar.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ProcessOutput {

    public static ProcessOutput runProcess(String command) throws IOException {
        Process process = Runtime.getRuntime().exec(command);

        InputStream processErr = process.getErrorStream();
        InputStream processOut = process.getInputStream();
        BufferedReader outbr = new BufferedReader(new InputStreamReader(processOut, StandardCharsets.UTF_8));
        BufferedReader errbr = new BufferedReader(new InputStreamReader(processErr, StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = outbr.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        String outputString = sb.toString();

        sb = new StringBuilder();
        line = null;
        while ((line = errbr.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        String errorString = sb.toString();

        return new ProcessOutput(outputString, errorString);
    }

    private final String outputString;
    private final String errorString;

    public ProcessOutput(String outputString, String errorString) {
        this.outputString = outputString;
        this.errorString = errorString;
    }

    public void printOutput() {
        System.out.println(outputString);
        if (!errorString.isBlank()) {
            System.err.println(errorString);
        }
    }

    public String getOutputString() {
        return outputString;
    }

    public String getErrorString() {
        return errorString;
    }
}
