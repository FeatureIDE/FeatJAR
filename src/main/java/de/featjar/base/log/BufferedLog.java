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

import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Buffers all messages for later consumption instead of writing them directly to any streams.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class BufferedLog implements Log {

    /**
     * A message object to be printed later.
     * Contains a supplier for the actual message.
     */
    public static class Message {
        private final Supplier<String> message;
        private final Verbosity verbosity;
        private final boolean format;

        /**
         * Constructs a new message object.
         * @param message the message supplier
         * @param verbosity the verbosity of the message
         * @param format whether to format the message
         */
        public Message(Supplier<String> message, Verbosity verbosity, boolean format) {
            this.message = message;
            this.verbosity = verbosity;
            this.format = format;
        }

        /**
         * {@return the message supplier}
         */
        public Supplier<String> getMessage() {
            return message;
        }

        /**
         * {@return the verbosity of the message}
         */
        public Verbosity getVerbosity() {
            return verbosity;
        }

        /**
         * {@return whether to format the message}
         */
        public boolean isFormat() {
            return format;
        }
    }

    private final LinkedList<Message> logBuffer = new LinkedList<>();
    private boolean printStacktrace;

    @Override
    public void print(Supplier<String> message, Verbosity verbosity, boolean format) {
        synchronized (logBuffer) {
            logBuffer.add(new Message(message, verbosity, format));
        }
    }

    @Override
    public void println(Supplier<String> message, Verbosity verbosity, boolean format) {
        synchronized (logBuffer) {
            logBuffer.add(new Message(() -> message.get() + "\n", verbosity, format));
        }
    }

    @Override
    public void println(Throwable error, Verbosity verbosity) {
        synchronized (logBuffer) {
            logBuffer.add(new Message(() -> Log.getErrorMessage(error, printStacktrace) + "\n", verbosity, false));
        }
    }

    /**
     * {@return whether to print the stacktrace for error messages}
     */
    public boolean isPrintStacktrace() {
        return printStacktrace;
    }

    /**
     * Sets whether to print the stacktrace for error messages.
     * @param printStacktrace {@code true} if the stacktrace should be printed
     */
    public void setPrintStacktrace(boolean printStacktrace) {
        this.printStacktrace = printStacktrace;
    }

    /**
     * Consumes all messages currently in the buffer.
     * @param messageConsumer the consumer
     */
    public void flush(Consumer<Message> messageConsumer) {
        synchronized (logBuffer) {
            for (Message message : logBuffer) {
                messageConsumer.accept(message);
            }
            logBuffer.clear();
        }
    }
}
