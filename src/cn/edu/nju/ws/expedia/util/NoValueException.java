package cn.edu.nju.ws.expedia.util;

/**
 * Created by Xiangqian on 2015/4/12.
 */
public class NoValueException extends Exception {
    public String toString() {
        return "There is no value for your key.";
    }
}
