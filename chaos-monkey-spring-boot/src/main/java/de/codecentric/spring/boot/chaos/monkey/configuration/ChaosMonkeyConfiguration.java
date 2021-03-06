/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.spring.boot.chaos.monkey.configuration;

import de.codecentric.spring.boot.chaos.monkey.component.ChaosMonkey;
import de.codecentric.spring.boot.chaos.monkey.conditions.AttackComponentCondition;
import de.codecentric.spring.boot.chaos.monkey.conditions.AttackControllerCondition;
import de.codecentric.spring.boot.chaos.monkey.conditions.AttackRestControllerCondition;
import de.codecentric.spring.boot.chaos.monkey.conditions.AttackServiceCondition;
import de.codecentric.spring.boot.chaos.monkey.endpoints.ChaosMonkeyJmxEndpoint;
import de.codecentric.spring.boot.chaos.monkey.endpoints.ChaosMonkeyRestEndpoint;
import de.codecentric.spring.boot.chaos.monkey.watcher.SpringComponentAspect;
import de.codecentric.spring.boot.chaos.monkey.watcher.SpringControllerAspect;
import de.codecentric.spring.boot.chaos.monkey.watcher.SpringRestControllerAspect;
import de.codecentric.spring.boot.chaos.monkey.watcher.SpringServiceAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Benjamin Wilms
 */
@Configuration
@Profile("chaos-monkey")
@EnableConfigurationProperties({ChaosMonkeyProperties.class,AssaultProperties.class, WatcherProperties.class})
public class ChaosMonkeyConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChaosMonkey.class);
    private final ChaosMonkeyProperties chaosMonkeyProperties;
    private final WatcherProperties watcherProperties;
    private final AssaultProperties assaultProperties;


    public ChaosMonkeyConfiguration(ChaosMonkeyProperties chaosMonkeyProperties, WatcherProperties watcherProperties,
                                    AssaultProperties assaultProperties) {
        this.chaosMonkeyProperties = chaosMonkeyProperties;
        this.watcherProperties = watcherProperties;
        this.assaultProperties = assaultProperties;

        try {
            String chaosLogo = StreamUtils.copyToString(new ClassPathResource("chaos-logo.txt").getInputStream(), Charset.defaultCharset());
            LOGGER.info(chaosLogo);
        } catch (IOException e) {
            LOGGER.info("Chaos Monkey - ready to do evil");
        }

    }

    @Bean
    public ChaosMonkeySettings settings() {
        return new ChaosMonkeySettings(chaosMonkeyProperties, assaultProperties, watcherProperties);
    }

    @Bean
    public ChaosMonkey chaosMonkey() {
        return new ChaosMonkey(settings());
    }

    @Bean
    @Conditional(AttackControllerCondition.class)
    public SpringControllerAspect controllerAspect() {
        return new SpringControllerAspect(chaosMonkey());
    }

    @Bean
    @Conditional(AttackRestControllerCondition.class)
    public SpringRestControllerAspect restControllerAspect() {
        return new SpringRestControllerAspect(chaosMonkey());
    }

    @Bean
    @Conditional(AttackServiceCondition.class)
    public SpringServiceAspect serviceAspect() {
        return new SpringServiceAspect(chaosMonkey());
    }

    @Bean
    @Conditional(AttackComponentCondition.class)
    public SpringComponentAspect componentAspect() {
        return new SpringComponentAspect(chaosMonkey());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnEnabledEndpoint
    public ChaosMonkeyRestEndpoint chaosMonkeyRestEndpoint() {
        return new ChaosMonkeyRestEndpoint(settings());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnEnabledEndpoint
    public ChaosMonkeyJmxEndpoint chaosMonkeyJmxEndpoint() {
        return new ChaosMonkeyJmxEndpoint(settings());
    }
}
