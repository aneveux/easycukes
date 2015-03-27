package com.worldline.easycukes.commons.config;

import com.worldline.easycukes.commons.config.beans.CommonConfigurationBean;
import org.apache.commons.httpclient.HostConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;

/**
 * EasyCukesConfiguration deals with all the common configuration for all EasyCukes projects.
 * It allows to load and manage the EasyCukes configuration file (easycukes.yml), and make use of its values for configuring SSL or the proxy.
 * You can extend this class and implement the #computeConstructor method in order to extend the configuration file.
 * <p/>
 * You can specify using generics the type of Configuration Bean you need to use. It needs to extend CommonConfigurationBean though.
 *
 * @author aneveux
 * @version 3.0
 */
public class EasyCukesConfiguration<E extends CommonConfigurationBean> {

    /**
     * POJO representation of the YML file parsing. Allows to store the actual values contained in the configuration file.
     */
    protected E configurationBean;

    /**
     * This field allows to store the type of the generic which is used for the ConfigurationBean
     */
    final Class<E> typeParameterClass;

    /**
     * Constant refering to the EasyCukes configuration file location.
     */
    public final String CONFIGURATION_FILE = "/easycukes.yml";

    /**
     * SLF4J Logger
     */
    private final Logger LOG = LoggerFactory.getLogger(EasyCukesConfiguration.class);

    /**
     * Default constructor allowing to load the YML file
     */
    public EasyCukesConfiguration(Class<E> typeParameterClass) {
        this.typeParameterClass = typeParameterClass;
        displayFrameworkName();
        LOG.info("[EASYCUKES] Loading configuration...");
        LOG.debug("Searching for configuration file in the classpath...");
        final InputStream is = EasyCukesConfiguration.class.getResourceAsStream(CONFIGURATION_FILE);
        if (is != null) {
            LOG.debug("Configuration file found and loaded...");
            LOG.debug("Starting YAML parsing...");
            Yaml yaml = new Yaml(new Constructor(typeParameterClass));
            LOG.debug("Successfully loaded YAML parsing... Now loading YAML file content...");
            configurationBean = (E) yaml.load(is);
            if (configurationBean != null)
                LOG.info("[EASYCUKES] Configuration file successfully loaded!");
            else
                LOG.error("Error while loading EasyCukes configuration file content...");
            try {
                is.close();
            } catch(IOException ioe) {
                LOG.error("Error while trying to close the InputStream: "+ioe.getMessage(),ioe);
            }
        } else {
            LOG.error("Cannot find EasyCukes configuration file in the classpath...");
        }
    }

    /**
     * Returns the CommonConfigurationBean object computed from the YML file
     *
     * @return #configurationBean an object representation of the configuration file values
     */
    public E getValues() {
        return configurationBean;
    }

    /**
     * Allows to know whether the proxy is needed or not
     *
     * @return true if the proxy is needed, as mentioned in the configuration file
     */
    public boolean isProxyNeeded() {
        return configurationBean.proxy != null && configurationBean.proxy.enabled;
    }

    /**
     * Configures the proxy using System properties, then returns an HostConfiguration object containing the proxy definition
     *
     * @return an instance of HostConfiguration containing the proxy definition, as per defined in the configuration file
     */
    public HostConfiguration configureProxy() {
        if (isProxyNeeded()) {
            LOG.info("[EASYCUKES] Proxy required: Configuring HTTP Client...");
            HostConfiguration hostCfg = new HostConfiguration();
            hostCfg.setProxy(configurationBean.proxy.host, configurationBean.proxy.port);
            LOG.info(String.format("[EASYCUKES] Now using proxy: %s:%d", configurationBean.proxy.host, configurationBean.proxy.port));
            System.setProperty("https.proxyHost", configurationBean.proxy.host);
            System.setProperty("https.proxyPort", Integer.toString(configurationBean.proxy.port));
            return hostCfg;
        }
        return null;
    }

    /**
     * Allows to configure the SSL using some System properties. Values to be used are retrieved from the configuration file.
     */
    public void configureSSL() {
        if (configurationBean.ssl != null) {
            LOG.info("[EASYCUKES] Configuring SSL...");
            if (configurationBean.ssl.keystore != null && configurationBean.ssl.keystore.length() > 0) {
                LOG.debug("Defining keystore...");
                System.setProperty("javax.net.ssl.keyStore", configurationBean.ssl.keystore);
                if (configurationBean.ssl.keystore_password != null && configurationBean.ssl.keystore_password.length() > 0) {
                    LOG.debug("Providing password required by SSL Keystore...");
                    System.setProperty("javax.net.ssl.keyStorePassword", configurationBean.ssl.keystore_password);
                }
            }
            if (configurationBean.ssl.truststore != null && configurationBean.ssl.truststore.length() > 0) {
                LOG.debug("Defining truststore...");
                System.setProperty("javax.net.ssl.trustStore", configurationBean.ssl.truststore);
                if (configurationBean.ssl.truststore_password != null && configurationBean.ssl.truststore_password.length() > 0) {
                    LOG.debug("Providing password required by SSL TrustStore...");
                    System.setProperty("javax.net.ssl.trustStorePassword", configurationBean.ssl.truststore_password);
                }
            }
            LOG.info("[EASYCUKES] SSL properly configured...");
        }
    }

    /**
     * Just an useless stuff allowing to display some ascii art...
     */
    private void displayFrameworkName() {
        LOG.info("-------------------------------------");
        LOG.info("                          _          ");
        LOG.info(" ___ __ _ ____  _ __ _  _| |_____ ___");
        LOG.info("/ -_) _` (_-< || / _| || | / / -_|_-<");
        LOG.info("\\___\\__,_/__/\\_, \\__|\\_,_|_\\_\\___/__/");
        LOG.info("             |__/                    ");
        LOG.info("-------------------------------------");
        LOG.info("[EASYCUKES] Starting...");
    }

}