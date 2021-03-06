package sidev.lib;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import sidev.lib.annotation.ChangeLog;
import sidev.lib.collection.lazy_list._LazyListFunKt;
import sidev.lib.number._OpFunKt;
import sidev.lib.number._NumberFunKt;

import static kotlin.collections.CollectionsKt.*;

import java.util.Optional;
import java.util.function.Predicate;

import static kotlin.sequences.SequencesKt.*;
import static kotlin.TuplesKt.*;
import static sidev.lib.console._ConsoleFunJvm.prin;

public class SampleTestJava {
    class AA{
        @Nullable
        Boolean bool;
        AB ab;
    }
    class AB{}
    @Test
    public void lazyListTest(){
        @ChangeLog(date ="a", log= "")
        var lazi1 = _LazyListFunKt.<String, Integer>lazyMapOf();
        var seq1 = sequenceOf(to("Kamu",1), to("Dia",10), to("Makan",5));

        var ac = new AA();
        var opt = Optional.ofNullable(ac.bool);

        var list = listOf(1,2,3,4,5);
        var array = list.stream().filter(it -> it >= 3)//.forEach(System.out::println);
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer integer) {
                        return false;
                    }
                })
                .map(it -> it.toString())
                .toArray(String[]::new);

        new Function1<Boolean, Unit>(){
            @Override
            public Unit invoke(Boolean aBoolean) {
                return null;
            }
        };
/*
        notNull(ac.ab, it -> {

            return Unit.INSTANCE;
        });
 */

//        lazi1.addIterator(seq1.iterator());

//        this.<String>ad("kll");
    }

    <T, O> O ad(T a){
        return (O) a;
    }

    @Test
    public void cob(){
        prin(_NumberFunKt.isNegative(1));
        prin(_NumberFunKt.asNumber(1).floatValue());
        prin(_OpFunKt.pow(2, 3));
        Number n= 1;
    }
}
