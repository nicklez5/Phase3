import java.util.*;
import cs132.vapor.ast.*;
import cs132.vapor.ast.VInstr.Visitor;
import cs132.vapor.ast.VVarRef.Register;
import cs132.vapor.ast.VVarRef.Local;
import cs132.vapor.ast.VMemRef.Global;
import cs132.vapor.ast.VMemRef.Stack;
public class Node_Visitor extends VInstr.VisitorPR< Integer ,String, RuntimeException>{
    public Map<Scope, Integer> VInstr_map;
    public Set<String> def_set;
    public Set<String> use_set;
    public Set<String> in_set;
    public Set<String> out_set;
    //public Vector<String> local_set;
    public int local_size;
    public int out_size;
    public int in_size;
    public int current_pos;
    public boolean dont_add;
    public String branch_label_wait;

    public Node_Visitor(){
        VInstr_map = new HashMap<Scope, Integer>();
        def_set = new HashSet<String>();
        use_set = new HashSet<String>();
        in_set = new HashSet<String>();
        out_set = new HashSet<String>();
        //local_set = new Vector<String>();
        local_size = 0;
        out_size = 0;
        in_size = 0;
        current_pos = 0;
        branch_label_wait = "";
        dont_add = false;
    }
    public void print_function(){
        //remove_duplicates(def_set);
        //remove_duplicates(use_set);
        System.out.println("Def set vector: " + def_set);
        System.out.println("Use set vector: " + use_set);
        for(Map.Entry<Scope,Integer> entry: VInstr_map.entrySet()){
            Scope local_scope = entry.getKey();
            int local_value = entry.getValue();
            System.out.println("Value: " + local_value);
            local_scope.print_scope();
        }
    }

