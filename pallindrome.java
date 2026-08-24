import java.util.*;
public class pallindrome {
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        String n = sc.nextLine();
        String rev = "";
        for(int i = n.length()-1;i>=0;i--){
            rev += n.charAt(i);
        }
        if(n.equals(rev)){
            System.out.print("Pallindrome");
        }
        else{
            System.out.print("Not Pallindrome");
        }
    }
}
