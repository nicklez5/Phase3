import java.util.*;
import cs132.vapor.ast.*;
import cs132.vapor.ast.VInstr.Visitor;
import cs132.vapor.ast.VVarRef.Register;
import cs132.vapor.ast.VVarRef.Local;
import cs132.vapor.ast.VMemRef.Global;
import cs132.vapor.ast.VMemRef.Stack;
public class Print_Visitor extends VInstr.VisitorPR< Integer , String, RuntimeException>{
    public boolean first_time;
    public Map<String,String> print_visitor_map;
    public Map<Pair<Integer,String>,String> cur_reg_map;
    public int local_t;
    public int local_s;
    public int local_a;
    public int local_v;
    public int current_index;
    public Print_Visitor(){
        local_t = 0;
        local_s = 0;
        local_a = 0;
        local_v = 0;
        first_time = true;
        print_visitor_map = new HashMap<String,String>();
        cur_reg_map = new HashMap<Pair<Integer,String>,String>();
        current_index = 0;
    }
    public void set_index(int temp_xyz){
        current_index = temp_xyz;
    }
    public void set_reg_map(Map<Pair<Integer,String>,String> temp_xyz){
        cur_reg_map = temp_xyz;
    }
    public void set_map(Map<String,String> temp_map_xyz){
        print_visitor_map = temp_map_xyz;
    }
    public String visit(Integer p, VAssign a){

        String _ret = "";
        if(a.source instanceof VLitStr){
            VLitStr temp_x = (VLitStr)a.source;

            //System.out.println("VAssign - String Value: " + temp_x.toString());

        }else if(a.source instanceof VLitInt){
            VOperand.Static list_args = (VOperand.Static)a.source;
            VLitInt int_literal = (VLitInt)list_args;
            //System.out.println("VAssign - Integer Value: " + int_literal.toString());
        }
        if(a.dest instanceof VVarRef.Local){
            VVarRef.Local temp_local = (VVarRef.Local)a.dest;
            //System.out.println("VAssign - Store to: " + temp_local.toString());

        }
        return _ret;
    }
    public String visit(Integer p, VCall c){
        String _ret = "";
        String call_id = "";
        if(c.addr instanceof VAddr.Label){
            VAddr.Label temp_label = (VAddr.Label)c.addr;
            //System.out.println("VCall Addr Function Label: " + temp_label.toString());
        }else if(c.addr instanceof VAddr.Var){
            VAddr.Var temp_var = (VAddr.Var)c.addr;
            //System.out.println("VCall Addr Function Variable: " + temp_var.toString());
            call_id = temp_var.toString();
            call_id = print_visitor_map.get(call_id);
        }
        if(c.dest != null){
            //System.out.println("VCall Dest: " + c.dest.toString());
            //Defining variable
            print_visitor_map.put(c.dest.toString(),call_id);

        }
        VOperand[] list_args = c.args;
        for(int i = 0 ; i < list_args.length ; i++){
            if(list_args[i] instanceof VLitStr){
                //System.out.println("VCall - String Literal Argument " + i + ": " + list_args[i].toString());
                //in_set.add(list_args[i].toString());
            }else if(list_args[i] instanceof VVarRef){
                VVarRef temp_var_ref = (VVarRef)list_args[i];
                if(temp_var_ref instanceof VVarRef.Local){
                    VVarRef.Local temp_var_ref_local = (VVarRef.Local)temp_var_ref;
                    System.out.println("\t" + "$a" + local_a + " = " + temp_var_ref.toString());
                    local_a++;

                    //in_set.add(temp_var_ref.toString());
                }

            }else if(list_args[i] instanceof VLitInt){
                VOperand.Static temp_static = (VOperand.Static)list_args[i];
                VLitInt literal_int = (VLitInt)temp_static;
                System.out.println("\t" + "$a" + local_a + " = " + literal_int.toString());
                local_a++;
                //in_set.add(literal_int.toString());
            }
        }

        System.out.println("\t" + "call " + call_id );
        System.out.println("\t" + call_id + " = $v" + local_v );
        local_v++;
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
        String built_in_label = c.op.name;
        String parameter_value = "";
        //System.out.println("Index: " + Integer.toString(p));
        //System.out.println("Op Name: " + c.op.name + " Param Size: " + c.op.numParams);
        //System.out.println("Destination: " + c.dest);
        if(c.dest instanceof VVarRef.Local){
            VVarRef.Local temp_local = (VVarRef.Local)c.dest;
            //System.out.println("Dest Local: " + temp_local.toString());

        }

        VOperand[] list_args = c.args;
        for(int i = 0 ; i < list_args.length ; i++){

            if(list_args[i] instanceof VLitStr){
                System.out.println("\t" + "Error(" + list_args[i].toString() + ")");
                _ret = "false";
                return _ret;
            }else if(list_args[i] instanceof VVarRef){
                VVarRef temp_var_ref = (VVarRef)list_args[i];
                if(temp_var_ref instanceof VVarRef.Local){
                    VVarRef.Local temp_var_ref_local = (VVarRef.Local)temp_var_ref;
                    _ret = print_visitor_map.get(temp_var_ref_local.toString());

                    //in_set.add(integer_literal.toString());
                }
            }else if(list_args[i] instanceof VLitInt){
                VOperand.Static static_value = (VOperand.Static)list_args[i];
                VLitInt integer_literal = (VLitInt)static_value;
                //System.out.println("VBuiltIn - Integer Literal: " + integer_literal.toString());
                parameter_value = integer_literal.toString();
                //in_set.add(integer_literal.toString());
            }
        }
        if(built_in_label.contains("HeapAllocZ")){
            System.out.println("\t" + "$t" + local_t + " = " + built_in_label + "(" + parameter_value + ")");
            print_visitor_map.put(c.dest.toString(),"$t" + local_t);
            local_t++;
        }else if(built_in_label.contains("PrintIntS")){
            System.out.println("\t" + "PrintIntS(" + _ret + ")");
        }
        //System.out.println("VBuiltIn was accessed");
        return _ret;
    }
    /*
        VMemRef dest - memory location being written to
        VOperand source- value being written.
    */
    public boolean check_if_contain_reg(String xyz){
        if(print_visitor_map.containsKey(xyz)){
            return true;
        }
        return false;
    }
    public String visit(Integer p , VMemWrite w){
        String _ret = "";
        String dest_id = "";

        if(w.dest instanceof VMemRef.Global){

            //VMemRef data = w.dest;
            VMemRef.Global c2 = (VMemRef.Global)w.dest;

            VAddr<VDataSegment> holy_one = c2.base;
            if(holy_one instanceof VAddr.Label){
                VAddr.Label temp_label = (VAddr.Label)holy_one;
                //System.out.println("Write to Addr of Label: " + temp_label.toString());
            }else if(holy_one instanceof VAddr.Var){
                VAddr.Var temp_var = (VAddr.Var)holy_one;
                //System.out.println("Write to Addr of Variable: " + temp_var.toString());
                dest_id = print_visitor_map.get(temp_var.toString());

            }
            VOperand list_args = w.source;
            VOperand.Static temp_label = (VOperand.Static)list_args;

            if(list_args instanceof VLitStr){
                //System.out.println("VMemWrite - String Literal Argument: " + list_args.toString());
            }else if(list_args instanceof VVarRef){
                VVarRef temp_var_ref = (VVarRef)list_args;
                if(temp_var_ref instanceof VVarRef.Local){
                    VVarRef.Local temp_var_ref_local = (VVarRef.Local)temp_var_ref;
                    //System.out.println("VMemWrite - Local Argument: " + temp_var_ref_local.toString());

                }
            }else if(temp_label instanceof VLitInt){
                VOperand.Static list_value = (VOperand.Static)list_args;
                VLitInt integer_literal = (VLitInt)list_value;
                //System.out.println("VMemWrite - Integer Literal: " + integer_literal.toString());

            }else if(temp_label instanceof VLabelRef){
                VLabelRef temp_label_ref = (VLabelRef)temp_label;
                System.out.println("\t" + "[" + dest_id + "] = :" + temp_label_ref.ident);
            }
        }
        return _ret;

        //System.out.println("VMemWrite was accessed");
    }
    public String return_reg(Pair<Integer,String> temp_pairs){
        String _ret = "";
        for(Map.Entry<Pair<Integer,String>,String> entry: cur_reg_map.entrySet()){
            Pair<Integer,String> temp_paird = entry.getKey();
            if(temp_paird.getKey() == temp_pairs.getKey() && temp_paird.getValue().equals(temp_pairs.getValue())){
                _ret = entry.getValue();
                return _ret;
            }
        }
        return _ret;
    }
    /*
        VVarRef dest - variable/register to store the value isnt_found
        VMemRef source - memory location being read
    */
    public String visit(Integer p ,VMemRead r)  {
        Pair<Integer,String> temp_pair;
        String _ret = "";
        String LHS = "";
        String RHS = "";
        if(r.source instanceof VMemRef.Global){
            VMemRef.Global _global = (VMemRef.Global)r.source;
            //VMemRef.Global c = r.source;
            VAddr<VDataSegment> c2 = _global.base;
            if(c2 instanceof VAddr.Label){
                VAddr.Label temp_label = (VAddr.Label)c2;
                //ystem.out.println("Read Addr of Label: " + temp_label.toString());
            }else if(c2 instanceof VAddr.Var){
                VAddr.Var temp_var = (VAddr.Var)c2;
                //System.out.println("Read Addr of Variable: " + temp_var.toString());
                RHS = print_visitor_map.get(temp_var.toString());
                if(temp_var.toString().contains("t.")){

                    //Scope temp_scope = new Scope(current_pos,current_pos,temp_var.toString());
                    //add_scope(temp_scope,current_pos);
                }
            }
        }
        if(r.dest instanceof VVarRef.Local){
            VVarRef.Local temp_local = (VVarRef.Local)r.dest;
            //System.out.println("Index: " + Integer.toString(p) + " Store to: " + temp_local.toString());
            if(temp_local.toString().contains("t.")){
                temp_pair = new Pair<Integer,String>(p.intValue(), temp_local.toString());
                _ret = return_reg(temp_pair);
                if(!_ret.isEmpty()){
                    if(!check_if_contain_reg(temp_local.toString())){
                        print_visitor_map.put(temp_local.toString(),_ret);
                        System.out.println("\t" + _ret + " = [" + RHS + "]");
                    }else{
                        LHS = print_visitor_map.get(temp_local.toString());

                    }
                }
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
        String var_id = "";
        String branch_target = b.target.toString();
        //System.out.println("Current_index: " + Integer.toString(p) + " Goto branch " + b.target.toString());
        //System.out.println("Branch Boolean Value: " + b.positive);

        _ret = b.target.toString();


        //System.out.println("VBranch was accessed");
        VOperand list_args = b.value;
        if(list_args instanceof VOperand.Static){
            VOperand.Static temp_branch_label = (VOperand.Static)list_args;
            VLabelRef da_label = (VLabelRef)temp_branch_label;
            //System.out.println("Branch Label: " + da_label.ident);
        }
        if(list_args instanceof VLitStr){
            //System.out.println("VBranch - String Literal Argument: " + list_args.toString());
        }else if(list_args instanceof VVarRef){
            VVarRef temp_var_ref = (VVarRef)list_args;
            if(temp_var_ref instanceof VVarRef.Local){
                //System.out.println("VBranch - Local Argument: " + temp_var_ref.toString());
                var_id = print_visitor_map.get(temp_var_ref.toString());

            }
        }else if(list_args instanceof VLitInt){
            VOperand.Static list_value = (VOperand.Static)list_args;
            VLitInt integer_literal = (VLitInt)list_value;
            //System.out.println("VBranch - Integer Literal: " + integer_literal.toString());
        }

        //if
        if(b.positive){
            System.out.println("\t" + "if " + var_id + " goto " + branch_target);
        }else{
            //if0
            System.out.println("\t" + "if0 " + var_id + " goto " + branch_target);
        }
        return _ret;
    }
    public String visit(Integer p, VGoto g)  {
        String _ret = "";
        VAddr<VCodeLabel> temp_g = g.target;
        if(temp_g instanceof VAddr.Label){
            VAddr.Label temp_g2 = (VAddr.Label)temp_g;
            //System.out.println("Current_index: " + Integer.toString(p) + " Goto " + temp_g2.toString());
            _ret = temp_g2.toString();


        }else if(temp_g instanceof VAddr.Var){
            VAddr.Var temp_g3 = (VAddr.Var)temp_g;
            //System.out.println("Current_index: " + Integer.toString(p) + " Goto " + temp_g3.toString());
            _ret = temp_g3.toString();

        }

        return _ret;
        //System.out.println("VGoto was accessed");
    }
    public String visit(Integer p , VReturn r)  {
        String _ret = "";
        VOperand list_args = r.value;
        boolean isEmpty = true;
        if(list_args instanceof VOperand.Static){
            VOperand.Static return_label = (VOperand.Static)list_args;
            VLabelRef da_label = (VLabelRef)return_label;
            //System.out.println("Return Label: " + da_label.ident);
            isEmpty = false;
        }
        if(list_args instanceof VLitStr){
            //System.out.println("VReturn - String Literal Argument: " + list_args.toString());
            isEmpty = false;
        }else if(list_args instanceof VVarRef.Local){
            //System.out.println("VReturn - Local Argument: " + list_args.toString());

            isEmpty = false;
        }else if(list_args instanceof VLitInt){
            VOperand.Static list_value = (VOperand.Static)list_args;
            VLitInt integer_literal = (VLitInt)list_value;
            //System.out.println("VReturn - Integer Literal: " + integer_literal.toString());
            isEmpty = false;
        }
        if(isEmpty){
            System.out.println("\t" + "ret");
        }
        //System.out.println("VReturn was accessed");
        return _ret;
    }
}
