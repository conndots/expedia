package cn.edu.nju.ws.expedia.util;

/**
 * Created by Xiangqian on 2014/11/16.
 */
public class FourTuple<F, S, T, O> extends ThreeTuple<F, S, T> {
    protected O fourthElem = null;
    public FourTuple(F f, S s, T t, O o) {
        super(f, s, t);
        this.fourthElem = o;
    }

    public O getFourth(){
        return this.fourthElem;
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
        this.hashCode = 31 * this.hashCode + this.fourthElem.hashCode();
        return this.hashCode;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof FourTuple){
            FourTuple<F, S, T, O> tuple = (FourTuple<F, S, T, O>) o;
            return tuple.firstElem.equals(this.firstElem) && tuple.secondElem.equals(this.secondElem)
                    && tuple.thirdElem.equals(this.thirdElem) && tuple.fourthElem.equals(this.fourthElem);
        }
        return false;
    }

    @Override
    public String toString(){
        StringBuilder ret = new StringBuilder();
        return ret.append("<").append(this.firstElem.toString()).append(", ")
                .append(this.secondElem.toString()).append(", ")
                .append(this.thirdElem.toString()).append(", ")
                .append(this.fourthElem.toString()).append(">").toString();
    }
}
