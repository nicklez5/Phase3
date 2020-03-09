import java.util.*;
public class Stacks{
    public Vector<String> in;
    public Vector<String> out;
    public Vector<String> local;

    public Stacks(){
        in = new Vector<String>();
        out = new Vector<String>();
        local = new Vector<String>();

    }
    public void store_in(String local_xyz){
        in.add(local_xyz);
    }
    public void store_out(String local_xyz){
        out.add(local_xyz);
    }
    public void store_local(String local_xyz){
        local.add(local_xyz);
    }

}
