package com.diplom.faces_recognition.utils.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerImpl implements ILog {

    private static Logger logger;
    private static ILog instance;

    public LoggerImpl(){
        logger = LoggerFactory.getLogger( this.getClass() );
    }
    @Override
    public ILog init(Class<?> className){

        if ( instance == null){
            logger = LoggerFactory.getLogger( className );
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
