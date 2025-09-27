package edu.hitsz.Factories;
import edu.hitsz.prop.BaseProp;

public interface PropCreator {
    public abstract BaseProp createProp(int locationX, int locationY);
}
