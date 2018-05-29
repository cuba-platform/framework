/*
 * Copyright 2000-2018 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.ui.renderers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import com.vaadin.shared.ui.grid.renderers.LocalDateRendererState;

import elemental.json.JsonValue;

/**
 * A renderer for presenting date values.
 *
 * @author Vaadin Ltd
 * @since 8.1
 */
public class LocalDateRenderer extends AbstractRenderer<Object, LocalDate> {

    private DateTimeFormatter formatter;
    private boolean getLocaleFromGrid;

    /**
     * Creates a new LocalDateRenderer.
     * <p>
     * The renderer is configured to render with the grid's locale it is
     * attached to, with the format style being {@code FormatStyle.LONG} and an
     * empty string as its null representation.
     *
     * @see <a href=
     *      "https://docs.oracle.com/javase/8/docs/api/java/time/format/FormatStyle.html#LONG">
     *      FormatStyle.LONG</a>
     */
    public LocalDateRenderer() {
        this(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG), "");
        getLocaleFromGrid = true;
    }

    /**
     * Creates a new LocalDateRenderer.
     * <p>
     * The renderer is configured to render with the given string format, as
     * displayed in the grid's locale it is attached to, with an empty string as
     * its null representation.
     *
     * @param formatPattern
     *            the format pattern to format the date with, not {@code null}
     *
     * @throws IllegalArgumentException
     *             if format pattern is null
     *
     * @see <a href=
     *      "https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#patterns">
     *      Format Pattern Syntax</a>
     */
    public LocalDateRenderer(String formatPattern) {
        this(formatPattern, Locale.getDefault());
        getLocaleFromGrid = true;
    }

    /**
     * Creates a new LocalDateRenderer.
     * <p>
     * The renderer is configured to render with the given string format, as
     * displayed in the given locale, with an empty string as its null
     * representation.
     *
     * @param formatPattern
     *            the format pattern to format the date with, not {@code null}
     * @param locale
     *            the locale to use, not {@code null}
     *
     * @throws IllegalArgumentException
     *             if format pattern is null
     * @throws IllegalArgumentException
     *             if locale is null
     *
     * @see <a href=
     *      "https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#patterns">
     *      Format Pattern Syntax</a>
     */
    public LocalDateRenderer(String formatPattern, Locale locale) {
        this(formatPattern, locale, "");
    }

    /**
     * Creates a new LocalDateRenderer.
     * <p>
     * The renderer is configured to render with the given string format, as
     * displayed in the given locale.
     *
     * @param formatPattern
     *            the format pattern to format the date with, not {@code null}
     * @param locale
     *            the locale to use, not {@code null}
     * @param nullRepresentation
     *            the textual representation of the {@code null} value
     *
     * @throws IllegalArgumentException
     *             if format pattern is null
     * @throws IllegalArgumentException
     *             if locale is null
     *
     * @see <a href=
     *      "https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#patterns">
     *      Format Pattern Syntax</a>
     */
    public LocalDateRenderer(String formatPattern, Locale locale,
            String nullRepresentation) {
        super(LocalDate.class, nullRepresentation);

        if (formatPattern == null) {
            throw new IllegalArgumentException(
                    "format pattern may not be null");
        }

        if (locale == null) {
            throw new IllegalArgumentException("locale may not be null");
        }

        formatter = DateTimeFormatter.ofPattern(formatPattern, locale);
    }

    /**
     * Creates a new LocalDateRenderer.
     * <p>
     * The renderer is configured to render with the given formatter, with an
     * empty string as its null representation.
     *
     * @param formatter
     *            the formatter to use, not {@code null}
     *
     * @throws IllegalArgumentException
     *             if formatter is null
     */
    public LocalDateRenderer(DateTimeFormatter formatter) {
        this(formatter, "");
    }

    /**
     * Creates a new LocalDateRenderer.
     * <p>
     * The renderer is configured to render with the given formatter.
     *
     * @param formatter
     *            the formatter to use, not {@code null}
     * @param nullRepresentation
     *            the textual representation of the {@code null} value
     *
     * @throws IllegalArgumentException
     *             if formatter is null
     */
    public LocalDateRenderer(DateTimeFormatter formatter,
            String nullRepresentation) {
        super(LocalDate.class, nullRepresentation);

        if (formatter == null) {
            throw new IllegalArgumentException("formatter may not be null");
        }

        this.formatter = formatter;
    }

    @Override
    public JsonValue encode(LocalDate value) {
        String dateString;
        if (value == null) {
            dateString = getNullRepresentation();
        } else if (getLocaleFromGrid) {
            if (getParentGrid() == null) {
                throw new IllegalStateException(
                        "Could not find a locale to format with: "
                                + "this renderer should either be attached to a grid "
                                + "or constructed with locale information");
            }
            dateString = value
                    .format(formatter.withLocale(getParentGrid().getLocale()));
        } else {
            dateString = value.format(formatter);
        }
        return encode(dateString, String.class);
    }

    @Override
    protected LocalDateRendererState getState() {
        return (LocalDateRendererState) super.getState();
    }

    @Override
    protected LocalDateRendererState getState(boolean markAsDirty) {
        return (LocalDateRendererState) super.getState(markAsDirty);
    }
}
