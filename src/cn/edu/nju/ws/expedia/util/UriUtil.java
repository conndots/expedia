package cn.edu.nju.ws.expedia.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Xiangqian Lee
 * Created by Xiangqian on 2014/11/16.
 */
public class UriUtil {
    /**
     *get the localname of the given uri
     * @param uri
     * @return
     */
    public static String getLocalnameFromUri(String uri){
        String localname = null;
        int posHash = uri.lastIndexOf('#');
        if(posHash != -1){
            if(posHash != uri.length() - 1)
                localname = uri.substring(posHash+1);
            else{
                localname = getLocalnameFromUri(uri.substring(0, posHash));
            }

        }else{
            int lastIndex = uri.lastIndexOf("/");
            while(lastIndex != -1 && lastIndex == uri.length() - 1) {
                uri = uri.substring(0, uri.length() - 1);
                lastIndex = uri.lastIndexOf("/");
            }
            if(uri.lastIndexOf("/") != -1)
                localname = uri.substring(uri.lastIndexOf("/") + 1);
            else if(uri.lastIndexOf(":") != -1)
                localname = uri.substring(uri.lastIndexOf(":") + 1);
            else
                localname = uri;
        }
        // to decode the specific codes in the uri
            try {
                localname = URLDecoder.decode(localname, "utf-8");
            } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            }
        // to split the uris by "_"
        localname = localname.replaceAll("_", " ");

        //对于yago的类型URI进行特殊处理
        if(uri.startsWith("http://dbpedia.org/class/yago/")){
            Pattern pattern = Pattern.compile("(^([^0-9]+)[0-9]+$)");
            Matcher matcher = pattern.matcher(localname);
            if(matcher.find()){
                localname = matcher.group(2);
            }
        }
        return localname;
    }

    /**
     * transform the localname to a human readable version.
     * @param localname
     * @param separator
     * @return
     */
    public static String localnameSplit(String localname, String separator){
        if(localname == null)
            return "";
        if(localname.length() == 0)
            return "";
        localname = localname.trim().replaceAll("_", separator);
        localname = localname.replaceAll("\\.", separator);
        localname = localname.replaceAll("[/]+", separator);

        String temp = new String(localname);
        StringBuilder result = new StringBuilder();
        Pattern patt = Pattern.compile("[a-zA-Z0-9\\-]+");
        Matcher matcher = patt.matcher(temp);
        while(matcher.find()){
            result.append(matcher.group()).append(separator);
        }
        if(result.length() == 0)
            return "";

        StringBuilder res = new StringBuilder();
        res.append(result.charAt(0));
        for(int i = 1; i < result.length(); i++){
            char ch = result.charAt(i);
            String mat = result.substring(((i > 0) ? (i - 1) : 0), (i < result.length() - 1) ? (i + 2) : result.length());
            if(mat.matches("[a-z0-9][A-Z][a-z0-9]([a-z0-9]|\\b)")){
                String curr = ("" + ch).toLowerCase();
                res.append(separator).append(curr);
            }
            else
                res.append(ch);
        }

        boolean midOfNum = false;
        StringBuilder ret = new StringBuilder();
        for(int i = 0; i < res.length(); i++){
            char ch = res.charAt(i);
            if(ch >= '0' && ch <= '9'){
                if(! midOfNum){
                    ret.append(separator);
                    midOfNum = true;
                }
            }
            else{
                if(midOfNum){
                    ret.append(separator);
                    midOfNum = false;
                }
            }
            ret.append(ch);
        }

        String toReturn = ret.toString().trim();
        return toReturn;
    }

}
