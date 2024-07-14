package com.diplom.faces_recognition.utils.log;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class LoggerImpl implements ILog {

    private static Logger logger;
    private static ILog instance;

    @Override
    public ILog init(Class<?> className){

        if ( instance == null){
            logger = LoggerFactory.getLogger( className );
            return new LoggerImpl();
        }

        return instance;
    }


    @Override
    public void warn(String message) {
       logger.warn(message);
    }

    @Override
    public void error(String message) {
      logger.error(message);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }
}
