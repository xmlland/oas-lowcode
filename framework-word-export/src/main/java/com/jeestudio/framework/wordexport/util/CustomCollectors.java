package com.jeestudio.framework.wordexport.util;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * @Description: 自定义分组收集器
 */
public class CustomCollectors {
    // 默认采用2个一起分组
    public static <T> Collector<T, List<List<T>>, List<List<T>>> groupByNumber(){
        return CustomCollectors.groupByNumber(2);
    }

    // 根据number的大小进行分组
    public static <T> Collector<T, List<List<T>>, List<List<T>>> groupByNumber(int number){
        return new NumberCollectorImpl(number);
    }

    /**
     * 个数分组器
     * @param <T>
     */
    static class NumberCollectorImpl<T> implements Collector<T, List<List<T>>, List<List<T>>> {
        // 每组的个数
        private int number;

        public NumberCollectorImpl(int number) {
            this.number = number;
        }

        @Override
        public Supplier<List<List<T>>> supplier() {
            return ArrayList::new;
        }

        @Override
        public BiConsumer<List<List<T>>, T> accumulator() {
            return (list, item) -> {
                if (list.isEmpty()){
                    list.add(this.createNewList(item));
                }else {
                    List<T> last = (List<T>) list.get(list.size() - 1);
                    if (last.size() < number){
                        last.add(item);
                    }else{
                        list.add(this.createNewList(item));
                    }
                }
            };
        }

        @Override
        public BinaryOperator<List<List<T>>> combiner() {
            return (list1, list2) -> {
                list1.addAll(list2);
                return list1;
            };
        }

        @Override
        public Function<List<List<T>>, List<List<T>>> finisher() {
            return Function.identity();
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Collections.unmodifiableSet(EnumSet.of(Characteristics.IDENTITY_FINISH));
        }

        private List<T> createNewList(T item){
            List<T> newOne = new ArrayList<T>();
            newOne.add(item);
            return newOne;
        }
    }
}
