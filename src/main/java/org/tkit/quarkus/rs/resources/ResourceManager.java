/*
 * Copyright 2020 tkit.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tkit.quarkus.rs.resources;


import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.tkit.quarkus.rs.exceptions.RestException;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The resource manager for messages.
 */
@Slf4j
public class ResourceManager {

    /**
     * The quarkus config variable for default locale.
     */
    private static final String QUARKUS_DEFAULT_LOCALE = "quarkus.default-locale";

    /**
     * The quarkus default locale.
     */
    private static final Locale DEFAULT_LOCALE = new Locale(
            ConfigProvider.getConfig()
                    .getOptionalValue(QUARKUS_DEFAULT_LOCALE, String.class)
                    .orElse(Locale.ENGLISH.getLanguage())
    );

    /**
     * Gets the message for the enum key and parameters
     *
     * @param key    the {@link RestException}
     * @param locale the locale
     * @param params the list of parameters.
     * @return the corresponding message.
     */
    public static String getMessage(Enum<?> key, Locale locale, List<Object> params) {
        String resourceBundleName = key.getClass().getSimpleName();
        ClassLoader classLoader = key.getClass().getClassLoader();

        ResourceBundle bundle = null;
        if (locale != null) {
            bundle = getBundle(resourceBundleName, locale, classLoader);
        }
        if (bundle == null) {
            locale = DEFAULT_LOCALE;
            bundle = getBundle(resourceBundleName, locale, classLoader);
        }
        if (bundle == null) {
            return key.name() + params;
        }

        String tr = bundle.getString(key.name());
        if (params == null || params.isEmpty()) {
            return tr;
        }

        MessageFormat msgFormat = new MessageFormat(tr, locale);
        StringBuffer bf = msgFormat.format(params.toArray(), new StringBuffer(), null);
        return bf.toString();
    }

    /**
     * Gets the resource bundle for the bundle name and locale.
     *
     * @param bundleName the bundle name.
     * @param locale     the locale.
     * @param loader     the class-loader.
     * @return corresponding resoruce bundle or {@code null}.
     */
    private static ResourceBundle getBundle(final String bundleName, final Locale locale, final ClassLoader loader) {
        try {
            if (loader != null && locale != null) {
                return ResourceBundle.getBundle(bundleName, locale, loader);
            } else if (locale != null) {
                return ResourceBundle.getBundle(bundleName, locale);
            } else {
                return ResourceBundle.getBundle(bundleName);
            }
        } catch (MissingResourceException e) {
            log.debug("Error loading the bundle {} for the locale {}", bundleName, locale);
        }
        return null;
    }


}
