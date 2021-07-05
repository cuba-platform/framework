/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.server.communication;

import com.vaadin.server.VaadinServlet;
import org.atmosphere.cpr.AtmosphereFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebListener;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

/**
 * Initializer class for JSR 356 websockets.
 * <p>
 * Websocket specification says that initialization of websocket end points
 * should be done in the servlet context initialization phase. Some servers
 * implement this strictly so that end points cannot be registered after the
 * context initialization phase.
 * <p>
 * Note that {@link WebListener} is Servlet 3.0 API so this will not be run for
 * older servers (unless added to web.xml), but these servers do not support JSR
 * 356 websockets either.
 *
 * @since 7.5.0
 * @author Vaadin Ltd
 */
// Haulmont API - disabled
// @WebListener
public class JSR356WebsocketInitializer implements ServletContextListener {

    private static boolean atmosphereAvailable = false;
    static {
        try {
            org.atmosphere.util.Version.getRawVersion();
            atmosphereAvailable = true;
        } catch (NoClassDefFoundError e) {
        }
    }

    /**
     * "ServletConfig" which only provides information from a
     * {@link ServletRegistration} and its {@link ServletContext}.
     */
    public static class FakeServletConfig implements ServletConfig {

        private final ServletRegistration servletRegistration;
        private final ServletContext servletContext;

        public FakeServletConfig(ServletRegistration servletRegistration,
                ServletContext servletContext) {
            this.servletContext = servletContext;
            this.servletRegistration = servletRegistration;
        }

        @Override
        public String getServletName() {
            return servletRegistration.getName();
        }

        @Override
        public ServletContext getServletContext() {
            return servletContext;
        }

        @Override
        public String getInitParameter(String name) {
            String initParameterValue = servletRegistration
                    .getInitParameter(name);
            if (initParameterValue == null
                    && "org.atmosphere.cpr.recoverFromDestroyedBroadcaster"
                            .equals(name)) {
                return "false";
            }
            if (initParameterValue == null
                    && "org.atmosphere.cpr.asyncSupport".equals(name)) {
                // try to detect Jetty 9.3.x
                Class<?> jettyClass = null;
                try {
                    ClassLoader classLoader = servletContext.getClassLoader();
                    jettyClass = classLoader
                            .loadClass("org.eclipse.jetty.util.Jetty");
                } catch (ClassNotFoundException ignored) {
                }

                if (jettyClass != null) {
                    try {
                        Field versionField = jettyClass
                                .getDeclaredField("VERSION");
                        String jettyVersion = (String) versionField.get(null);

                        if (jettyVersion.startsWith("9.3.")) {
                            return "org.atmosphere.container.Jetty93AsyncSupportWithWebSocket";
                        }
                    } catch (NoSuchFieldException | IllegalAccessException
                            | ClassCastException e) {
                        getLogger().error("Unable to determine Jetty version",
                                e);
                    }
                }
            }
            return initParameterValue;
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return Collections.enumeration(
                    servletRegistration.getInitParameters().keySet());
        }

    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        if (servletContext.getMajorVersion() < 3) {
            return;
        }

        init(servletContext);
    }

    /**
     * Initializes Atmosphere for use with Vaadin servlets found in the given
     * context.
     * <p>
     * For JSR 356 websockets to work properly, the initialization must be done
     * in the servlet context initialization phase.
     *
     * @param servletContext
     *            The servlet context
     */
    public void init(ServletContext servletContext) {
        if (!atmosphereAvailable) {
            return;
        }

        Map<String, ? extends ServletRegistration> regs = servletContext
                .getServletRegistrations();
        for (String servletName : regs.keySet()) {
            ServletRegistration servletRegistration = regs.get(servletName);

            if (isVaadinServlet(servletRegistration, servletContext)) {
                try {
                    initAtmosphereForVaadinServlet(servletRegistration,
                            servletContext);
                } catch (Exception e) {
                    getLogger().warn("Failed to initialize Atmosphere for "
                            + servletName, e);
                }
            }
        }
    }

