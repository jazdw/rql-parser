/*
 * Copyright (C) 2015 Jared Wiltshire (https://jazdw.net).
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 3 which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/lgpl.txt
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */

package net.jazdw.rql.converter;

/**
 * @param <T> output type
 * @author Jared Wiltshire
 */
@FunctionalInterface
public interface ValueConverter<T> {
    /**
     * Converts a string value to its Java object representation
     *
     * @param input input string from RQL
     * @return converted output
     * @throws ConverterException if converter encountered error converting
     */
    T convert(String input) throws ConverterException;
}