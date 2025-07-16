package ru.innopolis.gtmtechcard;

/**
 * Created by ai.khafizov
 * on 31.08.2022
 */

import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.ext.ScriptUtils;
import org.testcontainers.jdbc.JdbcDatabaseDelegate;
import org.testcontainers.junit.jupiter.Testcontainers;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTest.DockerPostgreDataSourceInitializer.class)
@Testcontainers
@Slf4j
public class AbstractTest {

    public static PostgreSQLContainer<?> postgreDBContainer = new PostgreSQLContainer<>("postgres:13:3");

    static {
//        postgreDBContainer
//                .withInitScript("./db/dump-gtm_develop-202208310938-icp.sql");
        postgreDBContainer.start();
        log.info("******************************** container started *************************************");
        var containerDelegate = new JdbcDatabaseDelegate(postgreDBContainer, "");

        ScriptUtils.runInitScript(containerDelegate, "./db/dump-gtm_develop-202208310954-icp-1.sql");
        log.info("******************************** script 1 *************************************");
        ScriptUtils.runInitScript(containerDelegate, "./db/dump-gtm_develop-202208310954-icp-2.sql");
        log.info("******************************** script 2 *************************************");
        ScriptUtils.runInitScript(containerDelegate, "./db/dump-gtm_develop-202208310954-icp-3.sql");
        log.info("******************************** script 3 *************************************");
        ScriptUtils.runInitScript(containerDelegate, "./db/dump-gtm_develop-202208310957-cdm.sql");
        log.info("******************************** scripts execute completed *************************************");
    }

    public static class DockerPostgreDataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            log.info("******************************" +
                    "url: " + postgreDBContainer.getJdbcUrl() +
                    " username:" + postgreDBContainer.getUsername() +
                    " password:" + postgreDBContainer.getPassword());
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.datasource.url=" + postgreDBContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreDBContainer.getUsername(),
                    "spring.datasource.password=" + postgreDBContainer.getPassword()
            );
        }
    }

}
