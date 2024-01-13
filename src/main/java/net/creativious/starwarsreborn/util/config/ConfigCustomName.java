package net.creativious.starwarsreborn.util.config;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigCustomName {
    String value();
}
