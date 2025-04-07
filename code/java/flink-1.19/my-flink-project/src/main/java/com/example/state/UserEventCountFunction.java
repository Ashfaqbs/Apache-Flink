package com.example.state;


import com.example.dto.UserPlain;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.apache.flink.api.common.typeinfo.Types;

public class UserEventCountFunction extends RichFlatMapFunction<UserPlain, String> {
    private transient ValueState<Integer> countState;

    @Override
    public void open(Configuration parameters) {
        ValueStateDescriptor<Integer> descriptor = new ValueStateDescriptor<>("eventCount", Types.INT);
        countState = getRuntimeContext().getState(descriptor);
    }

    @Override
    public void flatMap(UserPlain user, Collector<String> out) throws Exception {
        Integer count = countState.value();
        if (count == null) count = 0;
        count++;

        countState.update(count);
        out.collect("User: " + user.getName() + ", Total Events: " + count);
    }
}
