package stjava.tests.multi;


import java.util.Collections;

public class Main {
    public static void main(String[] args) {
        Class1 class1 = new Class1();
        doCompatible1(class1); // should not be compile time error
        doCompatible2(class1); // should not be compile time error
    }

    private static void doCompatible1(Compatible1 compatible1) {
        System.out.println(compatible1.getId());
    }

    private static void doCompatible2(Compatible2 compatible2) {
        System.out.println(compatible2.findSomething(1L, Collections.emptyList()));
    }
}