    public void remove_duplicates(Vector<String> temp_vector){
        for(int i = 0;i < temp_vector.size();i++){
            for(int j = 0 ; j < temp_vector.size();j++){
                if(i != j){
                    if(temp_vector.elementAt(i).equals(temp_vector.elementAt(j))){
                        temp_vector.removeElementAt(j);
                    }
                }
            }
        }
    }
    public void increment_pos(){
        current_pos = current_pos + 1;
    }
    public void set_current_pos(int current_index){
        current_pos = current_index;
    }
    public int get_live_interval(int current_index, int start_index){
        int total_size = start_index - current_index;
        return Math.abs(total_size);
    }
    public void add_scope(Scope temp_scope,int index){
        boolean isnt_found = false;
        for(Map.Entry<Scope,Integer> entry : VInstr_map.entrySet()){
            Scope local_scope = entry.getKey();
            if(local_scope.get_name().equals(temp_scope.get_name())){
                if(local_scope.end_point < index){
                    local_scope.end_point = index;
                }
                int total_distance = get_live_interval(index,local_scope.start_point);
                VInstr_map.replace(local_scope,total_distance);
                isnt_found = true;
            }
        }
        if(!isnt_found){
            VInstr_map.put(temp_scope,index);
        }
    }
    public void clear_sets(){
        def_set = new HashSet<String>();
        use_set = new HashSet<String>();
        in_set = new HashSet<String>();
        out_set = new HashSet<String>();

    }
    //num_aux = 1
    //VVraf Dest - Location being stored to. - num_aux
    //VOperand Source - 1
    public String visit(Integer p, VAssign a){
        System.out.println("Integer: " + p);
        String _ret = "";
        if(a.source instanceof VLitStr){
            VLitStr temp_x = (VLitStr)a.source;
            if(temp_x.toString().contains("t.")){
                use_set.add(temp_x.toString());
                //Scope temp_scope = new Scope(current_pos,current_pos,temp_x.toString());
                //add_scope(temp_scope,current_pos);
            }
            System.out.println("VAssign - String Value: " + temp_x.toString());

        }else if(a.source instanceof VLitInt){
            VOperand.Static list_args = (VOperand.Static)a.source;
            VLitInt int_literal = (VLitInt)list_args;
            System.out.println("VAssign - Integer Value: " + int_literal.toString());
        }
        if(a.dest instanceof VVarRef.Local){
            VVarRef.Local temp_local = (VVarRef.Local)a.dest;
            System.out.println("VAssign - Store to: " + temp_local.toString());
            if(temp_local.toString().contains("t.")){
                def_set.add(temp_local.toString());
                //Scope temp_scope = new Scope(current_pos,current_pos,temp_local.toString());
                //add_scope(temp_scope,current_pos);
            }else{
                //local_set.add(temp_local.toString());
                local_size = local_size + 1;
            }
        }
        return _ret;
    }
    /*
    VAddr<VFunction> addr; address of function being called
    VOperand[] args - arguments pass into the functions
    VVarRef.Local dest - variable used to store the return value of the function - null also
    */
    public String visit(Integer p , VCall c){
        String _ret = "";
        if(c.addr instanceof VAddr.Label){
            VAddr.Label temp_label = (VAddr.Label)c.addr;
            System.out.println("VCall Addr Function Label: " + temp_label.toString());
        }else if(c.addr instanceof VAddr.Var){
            VAddr.Var temp_var = (VAddr.Var)c.addr;
            System.out.println("VCall Addr Function Variable: " + temp_var.toString());
            if(temp_var.toString().contains("t.")){
                use_set.add(temp_var.toString());
                //Scope temp_scope = new Scope(current_pos,current_pos,temp_var.toString());
                //add_scope(temp_scope,current_pos);
            }
        }
        if(c.dest != null){
            System.out.println("VCall Dest: " + c.dest.toString());
            //Defining variable
            if(c.dest.toString().contains("t.")){
                def_set.add(c.dest.toString());
                //Scope temp_scope = new Scope(current_pos,current_pos,c.dest.toString());
                //add_scope(temp_scope,current_pos);
            }else{
                local_size = local_size + 1;
            }
        }
        VOperand[] list_args = c.args;
        for(int i = 0 ; i < list_args.length ; i++){
            if(list_args[i] instanceof VLitStr){
                System.out.println("VCall - String Literal Argument " + i + ": " + list_args[i].toString());
                //in_set.add(list_args[i].toString());
            }else if(list_args[i] instanceof VVarRef){
                VVarRef temp_var_ref = (VVarRef)list_args[i];
                if(temp_var_ref instanceof VVarRef.Local){
                    VVarRef.Local temp_var_ref_local = (VVarRef.Local)temp_var_ref;
                    System.out.println("VCall - Local Argument: " + temp_var_ref.toString());
                    if(temp_var_ref.toString().contains("t.")){
                        use_set.add(temp_var_ref.toString());
                        //Scope temp_scope = new Scope(current_pos,current_pos, temp_var_ref.toString());
                        //add_scope(temp_scope,current_pos);
                    }
                    //in_set.add(temp_var_ref.toString());
                }

            }else if(list_args[i] instanceof VLitInt){
                VOperand.Static temp_static = (VOperand.Static)list_args[i];
                VLitInt literal_int = (VLitInt)temp_static;
                System.out.println("VCall - Integer Literal Argument " + i + ": " + literal_int.toString());
                //in_set.add(literal_int.toString());
            }
        }
        //System.out.println("VCall was accessed");
        return _ret;
    }
    /*
    Op op - operation being performed
    VOperand[] args- arguments to the operations
    VVarRef dest - variable/register to store the result of the operation

    */
    public String visit(Integer p ,VBuiltIn c) {
        String _ret = "";
        System.out.println("Index: " + Integer.toString(p));
        System.out.println("Op Name: " + c.op.name + " Param Size: " + c.op.numParams);
        if(c.dest instanceof VVarRef.Local){
            VVarRef.Local temp_local = (VVarRef.Local)c.dest;
            System.out.println("Dest Local: " + temp_local.toString());
            if(temp_local.toString().contains("t.")){
                def_set.add(temp_local.toString());
                //Scope temp_scope = new Scope(current_pos,current_pos,temp_local.toString());
                //add_scope(temp_scope,current_pos);
            }else{
                local_size = local_size + 1;
            }
        }

        VOperand[] list_args = c.args;
        for(int i = 0 ; i < list_args.length ; i++){
            if(list_args[i] instanceof VLitStr){
                System.out.println("Index: " + Integer.toString(p)  + " VBuilt in - String Literal Argument " + i + ": " + list_args[i].toString());
                _ret = "false";
            }else if(list_args[i] instanceof VVarRef){
                VVarRef temp_var_ref = (VVarRef)list_args[i];
                if(temp_var_ref instanceof VVarRef.Local){
                    VVarRef.Local temp_var_ref_local = (VVarRef.Local)temp_var_ref;
                    System.out.println("VBuiltIn - Local Argument: " + temp_var_ref_local.toString());
                    if(temp_var_ref_local.toString().contains("t.")){
                        use_set.add(temp_var_ref_local.toString());
                        //Scope temp_scope = new Scope(current_pos,current_pos,temp_var_ref_local.toString());
                        //add_scope(temp_scope,current_pos);
                    }
                    //in_set.add(integer_literal.toString());
                }
            }else if(list_args[i] instanceof VLitInt){
                VOperand.Static static_value = (VOperand.Static)list_args[i];
                VLitInt integer_literal = (VLitInt)static_value;
                System.out.println("VBuiltIn - Integer Literal: " + integer_literal.toString());
                //in_set.add(integer_literal.toString());
            }
        }
        //System.out.println("VBuiltIn was accessed");
        return _ret;
    }
    /*
        VMemRef dest - memory location being written to
        VOperand source- value being written.
    */
    public String visit(Integer p , VMemWrite w){
        String _ret = "";


        if(w.dest instanceof VMemRef.Global){

            //VMemRef data = w.dest;
            VMemRef.Global c2 = (VMemRef.Global)w.dest;

            VAddr<VDataSegment> holy_one = c2.base;
            if(holy_one instanceof VAddr.Label){
                VAddr.Label temp_label = (VAddr.Label)holy_one;
                System.out.println("Write to Addr of Label: " + temp_label.toString());
            }else if(holy_one instanceof VAddr.Var){
                VAddr.Var temp_var = (VAddr.Var)holy_one;
                System.out.println("Write to Addr of Variable: " + temp_var.toString());
                if(temp_var.toString().contains("t.")){
                    def_set.add(temp_var.toString());
                    //Scope temp_scope = new Scope(current_pos,current_pos,temp_var.toString());
                    //add_scope(temp_scope,current_pos);
                }
            }
            VOperand list_args = w.source;
            VOperand.Static temp_label = (VOperand.Static)list_args;

            if(list_args instanceof VLitStr){
                System.out.println("VMemWrite - String Literal Argument: " + list_args.toString());
            }else if(list_args instanceof VVarRef){
                VVarRef temp_var_ref = (VVarRef)list_args;
                if(temp_var_ref instanceof VVarRef.Local){
                    VVarRef.Local temp_var_ref_local = (VVarRef.Local)temp_var_ref;
                    System.out.println("VMemWrite - Local Argument: " + temp_var_ref_local.toString());
                    if(temp_var_ref_local.toString().contains("t.")){
                        use_set.add(temp_var_ref_local.toString());
                        //Scope temp_scope = new Scope(current_pos,current_pos,temp_var_ref_local.toString());
                        //add_scope(temp_scope,current_pos);
                    }
                }
            }else if(temp_label instanceof VLitInt){
                VOperand.Static list_value = (VOperand.Static)list_args;
                VLitInt integer_literal = (VLitInt)list_value;
                System.out.println("VMemWrite - Integer Literal: " + integer_literal.toString());

            }else if(temp_label instanceof VLabelRef){
                VLabelRef temp_label_ref = (VLabelRef)temp_label;
                System.out.println("Label Ref: " + temp_label_ref.ident);
            }
        }
        return _ret;

        //System.out.println("VMemWrite was accessed");
    }
    /*
        VVarRef dest - variable/register to store the value isnt_found
        VMemRef source - memory location being read
    */
    public String visit(Integer p ,VMemRead r)  {
        String _ret = "";
        if(r.source instanceof VMemRef.Global){
            VMemRef.Global _global = (VMemRef.Global)r.source;
            //VMemRef.Global c = r.source;
            VAddr<VDataSegment> c2 = _global.base;
            if(c2 instanceof VAddr.Label){
                VAddr.Label temp_label = (VAddr.Label)c2;
                System.out.println("Read Addr of Label: " + temp_label.toString());
            }else if(c2 instanceof VAddr.Var){
                VAddr.Var temp_var = (VAddr.Var)c2;
                System.out.println("Read Addr of Variable: " + temp_var.toString());
                if(temp_var.toString().contains("t.")){
                    use_set.add(temp_var.toString());
                    //Scope temp_scope = new Scope(current_pos,current_pos,temp_var.toString());
                    //add_scope(temp_scope,current_pos);
                }
            }
        }
        if(r.dest instanceof VVarRef.Local){
            VVarRef.Local temp_local = (VVarRef.Local)r.dest;
            System.out.println("Index: " + Integer.toString(p) + " Store to: " + temp_local.toString());
            if(temp_local.toString().contains("t.")){
                def_set.add(temp_local.toString());
                //Scope temp_scope = new Scope(current_pos, current_pos, temp_local.toString());
                //add_scope(temp_scope,current_pos);
            }
        }
        return _ret;
        //System.out.println("VMemRead was accessed");
    }
    /*
        VOperand value - value

    */
    public String visit(Integer p ,VBranch b) {
        String _ret = "";
        System.out.println("Current_index: " + Integer.toString(p) + " Goto branch " + b.target.toString());
        System.out.println("Branch Boolean Value: " + b.positive);
        _ret = b.target.toString();


        //System.out.println("VBranch was accessed");
        VOperand list_args = b.value;
        if(list_args instanceof VOperand.Static){
            VOperand.Static temp_branch_label = (VOperand.Static)list_args;
            VLabelRef da_label = (VLabelRef)temp_branch_label;
            System.out.println("Branch Label: " + da_label.ident);
        }
        if(list_args instanceof VLitStr){
            System.out.println("VBranch - String Literal Argument: " + list_args.toString());
        }else if(list_args instanceof VVarRef){
            VVarRef temp_var_ref = (VVarRef)list_args;
            if(temp_var_ref instanceof VVarRef.Local){
                System.out.println("VBranch - Local Argument: " + temp_var_ref.toString());
                if(temp_var_ref.toString().contains("t.")){
                    use_set.add(temp_var_ref.toString());

                    //Scope temp_scope = new Scope(current_pos,current_pos,temp_var_ref.toString());
                    //add_scope(temp_scope,current_pos);
                }
            }
        }else if(list_args instanceof VLitInt){
            VOperand.Static list_value = (VOperand.Static)list_args;
            VLitInt integer_literal = (VLitInt)list_value;
            System.out.println("VBranch - Integer Literal: " + integer_literal.toString());
        }
        return _ret;
    }
    public String visit(Integer p, VGoto g)  {
        String _ret = "";
        VAddr<VCodeLabel> temp_g = g.target;
        if(temp_g instanceof VAddr.Label){
            VAddr.Label temp_g2 = (VAddr.Label)temp_g;
            System.out.println("Current_index: " + Integer.toString(p) + " Goto " + temp_g2.toString());
            _ret = temp_g2.toString();
            increment_pos();

        }else if(temp_g instanceof VAddr.Var){
            VAddr.Var temp_g3 = (VAddr.Var)temp_g;
            System.out.println("Current_index: " + Integer.toString(p) + " Goto " + temp_g3.toString());
            _ret = temp_g3.toString();
            increment_pos();
        }

        return _ret;
        //System.out.println("VGoto was accessed");
    }
    public String visit(Integer p , VReturn r)  {
        String _ret = "";
        VOperand list_args = r.value;
        if(list_args instanceof VOperand.Static){
            VOperand.Static return_label = (VOperand.Static)list_args;
            VLabelRef da_label = (VLabelRef)return_label;
            System.out.println("Return Label: " + da_label.ident);
        }
        if(list_args instanceof VLitStr){
            System.out.println("VReturn - String Literal Argument: " + list_args.toString());
        }else if(list_args instanceof VVarRef.Local){
            System.out.println("VReturn - Local Argument: " + list_args.toString());
            if(list_args.toString().contains("t.")){
                use_set.add(list_args.toString());
                //Scope temp_scope = new Scope(current_pos, current_pos, list_args.toString());
                //add_scope(temp_scope,current_pos);
            }
        }else if(list_args instanceof VLitInt){
            VOperand.Static list_value = (VOperand.Static)list_args;
            VLitInt integer_literal = (VLitInt)list_value;
            System.out.println("VReturn - Integer Literal: " + integer_literal.toString());
        }
        //System.out.println("VReturn was accessed");
        return _ret;
    }
}
