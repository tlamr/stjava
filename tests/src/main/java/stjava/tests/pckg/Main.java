package stjava.tests.pckg;

public class Main {
    public static void main(String[] args) {
        Class1 class1 = new Class1();
        doReturnParam(class1); // should not be compile time error
        //doIncompatible(class1); // this is compile time error, thus commented out
    }

    private static void doReturnParam(Returnable returnable) {
        System.out.println(returnable.returnParam("aaa"));
    }

    private static void doIncompatible(Incompatible incompatible) {
        System.out.println(incompatible.returnParam(1L));
    }
}
