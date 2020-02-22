import cs132.vapor.ast.*;
import cs132.vapor.ast.VInstr.Visitor;
import cs132.vapor.ast.VVarRef.Register;
import cs132.vapor.ast.VVarRef.Local;
import cs132.vapor.ast.VMemRef.Global;
import cs132.vapor.ast.VMemRef.Stack;
public class Node_Visitor extends VInstr.Visitor{

    public Node_Visitor(){

    }
    public void visit(VAssign a){
        //System.out.println("VAssign was accessed");
        if(a.dest instanceof VVarRef.Register){
            VVarRef.Register temp_reg = (VVarRef.Register)a.dest;
            System.out.println("Store to:" + temp_reg.toString());
        }else if(a.dest instanceof VVarRef.Local){
            VVarRef.Local temp_local = (VVarRef.Local)a.dest;
            System.out.println("Store to:" + temp_local.toString());
        }
    }
    public void visit(VCall c){
        if(c.addr instanceof VAddr.Label){
            VAddr.Label temp_label = (VAddr.Label)c.addr;
            System.out.println("Addr of Label:" + temp_label.toString());
        }else if(c.addr instanceof VAddr.Var){
            VAddr.Var temp_var = (VAddr.Var)c.addr;
            System.out.println("Addr of Variable:" + temp_var.toString());
        }
        if(c.dest != null){
            System.out.println("Dest:" + c.dest.toString());
        }
        //System.out.println("VCall was accessed");
    }
    public void visit(VBuiltIn c){
        System.out.println("Op Name:" + c.op.name);
        if(c.dest instanceof VVarRef.Register){
            VVarRef.Register temp_reg = (VVarRef.Register)c.dest;
            System.out.println("Dest:" + temp_reg.toString());
        }else if(c.dest instanceof VVarRef.Local){
            VVarRef.Local temp_local = (VVarRef.Local)c.dest;
            System.out.println("Dest:" + temp_local.toString());
        }
        //System.out.println("VBuiltIn was accessed");
    }
    public void visit(VMemWrite w){
        if(w.dest instanceof VMemRef.Global){

            //VMemRef data = w.dest;
            VMemRef.Global c2 = (VMemRef.Global)w.dest;

            VAddr<VDataSegment> holy_one = c2.base;
            if(holy_one instanceof VAddr.Label){
                VAddr.Label temp_label = (VAddr.Label)holy_one;
                System.out.println("Write to Addr of Label:" + temp_label.toString());
            }else if(holy_one instanceof VAddr.Var){
                VAddr.Var temp_var = (VAddr.Var)holy_one;
                System.out.println("Write to Addr of Variable:" + temp_var.toString());
            }
        }
        //System.out.println("VMemWrite was accessed");
    }
    public void visit(VMemRead r){
        if(r.source instanceof VMemRef.Global){
            VMemRef.Global _global = (VMemRef.Global)r.source;
            //VMemRef.Global c = r.source;
            VAddr<VDataSegment> c2 = _global.base;
            if(c2 instanceof VAddr.Label){
                VAddr.Label temp_label = (VAddr.Label)c2;
                System.out.println("Read Addr of Label:" + temp_label.toString());
            }else if(c2 instanceof VAddr.Var){
                VAddr.Var temp_var = (VAddr.Var)c2;
                System.out.println("Read Addr of Variable:" + temp_var.toString());
            }
        }
        if(r.dest instanceof VVarRef.Register){
            VVarRef.Register temp_reg = (VVarRef.Register)r.dest;
            System.out.println("Store to:" + temp_reg.toString());
        }else if(r.dest instanceof VVarRef.Local){
            VVarRef.Local temp_local = (VVarRef.Local)r.dest;
            System.out.println("Store to:" + temp_local.toString());
        }
        //System.out.println("VMemRead was accessed");
    }
    public void visit(VBranch b){
        System.out.println("Goto branch" + b.target.toString());
        //System.out.println("VBranch was accessed");
    }
    public void visit(VGoto g){
        VAddr<VCodeLabel> temp_g = g.target;
        if(temp_g instanceof VAddr.Label){
            VAddr.Label temp_g2 = (VAddr.Label)temp_g;
            System.out.println("Goto:" + temp_g2.toString());
        }else if(temp_g instanceof VAddr.Var){
            VAddr.Var temp_g3 = (VAddr.Var)temp_g;
            System.out.println("Goto:" + temp_g3.toString());
        }
        //System.out.println("VGoto was accessed");
    }
    public void visit(VReturn r){
        //System.out.println("VReturn was accessed");
    }
}
