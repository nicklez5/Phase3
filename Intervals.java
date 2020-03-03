import java.util.*;
public class Intervals{

    public int start_point = 0;
    public int end_point = 0;
    public String string_name = "";
    public Intervals(int start_time, int end_time, String _name){
        start_point = start_time;
        end_point = end_time;
        string_name = _name;
    }
    public void simple_print(){
        System.out.println("Start point: " + start_point + " End point: " + end_point + " ID: " + string_name);

    }
}
