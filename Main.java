import java.util.*;
public class Main{
    public static void main(String[]args){
      int a[][] = {{1,2},{2,3}};
      int b[][] = {{4,5},{6,7}};
      int c[][] = new int[2][2];
      for(int i=0;i<2;i++){
          for(int j=0;j<2;j++){
              for(int k=0;k<2;k++){
                  c[i][j] +=a[i][k]*b[k][j];
              }
          }
      }
      for(int i=0;i<2;i++){
          for(int j=0;j<2;j++){
              System.out.print(c[i][j]+" ");
          }
          System.out.println();
      }
    }

}