package com.tugoserya.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDate;

import static com.tugoserya.utils.Utils.YYYYMMDD;
import static java.lang.Integer.parseInt;

public class LocalDateSerializer extends JsonSerializer<LocalDate> {

	@Override
	public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeNumber(parseInt(value.format(YYYYMMDD)));
	}
}
