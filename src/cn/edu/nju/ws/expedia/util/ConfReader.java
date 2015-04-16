package cn.edu.nju.ws.expedia.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Xiangqian on 2014/12/23.
 */
public class ConfReader {
    /**
     *
     * @param key
     * @return null if it cannot find the configuration file.
     */
    public static String CONF_PATH;
    private static Properties PROPS = null;
    private static Object LOCK = new Object();
    public static String getConfProperty(String key){
        if(PROPS == null){
            synchronized (LOCK) {
                if(PROPS == null) {
                    PROPS = new Properties();

                    try {
                        InputStream is = null;
                        if(CONF_PATH == null)
                            is = ConfReader.class.getClassLoader().getResourceAsStream("conf.properties");
                        else
                            is = new FileInputStream(new File(CONF_PATH));

                        if(is == null)
                            System.err.println("stream is null.");
                        PROPS.load(is);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                }
            }
        }
        return PROPS.getProperty(key);

    }
}