    /**
     * Initializes Atmosphere for use with the given Vaadin servlet
     * <p>
     * For JSR 356 websockets to work properly, the initialization must be done
     * in the servlet context initialization phase.
     *
     * @param servletRegistration
     *            The servlet registration info for the servlet
     * @param servletContext
     */
    public static void initAtmosphereForVaadinServlet(
            ServletRegistration servletRegistration,
            ServletContext servletContext) {
        String servletName = servletRegistration.getName();
        String attributeName = getAttributeName(servletName);

        if (servletContext.getAttribute(attributeName) != null) {
            // Already initialized
            getLogger().warn("Atmosphere already initialized");
            return;
        }
        getLogger().trace("Creating AtmosphereFramework for {}", servletName);

        AtmosphereFramework framework = PushRequestHandler.initAtmosphere(
                new FakeServletConfig(servletRegistration, servletContext));
        servletContext.setAttribute(attributeName, framework);

        getLogger().trace("Created AtmosphereFramework for {}", servletName);

    }

    /**
     * Returns the name of the attribute in the servlet context where the
     * pre-initialized Atmosphere object is stored.
     *
     * @param servletName
     *            The name of the servlet
     * @return The attribute name which contains the initialized Atmosphere
     *         object
     */
    public static String getAttributeName(String servletName) {
        return JSR356WebsocketInitializer.class.getName() + "." + servletName;
    }

    /**
     * Checks if the given attribute name matches the convention used for
     * storing AtmosphereFramework references.
     *
     * @param attributeName
     *            the attribute name to check
     * @return <code>true</code> if the attribute name matches the convention,
     *         <code>false</code> otherwise
     */
    private static boolean isAtmosphereFrameworkAttribute(
            String attributeName) {
        return attributeName
                .startsWith(JSR356WebsocketInitializer.class.getName() + ".");
    }

    /**
     * Tries to determine if the given servlet registration refers to a Vaadin
     * servlet.
     *
     * @param servletRegistration
     *            The servlet registration info for the servlet
     * @return false if the servlet is definitely not a Vaadin servlet, true
     *         otherwise
     */
    protected boolean isVaadinServlet(ServletRegistration servletRegistration,
            ServletContext servletContext) {
        try {
            String servletClassName = servletRegistration.getClassName();
            if (servletClassName.equals("com.ibm.ws.wsoc.WsocServlet")) {
                // Websphere servlet which implements websocket endpoints,
                // dynamically added
                return false;
            }
            // Must use servletContext class loader to load servlet class to
            // work correctly in an OSGi environment (#20024)
            Class<?> servletClass = servletContext.getClassLoader()
                    .loadClass(servletClassName);
            return VaadinServlet.class.isAssignableFrom(servletClass);
        } catch (Exception e) {
            // This will fail in OSGi environments, assume everything is a
            // VaadinServlet
            return true;
        }
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(JSR356WebsocketInitializer.class);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Destroy any AtmosphereFramework instance we have initialized.
        // This must be done here to ensure that we cleanup Atmosphere instances
        // related to servlets which are never initialized
        ServletContext servletContext = sce.getServletContext();
        Enumeration<String> attributeNames = servletContext.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            if (isAtmosphereFrameworkAttribute(attributeName)) {
                Object value = servletContext.getAttribute(attributeName);
                if (value instanceof AtmosphereFramework) {
                    // This might result in calling destroy() twice, once from
                    // here and once from PushRequestHandler but
                    // AtmosphereFramework.destroy() deals with that
                    ((AtmosphereFramework) value).destroy();
                }
            }
        }
    }

    /**
     * Checks if Atmosphere is available on the classpath.
     *
     * @return <code>true</code> if Atmosphere is available, <code>false</code>
     *         otherwise
     */
    public static boolean isAtmosphereAvailable() {
        return atmosphereAvailable;
    }
}
