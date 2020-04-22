public class DoubleLabe {
    public static void main(String[] args) {
        double x = 49999 / 100000.0;
        x = ((int) (x * 10000)) / 10000.0;
        System.out.println(x);
        System.out.println(Math.rint((49999 / 100000.0) * 10000) / 10000.0);
    }
}
