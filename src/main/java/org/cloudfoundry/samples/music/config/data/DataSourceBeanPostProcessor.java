package org.cloudfoundry.samples.music.config.data;

import com.microsoft.sqlserver.jdbc.SQLServerColumnEncryptionKeyStoreProvider;
import com.microsoft.sqlserver.jdbc.SQLServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import com.microsoft.sqlserver.jdbc.SQLServerColumnEncryptionAzureKeyVaultProvider;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Configuration;

@Component
//@ConditionalOnProperty(name = "spring.datasource.dataSourceProperties.ColumnEncryptionSetting", havingValue = "Enabled", matchIfMissing = false)
public class DataSourceBeanPostProcessor implements BeanPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceBeanPostProcessor.class);

        
	    private String clientId = "ee1d870d-993d-4edd-b536-34ad3e476586";
	    
	    private String clientSecret = "mNs5pjxlVj+n5P.kw5_2/_r06q=u1lV.";

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        if (bean instanceof DataSource) {
            if ( clientId ==null || clientId.isEmpty() || clientSecret == null || clientSecret.isEmpty() )
                throw new FatalBeanException("AlwaysEncrypted feature requires Service Principal that has access to Vaulr - setup clientId and ClientSecret");

            try {
                logger.info ("initializing DataSource AlwaysEncryption Vault provider");
                SQLServerColumnEncryptionAzureKeyVaultProvider akvProvider =
                        new SQLServerColumnEncryptionAzureKeyVaultProvider(clientId, clientSecret);

                Map<String, SQLServerColumnEncryptionKeyStoreProvider> keyStoreMap = new HashMap<String, SQLServerColumnEncryptionKeyStoreProvider>();
                keyStoreMap.put(akvProvider.getName(), akvProvider);

                SQLServerConnection.registerColumnEncryptionKeyStoreProviders(keyStoreMap);

            } catch (SQLException ex) {
                logger.error(ex.getMessage());
                throw new FatalBeanException(ex.getMessage());
            }
        }
        return bean;
    }
}