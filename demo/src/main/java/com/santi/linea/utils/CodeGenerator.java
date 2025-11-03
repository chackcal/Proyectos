package com.santi.linea.utils;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CodeGenerator {

    private final AtomicInteger opCounter = new AtomicInteger(1);
    private final AtomicInteger valeCounter = new AtomicInteger(1);

    public String nextOP(String opCode) {
        String yyyymm = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        return "OP-" + yyyymm + "-" + String.format("%04d", opCounter.getAndIncrement());
    }

    public String nextVale(String opCode) {
        return "VALE-" + opCode.replace("OP-", "") + "-" + String.format("%04d", valeCounter.getAndIncrement());
    }
}
