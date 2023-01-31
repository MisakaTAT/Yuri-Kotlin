package com.mikuac.yuri.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author zero
 */
@Component
public class BeanUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext = null;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (BeanUtils.applicationContext == null) {
            BeanUtils.applicationContext = applicationContext;
        }
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    public static <T> T getBean(Class<T> c) {
        return applicationContext.getBean(c);
    }

    public static <T> T getBean(String name, Class<T> c) {
        return getApplicationContext().getBean(name, c);
    }

}
