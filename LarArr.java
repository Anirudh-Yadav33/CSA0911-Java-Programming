import java.util.*;
public class LarArr{
    public static void main(String[] args) {
        int a[] = {1, 2, 9, 6, 5, 18, 4, 7};
        int max = a[0];
        for (int i = 0; i < a.length; i++) {
            if (a[i] > max) {
                max = a[i];
            }
        }
        System.out.println(max);
    }
}