package com.animallogic.server.conf;

import com.animallogic.markovchain.fsm.TextFiniteStateMachineFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonBeans {
    @Bean
    public TextFiniteStateMachineFactory createTextFiniteStateMachineFactory() {
        return new TextFiniteStateMachineFactory();
    }
}
