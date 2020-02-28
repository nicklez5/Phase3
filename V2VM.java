import java.util.*;
import cs132.util.ProblemException;
import cs132.vapor.parser.VaporParser;
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.ast.VBuiltIn.Op;
import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VFunction.Stack;
import cs132.vapor.ast.*;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;

public class V2VM{
    public VaporProgram the_chosen_one;
    public VFunction[] the_functions;
    public VDataSegment[] the_data;
    public Node_Visitor node_visit;
    public Map<Integer, List_Set> VSet_map;
    public List<Map<Integer, List_Set>> myMap;
    public Map<Integer, List_Set> current_map;
    public List<Map<String, Integer>> List_func_map;
    public Set<String> data_names;
    public ArrayList<Set<String>> group_data_names;
    public Map<String, Integer> live_interval_map;
    public List<Intervals> interval_times;
    public static VaporProgram parserVapor(InputStream in, PrintStream err) throws IOException
    {
        Op[] ops = {
            Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
            Op.PrintIntS, Op.HeapAllocZ, Op.Error,
        };
        boolean allowLocals = true;
        String[] registers = null;
        boolean allowStack = false;

        VaporProgram tree;
        try{
            tree = VaporParser.run(new InputStreamReader(in), 1, 1,
            java.util.Arrays.asList(ops),
            allowLocals, registers, allowStack);
        }
        catch (ProblemException ex) {
            err.println(ex.getMessage());
            return null;
        }
        return tree;

    }
    public static void main(String[] args) throws IOException {
        PrintStream ps = new PrintStream("data.txt");
        InputStream x = System.in;
        VaporProgram xyz = parserVapor(x, ps);
        if(xyz != null){
            //System.out.println("Print was successful");
            V2VM godfather = new V2VM(xyz);
            //godfather.loop_thru_function();
            godfather.loop_thru_func_labels();
            godfather.loop_instance();
            godfather.liveness_function();
            godfather.loop_thru_maps();
            godfather.extractdata();
            godfather.print_live_interval_map();
            //godfather.loop_thru_labels();
            //godfather.loop_thru_data();
            //godfather.print_func_labels();
            //godfather.loop_thru_vars_data();
        }
    }
    public V2VM(VaporProgram sourceProgram){
        the_chosen_one = sourceProgram;
        the_functions = sourceProgram.functions;
        node_visit = new Node_Visitor();
        the_data = sourceProgram.dataSegments;
        VSet_map = new HashMap<Integer , List_Set>();
        myMap = new ArrayList<Map<Integer, List_Set>>();
        List_func_map = new ArrayList<Map<String, Integer>>();
        data_names = new HashSet<String>();
        live_interval_map = new HashMap<String,Integer>();
        interval_times = new ArrayList<Intervals>();
        group_data_names = new ArrayList<Set<String>>();
    }
    public void print_live_interval_map(){
        System.out.println("Map");
        for(Map.Entry<String,Integer> entry: live_interval_map.entrySet()){
            System.out.println("ID:" + entry.getKey() + " Value:" + entry.getValue());

        }
    }
    public void extractdata(){

        int function_index = 0;

        print_live_interval_map();
        //Functions
        for(Map<Integer, List_Set> map: myMap){
            //instructions
            Set<String> table_namez = group_data_names.get(function_index);
            System.out.println("Function No: " + function_index);
            Iterator<String> _itr = table_namez.iterator();
            while(_itr.hasNext()){
                String map_string_value = (String)_itr.next();
                live_interval_map.put(map_string_value,0);
            }

            Set<String> active_sets = new HashSet<String>();
            for(Map.Entry<Integer, List_Set> entry: map.entrySet()){
                Integer time_value =  entry.getKey();
                List_Set temp_list_set = entry.getValue();
                System.out.println("Index: " + time_value.intValue() + " Current active sets: " + active_sets);
                if(active_sets.size() == 0){
                    active_sets.addAll(temp_list_set.def_set);
                    System.out.println("Def set: " + temp_list_set.def_set);
                    active_sets.addAll(temp_list_set.in_set);
                    System.out.println("In set: " + temp_list_set.in_set);

                }else{
                    Iterator<String> _itr2 = temp_list_set.in_set.iterator();
                    while(_itr2.hasNext()){
                        String string_obj = (String)_itr2.next();
                        if(active_sets.contains(string_obj)){
                            Integer distance_value = live_interval_map.get(string_obj);
                            live_interval_map.put(string_obj,distance_value.intValue() + 1);
                            active_sets.remove(string_obj);
                            Intervals random_interval = new Intervals(time_value.intValue() - 1,time_value.intValue(), string_obj);
                            interval_times.add(random_interval);
                        }else{
                            active_sets.add(string_obj);
                        }
                    }
                    _itr2 = temp_list_set.def_set.iterator();
                    while(_itr2.hasNext()){
                        String string_obj = (String)_itr2.next();
                        if(active_sets.contains(string_obj)){
                            Integer distance_value = live_interval_map.get(string_obj);
                            live_interval_map.put(string_obj,distance_value.intValue() + 1);
                            active_sets.remove(string_obj);
                            Intervals random_interval = new Intervals(time_value.intValue() - 1,time_value.intValue(), string_obj);
                            interval_times.add(random_interval);
                        }else{
                            active_sets.add(string_obj);
                        }
                    }
                }
            }

            function_index++;
        }
    }
    public boolean check_trueness(List_Set[] random_set,int function_index){
        int i = 0;
        for(Map<Integer, List_Set> map : myMap){
            if(i == function_index){
                for(Map.Entry<Integer,List_Set> entry: map.entrySet()){

                    List_Set temp_list = entry.getValue();
                    Integer temp_integer = entry.getKey();
                    List_Set cmp_list = random_set[temp_integer.intValue()];
                    if( ! (temp_list.in_set.equals(cmp_list.in_set) && temp_list.out_set.equals(cmp_list.out_set)) ){
                        return false;
                    }
                }
            }
            i++;

        }
        return true;
    }
    public void liveness_function(){
        int random_map_index = 0;
        //Loop thru functions
        for(Map<Integer, List_Set> map : myMap){
            int map_size = map.size();
            List_Set[] new_list = new List_Set[map_size];
            for(int i = 0; i < map_size ;i++){
                new_list[i] = new List_Set();
            }
            do{
                for(Map.Entry<Integer, List_Set> entry: map.entrySet()){
                    List_Set temp_list = entry.getValue();
                    Integer temp_integer = entry.getKey();
                    int da_real_index = temp_integer.intValue();
                    new_list[da_real_index].in_set.addAll(temp_list.in_set);
                    new_list[da_real_index].out_set.addAll(temp_list.out_set);
                    temp_list.in_set.addAll(temp_list.use_set);
                    Set<String> temp_out_set = new HashSet<String>(temp_list.out_set);
                    temp_out_set.removeAll(temp_list.def_set);
                    temp_list.in_set.addAll(temp_out_set);
                    Iterator<Integer> it = temp_list.succ_set.iterator();
                    while(it.hasNext()){
                        Integer successor_index = (it.next());
                        List_Set temporary_list_set = map.get(successor_index);
                        temp_list.out_set.addAll(temporary_list_set.in_set);

                    }
                }
            }while(!check_trueness(new_list,random_map_index));
            random_map_index++;
        }

    }
    public void loop_thru_vars_data(){
        for(int i = 0 ;i < the_functions.length; i++){
            String[] temp_vars = the_functions[i].vars;
            System.out.println("Function No: " + i);
            for(int j = 0; j < temp_vars.length; j++){
                System.out.println("Variable: " + temp_vars[j]);
            }
        }
    }

