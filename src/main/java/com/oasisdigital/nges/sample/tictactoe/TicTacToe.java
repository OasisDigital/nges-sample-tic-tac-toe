package com.oasisdigital.nges.sample.tictactoe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "com.oasisdigital.nges.sample")
public class TicTacToe {

    @Bean
    public EmbeddedServletContainerCustomizer servletContainer() {
        return new EmbeddedServletContainerCustomizer() {
            @Override
            public void customize(ConfigurableEmbeddedServletContainer container) {
                // By default Tomcat terminates the connection after 30s. Browser should reconnect by itself
                // in such case, but we can avoid the waste by removing the timeout.
                TomcatEmbeddedServletContainerFactory tomcat = (TomcatEmbeddedServletContainerFactory) container;
                tomcat.addConnectorCustomizers(connector -> connector.setAsyncTimeout(0));
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(TicTacToe.class, args);
    }
}
