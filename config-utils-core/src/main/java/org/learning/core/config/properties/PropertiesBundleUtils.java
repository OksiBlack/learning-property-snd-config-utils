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
package org.learning.core.config.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deprecated.apachecommons.configurations.Utils;


/**
 * <em>Consider this class private.</em>
 * <p>
 * Provides utility methods for managing {@link Properties} instances as well as access to the global configuration
 * system. Properties by default are loaded from the system properties, system environment, and a classpath resource
 * file. Additional properties can be loaded by implementing a custom
 * {@link PropertiesSource} service and specifying it via a {@link ServiceLoader} file called
 * {@code META-INF/services/org.apache.logging.log4j.util.PropertySource} with a list of fully qualified class names
 * implementing that interface.
 * </p>
 *
 * @see PropertiesSource
 */
public final class PropertiesBundleUtils {

    public static final Logger logger = LogManager.getLogger();


    private final Environment environment;

    /**
     * Constructs a PropertiesBundleUtils using a given Properties object as its source of defined properties.
     *
     * @param props the Properties to use by default
     */
    public PropertiesBundleUtils(final Properties props) {
        this.environment = new Environment(new PropertiesSource(props));
    }

    /**
     * Constructs a PropertiesBundleUtils for a given properties file name on the classpath. The properties specified in this
     * file are used by default. If a property is not defined in this file, then the equivalent system property is used.
     *
     * @param propertiesFileName the location of properties file to getProperties
     */
    public PropertiesBundleUtils(final String propertiesFileName) {
        this.environment = new Environment(new PropertiesFileSource(propertiesFileName));
    }

    /**
     * Loads and closes the given property input stream. If an error occurs, log to the status logger.
     *
     * @param in     a property input stream.
     * @param source a source object describing the source, like a resource string or a URL.
     * @return a new Properties object
     */
    static Properties loadClose(final InputStream in, final Object source) {
        final Properties props = new Properties();
        if (null != in) {
            try {
                props.load(in);
            } catch (final IOException e) {
                logger.error("Unable to read " + source, e);
            } finally {
                try {
                    in.close();
                } catch (final IOException e) {
                    logger.error("Unable to close " + source, e);
                }
            }
        }
        return props;
    }

    /**
     * Return the system properties or an empty Properties object if an error occurs.
     *
     * @return The system properties.
     */
    public static Properties getSystemProperties() {
        try {
            return new Properties(System.getProperties());
        } catch (final SecurityException ex) {
            logger.error("Unable to access system properties.", ex);
            // Sandboxed - can't read System Properties
            return new Properties();
        }
    }

    /**
     * Returns {@code true} if the specified property is defined, regardless of its value (it may not have a value).
     *
     * @param name the name of the property to verify
     * @return {@code true} if the specified property is defined, regardless of its value
     */
    public boolean hasProperty(final String name) {
        return environment.containsKey(name);
    }

    /**
     * Gets the named property as a boolean value. If the property matches the string {@code "true"} (case-insensitive),
     * then it is returned as the boolean value {@code true}. Any other non-{@code null} text in the property is
     * considered {@code false}.
     *
     * @param name the name of the property to look up
     * @return the boolean value of the property or {@code false} if undefined.
     */
    public boolean getBooleanProperty(final String name) {
        return getBooleanProperty(name, false);
    }

    /**
     * Gets the named property as a boolean value.
     *
     * @param name         the name of the property to look up
     * @param defaultValue the default value to use if the property is undefined
     * @return the boolean value of the property or {@code defaultValue} if undefined.
     */
    public boolean getBooleanProperty(final String name, final boolean defaultValue) {
        final String prop = getStringProperty(name);
        return prop == null ? defaultValue : "true".equalsIgnoreCase(prop);
    }

    /**
     * Gets the named property as a String.
     *
     * @param name the name of the property to look up
     * @return the String value of the property or {@code null} if undefined.
     */
    public String getStringProperty(final String name) {
        return environment.get(name);
    }

