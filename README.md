# Optional Structural Typing in Java

For the last several years I have been programming on JVM. I spend most of my time on our fancy ERP-like platform written mostly in Java with some Groovy inside. Throughout the years, I sometimes felt jealous that other people work in JavaScript/Clojure/Haskell/Elixir/Go or any other cool language and play with some more shiny brand new tools. But since I learned from LISPs that a lot of things can be done as a library, I realized that I also can have some new shiny cool stuff available in Java. 

Structural typing. So what is this about?

Let me start with some definitions. We need to understand what Nominal typing, Duck typing and Structural typing is.

**Nominal typing (e.g. Java)**
```java
public interface Quackable {
   void quack();
}

public class Duck implements Quackable {

   @Override
   public void quack() {
       System.out.println("quack");
   }
}
```

Having an interface I and a type (class) Duck, the only way to say that Duck implements Qackable is to declare it explicitly in the Duck definition. So I need to use a reference to the Quackable name. And that’s it, nominal typing.

**Duck typing (e.g. Groovy (or Java through reflection))**
```groovy
class Duck {
   void quack() {
       println("quack");
   }
}

class Kid {
   void quack() {
       println("quack! quack!");
   }
}

static main(String[] args) {
   def qackables = [new Duck(), new Kid()]
   qackables.each {
       it.quack()
   }
}
``` 
If it looks like a duck and if it quacks like a duck, it needs to be a duck! The dispatch of the quack function is handled in runtime by trying to find some matching signature on the underlying instance.

**Structural typing (e.g. Go)**
```go
type Quackable interface {
  Quack()
}

type Duck struct {
}

func (duck Duck) Quack() {
  fmt.Println("quack")
}

func main() {
  var q Quackable = Duck{}
  q.Quack()
}
```
In Go, types implement interfaces implicitly. There is no explicit declaration of the implementation relation, simply any type having all matching methods of some interface can be used in place where the interface is required. And since Go is a statically typed language, this is checked in the compile time. This is how Structural typing works.

## Pros & Cons

**Nominal typing:** less flexible, hard to extend foreign types (from 3rd party libraries), however pretty straightforward and checked in the compile time, no performance hit during runtime.

**Duck typing:** flexibility, no need to consider extending foreign types, performance hit in runtime, need for clumsy reflection code in Java even though some libs reduce the boilerplate.

**Structural typing:** benefits of both nominal and duck typing, although might seem alien to some people :)

## A new library to the rescue!

So, wouldn’t be nice to have structural typing available in Java? I really like the Lisp approach of adding things. Anything can be a library. Although this is not always possible in Java world, we seem to be lucky today.


**Manifold (http://manifold.systems/docs.html#structural-interfaces):** 
That is exactly what we would want. A nice API, everything is done. It seems they reused a lot of work already done on the Gosu Programming language (https://en.wikipedia.org/wiki/Gosu_(programming_language)) to make it available as a Java plugin. However, Manifold itself does a lot of magic and the only IDE that works with it is Intellij Idea currently.


**Lombok project – Extension methods (https://projectlombok.org/features/experimental/ExtensionMethod):** 
Not exactly structural typing but that can be implemented with extension methods (explained e.g. here: http://manifold.systems/docs.html#implementation-by-extension). However, it is marked as experimental, and indeed, it kind of works in Eclipse, does not work in Netbeans and has a some bugs currently. Also, doing a lot of magic underneath. 

**So, there must be a simpler way, right?**

Well, there should be. What if we just create some annotation, let’s say @StructurallyTyped, and allow it to be added to any class (e.g. ThatAnyClass1). Then we should be able to write an annotation processor (http://hannesdorfmann.com/annotation-processing/annotationprocessing101) and that processor can generate an interface with a fixed name e.g. STIoThatAnyClass1 (Structurally Typed Interface of ThatAnyClass1) which would extend interfaces that are candidates to be implemented by that class.

I didn’t know if that would work and an annotation processor would even process a code that would not be compilable:
```java
@StructurallyTyped
public class Class1 implements STIoClass1 { // you need to type this by hand
   public String returnParam(String param) {
       return param;
   }
}

public interface Returnable {
   String returnParam(String param);
}

public class Main {
   public static void main(String[] args) {
       Class1 class1 = new Class1();
       doReturnParam(class1); // should not be compile time error
   }

   private static void doReturnParam(Returnable returnable) {
       System.out.println(returnable.returnParam("aaah!"));
   }
}

// this is autogenerated by annotation processor, you never touch this
public interface STIoClass1 extends Returnable {
}
```

STIoClass1 does not exist and needs to be generated, but it turned out that this works well. The annotation processor just computes that Returnable is a matching interface and class1 can be used trivially in the doReturnParam method. No magic. Just plain Java.

The code is available here (https://github.com/tlamr/stjava) and does exactly this (check the tests submodule). It allows you to mark classes with the @StructurallyTyped annotation and generates the interfaces for you.

And that’s it. Pretty simple. Works in any IDE, no magic, just the annotation processor and some code generation. Might not work in all cases, it is still just a PoC ;).