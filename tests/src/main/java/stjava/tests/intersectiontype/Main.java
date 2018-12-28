package stjava.tests.intersectiontype;

public class Main {

    public static void main(String[] args) {
        DavidHasselhoff davidHasselhoff = new DavidHasselhoff();
        Duck duck = new Duck();
        Witch witch = new Witch();

        useCookAndSwim(davidHasselhoff); // should not be compile time error
        useSwimAndFly(duck);             // should not be compile time error
        useFlyAndCook(witch);            // should not be compile time error
    }

    private static void useSwimAndFly(CanSwimAndCanFly canSwimAndCanFly) {  // even though you wanted rather <T extends CanSwim & CanFly> void useSwimAndFly(T canSwimAndCanFly)
        canSwimAndCanFly.swim();
        canSwimAndCanFly.fly();
    }

    private static void useFlyAndCook(CanFlyAndCanCook canFlyAndCanCook) {
        canFlyAndCanCook.fly();
        canFlyAndCanCook.cook();
    }

    private static void useCookAndSwim(CanCookAndCanSwim canCookAndCanSwim) {
        canCookAndCanSwim.cook();
        canCookAndCanSwim.swim();
    }
}
