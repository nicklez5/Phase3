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
    public Set<Integer> random_succ_set;
    public Set<String> data_names;
    public ArrayList<Set<String>> group_data_names;
    public Map<String, Integer> live_interval_map;
    public List<Intervals> interval_times;
    public Vector<String> interval_name_vec;
    public ArrayList<List<Intervals>> list_of_interval_times;
    public List<Map<Intervals,Vector<String>>> list_pool_free_reg;
    public List<Intervals> active_sets;
    public Map<Intervals,Vector<String>> pool_free_reg;
    public List<Map<Integer,Set<String>>> line_no_active_var_map;
    public Map<Pair<Integer,String>,String> register_map;
    public List<Map<Pair<Integer,String>,String>> list_register_map;
    //public Map<Integer,Set<String>> liveness_map;
    public List<Map<Integer,Set<String>>> list_liveness_map;
    public Vector<String> reg_pool;
    public int function_index;
    public List<Stacks> function_stacks;
    public Map<String,String> argument_register_map;
    public Set<String> values_called;
    public List<Set<String>> list_values_called;
    public Map<String, String> register_map_local;
    public List<Map<String , String>> list_register_map_local;
    public Print_Visitor temp_print_xyz;
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
            godfather.printfunc_labels();
            godfather.start_instance();
            godfather.loop_thru_maps();

            godfather.liveness_function();
            godfather.loop_thru_maps();
            godfather.extract_active_data();
            godfather.funny_print();
            godfather.update_intervals_map();
            godfather.print_arraylist_intervals();
            godfather.initialize_function_stack();
            godfather.linear_scan_reg_algo();
            godfather.print_list_reg_map();
            godfather.check_out_function_stacks();
            godfather.loop_thru_function();
            godfather.print_list_values();
            //godfather.loop_thru_vars_data();
            /*


            //godfather.print_arraylist_intervals();
            //godfather.print_active_var_map();


            //Enter linear scan algo;
            godfather.update_intervals_map();
            godfather.print_arraylist_intervals();
            //godfather.print_data_names();
            System.out.println("Entering L-SCAN reg algo");
            godfather.linear_scan_reg_algo();
            System.out.println("Entering printing reg map");
            godfather.print_list_reg_map();



            */
            //godfather.loop_thru_maps();
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
        interval_name_vec = new Vector<String>();
        list_of_interval_times = new ArrayList<List<Intervals>>();
        list_pool_free_reg = new ArrayList<Map<Intervals,Vector<String>>>();
        active_sets = new ArrayList<Intervals>();
        pool_free_reg = new HashMap<Intervals, Vector<String>>();
        line_no_active_var_map = new ArrayList<Map<Integer,Set<String>>>();
        register_map = new HashMap<Pair<Integer,String>,String>();
        list_register_map = new ArrayList<Map<Pair<Integer,String>,String>>();
        reg_pool = new Vector<String>();
        function_index = 0;
        function_stacks = new ArrayList<Stacks>();
        random_succ_set = new HashSet<Integer>();
        //liveness_map = new HashMap<Integer,Set<String>>();
        argument_register_map = new HashMap<String,String>();
        list_liveness_map = new ArrayList<Map<Integer,Set<String>>>();
        values_called = new HashSet<String>();
        list_values_called = new ArrayList<Set<String>>();
        register_map_local = new HashMap<String,String>();
        list_register_map_local = new ArrayList<Map<String,String>>();
        temp_print_xyz = new Print_Visitor();
    }
    public void check_out_function_stacks(){
        for(int i = 0 ; i < function_stacks.size() ; i++){
            Stacks xyz = function_stacks.get(i);
            System.out.println("Vector In: " + xyz.in);
            System.out.println("Vector Out: " + xyz.out);
            System.out.println("Vector Local: " + xyz.local);
        }
    }
    public void print_list_values(){
        for(int i = 0; i < list_values_called.size();i++){
            Set<String> temp_xyz = list_values_called.get(i);
            System.out.println("Param values: " + temp_xyz);
        }
    }
    public void print_data_names(){
        for(int i = 0 ; i < group_data_names.size();i++){
            Set<String> list_stringz = group_data_names.get(i);
            System.out.println("Function Index: " + i + " Data names: " + list_stringz);
        }
    }
    public void print_list_reg_map(){
        for(int i = 0;i < list_register_map.size();i++){
            System.out.println("Printing Register Map for function index: " + i );
            Map<Pair<Integer,String>,String> current_reg_map = list_register_map.get(i);
            for(Map.Entry<Pair<Integer,String>,String> entry: current_reg_map.entrySet() ){
                Pair<Integer,String> small_pair = entry.getKey();
                System.out.println("Line No:" + small_pair.getKey() + " Var id:" + small_pair.getValue() + " Location:" + entry.getValue());

            }
        }
    }
    public void funny_print(){
        for(int i = 0 ; i < line_no_active_var_map.size();i++){
            System.out.println("Printing Map Active Var 1");
            Map<Integer,Set<String>> homicide_case = line_no_active_var_map.get(i);
            for(Map.Entry<Integer,Set<String>> entry: homicide_case.entrySet()){
                System.out.println("Instruction Line No. " + entry.getKey() + " Active Sets: " + entry.getValue());

            }
        }
    }
    public void initialize_function_stack(){
        for(int i = 0; i < line_no_active_var_map.size();i++){
            Stacks temp_stack = new Stacks();
            function_stacks.add(temp_stack);
        }
    }
    public void print_arraylist_intervals(){
        System.out.println("Printing intervals");
        for(int i = 0; i < list_of_interval_times.size();i++){
            System.out.println("Function " + i + " array_list interval");
            List<Intervals> test_me = list_of_interval_times.get(i);
            System.out.println("Interval Size: " + test_me.size());
            for(Intervals num : test_me){
                num.simple_print();
            }
        }


    }
    public void sort_active_sets(){
        int n = active_sets.size();
        for(int i = 1; i < n; i++){
            int key = active_sets.get(i).end_point;
            int j = i - 1;
            while(j >= 0 && active_sets.get(j).end_point > key){
                active_sets.set(j+1,active_sets.get(j));
                j = j - 1;
            }
            active_sets.set(j+1,active_sets.get(i));
        }
    }
    public boolean check_if_bigger_found(Integer current_index, String random_string){

        for(int i = 0 ;i < interval_times.size();i++){
            Intervals cmp_interval = interval_times.get(i);

            if(cmp_interval.string_name.equals(random_string)){
                //System.out.println("Integer: " + current_index + " String: " + random_string);
                if( cmp_interval.start_point <= current_index.intValue() && cmp_interval.end_point >= current_index.intValue())  {
                    return false;
                }
            }
        }
        return true;


    }
    public boolean bigger_found(Intervals interval_obj,List<Intervals> temp_xyz){

        Iterator _itr = temp_xyz.iterator();
        while(_itr.hasNext()){
            Intervals temp_interval = (Intervals)_itr.next();
            if(temp_interval.string_name.equals(interval_obj.string_name)){
                if(interval_obj.start_point > temp_interval.start_point){
                    return true;
                }
            }
        }
        return false;
    }
    public void update_intervals_map(){
        for(int i = 0 ; i < line_no_active_var_map.size();i++ ){
            Map<Integer,Set<String>> regular_map = line_no_active_var_map.get(i);
            interval_times = new ArrayList<Intervals>();
            Vector<String> already_appeared = new Vector<String>();
            for(Map.Entry<Integer,Set<String>> entry: regular_map.entrySet()){
                Integer current_set_index = entry.getKey();
                Set<String> active_strings = entry.getValue();
                if(active_strings.isEmpty()){
                    continue;
                }
                Iterator _itr = active_strings.iterator();
                while(_itr.hasNext()){
                    String majin_buu = (String)_itr.next();
                    int temp_index_value = return_finishing_index(majin_buu,current_set_index,i);
                    Intervals random_interval = new Intervals(current_set_index, temp_index_value -1 , majin_buu);
                    if(!check_if_contain_string(majin_buu,interval_times)){
                        interval_times.add(random_interval);
                    }
                    /*
                    else{
                        if(bigger_found(random_interval,interval_times)){
                            interval_times.add(random_interval);
                        }
                    }
                    */
                }
            }

            list_of_interval_times.add(interval_times);
        }
    }
    public boolean check_if_contain_string(String xyz, List<Intervals> tmp_list){
        for(int i = 0 ; i < tmp_list.size();i++){
            Intervals tmp_interval = tmp_list.get(i);
            if(tmp_interval.string_name.equals(xyz)){
                return true;
            }
        }
        return false;
    }
    public int return_finishing_index(String obj_name,Integer starting_index,Integer current_function_index){
        int ending_index = 0;
        Map<Integer,Set<String>> tmp_mapping = line_no_active_var_map.get(current_function_index);
        for(Map.Entry<Integer,Set<String>> entry: tmp_mapping.entrySet()){
            Integer instruction_set_index = entry.getKey();
            Set<String> find_string_set = entry.getValue();
            if(starting_index.intValue() < instruction_set_index.intValue() && !find_string_set.contains(obj_name)){
                ending_index = instruction_set_index.intValue();
                return ending_index;
            }
        }
        return ending_index;

    }
    public void print_active_var_map(){
        for(int i = 0; i < line_no_active_var_map.size();i++){
            System.out.println("Function " + i + " active var map");
            Map<Integer,Set<String>> regular_map = line_no_active_var_map.get(i);
            for(Map.Entry<Integer,Set<String>> entry: regular_map.entrySet()){
                Integer line_no_index = entry.getKey();
                Set<String> set_strings = entry.getValue();
                System.out.println("Line index: " + line_no_index.intValue() + " Set of Active Variables: " + set_strings);

            }
        }
    }
    public void initialize_register_vector(Vector<String> vector_name){
        vector_name.add("$t0");
        vector_name.add("$t1");
        vector_name.add("$t2");
        vector_name.add("$t3");
        vector_name.add("$t4");
        vector_name.add("$t5");
        vector_name.add("$t6");
        vector_name.add("$t7");
        vector_name.add("$t8");
        vector_name.add("$s0");
        vector_name.add("$s1");
        vector_name.add("$s2");
        vector_name.add("$s3");
        vector_name.add("$s4");
        vector_name.add("$s5");
        vector_name.add("$s6");
        vector_name.add("$s7");


    }
    //Map of Line no | Var Name | Reg
    public void create_init_register_map(){
        for(int i = 0 ; i < list_of_interval_times.size();i++){
            register_map = new LinkedHashMap<Pair<Integer,String>,String>();
            List<Intervals> tmp_interval_list = list_of_interval_times.get(i);
            //System.out.println("New Function");
            for(Intervals tmp_ptr : tmp_interval_list){
                for(int k = tmp_ptr.start_point; k <= tmp_ptr.end_point; k++){
                    Pair<Integer,String> tmp_pair = new Pair<Integer,String>(k,tmp_ptr.string_name);
                    //tmp_pair.print_pair();
                    register_map.put(tmp_pair,"");
                }
            }
            list_register_map.add(register_map);

        }
    }


    public void linear_scan_reg_algo(){
        reg_pool = new Vector<String>();
        initialize_register_vector(reg_pool);
        create_init_register_map();
        print_list_reg_map();
        for(int i = 0; i < list_of_interval_times.size();i++){
            function_index = i;
            interval_times = list_of_interval_times.get(i);
            //System.out.println("Interval times size:" + interval_times.size());
            active_sets = new ArrayList<Intervals>();
            //System.out.println("Active set is cleared ");

            //sort by starting time.
            sort_group_data_names();

            register_map = list_register_map.get(i);

            //System.out.println("ENTERING function" + function_index);
            //pool_free_reg.put(test_me,register_names);
            for(Intervals num : interval_times){
                expire_old_intervals(num);
                if(active_sets.size() == 17){
                    spill_at_interval(num);
                }else{
                    System.out.println("Entering weird phase");
                    String reg_name = reg_pool.get(0);
                    //System.out.println("Reg used: " + reg_name);
                    reg_pool.removeElement(reg_name);
                    int live_interval_line_no = num.start_point;
                    String live_interval_var_num = num.string_name;
                    boolean no_pair_found = true;
                    //Pair<Integer,String> tmp_pair = new Pair<>(live_interval_line_no, live_interval_var_num);
                    //System.out.println("Current Reg: " + reg_name);
                    set_register(num,reg_name);
                    active_sets.add(num);
                    sort_active_sets();


                }
            }


        }



    }
    public void sort_start_all_intervals(){

    }
    public void expire_old_intervals(Intervals temp_interval){

        sort_active_sets();

        for(int i = 0; i < active_sets.size();i++){
            Intervals active_interval = active_sets.get(i);
            if(active_interval.end_point >= temp_interval.start_point){
                return;
            }
            active_sets.remove(active_interval);
            //System.out.println("Active Interval: " + active_interval.string_name + "Reg: " + return_register(active_interval));
            reg_pool.add(return_register(active_interval));
            set_register(active_interval,"");



        }
    }
    public void start_instance_part2(int function_index_no){
        boolean add_time = false;
        Map<String,Integer> current_func_label_map = List_func_map.get(function_index_no);
        VInstr[] list_instructions = the_functions[function_index_no].body;
        int label_index = 0;
        int start_index = 0;
        for(Map.Entry<String,Integer> label_entry: current_func_label_map.entrySet()){
            label_index = label_entry.getValue();
            String label_name_map = label_entry.getKey();
            loop_instance_pt2(i,start_index,label_index,increment_on,add_another_increment);
            add_another_increment += 1;
            start_index = label_index;
            if(increment_on == false){
                increment_on = true;
            }
        }
        if(label_index == list_instructions.length){
            loop_instance_pt2(i,label_index-1,list_instructions.length,increment_on,add_another_increment);
        }else{
            loop_instance_pt2(i,label_index,list_instructions.length,increment_on,add_another_increment);
        }


    }
    public String return_register(Intervals temp_interval){
        String _ret = "false";
        boolean register_not_found = true;
        for(Map.Entry<Pair<Integer,String>,String> pair_entry : register_map.entrySet()){
            Pair<Integer,String> little_pair = pair_entry.getKey();
            String reg_value = pair_entry.getValue();
            if(little_pair.getKey() == temp_interval.start_point && little_pair.getValue().equals(temp_interval.string_name)){
                _ret = reg_value;
                register_not_found = false;
                return _ret;
            }
        }
        if(register_not_found){
            System.out.println("Return Register is not found");
        }
        return _ret;
    }
    public void set_register(Intervals random_interval, String reg_value){
        boolean register_not_found = true;
        //System.out.println("Set register with this: " + reg_value);
        for(Map.Entry<Pair<Integer,String>,String> pair_entry : register_map.entrySet()){
            Pair<Integer,String> little_pair = pair_entry.getKey();
            if(little_pair.getKey() == random_interval.start_point && little_pair.getValue().equals(random_interval.string_name)){
                //little_pair.print_pair();
                //System.out.println("Reg value:" + reg_value);
                pair_entry.setValue(reg_value);
                register_not_found = false;
            }
        }
        if(register_not_found){
            System.out.println("Set Register is not found");
        }
    }
    public void spill_at_interval(Intervals temp_interval){
        Intervals spill = active_sets.get(active_sets.size()-1);

        Stacks stk_ptr = function_stacks.get(function_index);
        String location_reg_value = "";
        String spill_reg = return_register(spill);
        if(spill.end_point > temp_interval.end_point){
            set_register(temp_interval,spill_reg);
            if(spill_reg.contains("$t")){
                stk_ptr.store_local(spill_reg);
            }else{
                stk_ptr.store_in(spill_reg);
            }
            set_register(spill,"");
            active_sets.remove(spill);
            active_sets.add(temp_interval);
            sort_active_sets();
        }else{
            spill_reg = return_register(temp_interval);
            if(spill_reg.contains("$t")){
                stk_ptr.store_local(spill_reg);
            }else{
                stk_ptr.store_in(spill_reg);
            }
            set_register(temp_interval,"");

        }
    }

    public void sort_group_data_names(){
        for(int i = 0; i < interval_times.size()-1; i++){
            for(int j = 0; j < interval_times.size()-i-1; j++){
                Intervals current_interval = interval_times.get(j);
                Intervals next_interval = interval_times.get(j+1);
                if(current_interval.start_point > next_interval.start_point){
                    Intervals fake_interval = interval_times.get(j);
                    interval_times.set(j,next_interval);
                    interval_times.set(j+1,fake_interval);
                }
            }
        }


    }
    public void print_list_liveness_map(){
        for(int i = 0 ;i < list_liveness_map.size(); i++){
            Map<Integer,Set<String>> liveness_map = list_liveness_map.get(i);
            for(Map.Entry<Integer,Set<String>> entry: liveness_map.entrySet()){
                System.out.println("Index: " + entry.getKey() + " Liveness Set: " + entry.getValue());
            }
            System.out.println("END PRINT");
        }
    }
    public void print_live_interval_map(){
        System.out.println("Map");
        for(Map.Entry<String,Integer> entry: live_interval_map.entrySet()){
            System.out.println("ID:" + entry.getKey() + " Value:" + entry.getValue());

        }
    }

    public void extract_live_data(){
        int current_func_index = 0;
        //Every function
        for(Map<Integer,List_Set> map: myMap){
            Set<String> table_namez = group_data_names.get(current_func_index);
            live_interval_map.clear();
            Iterator<String> _itr = table_namez.iterator();
            while(_itr.hasNext()){
                String map_string_value = (String)_itr.next();
                live_interval_map.put(map_string_value,0);
            }
            Set<String> live_sets = new HashSet<String>();
            Map<Integer,Set<String>> temp_map = new HashMap<Integer,Set<String>>();
            Map<Integer,List_Set> current_map_live = map;
            //Every instruction
            for(Map.Entry<Integer,List_Set> entry: map.entrySet()){

                String def_set_variable = "";
                //Respawn on this set.
                live_sets = new HashSet<String>();
                Set<String> temp_set = new HashSet<String>();
                Integer index_value = entry.getKey();
                List_Set temp_list_set = entry.getValue();


                temp_set.addAll(temp_list_set.def_set);
                if(temp_set.isEmpty()){
                    temp_map.put(index_value,live_sets);
                    continue;
                }
                Iterator<String> def_set_itr = temp_set.iterator();
                def_set_variable = def_set_itr.next();
                //String def_set_variable = def_set_itr.next();

                Set<Integer> succ_index_values = temp_list_set.succ_set;
                Iterator _itr3 = succ_index_values.iterator();

                //Loop at this guys successor sets.
                while(_itr3.hasNext()){

                    Integer succ_index = (Integer)_itr3.next();
                    if(succ_index.intValue() + 1 < current_map_live.size()){
                        List_Set cmp_list_set = current_map_live.get(succ_index.intValue() );
                        Set<String> cmp_list_use_set = cmp_list_set.use_set;
                        Set<String> cmp_list_def_set = cmp_list_set.def_set;
                        if(cmp_list_use_set.contains(def_set_variable)){
                            if(!cmp_list_def_set.contains(def_set_variable)) {
                                live_sets.add(def_set_variable);
                            }
                        }
                    }
                }
                temp_map.put(index_value,live_sets);

                //System.out.println("Next Function");

            }
            list_liveness_map.add(temp_map);
        }
    }
    public void extract_active_data(){

        int function_index = 0;
        System.out.println("EXTRACTING DATA");
        print_live_interval_map();
        //Functions
        for(Map<Integer, List_Set> map: myMap){
            //instructions
            //interval_times = new ArrayList<Intervals>();
            Set<String> table_namez = group_data_names.get(function_index);
            live_interval_map.clear();
            System.out.println("Table Data names: " + table_namez);
            System.out.println("Function No: " + function_index);
            Iterator<String> _itr = table_namez.iterator();
            while(_itr.hasNext()){
                String map_string_value = (String)_itr.next();
                live_interval_map.put(map_string_value,0);
            }
            boolean first_Time = true;
            Set<String> active_sets = new HashSet<String>();
            //for every instruction.
            Map<Integer,Set<String>> temporary_map = new HashMap<Integer,Set<String>>();
            for(Map.Entry<Integer, List_Set> entry: map.entrySet()){
                Set<String> temp_set = new HashSet<String>();
                Integer time_value =  entry.getKey();
                List_Set temp_list_set = entry.getValue();
                String instruction_value = "";
                active_sets.addAll(temp_list_set.def_set);
                active_sets.addAll(temp_list_set.in_set);

                temp_set.addAll(active_sets);
                temporary_map.put(time_value,temp_set);
                System.out.println("Index: " + time_value.intValue() + " Current active sets: " + active_sets);
                active_sets.clear();

            }

            line_no_active_var_map.add(temporary_map);
            //list_of_interval_times.add(interval_times);


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
                    if(cmp_list.in_set.equals(temp_list.in_set) && cmp_list.out_set.equals(temp_list.out_set) ) {
                    }else{
                        return false;
                    }
                }
                return true;
            }
            i++;

        }
        return true;
    }

    public void liveness_function(){
        System.out.println("Entering liveness function");
        int random_map_index = 0;
        //Loop thru functions
        for(Map<Integer, List_Set> map : myMap){
            int map_size = map.size();

            List_Set[] new_list = new List_Set[map_size];
            for(int i = 0; i < map_size ;i++){
                new_list[i] = new List_Set();
            }
            for(Map.Entry<Integer, List_Set> entry: map.entrySet()){
                List_Set temp_list = entry.getValue();
                temp_list.in_set.clear();
                temp_list.out_set.clear();
                entry.setValue(temp_list);
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
                    entry.setValue(temp_list);

                }
            }while(!check_trueness(new_list,random_map_index));
            random_map_index++;
        }

    }
    public void big_print_function(){
        for(int i = 0 ; i < the_functions.length; i++){
            VFunction test_function = the_functions[i];
            VInstr[] instructionz = test_function.body;
            VVarRef.Local[] parameter_list = test_function.params;
            Set<String> function_local_variables = list_values_called.get(i);
            Stacks temp_stack = function_stacks.get(i);
            int arguments = 0;
            int local_t = 0;
            int local_s = 0;
            Iterator _itr = function_local_variables.iterator();
            while(_itr.hasNext()){
                String da_boss_string = _itr.next();
                if(!da_boss_strings.contains("t.")){
                    System.out.println("local[" + temp_stack_local.size() + "] = $s" + arguments);
                    temp_stack.store_local(da_boss_string);
                    register_map_local.put(da_boss_string,"$s" + arguments);
                    arguments++;

                }
            }
            for(int j = 0 ; j < parameter_list.size(); j++){
                String xyz_value = parameter_list[j].ident;
                if(function_local_variables.contains(xyz_value)){
                    System.out.println("$s" + local_s + " = $a" + j);
                    local_s++;
                }else{
                    System.out.println("$t" + local_t + " = $a" + j);
                    local_t++;
                }
            }


        }
        //VFunction test_functions = the_functions
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
    public void printfunc_labels(){
        for(int i = 0 ; i < List_func_map.size();i++){
            Map<String,Integer> func_map = List_func_map.get(i);
            System.out.println("Function: " + i);
            for(Map.Entry<String,Integer> entry: func_map.entrySet()){
                System.out.println("Branch Label: " + entry.getKey() + " Index: " +entry.getValue());
            }
        }
        System.out.println("___________________");
    }
    public void loop_thru_func_labels(){
        for(int i = 0; i < the_functions.length; i++){
            Map<String,Integer> temp_map_label = new HashMap<String,Integer>();
            System.out.println("Function " + i);
            VCodeLabel[] temp_labels = the_functions[i].labels;
            for(int j = 0 ; j < temp_labels.length; j++){
                Integer label_no = temp_labels[j].instrIndex;
                if(temp_labels[j].ident.contains("_end")){
                    label_no += 1;
                }
                System.out.println("Instruction Line No: " + label_no);
                System.out.println("Label Name: " + temp_labels[j].ident);
                temp_map_label.put(temp_labels[j].ident, label_no);
            }
            List_func_map.add(i,temp_map_label);
        }



    }
    public void loop_thru_maps(){
        int lame_index = 0;
        group_data_names.clear();
        for(Map<Integer, List_Set> map : myMap){
            System.out.println("Map function index " + lame_index);
            for(Map.Entry<Integer, List_Set> entry: map.entrySet()){
                List_Set temp_list_set = entry.getValue();
                Integer temp_integer = entry.getKey();
                System.out.println("Instruction Line No. : " + Integer.toString(temp_integer));
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
            lame_index += 1;
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
    public void loop_instance_pt2(int function_index,int start_index, int end_index, boolean increment_on, int map_value){
        int wait_index;
        String branch_name = "";
        VInstr[] list_instructions = the_functions[function_index].body;
        Map<String, Integer> current_func_label_map = List_func_map.get(function_index);
        Vector<Integer> label_no_vars = new Vector<Integer>();
        for(Map.Entry<String,Integer> entry: current_func_label_map.entrySet()){
            Integer temp_num = entry.getValue();
            label_no_vars.add(temp_num);
        }
        for(int k = start_index; k < end_index; k++){
            if(list_instructions[k] instanceof VCall){
                temp_print_xyz.visit(k,(VCall)list_instructions[k]);
            }else if(list_instructions[k] instanceof VAssign){
                temp_print_xyz.visit(k,(VAssign)list_instructions[k]);
            }else if(list_instructions[k] instanceof VBuiltIn){
                temp_print_xyz.visit(k,(VBuiltIn)list_instructions[k]);
            }else if(list_instructions[k] instanceof VMemWrite){
                temp_print_xyz.visit(k,(VMemWrite)list_instructions[k]);
            }else if(list_instructions[k] instanceof VMemRead){
                temp_print_xyz.visit(k,(VMemRead)list_instructions[k]);
            }else if(list_instructions[k] instanceof VBranch){
                temp_print_xyz.visit(k, (VBranch)list_instructions[k]);
            }else if(list_instructions[k] instanceof VGoto){
                temp_print_xyz.visit(k,(VGoto)list_instructions[k]);
                /*
                branch_name = branch_name.replace(":","");
                int random_index = current_func_label_map.get(branch_name);
                System.out.println("GOTO:  " + branch_name);
                */
                //random_succ_set.add(random_index+1);
            }else if(list_instructions[k] instanceof VReturn){
                temp_print_xyz.visit(k,(VReturn)list_instructions[k]);
            }


        }
    }
    public void loop_instance(int function_index, int start_index, int end_index,boolean increment_on,int map_value){
        int wait_index;
        String branch_name = "";
        VInstr[] list_instructions = the_functions[function_index].body;
        Map<String, Integer> current_func_label_map = List_func_map.get(function_index);
        Vector<Integer> label_no_vars = new Vector<Integer>();
        for(Map.Entry<String,Integer> entry: current_func_label_map.entrySet()){
            Integer temp_num = entry.getValue();
            label_no_vars.add(temp_num);
        }
        for(int k = start_index; k < end_index; k++){
            int cool_ass_index = k;
            if(increment_on){
                cool_ass_index += 1;
            }else{
                cool_ass_index = k + 1;
            }
            if(label_no_vars.contains(cool_ass_index + map_value)){
                cool_ass_index += 1;
            }
            node_visit.clear_sets();
            Set<Integer> random_succ_set = new HashSet<Integer>();
            if(list_instructions[k] instanceof VCall){
                node_visit.visit(k,(VCall)list_instructions[k]);
                if(cool_ass_index < list_instructions.length)
                random_succ_set.add(cool_ass_index + map_value);
            }else if(list_instructions[k] instanceof VAssign){
                node_visit.visit(k,(VAssign)list_instructions[k]);
                if(cool_ass_index < list_instructions.length)
                random_succ_set.add(cool_ass_index + map_value);
            }else if(list_instructions[k] instanceof VBuiltIn){
                String empty_string;
                empty_string = node_visit.visit(k,(VBuiltIn)list_instructions[k]);
                if(empty_string.isEmpty()){
                    random_succ_set.add(cool_ass_index + map_value);

                }
            }else if(list_instructions[k] instanceof VMemWrite){
                node_visit.visit(k,(VMemWrite)list_instructions[k]);
                if(k+1 < list_instructions.length)
                random_succ_set.add(cool_ass_index + map_value);
            }else if(list_instructions[k] instanceof VMemRead){
                node_visit.visit(k,(VMemRead)list_instructions[k]);
                if(k+1 < list_instructions.length)
                random_succ_set.add(cool_ass_index + map_value);
            }else if(list_instructions[k] instanceof VBranch){
                //Null Branch
                branch_name = node_visit.visit(k, (VBranch)list_instructions[k]);
                if(branch_name.contains(":null")){
                    random_succ_set.add(cool_ass_index + map_value);
                    if(label_no_vars.contains(cool_ass_index+1+map_value)){
                        cool_ass_index += 1;
                    }
                    random_succ_set.add(cool_ass_index+1 + map_value);

                    //Regular ass Branch
                }else{
                    branch_name = branch_name.replace("else","end");
                    wait_index = start_looping(k,function_index,branch_name);
                    random_succ_set.add(cool_ass_index + map_value);
                    random_succ_set.add(wait_index+2);
                }
            }else if(list_instructions[k] instanceof VGoto){
                branch_name = node_visit.visit(k,(VGoto)list_instructions[k]);
                branch_name = branch_name.replace(":","");
                int random_index = current_func_label_map.get(branch_name);
                System.out.println("GOTO:  " + branch_name);
                random_succ_set.add(random_index+1);
            }else if(list_instructions[k] instanceof VReturn){
                node_visit.visit(k,(VReturn)list_instructions[k]);
                System.out.println("Return Index: " + k);
            }

            List_Set da_list_set = new List_Set(node_visit.in_set,node_visit.out_set,node_visit.def_set, node_visit.use_set, random_succ_set);
            values_called.addAll(node_visit.param_values);
            VSet_map.put(k+map_value,da_list_set);

        }
    }
    public void start_instance(){
        boolean add_time = false;

        for(int i = 0 ; i < the_functions.length ;i++){
            VSet_map = new HashMap<Integer, List_Set>();
            System.out.println("Function: " + the_functions[i].index);
            boolean increment_on = false;
            VInstr[] list_instructions = the_functions[i].body;
            int start_index = 0;
            int label_index = 0;
            int add_another_increment = 0;
            Map<String, Integer> current_func_label_map = List_func_map.get(i);
            Set<String> tmp_param_values = new HashSet<String>();
            for(Map.Entry<String,Integer> label_entry: current_func_label_map.entrySet()){
                label_index = label_entry.getValue();
                String label_name_map = label_entry.getKey();
                System.out.println("Label Name: " + label_name_map + " Label Index: " + label_index);
                loop_instance(i,start_index,label_index,increment_on,add_another_increment);
                List_Set clear_set = new List_Set();
                VSet_map.put(label_index,clear_set);
                add_another_increment += 1;
                start_index = label_index;
                if(increment_on == false){
                    increment_on = true;
                }

                System.out.println("Finish printing");
            }
            if(label_index == list_instructions.length){
                loop_instance(i,label_index-1,list_instructions.length,increment_on,add_another_increment);
            }else{
                loop_instance(i,label_index,list_instructions.length,increment_on,add_another_increment);
            }
            myMap.add(i,VSet_map);
            list_values_called.add(values_called);
            values_called = new HashSet<String>();
            node_visit.empty_param_values();
        }


        //loop_thru_maps();
        //node_visit.print_function();
    }


}
