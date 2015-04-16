package cn.edu.nju.ws.expedia.util;

import java.util.Map;

/**
 * Created by Xiangqian on 2014/11/16.
 */
public class TwoTuple<K, V> implements Map.Entry<K, V> {
    protected K firstElem = null;
    protected V secondElem = null;
    protected int hashCode = -1;

    public TwoTuple(K k, V v) {
        this.firstElem = k;
        this.secondElem = v;
    }

    @Override
    public K getKey(){
        return this.firstElem;
    }

    @Override
    public V getValue() {
        return this.secondElem;
    }

    @Deprecated
    @Override
    public V setValue(V value) {
        V temp = this.secondElem;
        this.secondElem = value;
        return temp;
    }

    /**
     *
     * @return
     */
    public K getFirst(){
        return this.firstElem;
    }

    /**
     *
     * @return
     */
    public V getSecond(){
        return this.secondElem;
    }

    @Override
    public int hashCode(){
        if(this.hashCode != -1) {
            return this.hashCode;
        }
        this.hashCode = 23;
        this.hashCode = 31 * this.hashCode + (firstElem == null ? 0 : firstElem.hashCode());
        this.hashCode = 31 * this.hashCode + (secondElem == null ? 0 : secondElem.hashCode());
        return this.hashCode;
    }

    @Override
    public boolean equals(Object o){
        if(! (o instanceof TwoTuple)){
            return false;
        }
        TwoTuple<K, V> tt = (TwoTuple<K, V>) o;
        return this.firstElem.equals(tt.firstElem) && this.secondElem.equals(tt.secondElem);
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append("<").append(this.firstElem.toString()).append(", ").append(this.secondElem.toString()).append(">");
        return str.toString();
    }
}
