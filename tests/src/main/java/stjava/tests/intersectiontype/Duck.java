package stjava.tests.intersectiontype;

import stjava.annotation.StructurallyTyped;

@StructurallyTyped
public class Duck implements CanSwim, CanFly, STIoDuck {
    @Override
    public void fly() {
        System.out.println("Duck flies");
    }

    @Override
    public void swim() {
        System.out.println("Duck swims");
    }
}
