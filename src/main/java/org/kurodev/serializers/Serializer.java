package org.kurodev.serializers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;

public abstract class Serializer implements Closeable, AutoCloseable {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

}
