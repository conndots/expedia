package cn.edu.nju.ws.expedia.util;

/**
 * Created by Xiangqian on 2014/11/16.
 */
public class ThreeTuple<F, S, T> extends TwoTuple<F, S> {
    protected T thirdElem = null;

    public ThreeTuple(F f, S s, T t){
        super(f, s);
        this.thirdElem = t;
    }

    @Deprecated
    @Override
    public S getValue(){
        return this.secondElem;
    }

    public T getThird(){
        return this.thirdElem;
    }

    @Override
    public int hashCode(){
        if(this.hashCode != -1){
            return this.hashCode;
        }

        this.hashCode = 23;
        this.hashCode = 31 * this.hashCode + this.firstElem.hashCode();
        this.hashCode = 31 * this.hashCode + this.secondElem.hashCode();
        this.hashCode = 31 * this.hashCode + this.thirdElem.hashCode();
        return this.hashCode;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof ThreeTuple){
            ThreeTuple<F, S, T> tt = (ThreeTuple<F, S, T>) o;
            return this.firstElem.equals(tt.secondElem) && this.secondElem.equals(tt.secondElem)
                    && this.thirdElem.equals(tt.thirdElem);
        }
        return false;
    }

    @Override
    public String toString(){
        StringBuilder ret = new StringBuilder();
        return ret.append("<").append(this.firstElem.toString()).append(", ").append(this.secondElem.toString())
                .append(", ").append(this.thirdElem.toString()).append(">").toString();
    }
}
