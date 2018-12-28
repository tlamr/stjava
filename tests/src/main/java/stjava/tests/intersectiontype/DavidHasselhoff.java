package stjava.tests.intersectiontype;

import stjava.annotation.StructurallyTyped;

@StructurallyTyped
public class DavidHasselhoff implements CanCook, CanSwim, STIoDavidHasselhoff {
    @Override
    public void cook() {
        System.out.println("David cooks");
    }

    @Override
    public void swim() {
        System.out.println("David swims");
    }
}
