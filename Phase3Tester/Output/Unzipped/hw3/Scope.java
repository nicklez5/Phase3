import java.util.*;
public class Scope{
    public int start_point;
    public int end_point;
    public String string_name;
    public Scope(){
        start_point = 0;
        end_point = 0;
        string_name = "";
    }
    public Scope(int start_1, int end_1, String reg_1){
        start_point = start_1;
        end_point = end_1;
        string_name = reg_1;
    }
    public String get_name(){
        return string_name;
    }
    public void print_scope(){
        System.out.println("String Name: " + string_name);
        System.out.println("Start Point: " + start_point);
        System.out.println("End Point: " + end_point);
    }
}
