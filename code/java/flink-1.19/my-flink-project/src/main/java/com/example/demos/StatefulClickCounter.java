package com.example.demos;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

public class StatefulClickCounter  extends KeyedProcessFunction<String, UserEvent, String> {
    private ValueState<Long> clickCount;

    @Override
    public void open(Configuration parameters) {
        clickCount = getRuntimeContext().getState(
                new ValueStateDescriptor<>("clickCount", Long.class)
        );
    }

    @Override
    public void processElement(UserEvent event, Context ctx, Collector<String> out) throws Exception {
        Long count = clickCount.value();
        if (count == null) count = 0L;

        count++;
        clickCount.update(count);

        out.collect("User " + ctx.getCurrentKey() + " clicked " + count + " times");
    }
}