    /**
     * Gets the named property as a boolean value.
     *
     * @param name                  the name of the property to look up
     * @param defaultValueIfAbsent  the default value to use if the property is undefined
     * @param defaultValueIfPresent the default value to use if the property is defined but not assigned
     * @return the boolean value of the property or {@code defaultValue} if undefined.
     */
    public boolean getBooleanProperty(final String name, final boolean defaultValueIfAbsent,
        final boolean defaultValueIfPresent) {
        final String prop = getStringProperty(name);
        return prop == null ? defaultValueIfAbsent
            : prop.isEmpty() ? defaultValueIfPresent : "true".equalsIgnoreCase(prop);
    }

    /**
     * Gets the named property as a double.
     *
     * @param name         the name of the property to look up
     * @param defaultValue the default value to use if the property is undefined
     * @return the parsed double value of the property or {@code defaultValue} if it was undefined or could not be parsed.
     */
    public double getDoubleProperty(final String name, final double defaultValue) {
        final String prop = getStringProperty(name);
        if (prop != null) {
            try {
                return Double.parseDouble(prop);
            } catch (final Exception ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Gets the named property as an integer.
     *
     * @param name         the name of the property to look up
     * @param defaultValue the default value to use if the property is undefined
     * @return the parsed integer value of the property or {@code defaultValue} if it was undefined or could not be
     *     parsed.
     */
    public int getIntegerProperty(final String name, final int defaultValue) {
        final String prop = getStringProperty(name);
        if (prop != null) {
            try {
                return Integer.parseInt(prop);
            } catch (final Exception ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Gets the named property as a long.
     *
     * @param name         the name of the property to look up
     * @param defaultValue the default value to use if the property is undefined
     * @return the parsed long value of the property or {@code defaultValue} if it was undefined or could not be parsed.
     */
    public long getLongProperty(final String name, final long defaultValue) {
        final String prop = getStringProperty(name);
        if (prop != null) {
            try {
                return Long.parseLong(prop);
            } catch (final Exception ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Gets the named property as a String.
     *
     * @param name         the name of the property to look up
     * @param defaultValue the default value to use if the property is undefined
     * @return the String value of the property or {@code defaultValue} if undefined.
     */
    public String getStringProperty(final String name, final String defaultValue) {
        final String prop = getStringProperty(name);
        return (prop == null) ? defaultValue : prop;
    }

    /**
     * Reloads all properties. This is primarily useful for unit tests.
     *
     * @since 2.10.0
     */
    public void reload() {
        environment.reload();
    }

    /**
     * Provides support for looking up global configuration properties via environment variables, property files,
     * and system properties
     *
     * @since 2.10.0
     */
    private static class Environment {

        private final Set<PropertiesSource> sources = new TreeSet<>(Comparator.comparing((f) -> f.getPriority()));
        private final Map<String, String> propertiesFromSources = new ConcurrentHashMap<>();


        private Environment(final PropertiesSource propertySource) {
            sources.add(propertySource);
            for (final PropertiesSource source : ServiceLoader.load(PropertiesSource.class)) {
                sources.add(source);
            }
            reload();
        }

        private synchronized void reload() {
            propertiesFromSources.clear();

            for (final PropertiesSource source : sources) {
                Map<String, String> properties = Utils.convertToTypedMap(source.getProperties(),
                    (k) -> k == null ? null : String.valueOf(k),
                    v -> v == null ? null : String.valueOf(v));
                properties
                    .forEach((BiConsumer<String, String>) (key, value) -> {
                        if (key != null && value != null) {
                            propertiesFromSources.put(key, value);
                        }
                    });
            }
        }

        private String get(final String key) {

            if (propertiesFromSources.containsKey(key)) {
                return propertiesFromSources.get(key);
            }
            if (hasSystemProperty(key)) {
                return System.getProperty(key);
            }
            if (hasEnvironmentProperty(key)) {
                return System.getenv(key);
            }
            return null;
        }

        private static boolean hasSystemProperty(final String key) {
            try {
                return System.getProperties()
                    .containsKey(key);
            } catch (final SecurityException ignored) {
                return false;
            }
        }

        private static boolean hasEnvironmentProperty(final String key) {
            try {
                return System.getenv()
                    .containsKey(key);
            } catch (final SecurityException ignored) {
                return false;
            }
        }

        private boolean containsKey(final String key) {
            return
                propertiesFromSources.containsKey(key) ||
                    hasSystemProperty(key) ||
                    hasEnvironmentProperty(key);
        }
    }


}
