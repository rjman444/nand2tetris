import java.io.*;

public class test {
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("input.txt")));
        int c;
        while((c = br.read()) != -1) {
            if (c == 13 || c == 10 || c == 32) continue;
            System.out.println((char) c);
        }
    }
}
