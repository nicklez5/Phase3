import java.util.*;
public class Stack_ptr{
    public int in;
    public int out;
    public int local;
    public Stack_ptr(){
        in = 0;
        out = 0;
        local = 0;
    }
    public Stack_ptr(int in,int out, int local){
        this.in = in;
        this.out = out;
        this.local = local;
    }

}
