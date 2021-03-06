/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */

package org.learning.core.config.patterns.factory.factories;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;


/**
 * Represents the source for the logging configuration.
 */
public class ConfigurationSource {
    /**
     * ConfigurationPropertyRetriever to use with Configurations that do not require a "real" configuration source.
     */
    public static final ConfigurationSource NULL_SOURCE = new ConfigurationSource(new byte[0]);

    private final File file;
    private final URL url;
    private final String location;
    private final InputStream stream;
    private final byte[] data;

    /**
     * Constructs a new {@code ConfigurationPropertyRetriever} with the specified input stream that originated from the specified
     * file.
     *
     * @param stream the input stream
     * @param file the file where the input stream originated
     */
    public ConfigurationSource(final InputStream stream, final File file) {
        this.stream = Objects.requireNonNull(stream, "stream is null");
        this.file = Objects.requireNonNull(file, "file is null");
        this.location = file.getAbsolutePath();
        this.url = null;
        this.data = null;
    }

    /**
     * Constructs a new {@code ConfigurationPropertyRetriever} with the specified input stream that originated from the specified
     * url.
     *
     * @param stream the input stream
     * @param url the URL where the input stream originated
     */
    public ConfigurationSource(final InputStream stream, final URL url) {
        this.stream = Objects.requireNonNull(stream, "stream is null");
        this.url = Objects.requireNonNull(url, "URL is null");
        this.location = url.toString();
        this.file = null;
        this.data = null;
    }

    /**
     * Constructs a new {@code ConfigurationPropertyRetriever} with the specified input stream. Since the stream is the only source
     * of data, this constructor makes a copy of the stream contents.
     *
     * @param stream the input stream
     * @throws IOException if an exception occurred reading from the specified stream
     */
    public ConfigurationSource(final InputStream stream) throws IOException {
        this(toByteArray(stream));
    }

    private ConfigurationSource(final byte[] data) {
        this.data = Objects.requireNonNull(data, "data is null");
        this.stream = new ByteArrayInputStream(data);
        this.file = null;
        this.url = null;
        this.location = null;
    }

    /**
     * Returns the contents of the specified {@code InputStream} as a byte array.
     *
     * @param inputStream the stream to read
     * @return the contents of the specified stream
     * @throws IOException if a problem occurred reading from the stream
     */
    private static byte[] toByteArray(final InputStream inputStream) throws IOException {
        final int buffSize = Math.max(4096, inputStream.available());
        final ByteArrayOutputStream contents = new ByteArrayOutputStream(buffSize);
        final byte[] buff = new byte[buffSize];

        int length = inputStream.read(buff);
        while (length > 0) {
            contents.write(buff, 0, length);
            length = inputStream.read(buff);
        }
        return contents.toByteArray();
    }

    /**
     * Returns the file configuration source, or {@code null} if this configuration source is based on an URL or has
     * neither a file nor an URL.
     *
     * @return the configuration source file, or {@code null}
     */
    public File getFile() {
        return file;
    }

    /**
     * Returns the configuration source URL, or {@code null} if this configuration source is based on a file or has
     * neither a file nor an URL.
     *
     * @return the configuration source URL, or {@code null}
     */
    public URL getURL() {
        return url;
    }

    /**
     * Returns a URI representing the configuration resource or null if it cannot be determined.
     * @return The URI.
     */
    public URI getURI() {
        URI sourceURI = null;
        if (url != null) {
            try {
                sourceURI = url.toURI();
            } catch (final URISyntaxException ex) {
                    /* Ignore the exception */
            }
        }
        if (sourceURI == null && file != null) {
            sourceURI = file.toURI();
        }
        if (sourceURI == null && location != null) {
            try {
                sourceURI = new URI(location);
            } catch (final URISyntaxException ex) {
                // Assume the scheme was missing.
                try {
                    sourceURI = new URI("file://" + location);
                } catch (final URISyntaxException uriEx) {
                    /* Ignore the exception */
                }
            }
        }
        return sourceURI;
    }

    /**
     * Returns a string describing the configuration source file or URL, or {@code null} if this configuration source
     * has neither a file nor an URL.
     *
     * @return a string describing the configuration source file or URL, or {@code null}
     */
    public String getLocation() {
        return location;
    }

    /**
     * Returns the input stream that this configuration source was constructed with.
     *
     * @return the input stream that this configuration source was constructed with.
     */
    public InputStream getInputStream() {
        return stream;
    }

    /**
     * Returns a new {@code ConfigurationPropertyRetriever} whose input stream is reset to the beginning.
     *
     * @return a new {@code ConfigurationPropertyRetriever}
     * @throws IOException if a problem occurred while opening the new input stream
     */
    public ConfigurationSource resetInputStream() throws IOException {
        if (file != null) {
            return new ConfigurationSource(new FileInputStream(file), file);
        } else if (url != null) {
            return new ConfigurationSource(url.openStream(), url);
        } else {
            return new ConfigurationSource(data);
        }
    }

    @Override
    public String toString() {
        if (location != null) {
            return location;
        }
        if (this == NULL_SOURCE) {
            return "NULL_SOURCE";
        }
        final int length = data == null ? -1 : data.length;
        return "stream (" + length + " bytes, unknown location)";
    }



}
