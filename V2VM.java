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
                godfather.loop_instance();
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
    }
    public void loop_thru_vars_data(){
        /*
        for(int i = 0 ; i < the_data.length ; i++){
            System.out.println("Function No: " + the_data[i].index + " " + the_data[i].ident);
        }
        */
        for(int i = 0 ;i < the_functions.length; i++){
            String[] temp_vars = the_functions[i].vars;
            System.out.println("Function No: " + i);
            for(int j = 0; j < temp_vars.length; j++){
                System.out.println("Variable: " + temp_vars[j]);
            }
        }
    }
    public void loop_thru_maps(){
        for(Map<Integer, List_Set> map : myMap){
            for(Map.Entry<Integer, List_Set> entry: map.entrySet()){
                List_Set temp_list_set = entry.getValue();
                Integer temp_integer = entry.getKey();
                System.out.println("Instruction index: " + Integer.toString(temp_integer));
                temp_list_set.print_set();
            }
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
        for(int i = start_index + 1; i < temporary_instructions.length ; i++){
            if(temporary_instructions[i] instanceof VGoto){
                temp_string = node_visit.visit(i,(VGoto)temporary_instructions[i]);
                if(temp_string.equals(string_to_find)){
                    big_index = i;
                    break;
                }
            }
        }
        return big_index;
    }
    public void loop_instance(){
        boolean add_time = false;
        for(int i = 0 ; i < the_functions.length ;i++){
            System.out.println("Function: " + the_functions[i].index);
            VInstr[] list_instructions = the_functions[i].body;
            boolean waitforme = false;
            int wait_index = 0;
            String branch_name = "";
            for(int k = 0; k < list_instructions.length; k++){
                node_visit.clear_sets();
                Vector<Integer> random_succ_set = new Vector<Integer>();
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
                    if(node_visit.visit(k,(VBranch)list_instructions[k]).contains(":null")){
                        random_succ_set.add(k+1);
                        random_succ_set.add(k+2);
                    //Regular ass Branch
                    }else{
                        branch_name = node_visit.visit(k,(VBranch)list_instructions[k]);
                        branch_name.replace("else","end");
                        wait_index = start_looping(k,i,branch_name);
                        random_succ_set.add(k+1);
                        random_succ_set.add(wait_index+1);
                    }
                }else if(list_instructions[k] instanceof VGoto){
                    node_visit.visit(k,(VGoto)list_instructions[k]);
                }else if(list_instructions[k] instanceof VReturn){
                    node_visit.visit(k,(VReturn)list_instructions[k]);
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
