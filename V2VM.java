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
    public void loop_instance(){
        for(int i = 0 ; i < the_functions.length ;i++){
            System.out.println("Function: " + the_functions[i].index);
            VInstr[] list_instructions = the_functions[i].body;
            for(int k = 0; k < list_instructions.length; k++){
                if(list_instructions[k] instanceof VCall){
                    node_visit.set_current_pos(k);
                    node_visit.visit((VCall)list_instructions[k]);
                }else if(list_instructions[k] instanceof VAssign){
                    node_visit.set_current_pos(k);
                    node_visit.visit((VAssign)list_instructions[k]);
                }else if(list_instructions[k] instanceof VBuiltIn){
                    node_visit.set_current_pos(k);
                    node_visit.visit((VBuiltIn)list_instructions[k]);
                }else if(list_instructions[k] instanceof VMemWrite){
                    node_visit.set_current_pos(k);
                    node_visit.visit((VMemWrite)list_instructions[k]);
                }else if(list_instructions[k] instanceof VMemRead){
                    node_visit.set_current_pos(k);
                    node_visit.visit((VMemRead)list_instructions[k]);
                }else if(list_instructions[k] instanceof VBranch){
                    node_visit.set_current_pos(k);
                    node_visit.visit((VBranch)list_instructions[k]);
                }else if(list_instructions[k] instanceof VGoto){
                    node_visit.set_current_pos(k);
                    node_visit.visit((VGoto)list_instructions[k]);
                }else if(list_instructions[k] instanceof VReturn){
                    node_visit.set_current_pos(k);
                    node_visit.visit((VReturn)list_instructions[k]);
                }

            }
        }
    }

}