    public void loop_thru_func_labels(){
        Map<String,Integer> temp_map_label = new HashMap<String,Integer>();
        for(int i = 0; i < the_functions.length; i++){
            VCodeLabel[] temp_labels = the_functions[i].labels;
            for(int j = 0 ; j < temp_labels.length; j++){
                System.out.println("Instruction Index: " + temp_labels[j].instrIndex);
                System.out.println("Label Name: " + temp_labels[j].ident);
                temp_map_label.put(temp_labels[j].ident, temp_labels[j].instrIndex);
            }
            List_func_map.add(i,temp_map_label);
        }



    }
    public void loop_thru_maps(){
        for(Map<Integer, List_Set> map : myMap){
            for(Map.Entry<Integer, List_Set> entry: map.entrySet()){
                List_Set temp_list_set = entry.getValue();
                Integer temp_integer = entry.getKey();
                System.out.println("Instruction index: " + Integer.toString(temp_integer));
                temp_list_set.print_set();
                data_names.addAll(temp_list_set.in_set);
                data_names.addAll(temp_list_set.out_set);
                data_names.addAll(temp_list_set.def_set);
                data_names.addAll(temp_list_set.use_set);
            }
            group_data_names.add(data_names);
            System.out.println("_________________________");
            System.out.println("Data List: " + data_names);
            data_names = new HashSet<String>();
        }


    }
    public void print_func_labels(){
        for(int i = 0;i < the_functions.length;i++){
            VCodeLabel[] temp_labels = the_functions[i].labels;
            for(int j = 0; j < temp_labels.length; j++){
                VFunction temp_func = temp_labels[j].function;
                System.out.println("Func Label: " + temp_func.ident);
            }
        }
    }
    //Loop through data segments
    public void loop_thru_data(){
        for(int i = 0;i < the_data.length ;i++){
            VOperand.Static[] the_data_values =  the_data[i].values;

            for(int j = 0; j < the_data_values.length ; j++){
                //Does not print any Integer Literals
                if(the_data_values[j] instanceof VLitInt){
                    VLitInt temp_register_no = (VLitInt)the_data_values[j];
                    System.out.println("Integer Literal:" + temp_register_no.toString());
                    //Prints out Label References.
                }else if(the_data_values[j] instanceof VLabelRef){
                    VLabelRef temp_label_no = (VLabelRef)the_data_values[j];
                    System.out.println("Label Ref: " + temp_label_no.toString());
                }


            }
        }
    }
    public void loop_thru_function(){
        Stack xyz;
        for(int i = 0 ; i < the_functions.length ;i++){
            xyz = the_functions[i].stack;
            System.out.println("Function: " + the_functions[i].index);
            //System.out.println("In:" + xyz.in + " Out:" + xyz.out + " Local:" + xyz.local);

            VVarRef.Local[] parameter_list = the_functions[i].params;
            System.out.println("Parameters");
            for(int j = 0; j < parameter_list.length ; j++){
                System.out.println(parameter_list[j].ident);
            }


        }
    }
    public int start_looping(int start_index, int function_index,String string_to_find){
        VFunction the_function = the_functions[function_index];
        VInstr[] temporary_instructions = the_function.body;
        int big_index = 0;
        String temp_string = "";
        node_visit.dont_add = true;
        for(int i = start_index + 1; i < temporary_instructions.length ; i++){
            if(temporary_instructions[i] instanceof VGoto){
                temp_string = node_visit.visit(i,(VGoto)temporary_instructions[i]);
                if(temp_string.equals(string_to_find)){
                    big_index = i;
                    break;
                }
            }
        }
        node_visit.dont_add = false;
        return big_index;
    }
    public void loop_instance(){
        boolean add_time = false;
        for(int i = 0 ; i < the_functions.length ;i++){
            VSet_map = new HashMap<Integer, List_Set>();
            System.out.println("Function: " + the_functions[i].index);
            System.out.println("_____________________________");
            VInstr[] list_instructions = the_functions[i].body;
            boolean waitforme = false;
            int wait_index;
            String branch_name = "";
            Map<String, Integer> current_func_label_map = List_func_map.get(i);
            for(int k = 0; k < list_instructions.length; k++){

                node_visit.clear_sets();
                Set<Integer> random_succ_set = new HashSet<Integer>();
                if(list_instructions[k] instanceof VCall){
                    node_visit.visit(k,(VCall)list_instructions[k]);
                    if(k+1 < list_instructions.length)
                    random_succ_set.add(k+1);
                }else if(list_instructions[k] instanceof VAssign){
                    node_visit.visit(k,(VAssign)list_instructions[k]);
                    if(k+1 < list_instructions.length)
                    random_succ_set.add(k+1);
                }else if(list_instructions[k] instanceof VBuiltIn){
                    node_visit.visit(k,(VBuiltIn)list_instructions[k]);
                    if(k+1 < list_instructions.length)
                    random_succ_set.add(k+1);
                }else if(list_instructions[k] instanceof VMemWrite){
                    node_visit.visit(k,(VMemWrite)list_instructions[k]);
                    if(k+1 < list_instructions.length)
                    random_succ_set.add(k+1);
                }else if(list_instructions[k] instanceof VMemRead){
                    node_visit.visit(k,(VMemRead)list_instructions[k]);
                    if(k+1 < list_instructions.length)
                    random_succ_set.add(k+1);
                }else if(list_instructions[k] instanceof VBranch){
                    //Null Branch
                    branch_name = node_visit.visit(k, (VBranch)list_instructions[k]);
                    if(branch_name.contains(":null")){
                        random_succ_set.add(k+1);
                        random_succ_set.add(k+2);
                        //Regular ass Branch
                    }else{
                        branch_name = branch_name.replace("else","end");
                        wait_index = start_looping(k,i,branch_name);
                        random_succ_set.add(k+1);
                        random_succ_set.add(wait_index+1);
                    }
                }else if(list_instructions[k] instanceof VGoto){
                    branch_name = node_visit.visit(k,(VGoto)list_instructions[k]);
                    branch_name = branch_name.replace(":","");
                    int random_index = current_func_label_map.get(branch_name);
                    System.out.println("GOTO:  " + branch_name);
                    random_succ_set.add(random_index);
                }else if(list_instructions[k] instanceof VReturn){
                    node_visit.visit(k,(VReturn)list_instructions[k]);
                    System.out.println("Return Index: " + k);
                }

                List_Set da_list_set = new List_Set(node_visit.in_set,node_visit.out_set,node_visit.def_set, node_visit.use_set, random_succ_set);
                VSet_map.put(k,da_list_set);

            }

            myMap.add(i,VSet_map);

        }
        loop_thru_maps();
        //node_visit.print_function();
    }


}
