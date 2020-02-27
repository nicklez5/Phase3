import java.util.*;
public class List_Set {
    Vector<String> in_set;
    Vector<String> out_set;
    Vector<String> def_set;
    Vector<String> use_set;
    Vector<Integer> succ_set;
    public List_Set(){
        in_set = new Vector<String>();
        out_set = new Vector<String>();
        def_set = new Vector<String>();
        use_set = new Vector<String>();
        succ_set = new Vector<Integer>();
    }
    public List_Set(Vector<String> temp_in_set, Vector<String> temp_out_set, Vector<String> temp_def_set, Vector<String> temp_use_set, Vector<Integer> temp_succ_set){
        in_set = temp_in_set;
        out_set = temp_out_set;
        def_set = temp_def_set;
        use_set = temp_use_set;
        succ_set = temp_succ_set;
    }
    public void print_set(){
        System.out.println("In Set: " + in_set);
        System.out.println("Out Set: " + out_set);
        System.out.println("Def Set: " + def_set);
        System.out.println("Use Set: " + use_set );
        System.out.println("Succ Set: " + succ_set);
    }


}
