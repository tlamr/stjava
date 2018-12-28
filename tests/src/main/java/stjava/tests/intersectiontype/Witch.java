package stjava.tests.intersectiontype;

import stjava.annotation.StructurallyTyped;

@StructurallyTyped
public class Witch implements CanFly, CanCook, STIoWitch {
    @Override
    public void cook() {
        System.out.println("Witch cooks");
    }

    @Override
    public void fly() {
        System.out.println("Witch flies");
    }
}
