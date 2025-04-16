package com.example.config;

import com.example.dto.VisaUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;

public class VisaUserDeserializer implements DeserializationSchema<VisaUser> {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public VisaUser deserialize(byte[] message) throws IOException {
        return mapper.readValue(message, VisaUser.class);
    }

    @Override
    public boolean isEndOfStream(VisaUser nextElement) {
        return false;
    }

    @Override
    public TypeInformation<VisaUser> getProducedType() {
        return TypeInformation.of(VisaUser.class);
    }
}

