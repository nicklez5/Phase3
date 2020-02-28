import java.util.*;
public class List_Set {
    public Set<String> in_set;
    public Set<String> out_set;
    public Set<String> def_set;
    public Set<String> use_set;
    public Set<Integer> succ_set;
    public Map<Integer,String> func_map_label;
    public List_Set(){
        in_set = new HashSet<String>();
        out_set = new HashSet<String>();
        def_set = new HashSet<String>();
        use_set = new HashSet<String>();
        succ_set = new HashSet<Integer>();

    }
    public List_Set(Set<String> temp_in_set, Set<String> temp_out_set, Set<String> temp_def_set, Set<String> temp_use_set, Set<Integer> temp_succ_set){
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

    //public void remove_duplicates(Vector<In)

}
