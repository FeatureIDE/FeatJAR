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
package de.featjar.base.extension;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Maps;
import de.featjar.base.data.Result;
import de.featjar.base.data.Sets;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Searches, installs, and uninstalls extension points and extensions defined on
 * the classpath. An {@link AExtensionPoint} or {@link IExtension} can be
 * defined on the classpath by registering it in
 * {@code resources/extensions.xml}.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class ExtensionManager implements AutoCloseable {

    private static class ExtensionPointEntry {
        private final String id;
        private int priority;
        private final LinkedHashSet<String> extensions = new LinkedHashSet<>();

        public ExtensionPointEntry(String id, int priority) {
            this.id = id;
            this.priority = priority;
        }

        public int priority() {
            return priority;
        }
    }

    private final LinkedHashMap<String, AExtensionPoint<?>> extensionPoints = Maps.empty();
    private final LinkedHashMap<String, IExtension> extensions = Maps.empty();

    /**
     * Installs all extensions and extension points that can be found on the
     * classpath. To this end, filters all files on the classpath for extension
     * definition files, and loads each of them.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void install() {
        FeatJAR.log().debug("initializing extension manager");
        final LinkedHashMap<String, ExtensionPointEntry> extensionMap = new LinkedHashMap();
        getResources().stream()
                .filter(ExtensionManager::filterByFileName)
                .forEach(f -> loadExtensionDefinitionFile(f, extensionMap));
        List<ExtensionPointEntry> extensionPointEntries = new ArrayList<>(extensionMap.values());
        Collections.sort(
                extensionPointEntries,
                Comparator.comparing(ExtensionPointEntry::priority).reversed());
        final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        for (final ExtensionPointEntry entry : extensionPointEntries) {
            final String extensionPointId = entry.id;
            try {
                final Class<AExtensionPoint<?>> extensionPointClass =
                        (Class<AExtensionPoint<?>>) systemClassLoader.loadClass(extensionPointId);
                FeatJAR.log().debug("installing extension point " + extensionPointClass.getName());
                final AExtensionPoint ep = extensionPointClass.getConstructor().newInstance();
                extensionPoints.put(ep.getIdentifier(), ep);
                for (final String extensionId : entry.extensions) {
                    try {
                        final Class<IExtension> extensionClass =
                                (Class<IExtension>) systemClassLoader.loadClass(extensionId);
                        FeatJAR.log().debug("installing extension " + extensionClass.getName());
                        IExtension e = extensionClass.getConstructor().newInstance();
                        ep.installExtension(e);
                        extensions.put(e.getIdentifier(), e);
                    } catch (final ClassNotFoundException e) {
                        FeatJAR.log()
                                .error(new Exception(
                                        String.format(
                                                "Could not find class %s for extension point %s",
                                                extensionId, extensionPointClass.getName()),
                                        e));
                    } catch (final NoSuchMethodException e) {
                        FeatJAR.log()
                                .error(new Exception(
                                        String.format(
                                                "Missing default constructor for class %s for extension point %s",
                                                extensionId, extensionPointClass.getName()),
                                        e));
                    } catch (final IllegalAccessException e) {
                        FeatJAR.log()
                                .error(new Exception(
                                        String.format(
                                                "Inaccesible default constructor for class %s for extension point %s",
                                                extensionId, extensionPointClass.getName()),
                                        e));
                    } catch (final InvocationTargetException | ExceptionInInitializerError e) {
                        FeatJAR.log()
                                .error(new Exception(
                                        String.format(
                                                "Internal problem when instantiating class %s for extension point %s",
                                                extensionId, extensionPointClass.getName()),
                                        e));
                    } catch (final Exception e) {
                        FeatJAR.log().error(e);
                    }
                }
            } catch (final Exception e) {
                FeatJAR.log().error(e);
            }
        }
        extensionMap.clear();
    }

    /**
     * Uninstalls all currently installed extensions and extension points.
     */
    @Override
    public void close() {
        extensionPoints.values().forEach(AExtensionPoint::close);
    }

    /**
     * {@return whether the file with the given name is an extension definition
     * file}
     *
     * @param pathName the file name
     */
    private static boolean filterByFileName(String pathName) {
        try {
            if (pathName != null) {
                Path fileName = Paths.get(pathName).getFileName();
                return fileName != null && fileName.toString().matches("extensions(-.*)?[.]xml");
            }
            return false;
        } catch (final Exception e) {
            FeatJAR.log().error(e);
            return false;
        }
    }

    /**
     * Installs all extensions from a given extension definition file.
     *
     * @param file the extension definition file
     */
    protected void loadExtensionDefinitionFile(String file, LinkedHashMap<String, ExtensionPointEntry> extensionMap) {
        FeatJAR.log().debug("loading extension definition file " + file);
        try {
            final Enumeration<URL> systemResources =
                    ClassLoader.getSystemClassLoader().getResources(file);
            while (systemResources.hasMoreElements()) {
                try {
                    final DocumentBuilder documentBuilder =
                            DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    final Document document =
                            documentBuilder.parse(systemResources.nextElement().openStream());
                    document.getDocumentElement().normalize();

                    final NodeList points = document.getElementsByTagName("point");
                    for (int i = 0; i < points.getLength(); i++) {
                        try {
                            final Node point = points.item(i);
                            if (point.getNodeType() == Node.ELEMENT_NODE) {
                                final Element pointElement = (Element) point;
                                final String extensionPointId = pointElement.getAttribute("id");
                                String extensionPointPriorityString = pointElement.getAttribute("priority");
                                final int extensionPointPriority = extensionPointPriorityString.isEmpty()
                                        ? 0
                                        : Integer.parseInt(extensionPointPriorityString);
                                ExtensionPointEntry extensionPoint = extensionMap.computeIfAbsent(
                                        extensionPointId,
                                        k -> new ExtensionPointEntry(extensionPointId, extensionPointPriority));
                                if (extensionPoint.priority < extensionPointPriority)
                                    extensionPoint.priority = extensionPointPriority;
                                final NodeList extensions = pointElement.getChildNodes();
                                for (int j = 0; j < extensions.getLength(); j++) {
                                    final Node extension = extensions.item(j);
                                    if (extension.getNodeType() == Node.ELEMENT_NODE) {
                                        final Element extensionElement = (Element) extension;
                                        final String extensionId = extensionElement.getAttribute("id");
                                        extensionPoint.extensions.add(extensionId);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            FeatJAR.log().error(e);
                            continue;
                        }
                    }
                } catch (final Exception e) {
                    FeatJAR.log().error(e);
                }
            }
        } catch (final Exception e) {
            FeatJAR.log().error(e);
        }
    }

    /**
     * {@return all names of files on the classpath}
     */
    private static LinkedHashSet<String> getResources() {
        final LinkedHashSet<String> resources = Sets.empty();
        final String classPathProperty = System.getProperty("java.class.path", ".");
        final String pathSeparatorProperty = System.getProperty("path.separator");
        for (final String element : classPathProperty.split(pathSeparatorProperty)) {
            final Path path = Paths.get(element);
            try {
                if (Files.isRegularFile(path)) {
                    try (ZipFile zf = new ZipFile(path.toFile())) {
                        zf.stream().map(ZipEntry::getName).forEach(resources::add);
                    }
                } else if (Files.isDirectory(path)) {
                    try (Stream<Path> pathStream = Files.walk(path)) {
                        pathStream.map(path::relativize).map(Path::toString).forEach(resources::add);
                    }
                }
            } catch (final IOException e) {
                FeatJAR.log().error(e);
            }
        }
        return resources;
    }

    /**
     * {@return all installed extension points}
     */
    public Collection<AExtensionPoint<?>> getExtensionPoints() {
        return extensionPoints.values();
    }

    /**
     * {@return all installed extensions}
     */
    public Collection<IExtension> getExtensions() {
        return extensions.values();
    }

    /**
     * {@return the installed extension point with a given identifier, if any}
     *
     * @param identifier the identifier
     */
    public Result<AExtensionPoint<?>> getExtensionPoint(String identifier) {
        Objects.requireNonNull(identifier, "identifier must not be null!");
        final AExtensionPoint<?> extensionPoint = extensionPoints.get(identifier);
        return extensionPoint != null ? Result.of(extensionPoint) : Result.empty();
    }

    /**
     * {@return the installed extension point for a given class, if any}
     *
     * @param klass the class
     */
    @SuppressWarnings("unchecked")
    public <T extends AExtensionPoint<?>> Result<T> getExtensionPoint(Class<T> klass) {
        return (Result<T>) getExtensionPoint(klass.getCanonicalName());
    }

    /**
     * {@return the installed extension point with a given identifier, if any}
     *
     * @param identifier the identifier
     */
    public Result<IExtension> getExtension(String identifier) {
        Objects.requireNonNull(identifier, "identifier must not be null!");
        final IExtension extension = extensions.get(identifier);
        return extension != null ? Result.of(extension) : Result.empty();
    }

    /**
     * {@return the installed extension point for a given class, if any}
     *
     * @param klass the class
     */
    @SuppressWarnings("unchecked")
    public <T extends IExtension> Result<T> getExtension(Class<T> klass) {
        return (Result<T>) getExtension(klass.getCanonicalName());
    }
}
